package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CrosshairConfig
import com.example.model.CrosshairShape
import com.example.ui.theme.CrimsonGlow
import com.example.ui.theme.PaseoBorderGreen
import com.example.ui.theme.PaseoDarkBackground
import com.example.ui.theme.TextMuted

@Composable
fun CrosshairPreview(
    config: CrosshairConfig,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(PaseoDarkBackground)
            .border(1.dp, PaseoBorderGreen, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Live Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f + (config.offsetX * density * 0.5f)
            val cy = size.height / 2f + (config.offsetY * density * 0.5f)

            // Background subtle grid
            val gridStep = 24.dp.toPx()
            val gridColor = Color(0xFF14201A)
            var x = 0f
            while (x < size.width) {
                drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                x += gridStep
            }
            var y = 0f
            while (y < size.height) {
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                y += gridStep
            }

            // Target circle guidelines
            drawCircle(
                color = Color(0xFF1B2C23),
                radius = 36.dp.toPx(),
                center = Offset(cx, cy),
                style = Stroke(width = 1f)
            )
            drawCircle(
                color = Color(0xFF1B2C23),
                radius = 64.dp.toPx(),
                center = Offset(cx, cy),
                style = Stroke(width = 1f)
            )

            // Crosshair Render Parameters
            val sizePx = (config.sizeDp * density) / 2f
            val thicknessPx = config.thicknessDp * density
            val gapPx = config.gapDp * density
            val dotRadiusPx = config.centerDotRadius * density

            val colorInt = config.colorValue.toInt()
            val alpha = config.opacity.coerceIn(0.1f, 1f)
            val mainColor = Color(
                red = ((colorInt shr 16) and 0xFF) / 255f,
                green = ((colorInt shr 8) and 0xFF) / 255f,
                blue = (colorInt and 0xFF) / 255f,
                alpha = alpha
            )
            val outlineColor = Color.Black.copy(alpha = alpha * 0.9f)
            val outlineThickness = thicknessPx + (1.5f * density)

            // Center Dot
            if (config.showCenterDot) {
                drawCircle(outlineColor, radius = dotRadiusPx + (1f * density), center = Offset(cx, cy))
                drawCircle(mainColor, radius = dotRadiusPx, center = Offset(cx, cy))
            }

            when (config.shape) {
                CrosshairShape.CLASSIC_CROSS -> {
                    // Top
                    drawLine(outlineColor, Offset(cx, cy - gapPx - sizePx), Offset(cx, cy - gapPx), strokeWidth = outlineThickness, cap = StrokeCap.Round)
                    drawLine(mainColor, Offset(cx, cy - gapPx - sizePx), Offset(cx, cy - gapPx), strokeWidth = thicknessPx, cap = StrokeCap.Round)
                    // Bottom
                    drawLine(outlineColor, Offset(cx, cy + gapPx), Offset(cx, cy + gapPx + sizePx), strokeWidth = outlineThickness, cap = StrokeCap.Round)
                    drawLine(mainColor, Offset(cx, cy + gapPx), Offset(cx, cy + gapPx + sizePx), strokeWidth = thicknessPx, cap = StrokeCap.Round)
                    // Left
                    drawLine(outlineColor, Offset(cx - gapPx - sizePx, cy), Offset(cx - gapPx, cy), strokeWidth = outlineThickness, cap = StrokeCap.Round)
                    drawLine(mainColor, Offset(cx - gapPx - sizePx, cy), Offset(cx - gapPx, cy), strokeWidth = thicknessPx, cap = StrokeCap.Round)
                    // Right
                    drawLine(outlineColor, Offset(cx + gapPx, cy), Offset(cx + gapPx + sizePx, cy), strokeWidth = outlineThickness, cap = StrokeCap.Round)
                    drawLine(mainColor, Offset(cx + gapPx, cy), Offset(cx + gapPx + sizePx, cy), strokeWidth = thicknessPx, cap = StrokeCap.Round)
                }
                CrosshairShape.DOT -> {
                    val radius = sizePx / 2.5f
                    drawCircle(outlineColor, radius = radius + (1.5f * density), center = Offset(cx, cy))
                    drawCircle(mainColor, radius = radius, center = Offset(cx, cy))
                }
                CrosshairShape.CIRCLE_CROSS -> {
                    val circleR = sizePx * 0.7f
                    drawCircle(outlineColor, radius = circleR, center = Offset(cx, cy), style = Stroke(width = outlineThickness))
                    drawCircle(mainColor, radius = circleR, center = Offset(cx, cy), style = Stroke(width = thicknessPx))

                    // 4 tick lines
                    val tickLen = sizePx * 0.4f
                    drawLine(outlineColor, Offset(cx, cy - circleR - tickLen), Offset(cx, cy - circleR), strokeWidth = outlineThickness, cap = StrokeCap.Round)
                    drawLine(mainColor, Offset(cx, cy - circleR - tickLen), Offset(cx, cy - circleR), strokeWidth = thicknessPx, cap = StrokeCap.Round)

                    drawLine(outlineColor, Offset(cx, cy + circleR), Offset(cx, cy + circleR + tickLen), strokeWidth = outlineThickness, cap = StrokeCap.Round)
                    drawLine(mainColor, Offset(cx, cy + circleR), Offset(cx, cy + circleR + tickLen), strokeWidth = thicknessPx, cap = StrokeCap.Round)

                    drawLine(outlineColor, Offset(cx - circleR - tickLen, cy), Offset(cx - circleR, cy), strokeWidth = outlineThickness, cap = StrokeCap.Round)
                    drawLine(mainColor, Offset(cx - circleR - tickLen, cy), Offset(cx - circleR, cy), strokeWidth = thicknessPx, cap = StrokeCap.Round)

                    drawLine(outlineColor, Offset(cx + circleR, cy), Offset(cx + circleR + tickLen, cy), strokeWidth = outlineThickness, cap = StrokeCap.Round)
                    drawLine(mainColor, Offset(cx + circleR, cy), Offset(cx + circleR + tickLen, cy), strokeWidth = thicknessPx, cap = StrokeCap.Round)
                }
                CrosshairShape.TACTICAL_T -> {
                    // Left, Right, Bottom bars
                    drawLine(outlineColor, Offset(cx - gapPx - sizePx, cy), Offset(cx - gapPx, cy), strokeWidth = outlineThickness, cap = StrokeCap.Round)
                    drawLine(mainColor, Offset(cx - gapPx - sizePx, cy), Offset(cx - gapPx, cy), strokeWidth = thicknessPx, cap = StrokeCap.Round)

                    drawLine(outlineColor, Offset(cx + gapPx, cy), Offset(cx + gapPx + sizePx, cy), strokeWidth = outlineThickness, cap = StrokeCap.Round)
                    drawLine(mainColor, Offset(cx + gapPx, cy), Offset(cx + gapPx + sizePx, cy), strokeWidth = thicknessPx, cap = StrokeCap.Round)

                    drawLine(outlineColor, Offset(cx, cy + gapPx), Offset(cx, cy + gapPx + sizePx), strokeWidth = outlineThickness, cap = StrokeCap.Round)
                    drawLine(mainColor, Offset(cx, cy + gapPx), Offset(cx, cy + gapPx + sizePx), strokeWidth = thicknessPx, cap = StrokeCap.Round)
                }
                CrosshairShape.SNIPER_CIRCLE -> {
                    val circleR = sizePx * 0.9f
                    drawCircle(outlineColor, radius = circleR, center = Offset(cx, cy), style = Stroke(width = outlineThickness))
                    drawCircle(mainColor, radius = circleR, center = Offset(cx, cy), style = Stroke(width = thicknessPx))

                    // Reticle lines
                    drawLine(mainColor, Offset(cx - circleR, cy), Offset(cx + circleR, cy), strokeWidth = thicknessPx * 0.8f)
                    drawLine(mainColor, Offset(cx, cy - circleR), Offset(cx, cy + circleR), strokeWidth = thicknessPx * 0.8f)
                }
                CrosshairShape.DIAMOND -> {
                    val d = sizePx * 0.8f
                    val points = listOf(
                        Offset(cx, cy - d),
                        Offset(cx + d, cy),
                        Offset(cx, cy + d),
                        Offset(cx - d, cy),
                        Offset(cx, cy - d)
                    )
                    drawPoints(points, PointMode.Polygon, outlineColor, strokeWidth = outlineThickness, cap = StrokeCap.Round)
                    drawPoints(points, PointMode.Polygon, mainColor, strokeWidth = thicknessPx, cap = StrokeCap.Round)
                }
            }
        }

        // Live Preview watermark badge
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Text(
                text = "LIVE RETICLE PREVIEW",
                color = TextMuted,
                fontSize = 10.sp
            )
        }
    }
}
