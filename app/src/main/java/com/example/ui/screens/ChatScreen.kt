package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Message
import com.example.data.UserProfile

@Composable
fun ChatScreen(
    isDarkMode: Boolean,
    activePartner: UserProfile?,
    allProfiles: List<UserProfile>,
    activeMessages: List<Message>,
    onSendMessage: (String) -> Unit,
    onCloseChat: () -> Unit,
    onOpenChat: (UserProfile) -> Unit
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
        if (activePartner == null) {
            // --- inbox threads view ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = "Aura Messages Inbox",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // List of Online profiles horizontal
                Text(
                    text = "Active Curation Circles",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(allProfiles.filter { it.username != "aura_dreamer" }) { profile ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { onOpenChat(profile) }
                        ) {
                            AuraAvatar(
                                avatarUrl = profile.avatarUrl,
                                size = 56.dp,
                                isOnline = profile.isOnline
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = profile.displayName.split(" ").firstOrNull() ?: profile.username,
                                color = textColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.width(56.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Conversation Streams",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // Threads vertical list
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(allProfiles.filter { it.username != "aura_dreamer" }) { profile ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(cardBg, shape = RoundedCornerShape(16.dp))
                                .border(1.dp, cardBorder, shape = RoundedCornerShape(16.dp))
                                .clickable { onOpenChat(profile) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AuraAvatar(
                                avatarUrl = profile.avatarUrl,
                                size = 48.dp,
                                isOnline = profile.isOnline
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = profile.displayName,
                                    color = textColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (profile.username.endsWith("ai")) "Direct AI dialogue active" else "Tap to send aesthetic message",
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (profile.isOnline) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(CyberpunkCyan, shape = CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // --- active chat conversation view ---
            var chatInput by remember { mutableStateOf("") }

            Column(modifier = Modifier.fillMaxSize()) {
                // Header conversation partner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 44.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onCloseChat,
                        modifier = Modifier.testTag("chat_back_btn")
                    ) {
                        Text("←", color = textColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    AuraAvatar(
                        avatarUrl = activePartner.avatarUrl,
                        size = 40.dp,
                        isOnline = activePartner.isOnline
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activePartner.displayName,
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (activePartner.isOnline) "Active in ether stream" else "Offline",
                            color = if (activePartner.isOnline) CyberpunkCyan else Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }

                Divider(color = cardBorder, thickness = 0.5.dp)

                // Conversation body
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
                ) {
                    items(activeMessages, key = { it.id }) { msg ->
                        val isMe = msg.sender == "aura_dreamer"
                        val align = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                        val msgBg = if (isMe) {
                            Brush.linearGradient(listOf(CyberpunkPink, CyberpunkPurple))
                        } else {
                            Brush.linearGradient(listOf(cardBg, cardBg))
                        }
                        val borderMod = if (isMe) Modifier else Modifier.border(1.dp, cardBorder, shape = RoundedCornerShape(16.dp))

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = align
                        ) {
                            Box(
                                modifier = Modifier
                                    .widthIn(max = 260.dp)
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = if (isMe) 16.dp else 4.dp,
                                            bottomEnd = if (isMe) 4.dp else 16.dp
                                        )
                                    )
                                    .background(msgBg)
                                    .then(borderMod)
                                    .padding(vertical = 10.dp, horizontal = 14.dp)
                            ) {
                                Text(
                                    text = msg.text,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                Divider(color = cardBorder, thickness = 0.5.dp)

                // Input bar footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .background(cardBg)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = chatInput,
                        onValueChange = { chatInput = it },
                        placeholder = { Text("Aura messaging line...", color = Color.Gray, fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_text_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberpunkCyan,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    IconButton(
                        onClick = {
                            if (chatInput.isNotBlank()) {
                                onSendMessage(chatInput)
                                chatInput = ""
                            }
                        },
                        modifier = Modifier
                            .background(CyberpunkCyan, shape = CircleShape)
                            .testTag("chat_send_submit_btn")
                    ) {
                        Text("⚡", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
