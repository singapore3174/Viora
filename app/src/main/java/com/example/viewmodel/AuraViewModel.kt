package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class AuraTab {
    HOME, EXPLORE, UPLOAD, REELS, PROFILE
}

class AuraViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AuraRepository.getDatabase(application)
    private val repository = AuraRepository(db)

    // --- Authentication ---
    private val _hasLoggedIn = MutableStateFlow(true) // Start authenticated for demo, allow logging out
    val hasLoggedIn: StateFlow<Boolean> = _hasLoggedIn.asStateFlow()

    private val _showAuthScreen = MutableStateFlow(false)
    val showAuthScreen: StateFlow<Boolean> = _showAuthScreen.asStateFlow()

    // --- UI Theme & Tab ---
    private val _isDarkMode = MutableStateFlow(true) // Neon cyberpunk theme is perfect as default
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _currentTab = MutableStateFlow(AuraTab.HOME)
    val currentTab: StateFlow<AuraTab> = _currentTab.asStateFlow()

    // --- Core Data Flows ---
    val feedPosts: StateFlow<List<Post>> = repository.feedPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reels: StateFlow<List<Post>> = repository.reels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stories: StateFlow<List<Story>> = repository.stories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<Notification>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProfiles: StateFlow<List<UserProfile>> = repository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Current Active Views & Overlays ---
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _activeChatPartner = MutableStateFlow<UserProfile?>(null)
    val activeChatPartner: StateFlow<UserProfile?> = _activeChatPartner.asStateFlow()

    private val _selectedPostForComments = MutableStateFlow<Post?>(null)
    val selectedPostForComments: StateFlow<Post?> = _selectedPostForComments.asStateFlow()

    private val _activeStoryView = MutableStateFlow<Story?>(null)
    val activeStoryView: StateFlow<Story?> = _activeStoryView.asStateFlow()

    // --- Comments Flow for selected post ---
    val activeComments: StateFlow<List<Comment>> = _selectedPostForComments
        .flatMapLatest { post ->
            if (post != null) repository.getComments(post.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Chat Messages Flow for active partner ---
    val activeChatMessages: StateFlow<List<Message>> = _activeChatPartner
        .flatMapLatest { partner ->
            if (partner != null) repository.getMessages(partner.username) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Explore Screen ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val explorePosts: StateFlow<List<Post>> = combine(feedPosts, searchQuery) { posts, query ->
        if (query.isBlank()) posts else {
            posts.filter {
                it.caption.contains(query, ignoreCase = true) ||
                        it.username.contains(query, ignoreCase = true) ||
                        it.userDisplayName.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Upload Controls ---
    private val _selectedFilter = MutableStateFlow("Normal")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _captionInput = MutableStateFlow("")
    val captionInput: StateFlow<String> = _captionInput.asStateFlow()

    private val _uploading = MutableStateFlow(false)
    val uploading: StateFlow<Boolean> = _uploading.asStateFlow()

    private val _selectedUploadImage = MutableStateFlow("https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?auto=format&fit=crop&q=80&w=800")
    val selectedUploadImage: StateFlow<String> = _selectedUploadImage.asStateFlow()

    // AI recommendation state
    private val _aiRecommendationText = MutableStateFlow("")
    val aiRecommendationText: StateFlow<String> = _aiRecommendationText.asStateFlow()

    private val _loadingAiRecommendation = MutableStateFlow(false)
    val loadingAiRecommendation: StateFlow<Boolean> = _loadingAiRecommendation.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedMockDataIfEmpty()
            repository.getProfile("aura_dreamer").collect { profile ->
                _currentUser.value = profile
            }
        }
    }

    // --- Core Actions ---
    fun selectTab(tab: AuraTab) {
        _currentTab.value = tab
    }

    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            repository.toggleLikePost(postId)
        }
    }

    fun toggleSave(postId: String) {
        viewModelScope.launch {
            repository.toggleSavePost(postId)
        }
    }

    fun toggleFollowUser(username: String) {
        viewModelScope.launch {
            repository.toggleFollowUser(username)
        }
    }

    // --- Stories ---
    fun openStory(story: Story) {
        _activeStoryView.value = story
        viewModelScope.launch {
            repository.markStoryAsViewed(story.id)
        }
    }

    fun closeStory() {
        _activeStoryView.value = null
    }

    // --- Comments Sheet ---
    fun openCommentsForPost(post: Post) {
        _selectedPostForComments.value = post
    }

    fun closeComments() {
        _selectedPostForComments.value = null
    }

    fun postComment(postId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.addComment(
                postId = postId,
                username = "aura_dreamer",
                displayName = "Aura Dreamer",
                avatarUrl = "indigo",
                commentText = text
            )
        }
    }

    // --- Chat System ---
    fun openChatWith(partner: UserProfile) {
        _activeChatPartner.value = partner
    }

    fun closeChat() {
        _activeChatPartner.value = null
    }

    fun sendChatMessage(text: String) {
        val partner = _activeChatPartner.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(
                chatPartner = partner.username,
                sender = "aura_dreamer",
                text = text
            )
        }
    }

    // --- Upload Screen Logic ---
    fun selectUploadFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun updateCaption(caption: String) {
        _captionInput.value = caption
    }

    fun updateUploadImage(url: String) {
        _selectedUploadImage.value = url
    }

    fun testGenerateAiCaption() {
        viewModelScope.launch {
            _loadingAiRecommendation.value = true
            val prompt = "Generate a highly stylized cyberpunk and futuristic Instagram caption with hashtags for an image of: cyberpunk neon street at 3am"
            val aiReply = GeminiService.generateAiReply(prompt)
            _captionInput.value = aiReply
            _loadingAiRecommendation.value = false
        }
    }

    fun createNewPost(isReel: Boolean = false) {
        val user = _currentUser.value ?: return
        if (_captionInput.value.isBlank() && !isReel) return

        viewModelScope.launch {
            _uploading.value = true
            val post = Post(
                id = UUID.randomUUID().toString(),
                username = user.username,
                userDisplayName = user.displayName,
                userAvatarUrl = user.avatarUrl,
                mediaUrl = _selectedUploadImage.value,
                caption = _captionInput.value,
                filterApplied = _selectedFilter.value,
                likesCount = 0,
                commentsCount = 0,
                isLiked = false,
                isSaved = false,
                timestamp = System.currentTimeMillis(),
                isReel = isReel,
                isVideo = isReel,
                musicName = if (isReel) "Aura Beats (Original Music)" else null
            )
            repository.insertPost(post)

            // Auto-Generate AI Feedback comment after 2 seconds to make the upload feel alive!
            viewModelScope.launch {
                kotlinx.coroutines.delay(2000)
                val responseSystem = "You are code_companion, a supportive virtual user on Aura. Suggest high-vibe creative additions."
                val responseText = GeminiService.generateAiReply(
                    "Write a 1-sentence supportive aesthetic social comment on: " + post.caption,
                    responseSystem
                )
                repository.addComment(
                    postId = post.id,
                    username = "aero_ai",
                    displayName = "Aero AI Assistant",
                    avatarUrl = "purple",
                    commentText = responseText
                )
            }

            // Reset upload controls
            _captionInput.value = ""
            _selectedFilter.value = "Normal"
            _uploading.value = false
            selectTab(if (isReel) AuraTab.REELS else AuraTab.HOME)
        }
    }

    // --- Search & AI Recommendations ---
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun exploreAiRecommendation(interest: String) {
        viewModelScope.launch {
            _loadingAiRecommendation.value = true
            val prompt = "Provide short 1-sentence creative inspiration about the theme: $interest. Suggest two accounts starting with @ that align with this vibe."
            val reply = GeminiService.generateAiReply(prompt)
            _aiRecommendationText.value = reply
            _loadingAiRecommendation.value = false
        }
    }

    // --- Authentication Actions ---
    fun logout() {
        _hasLoggedIn.value = false
        _showAuthScreen.value = true
    }

    fun login(email: String, provider: String) {
        viewModelScope.launch {
            _hasLoggedIn.value = true
            _showAuthScreen.value = false

            // Notify follow state
            db.notificationDao().insertNotification(
                Notification(
                    id = UUID.randomUUID().toString(),
                    type = "FOLLOW",
                    senderUsername = "aero_ai",
                    senderDisplayName = "Aero AI Assistant",
                    senderAvatarUrl = "purple",
                    text = "welcomed you back to Aura via $provider auth.",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
}
