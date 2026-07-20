package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Color helper list for charts
val chartColors = listOf(
    Color(0xFF10B981), // Emerald
    Color(0xFF06B6D4), // Cyan
    Color(0xFFF59E0B), // Gold
    Color(0xFFEC4899), // Pink
    Color(0xFF3B82F6), // Blue
    Color(0xFF8B5CF6), // Purple
    Color(0xFFEF4444), // Red
    Color(0xFF14B8A6), // Teal
    Color(0xFF84CC16)  // Lime
)

@Composable
fun CustomDonutChart(
    data: Map<String, Double>,
    modifier: Modifier = Modifier,
    lang: String = "ar"
) {
    val total = data.values.sum()
    if (total == 0.0) {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (lang == "ar") "لا توجد مصروفات لعرضها" else "No expenses to display",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
        }
        return
    }

    var animationTriggered by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationTriggered) 1f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    LaunchedEffect(data) {
        animationTriggered = true
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Donut Canvas
        Box(
            modifier = Modifier.size(140.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                val strokeWidth = 35f

                data.values.forEachIndexed { index, value ->
                    val sweepAngle = (value / total * 360f).toFloat() * animatedProgress
                    val color = chartColors[index % chartColors.size]

                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth),
                        size = Size(size.width - strokeWidth, size.height - strokeWidth),
                        topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                    )
                    startAngle += sweepAngle
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (lang == "ar") "الإنفاق" else "Spent",
                    style = MaterialTheme.styleSchemeTitleSmall(),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "${"%.1f".format(total)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Legend
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            data.entries.take(5).forEachIndexed { index, entry ->
                val percentage = (entry.value / total * 100).toInt()
                val color = chartColors[index % chartColors.size]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Text(
                        text = "${entry.key} ($percentage%)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun CustomBarChart(
    data: List<Double>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    val maxVal = data.maxOrNull()?.coerceAtLeast(100.0) ?: 100.0
    var animationTriggered by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationTriggered) 1f else 0f,
        animationSpec = tween(durationMillis = 800)
    )

    LaunchedEffect(data) {
        animationTriggered = true
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            val width = constraints.maxWidth.toFloat()
            val height = constraints.maxHeight.toFloat()
            val barCount = data.size
            val barWidth = (width / (barCount * 1.6f)).coerceAtMost(40f)
            val spacing = (width - (barWidth * barCount)) / (barCount + 1)

            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw dynamic bars
                data.forEachIndexed { index, value ->
                    val barHeight = ((value / maxVal) * (height * 0.85f)).toFloat() * animatedProgress
                    val x = spacing + index * (barWidth + spacing)
                    val y = height - barHeight

                    // Draw rounded bar with elegant gradient
                    val brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF10B981), Color(0xFF06B6D4))
                    )

                    drawRoundRect(
                        brush = brush,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun CustomLineChart(
    data: List<Double>,
    modifier: Modifier = Modifier
) {
    if (data.size < 2) {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "سجل المزيد من العمليات لعرض المنحنى",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        return
    }

    val maxVal = data.maxOrNull()?.coerceAtLeast(100.0) ?: 100.0
    val minVal = data.minOrNull() ?: 0.0
    val diff = (maxVal - minVal).coerceAtLeast(1.0)

    var animationTriggered by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationTriggered) 1f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    LaunchedEffect(data) {
        animationTriggered = true
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            val width = size.width
            val height = size.height
            val pointsCount = data.size
            val xInterval = width / (pointsCount - 1)

            val path = Path()
            val fillPath = Path()

            data.forEachIndexed { index, value ->
                val x = index * xInterval
                val normY = ((value - minVal) / diff).toFloat()
                val y = height - (normY * (height * 0.8f))

                if (index == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, height)
                    fillPath.lineTo(x, y)
                } else {
                    // Cubic bezier for elegant smoothing curves
                    val prevX = (index - 1) * xInterval
                    val prevNormY = ((data[index - 1] - minVal) / diff).toFloat()
                    val prevY = height - (prevNormY * (height * 0.8f))

                    val controlX1 = prevX + (xInterval / 2f)
                    val controlY1 = prevY
                    val controlX2 = prevX + (xInterval / 2f)
                    val controlY2 = y

                    path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                    fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                }

                if (index == pointsCount - 1) {
                    fillPath.lineTo(x, height)
                    fillPath.close()
                }
            }

            // Draw fading area under curve
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF06B6D4).copy(alpha = 0.35f * animatedProgress),
                        Color(0xFF06B6D4).copy(alpha = 0.0f)
                    )
                )
            )

            // Draw line
            drawPath(
                path = path,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF10B981), Color(0xFF06B6D4))
                ),
                style = Stroke(width = 6f)
            )
        }
    }
}

// Material Theme backward typography utility
@Composable
fun MaterialTheme.styleSchemeTitleSmall() = MaterialTheme.typography.titleMedium.copy(
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp
)
