package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.Comment
import com.example.data.Post
import com.example.data.Story
import com.example.data.UserProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeFeedScreen(
    isDarkMode: Boolean,
    allProfiles: List<UserProfile>,
    feedPosts: List<Post>,
    stories: List<Story>,
    notificationsCount: Int,
    onStoryClick: (Story) -> Unit,
    onLikeToggle: (String) -> Unit,
    onSaveToggle: (String) -> Unit,
    onFollowToggle: (String) -> Unit,
    onOpenComments: (Post) -> Unit,
    onOpenChatPartner: (UserProfile) -> Unit,
    onOpenNotifications: () -> Unit
) {
    val bgCol = if (isDarkMode) DarkBackground else LightBackground
    val textColor = if (isDarkMode) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val cardBg = if (isDarkMode) GlassWhite else Color(0xD9FFFFFF)
    val cardBorder = if (isDarkMode) GlassBorderWhite else Color(0x22000000)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgCol)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- TOP HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 44.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Aura typography title
                val titleBrush = Brush.linearGradient(
                    colors = listOf(CyberpunkCyan, CyberpunkPurple)
                )
                Text(
                    text = "AURA",
                    style = LocalTextStyle.current.copy(
                        brush = titleBrush,
                        fontWeight = FontWeight.Black,
                        fontSize = 28.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    letterSpacing = (-1.5).sp,
                    modifier = Modifier.testTag("app_logo_title")
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Notifications Heart
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .clickable { onOpenNotifications() }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🔔", fontSize = 20.sp, modifier = Modifier.testTag("notif_bell_btn"))
                        if (notificationsCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .align(Alignment.TopEnd)
                                    .background(CyberpunkPink, shape = CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Inbox Direct Message
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .clickable {
                                // Default chat with Aero companion
                                val aero = allProfiles.find { it.username == "aero_ai" }
                                if (aero != null) {
                                    onOpenChatPartner(aero)
                                }
                            }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "✉️", fontSize = 20.sp, modifier = Modifier.testTag("inbox_chat_btn"))
                    }
                }
            }

            // --- MAIN FEED LazyColumn ---
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // 1. Stories Row
                item {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(stories, key = { it.id }) { story ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { onStoryClick(story) }
                            ) {
                                AuraAvatar(
                                    avatarUrl = story.userAvatarUrl,
                                    size = 64.dp,
                                    hasStory = true,
                                    storyViewed = story.isViewed
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = story.userDisplayName.split(" ").firstOrNull() ?: story.username,
                                    fontSize = 11.sp,
                                    color = textColor,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.width(64.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    Divider(color = cardBorder, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                }

                // 2. Feed list posts
                items(feedPosts, key = { it.id }) { post ->
                    val authorProfile = allProfiles.find { it.username == post.username }
                    PostItem(
                        post = post,
                        author = authorProfile,
                        isDarkMode = isDarkMode,
                        textColor = textColor,
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        onLike = onLikeToggle,
                        onSave = onSaveToggle,
                        onFollow = onFollowToggle,
                        onComments = onOpenComments,
                        onDirectMessage = onOpenChatPartner
                    )
                }

                // 3. Suggested Accounts section if list is loaded
                if (feedPosts.isNotEmpty()) {
                    item {
                        SuggestedAccountsSection(
                            allProfiles = allProfiles,
                            onFollowToggle = onFollowToggle,
                            isDarkMode = isDarkMode,
                            textColor = textColor,
                            cardBorder = cardBorder
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostItem(
    post: Post,
    author: UserProfile?,
    isDarkMode: Boolean,
    textColor: Color,
    cardBg: Color,
    cardBorder: Color,
    onLike: (String) -> Unit,
    onSave: (String) -> Unit,
    onFollow: (String) -> Unit,
    onComments: (Post) -> Unit,
    onDirectMessage: (UserProfile) -> Unit
) {
    var showDoubleTapLove by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp)
            .background(cardBg, shape = RoundedCornerShape(32.dp))
            .border(1.dp, cardBorder, shape = RoundedCornerShape(32.dp))
    ) {
        // Post Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AuraAvatar(
                avatarUrl = post.userAvatarUrl,
                size = 44.dp,
                onClick = null
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = post.userDisplayName,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "@" + post.username,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }

            // Follow button state
            if (post.username != "aura_dreamer") {
                val isFollowing = author?.isFollowing == true
                Text(
                    text = if (isFollowing) "Following" else "Follow",
                    color = if (isFollowing) Color.Gray else CyberpunkPink,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onFollow(post.username) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // Post Media with Dynamic double tap gesture
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(24.dp))
                .combinedClickable(
                    onDoubleClick = {
                        if (!post.isLiked) {
                            onLike(post.id)
                        }
                        showDoubleTapLove = true
                        coroutineScope.launch {
                            delay(800)
                            showDoubleTapLove = false
                        }
                    },
                    onClick = {}
                ),
            contentAlignment = Alignment.Center
        ) {
            // Unsplash loaded photostream
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(post.mediaUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Post file content",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop
            )

            // Dynamic cyberpunk shader effect representations on overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        when (post.filterApplied) {
                            "Neon Cyan" -> Brush.verticalGradient(
                                listOf(CyberpunkCyan.copy(alpha = 0.15f), Color.Transparent)
                            )
                            "Cyberpunk" -> Brush.linearGradient(
                                listOf(CyberpunkPink.copy(alpha = 0.12f), CyberpunkPurple.copy(alpha = 0.12f))
                            )
                            "Warm Gold" -> Brush.verticalGradient(
                                listOf(Color(0xFFE5A93C).copy(alpha = 0.15f), Color.Transparent)
                            )
                            else -> Brush.radialGradient(listOf(Color.Transparent, Color.Transparent))
                        }
                    )
            )

            // Animated double tap heart
            androidx.compose.animation.AnimatedVisibility(
                visible = showDoubleTapLove,
                enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xBB000000), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("❤️", fontSize = 48.sp)
                }
            }

            // Filter indicator badge bottom right
            if (post.filterApplied != "Normal") {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .background(Color(0xE0000000), shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = post.filterApplied,
                        color = CyberpunkCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Action icons row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Like Button
                IconButton(
                    onClick = { onLike(post.id) },
                    modifier = Modifier.testTag("like_post_${post.id}")
                ) {
                    Text(
                        text = if (post.isLiked) "❤️" else "🖤",
                        fontSize = 22.sp
                    )
                }

                // Comment Button
                IconButton(onClick = { onComments(post) }) {
                    Text("💬", fontSize = 22.sp)
                }

                // Send/Share direct button
                IconButton(onClick = {
                    if (author != null) {
                        onDirectMessage(author)
                    }
                }) {
                    Text("✈️", fontSize = 22.sp)
                }
            }

            // Save bookmark button
            IconButton(onClick = { onSave(post.id) }) {
                Text(
                    text = if (post.isSaved) "🔮" else "🏳️",
                    fontSize = 22.sp
                )
            }
        }

        // Likes count & caption text and comments count
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Text(
                text = "${post.likesCount} aura-points",
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = post.username + " ",
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontSize = 13.sp
                )
                Text(
                    text = post.caption,
                    color = if (isDarkMode) Color.LightGray else Color.DarkGray,
                    fontSize = 13.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (post.commentsCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "View all ${post.commentsCount} aesthetic comments",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clickable { onComments(post) }
                        .padding(vertical = 2.dp)
                )
            }
        }
    }
}

// --- Suggested Accounts Component ---
@Composable
fun SuggestedAccountsSection(
    allProfiles: List<UserProfile>,
    onFollowToggle: (String) -> Unit,
    isDarkMode: Boolean,
    textColor: Color,
    cardBorder: Color
) {
    val nonUserProfiles = allProfiles.filter { it.username != "aura_dreamer" && !it.isFollowing }

    if (nonUserProfiles.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Suggested Curators",
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(nonUserProfiles) { profile ->
                    Column(
                        modifier = Modifier
                            .width(140.dp)
                            .background(
                                if (isDarkMode) Color(0x10FFFFFF) else Color(0x0A000000),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(1.dp, cardBorder, shape = RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AuraAvatar(
                            avatarUrl = profile.avatarUrl,
                            size = 52.dp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = profile.displayName,
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "@" + profile.username,
                            color = Color.Gray,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(CyberpunkPurple)
                                .clickable { onFollowToggle(profile.username) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Curate", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// --- COMMENTS BOTTOM SHEET OVERLAY OVERLAY ---
@Composable
fun CommentsOverlay(
    isDarkMode: Boolean,
    post: Post?,
    comments: List<Comment>,
    onAddComment: (commentText: String) -> Unit,
    onClose: () -> Unit
) {
    if (post != null) {
        var commentDraft by remember { mutableStateOf("") }
        val sheetBg = if (isDarkMode) GlassWhite else Color(0x0CFFFFFF)
        val sheetBorder = if (isDarkMode) GlassBorderWhite else Color(0x1AFFFFFF)
        val textColor = if (isDarkMode) Color(0xFFF8FAFC) else Color(0xFFE2E8F0)

        Dialog(
            onDismissRequest = onClose,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(16.dp)
                    .border(1.dp, sheetBorder, shape = RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = sheetBg)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Aura Comments",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = textColor
                        )
                        IconButton(onClick = onClose) {
                            Text("✕", color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Divider(color = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

                        // Comments List view
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            if (comments.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "No comments on this vibe yet. Be first to add comments!",
                                            color = Color.Gray,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            } else {
                                items(comments, key = { it.id }) { comment ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 10.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        AuraAvatar(avatarUrl = comment.userAvatarUrl, size = 36.dp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = comment.userDisplayName,
                                                    color = textColor,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "@" + comment.username,
                                                    color = Color.Gray,
                                                    fontSize = 10.sp
                                                )
                                            }
                                            Text(
                                                text = comment.text,
                                                color = if (isDarkMode) Color.LightGray else Color.DarkGray,
                                                fontSize = 13.sp,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Divider(color = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

                        // Footer Input Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .imePadding(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = commentDraft,
                                onValueChange = { commentDraft = it },
                                placeholder = { Text("Add sound aesthetics comment") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("comment_draft_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberpunkPink,
                                    focusedTextColor = textColor,
                                    unfocusedTextColor = textColor
                                ),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            IconButton(
                                onClick = {
                                    if (commentDraft.isNotBlank()) {
                                        onAddComment(commentDraft)
                                        commentDraft = ""
                                    }
                                },
                                modifier = Modifier.background(CyberpunkPink, shape = CircleShape)
                            ) {
                                Text("⚡", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
