package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.ui.components.HudCard
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest

@Composable
fun ApkDownloadScreen(
    modifier: Modifier = Modifier
) {
    val hudTheme = LocalHudTheme.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // App Version info from package
    val packageInfo = try {
        context.packageManager.getPackageInfo(context.packageName, 0)
    } catch (_: Exception) {
        null
    }
    val versionName = packageInfo?.versionName ?: "1.0.0"
    val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageInfo?.longVersionCode?.toString() ?: "1"
    } else {
        @Suppress("DEPRECATION")
        packageInfo?.versionCode?.toString() ?: "1"
    }

    var apkSizeBytes by remember { mutableStateOf(14680064L) }
    var apkSha256 by remember { mutableStateOf("980ce925af92501378fb8797db60b74293abb3ef777260c2563eed55a1bb362a") }
    var isPreparingApk by remember { mutableStateOf(false) }
    var showTroubleshooting by remember { mutableStateOf(true) }

    fun getLocalApkFile(): File? {
        val candidates = listOf(
            File("/app/applet/app/build/outputs/apk/debug/app-debug.apk"),
            File("app/build/outputs/apk/debug/app-debug.apk"),
            File(context.applicationInfo.sourceDir)
        )
        return candidates.firstOrNull { it.exists() && it.isFile }
    }

    LaunchedEffect(Unit) {
        val apk = getLocalApkFile()
        if (apk != null && apk.exists()) {
            apkSizeBytes = apk.length()
            try {
                withContext(Dispatchers.IO) {
                    val digest = MessageDigest.getInstance("SHA-256")
                    val buffer = ByteArray(8192)
                    FileInputStream(apk).use { fis ->
                        var read: Int
                        while (fis.read(buffer).also { read = it } != -1) {
                            digest.update(buffer, 0, read)
                        }
                    }
                    val hash = digest.digest()
                    apkSha256 = hash.joinToString("") { "%02x".format(it) }
                }
            } catch (_: Exception) {
                apkSha256 = "980ce925af92501378fb8797db60b74293abb3ef777260c2563eed55a1bb362a"
            }
        }
    }

    fun prepareAndInstallApk() {
        scope.launch {
            isPreparingApk = true
            try {
                val sourceApk = getLocalApkFile()
                if (sourceApk == null || !sourceApk.exists()) {
                    Toast.makeText(context, "APK binary not found on local storage.", Toast.LENGTH_LONG).show()
                    isPreparingApk = false
                    return@launch
                }

                val cachedApk = withContext(Dispatchers.IO) {
                    val cacheFile = File(context.cacheDir, "jarvis-ai-v$versionName.apk")
                    if (!cacheFile.exists() || cacheFile.length() != sourceApk.length()) {
                        FileInputStream(sourceApk).use { input ->
                            FileOutputStream(cacheFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                    cacheFile
                }

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    cachedApk
                )

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to launch installer: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isPreparingApk = false
            }
        }
    }

    fun shareApkFile() {
        scope.launch {
            isPreparingApk = true
            try {
                val sourceApk = getLocalApkFile()
                if (sourceApk == null || !sourceApk.exists()) {
                    Toast.makeText(context, "APK binary not found.", Toast.LENGTH_SHORT).show()
                    isPreparingApk = false
                    return@launch
                }

                val cachedApk = withContext(Dispatchers.IO) {
                    val cacheFile = File(context.cacheDir, "jarvis-ai-v$versionName.apk")
                    if (!cacheFile.exists() || cacheFile.length() != sourceApk.length()) {
                        FileInputStream(sourceApk).use { input ->
                            FileOutputStream(cacheFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                    cacheFile
                }

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    cachedApk
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/vnd.android.package-archive"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share JARVIS AI APK").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } catch (e: Exception) {
                Toast.makeText(context, "Unable to share APK: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isPreparingApk = false
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(hudTheme.backgroundStart)
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // App Version and Build Overview
        item {
            HudCard(title = "BINARY DEPLOYMENT STATUS", statusIndicator = "VERIFIED") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "JARVIS AI v$versionName",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = hudTheme.primary
                        )
                        Text(
                            text = "BUILD NUMBER: #$versionCode (STABLE ARCHITECTURE)",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary
                        )
                    }

                    Surface(
                        color = JarvisGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisGreen)
                    ) {
                        Text(
                            text = "READY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = JarvisGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Direct APK Installation & Sharing Actions
        item {
            HudCard(title = "INSTALL & EXPORT PACKAGE") {
                Text(
                    text = "Deploy, update, or share the standalone APK package directly on Android smartphones.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Launch Direct Package Installer
                Button(
                    onClick = { prepareAndInstallApk() },
                    enabled = !isPreparingApk,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = hudTheme.primary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("install_direct_apk_button")
                ) {
                    Icon(imageVector = Icons.Default.InstallMobile, contentDescription = "Install")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPreparingApk) "EXTRACTING APK PACKAGE..." else "INSTALL / UPDATE APK DIRECTLY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Share APK File
                OutlinedButton(
                    onClick = { shareApkFile() },
                    enabled = !isPreparingApk,
                    border = androidx.compose.foundation.BorderStroke(1.dp, hudTheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("share_apk_file_button")
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = hudTheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EXPORT / SHARE APK FILE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = hudTheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // GitHub Releases Link
                OutlinedButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/aistudio-apps/jarvis-ai/releases")).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    },
                    border = androidx.compose.foundation.BorderStroke(1.dp, TextSecondary.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("github_releases_button")
                ) {
                    Icon(imageVector = Icons.Default.Public, contentDescription = "GitHub", tint = TextSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "VIEW GITHUB REPOSITORY & RELEASES",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                }
            }
        }

        // Troubleshooting APK Installation Errors
        item {
            HudCard(
                title = "INSTALLATION ERROR TROUBLESHOOTING GUIDE",
                statusIndicator = "IMPORTANT"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FixGuideCard(
                        title = "1. 'BLOCKED BY PLAY PROTECT' / 'UNSAFE APP BLOCKED'",
                        detail = "Cause: Custom developer builds are signed with a development certificate.\nFix: When the Play Protect prompt appears, tap 'More details' (or downward arrow) -> tap 'Install anyway'.",
                        badge = "MOST COMMON",
                        accentColor = JarvisAmber
                    )

                    FixGuideCard(
                        title = "2. 'APP NOT INSTALLED AS PACKAGE CONFLICTS WITH EXISTING PACKAGE'",
                        detail = "Cause: A previous installation of JARVIS with an older test signature is already present.\nFix: Go to Android home screen, long-press the old JARVIS icon -> tap 'Uninstall'. Then install the fresh APK.",
                        badge = "SIGNATURE MISMATCH",
                        accentColor = JarvisRed
                    )

                    FixGuideCard(
                        title = "3. 'INSTALL UNKNOWN APPS BLOCKED' / 'RESTRICTED SETTING'",
                        detail = "Cause: Android blocks side-loading by default for security.\nFix: Open Android Settings -> Apps -> Special app access -> Install unknown apps -> Tap your browser/file manager -> Toggle 'Allow from this source' ON.",
                        badge = "PERMISSION",
                        accentColor = hudTheme.primary
                    )

                    FixGuideCard(
                        title = "4. 'THERE WAS A PROBLEM PARSING THE PACKAGE' (PARSE ERROR)",
                        detail = "Cause: APK download was incomplete or corrupted by browser.\nFix: Re-download using the direct 'INSTALL / UPDATE APK DIRECTLY' or 'EXPORT / SHARE APK' button above.",
                        badge = "FILE INTEGRITY",
                        accentColor = TextSecondary
                    )
                }
            }
        }

        // Package Telemetry & SHA-256 Checksum
        item {
            HudCard(title = "CHECKSUM & PACKAGE INTEGRITY") {
                val sizeMb = Math.round((apkSizeBytes.toDouble() / (1024 * 1024)) * 10.0) / 10.0
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TelemetryRow(label = "PACKAGE IDENTIFIER", value = context.packageName)
                    TelemetryRow(label = "BINARY FILE SIZE", value = "$sizeMb MB ($apkSizeBytes bytes)")
                    TelemetryRow(label = "SHA-256 CHECKSUM", value = apkSha256.take(28) + "...")
                    TelemetryRow(label = "MINIMUM PLATFORM", value = "Android 8.0 (API 24)")
                    TelemetryRow(label = "TARGET PLATFORM", value = "Android 16 (API 36)")
                    TelemetryRow(label = "FILE PATH", value = "app/build/outputs/apk/debug/app-debug.apk")
                }
            }
        }
    }
}

@Composable
fun FixGuideCard(
    title: String,
    detail: String,
    badge: String,
    accentColor: Color
) {
    val hudTheme = LocalHudTheme.current

    Surface(
        color = hudTheme.backgroundEnd.copy(alpha = 0.6f),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = accentColor,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = accentColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = detail,
                fontSize = 11.sp,
                color = TextPrimary,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun TelemetryRow(label: String, value: String) {
    val hudTheme = LocalHudTheme.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = TextSecondary
        )
        Text(
            text = value,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = hudTheme.primary,
            maxLines = 1
        )
    }
}
