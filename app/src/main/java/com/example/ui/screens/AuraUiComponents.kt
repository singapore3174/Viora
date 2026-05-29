package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.Story

// --- Theme Color Palettes ---
val CyberpunkPink = Color(0xFFEC4899)
val CyberpunkCyan = Color(0xFF22D3EE)
val CyberpunkPurple = Color(0xFFA855F7)
val GlassWhite = Color(0x0EFFFFFF) // 5.5% opaque white
val GlassBorderWhite = Color(0x1AFFFFFF) // 10% opaque white
val DarkBackground = Color(0xFF050505)
val LightBackground = Color(0xFF0C0D14)

val AvatarGradients = mapOf(
    "rose" to Brush.linearGradient(listOf(Color(0xFFFE5196), Color(0xFFF77062))),
    "cyan" to Brush.linearGradient(listOf(Color(0xFF09F0FF), Color(0xFF0072FF))),
    "gold" to Brush.linearGradient(listOf(Color(0xFFFAD961), Color(0xFFF76B1C))),
    "purple" to Brush.linearGradient(listOf(Color(0xFFD4145A), Color(0xFFFBB03B))),
    "indigo" to Brush.linearGradient(listOf(Color(0xFF00C6FF), Color(0xFF0072FF)))
)

fun getAvatarGradient(colorName: String?): Brush {
    return AvatarGradients[colorName] ?: Brush.linearGradient(listOf(Color(0xFF8A2BE2), Color(0xFFFF2E93)))
}

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val bg = if (isDark) GlassWhite else Color(0x0AFFFFFF)
    val borderCol = if (isDark) GlassBorderWhite else Color(0x15FFFFFF)
    Column(
        modifier = modifier
            .background(bg, shape = RoundedCornerShape(32.dp))
            .border(1.dp, borderCol, shape = RoundedCornerShape(32.dp))
            .padding(16.dp),
        content = content
    )
}

@Composable
fun AuraAvatar(
    avatarUrl: String?,
    size: Dp = 56.dp,
    hasStory: Boolean = false,
    storyViewed: Boolean = false,
    isOnline: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "story_ring")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .size(size)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        contentAlignment = Alignment.Center
    ) {
        if (hasStory) {
            val ringBrush = if (storyViewed) {
                Brush.linearGradient(listOf(Color(0x4DFFFFFF), Color(0x22FFFFFF)))
            } else {
                Brush.sweepGradient(
                    listOf(
                        Color(0xFFFACC15), // yellow-400
                        Color(0xFFEF4444), // red-500
                        Color(0xFFEC4899), // pink-500
                        Color(0xFFA855F7), // purple-500
                        Color(0xFF22D3EE), // cyan-400
                        Color(0xFFFACC15)  // yellow-400
                    )
                )
            }
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        // Drawing static or animated rotating outer ring
                    }
            ) {
                drawArc(
                    brush = ringBrush,
                    startAngle = rotationAngle,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (hasStory) 5.dp else 0.dp)
                .clip(CircleShape)
                .background(getAvatarGradient(avatarUrl)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = (avatarUrl ?: "A").take(1).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.4f).sp,
                textAlign = TextAlign.Center
            )
        }

        if (isOnline) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .align(Alignment.BottomEnd)
                    .background(Color.Black, shape = CircleShape)
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF00FF66), shape = CircleShape)
                )
            }
        }
    }
}

@Composable
fun GradientButton(
    text: String,
    modifier: Modifier = Modifier,
    testTag: String = "",
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .testTag(testTag)
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(
                Brush.linearGradient(
                    listOf(CyberpunkPink, CyberpunkPurple, CyberpunkCyan)
                )
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

// --- Dynamic Story Viewer Overlay Overlay ---
@Composable
fun StoryViewerOverlay(
    story: Story?,
    onClose: () -> Unit
) {
    AnimatedVisibility(
        visible = story != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        if (story != null) {
            var progress by remember { mutableStateOf(0f) }
            LaunchedEffect(story.id) {
                progress = 0f
                val anim = TargetAnimation()
                animate(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = tween(5000, easing = LinearEasing)
                ) { value, _ ->
                    progress = value
                }
                onClose()
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // Background image
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(story.mediaUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Story content",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // High shadow overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0x90000000), Color.Transparent, Color(0xAA000000))
                            )
                        )
                )

                // Top Bars and progress
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(top = 48.dp, start = 16.dp, end = 16.dp)
                ) {
                    // Progress indicator
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Color.White,
                        trackColor = Color(0x33FFFFFF)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AuraAvatar(
                            avatarUrl = story.userAvatarUrl,
                            size = 40.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = story.userDisplayName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "@" + story.username,
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                        IconButton(onClick = onClose) {
                            Text("✕", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Middle Text Overlay
                if (!story.textOverlay.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(Color(0x99000000), shape = RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0x30FFFFFF), shape = RoundedCornerShape(16.dp))
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = story.textOverlay,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Story Feed Action Footer
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp, start = 16.dp, end = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Swipe down or tap close to exit • Powered by Aura",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

class TargetAnimation
