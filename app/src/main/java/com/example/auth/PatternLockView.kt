package com.example.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CrimsonGlow
import com.example.ui.theme.CrimsonPrimary
import kotlin.math.sqrt

/**
 * PASEO SENSI — Interactive 3x3 Pattern Lock View
 * Developer: ZUCCHERO XANN
 */
@Composable
fun PatternLockView(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    onPatternComplete: (List<Int>) -> Unit
) {
    val selectedNodes = remember { mutableStateListOf<Int>() }
    var currentTouchPoint by remember { mutableStateOf<Offset?>(null) }

    val lineColor = if (isError) Color(0xFFFF4444) else CrimsonGlow
    val dotActiveColor = if (isError) Color(0xFFFF4444) else CrimsonPrimary
    val dotInactiveColor = Color(0xFF33333E)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(16.dp)
            .testTag("pattern_lock_canvas_container"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .testTag("pattern_lock_view")
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    detectDragGestures(
                        onDragStart = { offset ->
                            selectedNodes.clear()
                            currentTouchPoint = offset
                            val hitNode = findHitNode(offset, size.width.toFloat(), size.height.toFloat())
                            if (hitNode != null) {
                                selectedNodes.add(hitNode)
                            }
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentTouchPoint = change.position
                            val hitNode = findHitNode(change.position, size.width.toFloat(), size.height.toFloat())
                            if (hitNode != null && !selectedNodes.contains(hitNode)) {
                                selectedNodes.add(hitNode)
                            }
                        },
                        onDragEnd = {
                            currentTouchPoint = null
                            if (selectedNodes.isNotEmpty()) {
                                onPatternComplete(selectedNodes.toList())
                            }
                        },
                        onDragCancel = {
                            currentTouchPoint = null
                            selectedNodes.clear()
                        }
                    )
                }
        ) {
            val cellWidth = size.width / 3f
            val cellHeight = size.height / 3f
            val hitRadius = cellWidth * 0.35f
            val dotRadius = 8.dp.toPx()
            val activeDotRadius = 14.dp.toPx()

            fun getNodeCenter(index: Int): Offset {
                val col = index % 3
                val row = index / 3
                return Offset((col + 0.5f) * cellWidth, (row + 0.5f) * cellHeight)
            }

            // Draw connecting lines between consecutive selected dots
            if (selectedNodes.size > 1) {
                for (i in 0 until selectedNodes.size - 1) {
                    val start = getNodeCenter(selectedNodes[i])
                    val end = getNodeCenter(selectedNodes[i + 1])
                    drawLine(
                        color = lineColor,
                        start = start,
                        end = end,
                        strokeWidth = 6.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // Draw line to current touch point
            if (selectedNodes.isNotEmpty() && currentTouchPoint != null) {
                val lastNodeCenter = getNodeCenter(selectedNodes.last())
                drawLine(
                    color = lineColor.copy(alpha = 0.7f),
                    start = lastNodeCenter,
                    end = currentTouchPoint!!,
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Draw 9 dots
            for (i in 0..8) {
                val center = getNodeCenter(i)
                val isSelected = selectedNodes.contains(i)

                if (isSelected) {
                    // Outer glow ring
                    drawCircle(
                        color = dotActiveColor.copy(alpha = 0.3f),
                        radius = activeDotRadius * 1.5f,
                        center = center
                    )
                    // Inner ring
                    drawCircle(
                        color = dotActiveColor,
                        radius = activeDotRadius,
                        center = center,
                        style = Stroke(width = 3.dp.toPx())
                    )
                    // Core dot
                    drawCircle(
                        color = Color.White,
                        radius = dotRadius * 0.8f,
                        center = center
                    )
                } else {
                    drawCircle(
                        color = dotInactiveColor,
                        radius = dotRadius,
                        center = center
                    )
                    drawCircle(
                        color = Color(0xFF555566),
                        radius = dotRadius * 0.5f,
                        center = center
                    )
                }
            }
        }
    }
}

private fun findHitNode(point: Offset, totalWidth: Float, totalHeight: Float): Int? {
    val cellWidth = totalWidth / 3f
    val cellHeight = totalHeight / 3f
    val hitRadius = cellWidth * 0.35f

    for (i in 0..8) {
        val col = i % 3
        val row = i / 3
        val center = Offset((col + 0.5f) * cellWidth, (row + 0.5f) * cellHeight)
        val dx = point.x - center.x
        val dy = point.y - center.y
        val dist = sqrt(dx * dx + dy * dy)
        if (dist <= hitRadius) {
            return i
        }
    }
    return null
}
