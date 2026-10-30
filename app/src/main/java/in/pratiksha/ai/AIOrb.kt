package `in`.pratiksha.ai

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AIOrb(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "orb")

    val pulse by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            tween(2200, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(12000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .size(280.dp)
            .graphicsLayer {
                scaleX = pulse
                scaleY = pulse
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size(270.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xAA8B5CFF),
                            Color(0x554A8CFF),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Canvas(Modifier.size(255.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2

            rotate(rotation) {
                drawCircle(
                    color = Color(0xFFB78CFF),
                    radius = radius * 0.88f,
                    center = center,
                    style = Stroke(2.dp.toPx())
                )

                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(
                            Color.Transparent,
                            Color(0xFF9D68FF),
                            Color(0xFF57D8FF),
                            Color.Transparent
                        )
                    ),
                    startAngle = 0f,
                    sweepAngle = 245f,
                    useCenter = false,
                    topLeft = Offset(
                        center.x - radius * 0.96f,
                        center.y - radius * 0.96f
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        radius * 1.92f,
                        radius * 1.92f
                    ),
                    style = Stroke(4.dp.toPx())
                )

                repeat(12) { index ->
                    val angle = Math.toRadians(index * 30.0)
                    val x = center.x + cos(angle).toFloat() * radius * 0.88f
                    val y = center.y + sin(angle).toFloat() * radius * 0.88f

                    drawCircle(
                        color = Color(0xFF9C7BFF),
                        radius = 2.5.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }

        Box(
            Modifier
                .size(185.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFFB9A2FF),
                            Color(0xFF7951FF),
                            Color(0xFF302078),
                            Color(0xFF10152E)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("✦", color = Color.White, fontSize = 34.sp)
                Text(
                    "P",
                    color = Color.White,
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "PRATIKSHA AI",
                    color = Color.White,
                    fontSize = 10.sp,
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
