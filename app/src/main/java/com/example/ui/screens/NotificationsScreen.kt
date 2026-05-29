package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Notification

@Composable
fun NotificationsScreen(
    isDarkMode: Boolean,
    notifications: List<Notification>,
    onClose: () -> Unit
) {
    val bgCol = if (isDarkMode) DarkBackground else LightBackground
    val textColor = if (isDarkMode) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val cardBg = if (isDarkMode) GlassWhite else Color(0xD9FFFFFF)
    val cardBorder = if (isDarkMode) GlassBorderWhite else Color(0x22000000)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgCol)
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(48.dp))

            // Header back
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Text("←", color = textColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Aura Activity stream",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor
                )
            }

            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No social updates on your aura yet.", color = Color.Gray, fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(notifications, key = { it.id }) { alert ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(cardBg, shape = RoundedCornerShape(16.dp))
                                .border(1.dp, cardBorder, shape = RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AuraAvatar(
                                avatarUrl = alert.senderAvatarUrl,
                                size = 44.dp
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = alert.senderDisplayName,
                                        color = textColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "@" + alert.senderUsername,
                                        color = Color.Gray,
                                        fontSize = 10.sp
                                    )
                                }
                                Text(
                                    text = alert.text,
                                    color = if (isDarkMode) Color.LightGray else Color.DarkGray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            // Alert type aesthetic tag
                            Text(
                                text = when (alert.type) {
                                    "LIKE" -> "❤️"
                                    "COMMENT" -> "💬"
                                    "FOLLOW" -> "👤"
                                    else -> "✨"
                                },
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
