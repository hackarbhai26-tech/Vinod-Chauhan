package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.service.ActionIntent
import com.example.service.AiAssistantEngine
import com.example.ui.theme.HudThemes
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("JARVIS AI", appName)
    }

    @Test
    fun `verify 14 hud themes configured`() {
        assertEquals(14, HudThemes.size)
        val arcReactor = HudThemes.first()
        assertEquals("Arc Reactor", arcReactor.name)
    }

    @Test
    fun `test ai assistant engine local processing`() = runBlocking {
        val engine = AiAssistantEngine()
        val reply = engine.processQuery(
            query = "turn on flashlight",
            assistantName = "JARVIS",
            language = "en",
            apiKey = "",
            backendUrl = ""
        )
        assertNotNull(reply)
        assertTrue(reply.action is ActionIntent.Flashlight)
        val flashAction = reply.action as ActionIntent.Flashlight
        assertTrue(flashAction.enable)
    }

    @Test
    fun `test ai assistant hindi query`() = runBlocking {
        val engine = AiAssistantEngine()
        val reply = engine.processQuery(
            query = "नमस्ते",
            assistantName = "JARVIS",
            language = "hi",
            apiKey = "",
            backendUrl = ""
        )
        assertNotNull(reply)
        assertTrue(reply.responseText.contains("नमस्ते"))
    }
}
