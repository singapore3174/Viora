package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.Post

@Composable
fun ReelsScreen(
    isDarkMode: Boolean,
    reelsList: List<Post>,
    onLikeToggle: (String) -> Unit,
    onSaveToggle: (String) -> Unit,
    onOpenComments: (Post) -> Unit
) {
    if (reelsList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDarkMode) DarkBackground else LightBackground),
            contentAlignment = Alignment.Center
        ) {
            Text("Aura Reels loading...", color = Color.Gray)
        }
        return
    }

    var currentReelIndex by remember { mutableStateOf(0) }
    val activeReel = reelsList[currentReelIndex % reelsList.size]

    // Vinyl spinning rotation
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_reels")
    val vinylRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing)
        ),
        label = "vinyl_rotation"
    )

    // Handle drag gesture swipe
    var dragAccumulator by remember { mutableStateOf(0f) }
    val dragThreshold = 150f

    val dragHandler = rememberDraggableState { delta ->
        dragAccumulator += delta
        if (dragAccumulator > dragThreshold) {
            // Swipe down: previous reel
            if (currentReelIndex > 0) {
                currentReelIndex--
            } else {
                currentReelIndex = reelsList.size - 1
            }
            dragAccumulator = 0f
        } else if (dragAccumulator < -dragThreshold) {
            // Swipe up: next reel
            if (currentReelIndex < reelsList.size - 1) {
                currentReelIndex++
            } else {
                currentReelIndex = 0
            }
            dragAccumulator = 0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .draggable(
                state = dragHandler,
                orientation = Orientation.Vertical,
                onDragStopped = { dragAccumulator = 0f }
            )
    ) {
        // Fullscreen backdrop visual representation
        AnimatedContent(
            targetState = activeReel,
            transitionSpec = {
                slideInVertically(animationSpec = tween(400)) { height -> -height } + fadeIn() togetherWith
                        slideOutVertically(animationSpec = tween(400)) { height -> height } + fadeOut()
            },
            label = "fullscreen_reel_animation"
        ) { reel ->
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(reel.mediaUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Reel fullscreen frames backdrop",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // High contrast bottom gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Transparent, Color(0x99000000), Color(0xDD000000))
                            )
                        )
                )
            }
        }

        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val titleBrush = Brush.linearGradient(
                colors = listOf(CyberpunkCyan, CyberpunkPurple)
            )
            Text(
                text = "AURA REELS",
                style = LocalTextStyle.current.copy(
                    brush = titleBrush,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                ),
                letterSpacing = (-1).sp
            )
            Text(
                text = "Live Stream",
                color = CyberpunkCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(Color.Red, shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        // Right side Action overlays
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Liking
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { onLikeToggle(activeReel.id) },
                    modifier = Modifier
                        .background(Color(0x66000000), shape = CircleShape)
                        .size(44.dp)
                        .testTag("like_reel_${activeReel.id}")
                ) {
                    Text(text = if (activeReel.isLiked) "❤️" else "🖤", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "${activeReel.likesCount}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // Comments
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { onOpenComments(activeReel) },
                    modifier = Modifier
                        .background(Color(0x66000000), shape = CircleShape)
                        .size(44.dp)
                ) {
                    Text(text = "💬", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "${activeReel.commentsCount}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // Share Air points
            IconButton(
                onClick = {},
                modifier = Modifier
                    .background(Color(0x66000000), shape = CircleShape)
                    .size(44.dp)
            ) {
                Text(text = "✈️", fontSize = 20.sp)
            }

            // Bookmark save bucket
            IconButton(
                onClick = { onSaveToggle(activeReel.id) },
                modifier = Modifier
                    .background(Color(0x66000000), shape = CircleShape)
                    .size(44.dp)
            ) {
                Text(text = if (activeReel.isSaved) "🔮" else "🏳️", fontSize = 20.sp)
            }

            // Spinning music Vinyl disk representation
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .border(2.dp, Color.White, shape = CircleShape)
                    .clip(CircleShape)
                    .rotate(vinylRotation)
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Text("🎵", fontSize = 18.sp)
            }
        }

        // Bottom left titles details
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 100.dp, start = 16.dp, end = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AuraAvatar(
                    avatarUrl = activeReel.userAvatarUrl,
                    size = 36.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "@" + activeReel.username,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Text(
                text = activeReel.caption,
                color = Color.LightGray,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Auto scrolling music bar loop simulator
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🎵", fontSize = 11.sp, color = CyberpunkCyan)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = activeReel.musicName ?: "Original sound wave - Aura",
                    color = CyberpunkCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Swipe instructional tip top right
        Text(
            text = "Drag vertically to flip reels",
            color = Color.Gray,
            fontSize = 10.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )
    }
}
