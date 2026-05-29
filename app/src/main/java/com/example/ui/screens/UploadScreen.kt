package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@Composable
fun UploadScreen(
    isDarkMode: Boolean,
    selectedImage: String,
    onImageSelect: (String) -> Unit,
    selectedFilter: String,
    onFilterSelect: (String) -> Unit,
    caption: String,
    onCaptionChange: (String) -> Unit,
    uploading: Boolean,
    onGenerateAiCaption: () -> Unit,
    onUploadSubmit: (isReel: Boolean) -> Unit
) {
    val bgCol = if (isDarkMode) DarkBackground else LightBackground
    val textColor = if (isDarkMode) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val cardBg = if (isDarkMode) GlassWhite else Color(0xD9FFFFFF)
    val cardBorder = if (isDarkMode) GlassBorderWhite else Color(0x22000000)

    val preSetImages = listOf(
        "https://images.unsplash.com/photo-1542751371-adc38448a05e?auto=format&fit=crop&q=80&w=800" to "Retro Neon Screen",
        "https://images.unsplash.com/photo-1511512578047-dfb367046420?auto=format&fit=crop&q=80&w=800" to "Cyberpunk Workspace",
        "https://images.unsplash.com/photo-1550745165-9bc0b252726f?auto=format&fit=crop&q=80&w=800" to "Minimal Desktop Setup",
        "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&q=80&w=800" to "Glass Skyscraper Elevator"
    )

    val filtersList = listOf("Normal", "Neon Cyan", "Cyberpunk", "Warm Gold")

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgCol)
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "New Aura Aesthetic Post",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = textColor,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Preview card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(cardBg)
                    .border(2.dp, CyberpunkPink, shape = RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(selectedImage)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Selected upload files preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Overlay filter representing shader color matrices
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            when (selectedFilter) {
                                "Neon Cyan" -> Brush.verticalGradient(
                                    listOf(CyberpunkCyan.copy(alpha = 0.25f), Color.Transparent)
                                )
                                "Cyberpunk" -> Brush.linearGradient(
                                    listOf(CyberpunkPink.copy(alpha = 0.21f), CyberpunkPurple.copy(alpha = 0.21f))
                                )
                                "Warm Gold" -> Brush.verticalGradient(
                                    listOf(Color(0xFFE5A93C).copy(alpha = 0.23f), Color.Transparent)
                                )
                                else -> Brush.radialGradient(listOf(Color.Transparent, Color.Transparent))
                            }
                        )
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                        .background(Color(0x99000000), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(text = "Preview Frame - Filter: $selectedFilter", color = Color.White, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Choose image preset title
            Text(
                text = "Pick Aura Canvas Asset",
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(preSetImages) { item ->
                    Column(
                        modifier = Modifier
                            .width(90.dp)
                            .clickable { onImageSelect(item.first) },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(item.first)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Thumbnail selection image asset",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    2.dp,
                                    if (selectedImage == item.first) CyberpunkCyan else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.second,
                            color = Color.Gray,
                            fontSize = 8.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Shader filter matrix list
            Text(
                text = "Apply Cyberpunk Shader Filter",
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filtersList.forEach { filter ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selectedFilter == filter) CyberpunkPurple else cardBg)
                            .border(1.dp, cardBorder, shape = RoundedCornerShape(12.dp))
                            .clickable { onFilterSelect(filter) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filter,
                            color = if (selectedFilter == filter) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Caption details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Caption Narrative",
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                // Gemini button helper
                Row(
                    modifier = Modifier
                        .clickable { onGenerateAiCaption() }
                        .background(CyberpunkPink.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🤖", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Get AI Caption", color = CyberpunkPink, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = caption,
                onValueChange = onCaptionChange,
                placeholder = { Text("What aesthetic story does this canvas carry?", color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .testTag("upload_caption_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberpunkCyan,
                    unfocusedBorderColor = cardBorder,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (uploading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally),
                    color = CyberpunkCyan
                )
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 100.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onUploadSubmit(false) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberpunkPink),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("submit_standard_post"),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text("Share as Post", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onUploadSubmit(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberpunkPurple),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("submit_reel_post"),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text("Share as Reel", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
