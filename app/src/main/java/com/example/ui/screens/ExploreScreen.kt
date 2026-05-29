package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.Post

@Composable
fun ExploreScreen(
    isDarkMode: Boolean,
    explorePosts: List<Post>,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    aiRecommendationText: String,
    loadingRecommendation: Boolean,
    onExploreAiRecommendation: (String) -> Unit
) {
    val bgCol = if (isDarkMode) DarkBackground else LightBackground
    val textColor = if (isDarkMode) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val cardBg = if (isDarkMode) GlassWhite else Color(0xD9FFFFFF)
    val cardBorder = if (isDarkMode) GlassBorderWhite else Color(0x22000000)

    var currentAiKeyword by remember { mutableStateOf("") }

    val hashtags = listOf("cyberpunk", "neon", "glassmorphism", "minimal", "studiolighting", "vaporwave")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgCol)
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Spacer for status bar padding
            Spacer(modifier = Modifier.height(48.dp))

            // Glassmorphism Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                placeholder = { Text("Search aesthetics, creators, filters...", color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("explore_search_input"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberpunkCyan,
                    unfocusedBorderColor = cardBorder,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedContainerColor = cardBg,
                    unfocusedContainerColor = cardBg
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Hashtags Row Category selectors
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                hashtags.take(4).forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (searchQuery == tag) CyberpunkPink else cardBg)
                            .border(1.dp, cardBorder, shape = RoundedCornerShape(16.dp))
                            .clickable {
                                if (searchQuery == tag) onQueryChange("") else onQueryChange(tag)
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "#$tag",
                            color = if (searchQuery == tag) Color.White else Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- AI CURATOR ASSISTANT PANEL ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBg, shape = RoundedCornerShape(20.dp))
                    .border(1.dp, cardBorder, shape = RoundedCornerShape(20.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(
                                Brush.linearGradient(listOf(CyberpunkPink, CyberpunkPurple)),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🤖", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Aura AI Curator",
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = currentAiKeyword,
                        onValueChange = { currentAiKeyword = it },
                        placeholder = { Text("Ask Aura AI curator...", fontSize = 12.sp, color = Color.Gray) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_curator_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberpunkPurple,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    if (loadingRecommendation) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = CyberpunkPink)
                    } else {
                        IconButton(
                            onClick = {
                                if (currentAiKeyword.isNotBlank()) {
                                    onExploreAiRecommendation(currentAiKeyword)
                                }
                            },
                            modifier = Modifier
                                .background(
                                    Brush.linearGradient(
                                        listOf(CyberpunkPurple, CyberpunkCyan)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .testTag("ai_curator_submit")
                        ) {
                            Text("⚡", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (aiRecommendationText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = aiRecommendationText,
                            color = CyberpunkCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Grid of media posts - Asymmetric style
            Text(
                text = "Trending Grid in High Contrast",
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(explorePosts, key = { it.id }) { post ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                // Filter click can search that user
                                onQueryChange(post.username)
                            }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(post.mediaUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Explore media image item",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Hover indicators
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color(0x66000000))
                                    )
                                )
                        )

                        Text(
                            text = "❤️ " + post.likesCount,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(6.dp)
                        )
                    }
                }
            }
        }
    }
}
