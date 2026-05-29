package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.Post
import com.example.data.UserProfile

@Composable
fun ProfileScreen(
    isDarkMode: Boolean,
    currentUser: UserProfile?,
    allProfiles: List<UserProfile>,
    allPosts: List<Post>,
    savedPosts: List<Post>,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit
) {
    val bgCol = if (isDarkMode) DarkBackground else LightBackground
    val textColor = if (isDarkMode) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val cardBg = if (isDarkMode) GlassWhite else Color(0xD9FFFFFF)
    val cardBorder = if (isDarkMode) GlassBorderWhite else Color(0x22000000)

    val currentProfile = currentUser ?: UserProfile(
        username = "aura_dreamer",
        displayName = "Aura Dreamer",
        avatarUrl = "indigo",
        bio = "Aesthetic architectural explorer. Denver, CO ✨🪐",
        followersCount = 4208,
        followingCount = 384
    )

    // Filter user's actual posts dynamically!
    val userPosts = allPosts.filter { it.username == currentProfile.username && !it.isReel }

    var selectedTab by remember { mutableStateOf("GRID") } // GRID, SAVED, TAGGED

    val highlights = listOf(
        "https://images.unsplash.com/photo-1542751371-adc38448a05e?auto=format&fit=crop&q=80&w=250" to "Tokyo",
        "https://images.unsplash.com/photo-1550745165-9bc0b252726f?auto=format&fit=crop&q=80&w=250" to "Desk",
        "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&q=80&w=250" to "Glass",
        "https://images.unsplash.com/photo-1511512578047-dfb367046420?auto=format&fit=crop&q=80&w=250" to "Workspace"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgCol)
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(48.dp))

            // TOP BUTTONS row: Settings, Theme Selector, Logout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "@" + currentProfile.username,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = textColor
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Dark / Light toggle button
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier.testTag("theme_toggle_btn")
                    ) {
                        Text(text = if (isDarkMode) "☀️" else "🌙", fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Logout",
                        color = CyberpunkPink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clickable { onLogout() }
                            .padding(8.dp)
                            .testTag("logout_btn")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main stats row with Avatar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AuraAvatar(
                    avatarUrl = currentProfile.avatarUrl,
                    size = 72.dp
                )

                Spacer(modifier = Modifier.width(24.dp))

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ProfileStat(count = "${userPosts.size}", label = "Posts", textColor = textColor)
                    ProfileStat(count = "${currentProfile.followersCount}", label = "Followers", textColor = textColor)
                    ProfileStat(count = "${currentProfile.followingCount}", label = "Following", textColor = textColor)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bio and display name details
            Text(
                text = currentProfile.displayName,
                color = textColor,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = currentProfile.bio,
                color = if (isDarkMode) Color.LightGray else Color.DarkGray,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Highlights row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                items(highlights) { highlight ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .border(1.dp, cardBorder, shape = CircleShape)
                                .padding(3.dp)
                                .clip(CircleShape)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(highlight.first)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Highlight category thumb",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = highlight.second, color = textColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab selects
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBg, shape = RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                ProfileTabButton(
                    selected = selectedTab == "GRID",
                    text = "My Grid",
                    textColor = textColor,
                    modifier = Modifier.weight(1f).testTag("profile_tab_grid")
                ) { selectedTab = "GRID" }

                ProfileTabButton(
                    selected = selectedTab == "SAVED",
                    text = "Saved Vibe",
                    textColor = textColor,
                    modifier = Modifier.weight(1f).testTag("profile_tab_saved")
                ) { selectedTab = "SAVED" }

                ProfileTabButton(
                    selected = selectedTab == "TAGGED",
                    text = "Tagged",
                    textColor = textColor,
                    modifier = Modifier.weight(1f).testTag("profile_tab_tagged")
                ) { selectedTab = "TAGGED" }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chosen Tab Content Grid
            val postsToDisplay = when (selectedTab) {
                "GRID" -> userPosts
                "SAVED" -> savedPosts
                "TAGGED" -> allPosts.take(2) // Fake standard tagged posts
                else -> emptyList()
            }

            if (postsToDisplay.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No content captures in this tab yet.",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(postsToDisplay, key = { it.id }) { post ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(post.mediaUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile grid image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileStat(count: String, label: String, textColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, fontWeight = FontWeight.Black, fontSize = 18.sp, color = textColor)
        Text(text = label, color = Color.Gray, fontSize = 11.sp)
    }
}

@Composable
fun ProfileTabButton(
    selected: Boolean,
    text: String,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) CyberpunkPink else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color.Gray,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}
