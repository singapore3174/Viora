package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID

class AuraRepository(private val db: AppDatabase) {

    // --- User Profiles ---
    val allProfiles: Flow<List<UserProfile>> = db.userProfileDao().getAllProfilesFlow()

    fun getProfile(username: String): Flow<UserProfile?> {
        return db.userProfileDao().getProfileFlow(username)
    }

    suspend fun insertProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        db.userProfileDao().insertProfile(profile)
    }

    suspend fun toggleFollowUser(username: String) = withContext(Dispatchers.IO) {
        val currentProfile = db.userProfileDao().getProfileFlow(username).first() ?: return@withContext
        val targetIsFollowing = !currentProfile.isFollowing
        val followsDiff = if (targetIsFollowing) 1 else -1
        db.userProfileDao().updateFollowingState(username, targetIsFollowing, followsDiff)

        // Generate a follow notification if newly following
        if (targetIsFollowing) {
            val notif = Notification(
                id = UUID.randomUUID().toString(),
                type = "FOLLOW",
                senderUsername = username,
                senderDisplayName = currentProfile.displayName,
                senderAvatarUrl = currentProfile.avatarUrl,
                text = "started following you.",
                timestamp = System.currentTimeMillis()
            )
            db.notificationDao().insertNotification(notif)
        }
    }

    // --- Posts & Reels ---
    val feedPosts: Flow<List<Post>> = db.postDao().getAllPostsFlow()
    val reels: Flow<List<Post>> = db.postDao().getReelsFlow()
    val savedPosts: Flow<List<Post>> = db.postDao().getSavedPostsFlow()

    fun getProfilePosts(username: String): Flow<List<Post>> {
        return db.postDao().getProfilePostsFlow(username)
    }

    suspend fun insertPost(post: Post) = withContext(Dispatchers.IO) {
        db.postDao().insertPost(post)
    }

    suspend fun toggleLikePost(postId: String) = withContext(Dispatchers.IO) {
        // Find post to get its user
        val postList = db.postDao().getAllPostsFlow().first()
        val reelList = db.postDao().getReelsFlow().first()
        val foundPost = postList.find { it.id == postId } ?: reelList.find { it.id == postId } ?: return@withContext

        val targetIsLiked = !foundPost.isLiked
        val likesDiff = if (targetIsLiked) 1 else -1
        db.postDao().updateLikeState(postId, targetIsLiked, likesDiff)

        if (targetIsLiked && foundPost.username != "aura_dreamer") {
            // Log a notification
            val notif = Notification(
                id = UUID.randomUUID().toString(),
                type = "LIKE",
                senderUsername = "aura_dreamer",
                senderDisplayName = "Aura Dreamer",
                senderAvatarUrl = "indigo",
                text = "liked your post.",
                timestamp = System.currentTimeMillis(),
                postId = postId
            )
            db.notificationDao().insertNotification(notif)
        }
    }

    suspend fun toggleSavePost(postId: String) = withContext(Dispatchers.IO) {
        val postList = db.postDao().getAllPostsFlow().first()
        val reelList = db.postDao().getReelsFlow().first()
        val foundPost = postList.find { it.id == postId } ?: reelList.find { it.id == postId } ?: return@withContext
        db.postDao().updateSaveState(postId, !foundPost.isSaved)
    }

    // --- Comments ---
    fun getComments(postId: String): Flow<List<Comment>> {
        return db.commentDao().getCommentsForPostFlow(postId)
    }

    suspend fun addComment(postId: String, username: String, displayName: String, avatarUrl: String, commentText: String) = withContext(Dispatchers.IO) {
        val comment = Comment(
            id = UUID.randomUUID().toString(),
            postId = postId,
            username = username,
            userDisplayName = displayName,
            userAvatarUrl = avatarUrl,
            text = commentText,
            timestamp = System.currentTimeMillis()
        )
        db.commentDao().insertComment(comment)
        db.postDao().updateCommentCount(postId, 1)

        // Seed feedback notification if commented on another person's post
        val postList = db.postDao().getAllPostsFlow().first()
        val targetPost = postList.find { it.id == postId }
        if (targetPost != null && targetPost.username != username) {
            db.notificationDao().insertNotification(
                Notification(
                    id = UUID.randomUUID().toString(),
                    type = "COMMENT",
                    senderUsername = username,
                    senderDisplayName = displayName,
                    senderAvatarUrl = avatarUrl,
                    text = "commented: \"$commentText\"",
                    timestamp = System.currentTimeMillis(),
                    postId = postId
                )
            )
        }
    }

    // --- Stories ---
    val stories: Flow<List<Story>> = db.storyDao().getAllStoriesFlow()

    suspend fun insertStory(story: Story) = withContext(Dispatchers.IO) {
        db.storyDao().insertStory(story)
    }

    suspend fun markStoryAsViewed(storyId: String) = withContext(Dispatchers.IO) {
        db.storyDao().markStoryAsViewed(storyId)
    }

    // --- Private Messages (Chat) ---
    fun getMessages(partner: String): Flow<List<Message>> {
        return db.messageDao().getMessagesForChatFlow(partner)
    }

    val allChatMessages: Flow<List<Message>> = db.messageDao().getAllMessagesFlow()

    suspend fun sendMessage(chatPartner: String, sender: String, text: String): Message = withContext(Dispatchers.IO) {
        val newMsg = Message(
            id = UUID.randomUUID().toString(),
            chatPartner = chatPartner,
            sender = sender,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        db.messageDao().insertMessage(newMsg)

        // Trigger AI Auto Response if partner is aero_ai or ending with AI!
        if (chatPartner.endsWith("_ai") && sender != chatPartner) {
            // Call Gemini
            val systemInstruction = "You are $chatPartner, a premium creative AI companion inside the social network app Aura. " +
                    "Your personality is futuristic, inspiring, encouraging, and highly creative. Keep responses brief, under 2 short sentences."
            val aiReplyText = GeminiService.generateAiReply(text, systemInstruction)
            val aiMsg = Message(
                id = UUID.randomUUID().toString(),
                chatPartner = chatPartner,
                sender = chatPartner,
                text = aiReplyText,
                timestamp = System.currentTimeMillis() + 800 // offset slightly for feel
            )
            db.messageDao().insertMessage(aiMsg)
        }

        newMsg
    }

    // --- Notifications ---
    val allNotifications: Flow<List<Notification>> = db.notificationDao().getAllNotificationsFlow()

    // --- Database Caching & Seed Logic ---
    suspend fun seedMockDataIfEmpty() = withContext(Dispatchers.IO) {
        val currentProfiles = db.userProfileDao().getAllProfilesFlow().first()
        if (currentProfiles.isEmpty()) {
            seedDatabase()
        }
    }

    private suspend fun seedDatabase() {
        val profiles = listOf(
            UserProfile(
                username = "aura_dreamer",
                displayName = "Aura Dreamer",
                avatarUrl = "indigo",
                bio = "Aesthetic architectural explorer. Capturing moments in high contrast. ✨🪐\nDenver, CO",
                followersCount = 4208,
                followingCount = 384,
                isFollowing = false,
                isOnline = true
            ),
            UserProfile(
                username = "nova_builder",
                displayName = "Nova Builder",
                avatarUrl = "rose",
                bio = "Solitary structuralist. Sculpting the sky with digital glass blocks. 📐🏙️",
                followersCount = 12903,
                followingCount = 492,
                isFollowing = true,
                isOnline = true,
                hasStory = true,
                storyUrl = "Design Sprint for tomorrow."
            ),
            UserProfile(
                username = "neon_dreamer",
                displayName = "Neon Dreamer",
                avatarUrl = "cyan",
                bio = "Cyberpunk streets at 3am. Capturing photons in dark alleys. 🌌🔋",
                followersCount = 8402,
                followingCount = 920,
                isFollowing = false,
                isOnline = true,
                hasStory = true,
                storyUrl = "Late night run in Neo Tokyo"
            ),
            UserProfile(
                username = "glass_vibe",
                displayName = "Glass Vibe",
                avatarUrl = "gold",
                bio = "Glassmorphic interfaces and transparent material design. Light & space. 👓🛸",
                followersCount = 20384,
                followingCount = 11,
                isFollowing = true,
                isOnline = false,
                hasStory = true,
                storyUrl = "Minimalism is the ultimate polish"
            ),
            UserProfile(
                username = "aero_ai",
                displayName = "Aero AI Assistant",
                avatarUrl = "purple",
                bio = "Your resident creative companion. Ask me captions, hashtags, or just chat. 🤖💫",
                followersCount = 999999,
                followingCount = 1,
                isFollowing = true,
                isOnline = true
            )
        )
        db.userProfileDao().insertProfiles(profiles)

        val posts = listOf(
            Post(
                id = "post_1",
                username = "nova_builder",
                userDisplayName = "Nova Builder",
                userAvatarUrl = "rose",
                mediaUrl = "https://images.unsplash.com/photo-1504307651254-35680f356dfd?auto=format&fit=crop&q=80&w=800",
                caption = "Drafting the future in code. Glass blocks overlapping. Cohesion is everything.",
                filterApplied = "Neon Cyan",
                likesCount = 1283,
                commentsCount = 2,
                isLiked = true,
                isSaved = false,
                timestamp = System.currentTimeMillis() - 3600000,
                isReel = false
            ),
            Post(
                id = "post_2",
                username = "neon_dreamer",
                userDisplayName = "Neon Dreamer",
                userAvatarUrl = "cyan",
                mediaUrl = "https://images.unsplash.com/photo-1515621061946-eff1c2a352bd?auto=format&fit=crop&q=80&w=800",
                caption = "The city breathes at night under neon beams. 🌌🔋",
                filterApplied = "Cyberpunk",
                likesCount = 932,
                commentsCount = 1,
                isLiked = false,
                isSaved = true,
                timestamp = System.currentTimeMillis() - 7200000,
                isReel = false
            ),
            Post(
                id = "post_3",
                username = "glass_vibe",
                userDisplayName = "Glass Vibe",
                userAvatarUrl = "gold",
                mediaUrl = "https://images.unsplash.com/photo-1550745165-9bc0b252726f?auto=format&fit=crop&q=80&w=800",
                caption = "A quiet desk in high contrast. Transparent panels reflecting pure sunlight.",
                filterApplied = "Warm Gold",
                likesCount = 2083,
                commentsCount = 0,
                isLiked = false,
                isSaved = false,
                timestamp = System.currentTimeMillis() - 86400000,
                isReel = false
            ),
            // REELS
            Post(
                id = "reel_1",
                username = "neon_dreamer",
                userDisplayName = "Neon Dreamer",
                userAvatarUrl = "cyan",
                mediaUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?auto=format&fit=crop&q=80&w=800",
                caption = "Neon raindrops catching the neon reflection. Addictive grid! ☔👾",
                likesCount = 4902,
                commentsCount = 24,
                isLiked = false,
                isSaved = false,
                timestamp = System.currentTimeMillis() - 10000000,
                isReel = true,
                isVideo = true,
                musicName = "Original Audio - Neon Dreamer"
            ),
            Post(
                id = "reel_2",
                username = "nova_builder",
                userDisplayName = "Nova Builder",
                userAvatarUrl = "rose",
                mediaUrl = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&q=80&w=800",
                caption = "Futuristic elevator climb. Floating inside glass columns. 🏢✨",
                likesCount = 8902,
                commentsCount = 92,
                isLiked = true,
                isSaved = true,
                timestamp = System.currentTimeMillis() - 15000000,
                isReel = true,
                isVideo = true,
                musicName = "Elevator Chill - CyberVibes"
            )
        )
        db.postDao().insertPosts(posts)

        val comments = listOf(
            Comment(
                id = "comment_1",
                postId = "post_1",
                username = "neon_dreamer",
                userDisplayName = "Neon Dreamer",
                userAvatarUrl = "cyan",
                text = "This structure is insane! Love the overlay blocks.",
                timestamp = System.currentTimeMillis() - 1800000
            ),
            Comment(
                id = "comment_2",
                postId = "post_1",
                username = "glass_vibe",
                userDisplayName = "Glass Vibe",
                userAvatarUrl = "gold",
                text = "Absolutely minimal and elegant. The lighting is pristine.",
                timestamp = System.currentTimeMillis() - 1200000
            ),
            Comment(
                id = "comment_3",
                postId = "post_2",
                username = "nova_builder",
                userDisplayName = "Nova Builder",
                userAvatarUrl = "rose",
                text = "Tempted to build a 3D glass replica of this street!",
                timestamp = System.currentTimeMillis() - 3600000
            )
        )
        comments.forEach { db.commentDao().insertComment(it) }

        val stories = listOf(
            Story(
                id = "story_1",
                username = "neon_dreamer",
                userDisplayName = "Neon Dreamer",
                userAvatarUrl = "cyan",
                mediaUrl = "https://images.unsplash.com/photo-1511512578047-dfb367046420?auto=format&fit=crop&q=80&w=800",
                textOverlay = "Late night workspace. Let's code.",
                isViewed = false,
                timestamp = System.currentTimeMillis() - 7200000
            ),
            Story(
                id = "story_2",
                username = "nova_builder",
                userDisplayName = "Nova Builder",
                userAvatarUrl = "rose",
                mediaUrl = "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&q=80&w=800",
                textOverlay = "Preview of the solar glass design ☀️",
                isViewed = false,
                timestamp = System.currentTimeMillis() - 14400000
            ),
            Story(
                id = "story_3",
                username = "glass_vibe",
                userDisplayName = "Glass Vibe",
                userAvatarUrl = "gold",
                mediaUrl = "https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&q=80&w=800",
                textOverlay = "Purity in form.",
                isViewed = false,
                timestamp = System.currentTimeMillis() - 21600000
            )
        )
        db.storyDao().insertStories(stories)

        val messages = listOf(
            Message(
                id = "msg_1",
                chatPartner = "aero_ai",
                sender = "aero_ai",
                text = "Hello! I am Aero, your AI Creative companion. Welcome to Aura social media. Ask me for some caption ideas or follow suggestions!",
                timestamp = System.currentTimeMillis() - 3600000
            )
        )
        db.messageDao().insertMessages(messages)

        val notifications = listOf(
            Notification(
                id = "notif_1",
                type = "FOLLOW",
                senderUsername = "neon_dreamer",
                senderDisplayName = "Neon Dreamer",
                senderAvatarUrl = "cyan",
                text = "just followed you.",
                timestamp = System.currentTimeMillis() - 1800000
            ),
            Notification(
                id = "notif_2",
                type = "LIKE",
                senderUsername = "nova_builder",
                senderDisplayName = "Nova Builder",
                senderAvatarUrl = "rose",
                text = "liked your story.",
                timestamp = System.currentTimeMillis() - 3600000
            ),
            Notification(
                id = "notif_3",
                type = "COMMENT",
                senderUsername = "glass_vibe",
                senderDisplayName = "Glass Vibe",
                senderAvatarUrl = "gold",
                text = "mentioned you in a caption: 'Collaborating soon layout with @aura_dreamer'",
                timestamp = System.currentTimeMillis() - 7200000
            )
        )
        db.notificationDao().insertNotifications(notifications)
    }

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aura_social_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
