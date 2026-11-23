package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ReadingModeContent

private enum class ReaderPalette(val label: String, val bg: Color, val text: Color, val muted: Color) {
    DARK("Dark", Color(0xFF161719), Color(0xFFE8EAED), Color(0xFF9AA0A6)),
    SEPIA("Sepia", Color(0xFFF6EFE0), Color(0xFF33291E), Color(0xFF6B5D4D)),
    LIGHT("Light", Color(0xFFFFFFFF), Color(0xFF1F1F1F), Color(0xFF5F6368))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingModeOverlay(
    content: ReadingModeContent,
    initialDark: Boolean,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    var palette by remember { mutableStateOf(if (initialDark) ReaderPalette.DARK else ReaderPalette.LIGHT) }
    var fontSizeSp by remember { mutableFloatStateOf(17f) }
    var showAppearanceBar by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = palette.bg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Reading mode",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.text
                        )
                        Text(
                            text = content.domain,
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.muted
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAppearanceBar = !showAppearanceBar }) {
                        Icon(
                            imageVector = Icons.Outlined.FormatSize,
                            contentDescription = "Reading appearance",
                            tint = palette.text
                        )
                    }
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close reading mode",
                            tint = palette.text
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = palette.bg)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(palette.bg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showAppearanceBar) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReaderPalette.entries.forEach { option ->
                            FilterChip(
                                selected = palette == option,
                                onClick = { palette = option },
                                label = { Text(option.label) }
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("A", fontSize = 13.sp, color = palette.muted)
                        Slider(
                            value = fontSizeSp,
                            onValueChange = { fontSizeSp = it },
                            valueRange = 14f..26f,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        )
                        Text("A", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = palette.text)
                    }
                }
                HorizontalDivider(color = palette.muted.copy(alpha = 0.25f))
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = content.title.ifBlank { content.domain },
                        style = MaterialTheme.typography.headlineMedium,
                        color = palette.text,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = content.url,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.muted
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = palette.muted.copy(alpha = 0.25f))
                }

                if (content.paragraphs.isEmpty()) {
                    item {
                        Text(
                            text = "This page doesn't contain long-form article text to simplify. Try Reading mode on a news article, blog post, or documentation page.",
                            fontSize = fontSizeSp.sp,
                            lineHeight = (fontSizeSp * 1.6f).sp,
                            color = palette.muted
                        )
                    }
                } else {
                    items(content.paragraphs) { paragraph ->
                        Text(
                            text = paragraph,
                            fontSize = fontSizeSp.sp,
                            lineHeight = (fontSizeSp * 1.62f).sp,
                            color = palette.text
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslatePageBottomSheet(
    currentUrl: String,
    onDismiss: () -> Unit,
    onSelectLanguage: (code: String, displayName: String) -> Unit
) {
    val languages = listOf(
        "en" to "English",
        "hi" to "Hindi (हिन्दी)",
        "es" to "Spanish (Español)",
        "fr" to "French (Français)",
        "de" to "German (Deutsch)",
        "ar" to "Arabic (العربية)",
        "ja" to "Japanese (日本語)",
        "pt" to "Portuguese (Português)",
        "zh-CN" to "Chinese (Simplified)"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Translate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Translate page",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Choose target language for this page",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            LazyColumn(modifier = Modifier.height(340.dp)) {
                items(languages) { (code, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectLanguage(code, label) }
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
