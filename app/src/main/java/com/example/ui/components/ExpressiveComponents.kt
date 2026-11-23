package com.example.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalIsDarkBrowserTheme

/**
 * Computes Chrome Android's exact Material 3 Expressive segmented card corners:
 * - Single item: 20.dp all around
 * - First item in group: 20.dp top, 4.dp bottom
 * - Middle item: 4.dp all around
 * - Last item in group: 4.dp top, 20.dp bottom
 */
fun expressiveCardShape(index: Int, totalCount: Int): Shape {
    val outer = 20.dp
    val inner = 4.dp
    return when {
        totalCount <= 1 -> RoundedCornerShape(outer)
        index == 0 -> RoundedCornerShape(topStart = outer, topEnd = outer, bottomStart = inner, bottomEnd = inner)
        index == totalCount - 1 -> RoundedCornerShape(topStart = inner, topEnd = inner, bottomStart = outer, bottomEnd = outer)
        else -> RoundedCornerShape(inner)
    }
}

@Composable
fun expressiveCanvasColor(): Color {
    return MaterialTheme.colorScheme.surfaceContainerLowest
}

@Composable
fun expressiveCardColor(): Color {
    val isDark = LocalIsDarkBrowserTheme.current
    return if (isDark) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.surface
}

@Composable
fun ExpressiveGroupedCard(
    index: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
    containerColor: Color = expressiveCardColor(),
    content: @Composable () -> Unit
) {
    Surface(
        shape = expressiveCardShape(index, totalCount),
        color = containerColor,
        modifier = modifier,
        content = content
    )
}
