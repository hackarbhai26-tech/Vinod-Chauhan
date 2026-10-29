package com.example.service

import android.content.Context
import android.os.Environment
import java.io.File

data class JarvisFileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val lastModified: Long,
    val childCount: Int = 0
)

class FileManagerHelper(private val context: Context) {

    val rootDirectories: List<File>
        get() = listOfNotNull(
            context.filesDir,
            context.getExternalFilesDir(null),
            context.cacheDir,
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        ).filter { it.exists() || it.mkdirs() }

    fun listFiles(dir: File): List<JarvisFileItem> {
        val files = dir.listFiles() ?: return emptyList()
        return files.map { f ->
            JarvisFileItem(
                name = f.name,
                path = f.absolutePath,
                isDirectory = f.isDirectory,
                sizeBytes = if (f.isFile) f.length() else 0L,
                lastModified = f.lastModified(),
                childCount = if (f.isDirectory) f.list()?.size ?: 0 else 0
            )
        }.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
    }

    fun createFolder(parentDir: File, folderName: String): Boolean {
        val target = File(parentDir, folderName)
        return if (!target.exists()) target.mkdirs() else false
    }

    fun createSampleDocument(parentDir: File, fileName: String, content: String): Boolean {
        return try {
            val target = File(parentDir, fileName)
            target.writeText(content)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun renameFile(file: File, newName: String): Boolean {
        val target = File(file.parentFile, newName)
        return file.renameTo(target)
    }

    fun deleteFile(file: File): Boolean {
        return if (file.isDirectory) {
            file.deleteRecursively()
        } else {
            file.delete()
        }
    }
}
