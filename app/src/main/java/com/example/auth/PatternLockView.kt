package com.example.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CrimsonGlow
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.PaseoBorderDark
import com.example.ui.theme.PaseoSurfaceDark

@Composable
fun PatternLockView(
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    enabled: Boolean = true,
    onPatternCompleted: (List<Int>) -> Unit
) {
    val selectedDots = remember { mutableStateListOf<Int>() }
    var currentTouchPoint by remember { mutableStateOf<Offset?>(null) }

    val activeColor = if (isError) Color(0xFFFF3333) else CrimsonPrimary
    val activeGlow = if (isError) Color(0x66FF3333) else CrimsonGlow
    val idleNodeColor = PaseoBorderDark
    val innerDotColor = if (isError) Color(0xFFFF5252) else Color(0xFFFF6B81)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(PaseoSurfaceDark)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput

                    detectDragGestures(
                        onDragStart = { offset ->
                            selectedDots.clear()
                            currentTouchPoint = offset
                            val hitNode = findHitNode(offset, size.width.toFloat(), size.height.toFloat())
                            if (hitNode != null) {
                                selectedDots.add(hitNode)
                            }
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentTouchPoint = change.position
                            val hitNode = findHitNode(change.position, size.width.toFloat(), size.height.toFloat())
                            if (hitNode != null && !selectedDots.contains(hitNode)) {
                                selectedDots.add(hitNode)
                            }
                        },
                        onDragEnd = {
                            currentTouchPoint = null
                            if (selectedDots.isNotEmpty()) {
                                onPatternCompleted(selectedDots.toList())
                            }
                        },
                        onDragCancel = {
                            currentTouchPoint = null
                            selectedDots.clear()
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height
            val cellWidth = width / 3f
            val cellHeight = height / 3f

            fun getNodeCenter(index: Int): Offset {
                val col = index % 3
                val row = index / 3
                return Offset(
                    x = col * cellWidth + cellWidth / 2f,
                    y = row * cellHeight + cellHeight / 2f
                )
            }

            // Draw connecting lines between selected dots
            if (selectedDots.size > 1) {
                for (i in 0 until selectedDots.size - 1) {
                    val start = getNodeCenter(selectedDots[i])
                    val end = getNodeCenter(selectedDots[i + 1])

                    // Glow line
                    drawLine(
                        color = activeGlow,
                        start = start,
                        end = end,
                        strokeWidth = 14.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    // Core line
                    drawLine(
                        color = activeColor,
                        start = start,
                        end = end,
                        strokeWidth = 5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // Draw line to current touch point while dragging
            val lastDot = selectedDots.lastOrNull()
            val touch = currentTouchPoint
            if (lastDot != null && touch != null) {
                val start = getNodeCenter(lastDot)
                drawLine(
                    color = activeColor.copy(alpha = 0.7f),
                    start = start,
                    end = touch,
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Draw 3x3 Nodes
            val nodeRadius = 26.dp.toPx()
            val dotRadius = 8.dp.toPx()

            for (i in 0 until 9) {
                val center = getNodeCenter(i)
                val isSelected = selectedDots.contains(i)

                if (isSelected) {
                    // Outer glow circle
                    drawCircle(
                        color = activeGlow,
                        radius = nodeRadius + 6.dp.toPx(),
                        center = center
                    )
                    // Selected node ring
                    drawCircle(
                        color = activeColor,
                        radius = nodeRadius,
                        center = center,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    // Inner filled dot
                    drawCircle(
                        color = innerDotColor,
                        radius = dotRadius + 2.dp.toPx(),
                        center = center
                    )
                } else {
                    // Idle node ring
                    drawCircle(
                        color = idleNodeColor,
                        radius = nodeRadius - 4.dp.toPx(),
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    // Idle inner dot
                    drawCircle(
                        color = Color(0xFF6B6C82),
                        radius = dotRadius - 3.dp.toPx(),
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
    val hitTolerance = (cellWidth * 0.42f)

    for (row in 0 until 3) {
        for (col in 0 until 3) {
            val centerX = col * cellWidth + cellWidth / 2f
            val centerY = row * cellHeight + cellHeight / 2f

            val dx = point.x - centerX
            val dy = point.y - centerY
            val distance = kotlin.math.sqrt(dx * dx + dy * dy)

            if (distance <= hitTolerance) {
                return row * 3 + col
            }
        }
    }
    return null
}
