package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.JarvisFileItem
import com.example.ui.components.HudCard
import com.example.ui.theme.*
import com.example.viewmodel.JarvisViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FileManagerScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val hudTheme = LocalHudTheme.current
    val rootDirs = viewModel.fileManager.rootDirectories

    var currentDir by remember { mutableStateOf(rootDirs.firstOrNull() ?: File("/")) }
    var fileList by remember { mutableStateOf<List<JarvisFileItem>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var showCreateDocDialog by remember { mutableStateOf(false) }
    var newDocTitle by remember { mutableStateOf("") }
    var newDocContent by remember { mutableStateOf("") }

    fun refresh() {
        fileList = viewModel.fileManager.listFiles(currentDir)
    }

    LaunchedEffect(currentDir) {
        refresh()
    }

    val filteredList = fileList.filter {
        searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(hudTheme.backgroundStart)
            .padding(bottom = 85.dp)
    ) {
        // Top File Manager Bar
        Surface(
            color = hudTheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, hudTheme.border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SECURE STORAGE EXPLORER",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = hudTheme.primary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = currentDir.absolutePath,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }

                    Row {
                        IconButton(
                            onClick = { showNewFolderDialog = true },
                            modifier = Modifier.testTag("create_folder_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CreateNewFolder,
                                contentDescription = "New Folder",
                                tint = hudTheme.primary
                            )
                        }
                        IconButton(
                            onClick = { showCreateDocDialog = true },
                            modifier = Modifier.testTag("create_document_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.NoteAdd,
                                contentDescription = "New Document",
                                tint = hudTheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Filter Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text("Search authorized files...", fontSize = 12.sp, color = TextMuted)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("file_search_input"),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = hudTheme.primary)
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = hudTheme.primary,
                        unfocusedBorderColor = hudTheme.border,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                // Root Directory Tabs
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rootDirs.take(3).forEach { dir ->
                        val isCurrent = currentDir.absolutePath == dir.absolutePath
                        FilterChip(
                            selected = isCurrent,
                            onClick = { currentDir = dir },
                            label = { Text(dir.name.ifBlank { "Root" }, fontSize = 9.sp, fontFamily = FontFamily.Monospace) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = hudTheme.primary.copy(alpha = 0.25f),
                                selectedLabelColor = hudTheme.primary
                            )
                        )
                    }
                }
            }
        }

        // Parent Directory Back Button
        if (currentDir.parentFile != null && currentDir.parentFile in rootDirs) {
            Surface(
                color = hudTheme.surface.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { currentDir = currentDir.parentFile!! }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = hudTheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = ".. [PARENT DIRECTORY]",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = hudTheme.primary
                    )
                }
            }
        }

        // File List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (filteredList.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Empty",
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "DIRECTORY EMPTY",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextMuted
                        )
                    }
                }
            } else {
                items(filteredList, key = { it.path }) { item ->
                    FileItemRow(
                        item = item,
                        onClick = {
                            if (item.isDirectory) {
                                currentDir = File(item.path)
                            }
                        },
                        onDelete = {
                            viewModel.requestFileDeletion(File(item.path)) {
                                viewModel.fileManager.deleteFile(File(item.path))
                                refresh()
                            }
                        }
                    )
                }
            }
        }
    }

    // New Folder Dialog
    if (showNewFolderDialog) {
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            containerColor = hudTheme.surface,
            title = { Text("CREATE FOLDER", fontFamily = FontFamily.Monospace, color = hudTheme.primary) },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("Folder Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("new_folder_name_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            viewModel.fileManager.createFolder(currentDir, newFolderName)
                            newFolderName = ""
                            showNewFolderDialog = false
                            refresh()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = hudTheme.primary, contentColor = Color.Black)
                ) {
                    Text("CREATE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFolderDialog = false }) {
                    Text("CANCEL", color = TextSecondary)
                }
            }
        )
    }

    // New Doc Dialog
    if (showCreateDocDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDocDialog = false },
            containerColor = hudTheme.surface,
            title = { Text("CREATE TEXT FILE", fontFamily = FontFamily.Monospace, color = hudTheme.primary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newDocTitle,
                        onValueChange = { newDocTitle = it },
                        label = { Text("File Name (e.g. notes.txt)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_doc_title_input")
                    )
                    OutlinedTextField(
                        value = newDocContent,
                        onValueChange = { newDocContent = it },
                        label = { Text("File Content") },
                        modifier = Modifier.fillMaxWidth().height(100.dp).testTag("new_doc_content_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newDocTitle.isNotBlank()) {
                            val name = if (newDocTitle.contains(".")) newDocTitle else "$newDocTitle.txt"
                            viewModel.fileManager.createSampleDocument(currentDir, name, newDocContent)
                            newDocTitle = ""
                            newDocContent = ""
                            showCreateDocDialog = false
                            refresh()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = hudTheme.primary, contentColor = Color.Black)
                ) {
                    Text("SAVE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDocDialog = false }) {
                    Text("CANCEL", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun FileItemRow(
    item: JarvisFileItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val hudTheme = LocalHudTheme.current
    val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    val dateStr = sdf.format(Date(item.lastModified))

    Surface(
        color = hudTheme.surface.copy(alpha = 0.8f),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, hudTheme.border),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = if (item.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                    contentDescription = if (item.isDirectory) "Folder" else "File",
                    tint = if (item.isDirectory) hudTheme.primary else TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = item.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                    Text(
                        text = if (item.isDirectory) "${item.childCount} items • $dateStr" else "${item.sizeBytes / 1024} KB • $dateStr",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete File",
                    tint = JarvisRed.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
