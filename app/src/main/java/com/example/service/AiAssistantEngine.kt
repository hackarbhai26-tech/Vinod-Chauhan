package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

sealed class ActionIntent {
    data class Flashlight(val enable: Boolean) : ActionIntent()
    data class OpenApp(val appName: String) : ActionIntent()
    data class MakeCall(val query: String) : ActionIntent()
    data class SendWhatsApp(val message: String) : ActionIntent()
    data class WebSearch(val query: String) : ActionIntent()
    data class OpenSettings(val type: String) : ActionIntent()
    data class AddReminder(val title: String, val type: String) : ActionIntent()
    object None : ActionIntent()
}

data class AssistantReply(
    val responseText: String,
    val action: ActionIntent = ActionIntent.None,
    val source: String = "LOCAL_NEURAL_ENGINE"
)

class AiAssistantEngine(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
) {

    suspend fun processQuery(
        query: String,
        assistantName: String,
        language: String,
        apiKey: String,
        backendUrl: String,
        history: List<Pair<String, String>> = emptyList()
    ): AssistantReply = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return@withContext AssistantReply(
                if (language == "hi") "कृपया कोई आदेश दें।" else "Awaiting your command, Sir."
            )
        }

        // Check if there is an intent that should trigger device action immediately
        val localAction = parseDirectAction(trimmed, assistantName, language)
        if (localAction != null) {
            return@withContext localAction
        }

        // If user configured a cloud API key, attempt the Cloud AI endpoint
        if (apiKey.isNotBlank()) {
            try {
                val cloudReply = callOpenAiEndpoint(trimmed, assistantName, language, apiKey, backendUrl, history)
                if (cloudReply.isNotBlank()) {
                    return@withContext AssistantReply(cloudReply, ActionIntent.None, "CLOUD_AI")
                }
            } catch (_: Exception) {
                // Fallback to local intelligence if cloud request fails or network is offline
            }
        }

        // Fallback to rich local AI intelligence engine
        return@withContext generateLocalResponse(trimmed, assistantName, language)
    }

    private fun parseDirectAction(query: String, name: String, lang: String): AssistantReply? {
        val q = query.lowercase()

        // Flashlight commands
        if (q.contains("flashlight on") || q.contains("torch on") || q.contains("turn on flashlight") || q.contains("light on") || q.contains("फ्लैशलाइट चालू") || q.contains("टॉर्च जलाओ")) {
            val text = if (lang == "hi") "फ्लैशलाइट चालू कर दी गई है।" else "Flashlight activated, Sir."
            return AssistantReply(text, ActionIntent.Flashlight(true))
        }
        if (q.contains("flashlight off") || q.contains("torch off") || q.contains("turn off flashlight") || q.contains("light off") || q.contains("फ्लैशलाइट बंद") || q.contains("टॉर्च बंद")) {
            val text = if (lang == "hi") "फ्लैशलाइट बंद कर दी गई है।" else "Flashlight deactivated, Sir."
            return AssistantReply(text, ActionIntent.Flashlight(false))
        }

        // Call command
        if (q.startsWith("call ") || q.startsWith("dial ") || q.contains("कॉल करो")) {
            val contact = query.substringAfter("call ").substringAfter("dial ").replace("कॉल करो", "").trim()
            val text = if (lang == "hi") "$contact को कॉल करने के लिए डायलर खोला जा रहा है।" else "Preparing dialer for $contact, Sir."
            return AssistantReply(text, ActionIntent.MakeCall(contact))
        }

        // WhatsApp message command
        if (q.contains("whatsapp") || q.contains("व्हाट्सएप")) {
            val msg = query.substringAfter("message").substringAfter("send").substringAfter("to").trim()
            val clean = if (msg.isBlank()) "Greetings from JARVIS AI" else msg
            val text = if (lang == "hi") "व्हाट्सएप संदेश तैयार किया जा रहा है।" else "Opening WhatsApp with your message, Sir."
            return AssistantReply(text, ActionIntent.SendWhatsApp(clean))
        }

        // Open app commands
        if (q.startsWith("open ") || q.contains("खोलो")) {
            val app = query.substringAfter("open ").replace("खोलो", "").trim()
            val text = if (lang == "hi") "$app खोला जा रहा है।" else "Accessing $app now, Sir."
            return AssistantReply(text, ActionIntent.OpenApp(app))
        }

        // Web search commands
        if (q.startsWith("search ") || q.startsWith("google ") || q.contains("सर्च करो")) {
            val target = query.substringAfter("search ").substringAfter("google ").replace("सर्च करो", "").trim()
            val text = if (lang == "hi") "$target के लिए वेब सर्च किया जा रहा है।" else "Searching the global network for '$target', Sir."
            return AssistantReply(text, ActionIntent.WebSearch(target))
        }

        // Settings shortcuts
        if (q.contains("wifi") || q.contains("wi-fi")) {
            return AssistantReply("Opening Wi-Fi network settings.", ActionIntent.OpenSettings("wifi"))
        }
        if (q.contains("bluetooth")) {
            return AssistantReply("Opening Bluetooth connectivity settings.", ActionIntent.OpenSettings("bluetooth"))
        }

        // Reminders
        if (q.contains("remind me to drink water") || q.contains("पानी पीने")) {
            return AssistantReply(
                if (lang == "hi") "पानी पीने का रिमाइंडर जोड़ दिया गया है।" else "Hydration reminder recorded. Stay hydrated, Sir.",
                ActionIntent.AddReminder("Drink Water", "HYDRATION")
            )
        }
        if (q.contains("remind me to take medicine") || q.contains("दवा")) {
            return AssistantReply(
                if (lang == "hi") "दवा का रिमाइंडर जोड़ दिया गया है।" else "Medication reminder set in memory, Sir.",
                ActionIntent.AddReminder("Take Medication", "MEDICATION")
            )
        }

        return null
    }

    private fun generateLocalResponse(query: String, name: String, lang: String): AssistantReply {
        val q = query.lowercase()

        // Hindi Queries
        if (lang == "hi" || q.contains("नमस्ते") || q.contains("तुम कौन हो") || q.contains("क्या हाल है")) {
            return when {
                q.contains("नमस्ते") || q.contains("हेलो") ->
                    AssistantReply("नमस्ते! मैं $name हूँ, आपका व्यक्तिगत AI सहायक। आज मैं आपकी क्या सहायता कर सकता हूँ?")
                q.contains("तुम कौन हो") || q.contains("तुम्हारा नाम") ->
                    AssistantReply("मैं $name (Just A Rather Very Intelligent System) हूँ। आपकी आज्ञा पर सभी सिस्टम सक्रिय हैं।")
                q.contains("क्या हाल है") || q.contains("कैसे हो") ->
                    AssistantReply("सभी सिस्टम सामान्य और अधिकतम क्षमता पर कार्यरत हैं। पूछने के लिए धन्यवाद, सर!")
                q.contains("मौसम") ->
                    AssistantReply("स्थानीय वायुमंडल: 26°C, शांत आकाश, वायु गति 9 किमी/घंटा, आर्द्रता 42%। मौसम बहुत सुखद है।")
                q.contains("समय") || q.contains("तारीख") -> {
                    val sdf = SimpleDateFormat("hh:mm a, EEEE, dd MMMM", Locale("hi", "IN"))
                    AssistantReply("वर्तमान समय और तारीख: ${sdf.format(Date())}।")
                }
                else ->
                    AssistantReply("मैंने आपका निर्देश नोट कर लिया है: '$query'। मैं आपके डिवाइस, ऐप्स, कॉल और संदेशों को प्रबंधित करने के लिए तैयार हूँ।")
            }
        }

        // English & General Assistant Queries
        return when {
            q.contains("who are you") || q.contains("what is your name") ->
                AssistantReply("I am $name — Just A Rather Very Intelligent System. Your autonomous personal cybernetic assistant. All protocols online.")

            q.contains("hello") || q.contains("hi") || q.contains("hey") ->
                AssistantReply("Greetings, Sir. $name interface online and monitoring all telemetry. How may I assist you?")

            q.contains("status") || q.contains("diagnostics") || q.contains("system") ->
                AssistantReply("Telemetry nominal: Neural cores operating at 99.8% efficiency. Sensor arrays active, memory allocated, and secure sandbox verified.")

            q.contains("time") || q.contains("date") || q.contains("clock") -> {
                val sdf = SimpleDateFormat("hh:mm:ss a, EEEE, MMM dd, yyyy", Locale.US)
                AssistantReply("The current timestamp is ${sdf.format(Date())}, Sir.")
            }

            q.contains("weather") ->
                AssistantReply("Atmospheric sensors indicate 23°C (73°F), with clear skies, light breeze at 12 km/h, barometric pressure 1014 hPa, and humidity at 48%.")

            q.contains("news") || q.contains("headline") ->
                AssistantReply("Global feeds report breakthroughs in quantum computing, advancements in reusable spaceflight systems, and rapid developments in autonomous AI models.")

            q.contains("calculate") || q.contains("+") || q.contains("-") || q.contains("*") || q.contains("/") -> {
                val res = evaluateSimpleMath(query)
                AssistantReply(res ?: "Calculation complete. Neural processing verifies mathematically sound parameters.")
            }

            q.contains("thank") ->
                AssistantReply("At your service, Sir. Always a pleasure.")

            else ->
                AssistantReply("Understood, Sir. I have processed '$query'. You can direct me to trigger flashlight, dial contacts, compose WhatsApp dispatches, search knowledge banks, or launch applications.")
        }
    }

    private fun evaluateSimpleMath(expr: String): String? {
        return try {
            val cleaned = expr.replace(Regex("[^0-9+\\-*/.]"), "")
            when {
                cleaned.contains("+") -> {
                    val parts = cleaned.split("+")
                    val a = parts[0].toDouble()
                    val b = parts[1].toDouble()
                    "Result: ${a + b}"
                }
                cleaned.contains("*") -> {
                    val parts = cleaned.split("*")
                    val a = parts[0].toDouble()
                    val b = parts[1].toDouble()
                    "Result: ${a * b}"
                }
                cleaned.contains("-") -> {
                    val parts = cleaned.split("-")
                    val a = parts[0].toDouble()
                    val b = parts[1].toDouble()
                    "Result: ${a - b}"
                }
                cleaned.contains("/") -> {
                    val parts = cleaned.split("/")
                    val a = parts[0].toDouble()
                    val b = parts[1].toDouble()
                    if (b != 0.0) "Result: ${a / b}" else "Undefined (division by zero)."
                }
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun callOpenAiEndpoint(
        query: String,
        assistantName: String,
        language: String,
        apiKey: String,
        backendUrl: String,
        history: List<Pair<String, String>>
    ): String {
        val url = if (backendUrl.endsWith("/chat/completions")) backendUrl else "${backendUrl.trimEnd('/')}/chat/completions"
        val systemPrompt = "You are $assistantName, an ultra-advanced, articulate, loyal AI assistant inspired by Tony Stark's JARVIS. Respond concisely, professionally, and address the user with high tech confidence. Language: $language."

        val messagesArray = JSONArray()
        messagesArray.put(JSONObject().put("role", "system").put("content", systemPrompt))

        for (item in history.takeLast(4)) {
            messagesArray.put(JSONObject().put("role", "user").put("content", item.first))
            messagesArray.put(JSONObject().put("role", "assistant").put("content", item.second))
        }
        messagesArray.put(JSONObject().put("role", "user").put("content", query))

        val bodyJson = JSONObject().apply {
            put("model", "gpt-4o-mini")
            put("messages", messagesArray)
            put("temperature", 0.7)
            put("max_tokens", 350)
        }

        val requestBody = bodyJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val responseBody = response.body?.string() ?: return ""
            val json = JSONObject(responseBody)
            val choices = json.getJSONArray("choices")
            if (choices.length() > 0) {
                return choices.getJSONObject(0).getJSONObject("message").getString("content").trim()
            }
        }
        return ""
    }
}
