package com.bansify.browser.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bansify.browser.browser.UrlUtils

/**
 * Authentic Chrome Android Incognito Hat & Glasses vector drawn crisp on Canvas.
 */
@Composable
fun IncognitoIcon(
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Hat crown (trapezoid)
        val hatPath = Path().apply {
            moveTo(w * 0.27f, h * 0.44f)
            lineTo(w * 0.35f, h * 0.14f)
            lineTo(w * 0.65f, h * 0.14f)
            lineTo(w * 0.73f, h * 0.44f)
            close()
        }
        drawPath(path = hatPath, color = tint)

        // Hat brim
        drawLine(
            color = tint,
            start = Offset(w * 0.10f, h * 0.48f),
            end = Offset(w * 0.90f, h * 0.48f),
            strokeWidth = h * 0.085f,
            cap = StrokeCap.Round
        )

        // Glasses left & right lenses
        val lensRadius = w * 0.145f
        val lensY = h * 0.72f
        val leftCenter = Offset(w * 0.31f, lensY)
        val rightCenter = Offset(w * 0.69f, lensY)
        val strokeW = h * 0.075f

        drawCircle(
            color = tint,
            radius = lensRadius,
            center = leftCenter,
            style = Stroke(width = strokeW)
        )
        drawCircle(
            color = tint,
            radius = lensRadius,
            center = rightCenter,
            style = Stroke(width = strokeW)
        )

        // Glasses bridge
        drawLine(
            color = tint,
            start = Offset(leftCenter.x + lensRadius, lensY - h * 0.02f),
            end = Offset(rightCenter.x - lensRadius, lensY - h * 0.02f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Authentic Chrome Android rounded-square tab counter badge.
 * Displays ":D" when count >= 100 just like real Chrome!
 */
@Composable
fun ChromeTabCountBadge(
    count: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    filled: Boolean = false,
    contentColor: Color = color
) {
    val label = if (count > 99) ":D" else count.coerceAtLeast(0).toString()
    val fontSize = when {
        count > 99 -> 10.sp
        count >= 10 -> 10.5.sp
        else -> 12.sp
    }

    Box(
        modifier = modifier
            .size(22.dp)
            .clip(RoundedCornerShape(5.dp))
            .then(
                if (filled) {
                    Modifier.background(color)
                } else {
                    Modifier.border(
                        border = BorderStroke(1.8.dp, color),
                        shape = RoundedCornerShape(5.dp)
                    )
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = contentColor,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

/**
 * Renders a live WebView favicon bitmap if present, or a crisp Material 3 domain monogram badge.
 */
@Composable
fun SiteAvatar(
    title: String,
    url: String,
    favicon: Bitmap? = null,
    accentColor: Long? = null,
    size: Dp = 36.dp,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(favicon) { favicon?.asImageBitmap() }
    val bgColor = remember(url, accentColor) {
        Color(accentColor ?: UrlUtils.colorForDomain(url))
    }
    val letter = remember(title, url) {
        UrlUtils.extractFirstLetter(title, url)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(if (bitmap != null) MaterialTheme.colorScheme.surfaceContainerHigh else bgColor.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier
                    .size(size * 0.62f)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Fit
            )
        } else {
            Text(
                text = letter,
                color = bgColor,
                fontSize = (size.value * 0.44f).sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
