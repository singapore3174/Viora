package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val username: String,
    val displayName: String,
    val avatarUrl: String, // String representation or placeholder color hex
    val bio: String,
    val followersCount: Int,
    val followingCount: Int,
    val isFollowing: Boolean = false,
    val isOnline: Boolean = false,
    val hasStory: Boolean = false,
    val storyUrl: String? = null
)

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val username: String,
    val userDisplayName: String,
    val userAvatarUrl: String,
    val mediaUrl: String, // Random premium high-res category search keyword / photo category
    val caption: String,
    val filterApplied: String = "Normal", // "Cyberpunk", "Vintage", "Neon Cyan", "Warm Gold"
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val isReel: Boolean = false,
    val isVideo: Boolean = false,
    val musicName: String? = null
)

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val postId: String,
    val username: String,
    val userDisplayName: String,
    val userAvatarUrl: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "stories")
data class Story(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val username: String,
    val userDisplayName: String,
    val userAvatarUrl: String,
    val mediaUrl: String, // Background color hex or dynamic image URL
    val textOverlay: String? = null,
    val isViewed: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val chatPartner: String, // The person who is part of the chat with active user
    val sender: String, // Username of the sender
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val type: String, // "LIKE", "COMMENT", "FOLLOW", "MENTION"
    val senderUsername: String,
    val senderDisplayName: String,
    val senderAvatarUrl: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val postId: String? = null
)
