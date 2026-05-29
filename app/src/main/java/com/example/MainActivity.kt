package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.screens.*
import com.example.viewmodel.AuraTab
import com.example.viewmodel.AuraViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: AuraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val hasLoggedIn by viewModel.hasLoggedIn.collectAsState()

            MyApplicationTheme(darkTheme = isDarkMode, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = if (isDarkMode) DarkBackground else LightBackground
                ) {
                    if (!hasLoggedIn) {
                        LoadingOnboardingScreen(isDark = isDarkMode) { email, provider ->
                            viewModel.login(email, provider)
                        }
                    } else {
                        MainAppContent(viewModel = viewModel, isDarkMode = isDarkMode)
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppContent(
    viewModel: AuraViewModel,
    isDarkMode: Boolean
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val feedPosts by viewModel.feedPosts.collectAsState()
    val reels by viewModel.reels.collectAsState()
    val stories by viewModel.stories.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val allProfiles by viewModel.allProfiles.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Active screen overrides / overlay sheets
    val activeChatPartner by viewModel.activeChatPartner.collectAsState()
    val activeStoryView by viewModel.activeStoryView.collectAsState()
    val selectedPostForComments by viewModel.selectedPostForComments.collectAsState()
    val activeComments by viewModel.activeComments.collectAsState()
    val activeChatMessages by viewModel.activeChatMessages.collectAsState()

    // Upload page controls
    val selectedUploadImage by viewModel.selectedUploadImage.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val captionInput by viewModel.captionInput.collectAsState()
    val uploading by viewModel.uploading.collectAsState()

    // Search page controls
    val searchQuery by viewModel.searchQuery.collectAsState()
    val explorePosts by viewModel.explorePosts.collectAsState()
    val aiRecommendationText by viewModel.aiRecommendationText.collectAsState()
    val loadingAiRecommendation by viewModel.loadingAiRecommendation.collectAsState()

    // Inside inbox toggle
    var isViewingNotificationsPanel by remember { mutableStateOf(false) }

    val textColor = if (isDarkMode) Color(0xFFF8FAFC) else Color(0xFFE2E8F0)
    val bottomNavBg = if (isDarkMode) Color(0xD9050505) else Color(0xD90C0D14)

    Box(modifier = Modifier.fillMaxSize()) {
        // --- SCREEN ROUTING CONTENT ---
        Scaffold(
            bottomBar = {
                // Persistent custom rounded bottom navigation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(bottomNavBg)
                        .border(
                            1.dp,
                            if (isDarkMode) Color(0x24FFFFFF) else Color(0x22000000),
                            shape = RoundedCornerShape(32.dp)
                        )
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BottomNavItem(
                            selected = currentTab == AuraTab.HOME,
                            icon = "🏠",
                            label = "Home",
                            textColor = textColor,
                            testTag = "nav_item_home"
                        ) { viewModel.selectTab(AuraTab.HOME) }

                        BottomNavItem(
                            selected = currentTab == AuraTab.EXPLORE,
                            icon = "🧭",
                            label = "Explore",
                            textColor = textColor,
                            testTag = "nav_item_explore"
                        ) { viewModel.selectTab(AuraTab.EXPLORE) }

                        // Special center adding button
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(CyberpunkCyan, CyberpunkPurple)
                                    )
                                )
                                .clickable { viewModel.selectTab(AuraTab.UPLOAD) }
                                .padding(10.dp)
                                .testTag("nav_item_upload"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("➕", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        }

                        BottomNavItem(
                            selected = currentTab == AuraTab.REELS,
                            icon = "🎬",
                            label = "Reels",
                            textColor = textColor,
                            testTag = "nav_item_reels"
                        ) { viewModel.selectTab(AuraTab.REELS) }

                        BottomNavItem(
                            selected = currentTab == AuraTab.PROFILE,
                            icon = "👤",
                            label = "Profile",
                            textColor = textColor,
                            testTag = "nav_item_profile"
                        ) { viewModel.selectTab(AuraTab.PROFILE) }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                when (currentTab) {
                    AuraTab.HOME -> {
                        HomeFeedScreen(
                            isDarkMode = isDarkMode,
                            allProfiles = allProfiles,
                            feedPosts = feedPosts,
                            stories = stories,
                            notificationsCount = notifications.size,
                            onStoryClick = { tagStory -> viewModel.openStory(tagStory) },
                            onLikeToggle = { postId -> viewModel.toggleLike(postId) },
                            onSaveToggle = { postId -> viewModel.toggleSave(postId) },
                            onFollowToggle = { username -> viewModel.toggleFollowUser(username) },
                            onOpenComments = { post -> viewModel.openCommentsForPost(post) },
                            onOpenChatPartner = { partner -> viewModel.openChatWith(partner) },
                            onOpenNotifications = { isViewingNotificationsPanel = true }
                        )
                    }
                    AuraTab.EXPLORE -> {
                        ExploreScreen(
                            isDarkMode = isDarkMode,
                            explorePosts = explorePosts,
                            searchQuery = searchQuery,
                            onQueryChange = { q -> viewModel.updateSearchQuery(q) },
                            aiRecommendationText = aiRecommendationText,
                            loadingRecommendation = loadingAiRecommendation,
                            onExploreAiRecommendation = { interest -> viewModel.exploreAiRecommendation(interest) }
                        )
                    }
                    AuraTab.UPLOAD -> {
                        UploadScreen(
                            isDarkMode = isDarkMode,
                            selectedImage = selectedUploadImage,
                            onImageSelect = { url -> viewModel.updateUploadImage(url) },
                            selectedFilter = selectedFilter,
                            onFilterSelect = { filter -> viewModel.selectUploadFilter(filter) },
                            caption = captionInput,
                            onCaptionChange = { text -> viewModel.updateCaption(text) },
                            uploading = uploading,
                            onGenerateAiCaption = { viewModel.testGenerateAiCaption() },
                            onUploadSubmit = { isReel -> viewModel.createNewPost(isReel) }
                        )
                    }
                    AuraTab.REELS -> {
                        ReelsScreen(
                            isDarkMode = isDarkMode,
                            reelsList = reels,
                            onLikeToggle = { postId -> viewModel.toggleLike(postId) },
                            onSaveToggle = { postId -> viewModel.toggleSave(postId) },
                            onOpenComments = { post -> viewModel.openCommentsForPost(post) }
                        )
                    }
                    AuraTab.PROFILE -> {
                        val savedList = feedPosts.filter { it.isSaved }
                        ProfileScreen(
                            isDarkMode = isDarkMode,
                            currentUser = currentUser,
                            allProfiles = allProfiles,
                            allPosts = feedPosts,
                            savedPosts = savedList,
                            onToggleTheme = { viewModel.toggleTheme() },
                            onLogout = { viewModel.logout() }
                        )
                    }
                }
            }
        }

        // --- FULL-SCREEN OVERLAYS AND SLIDES ---

        // Notification center slide
        AnimatedVisibility(
            visible = isViewingNotificationsPanel,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it })
        ) {
            NotificationsScreen(
                isDarkMode = isDarkMode,
                notifications = notifications,
                onClose = { isViewingNotificationsPanel = false }
            )
        }

        // Real-time Chat full-screen overlay
        AnimatedVisibility(
            visible = activeChatPartner != null,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it })
        ) {
            ChatScreen(
                isDarkMode = isDarkMode,
                activePartner = activeChatPartner,
                allProfiles = allProfiles,
                activeMessages = activeChatMessages,
                onSendMessage = { text -> viewModel.sendChatMessage(text) },
                onCloseChat = { viewModel.closeChat() },
                onOpenChat = { partner -> viewModel.openChatWith(partner) }
            )
        }

        // Comments Bottom Sheet overlay
        if (selectedPostForComments != null) {
            CommentsOverlay(
                isDarkMode = isDarkMode,
                post = selectedPostForComments,
                comments = activeComments,
                onAddComment = { text -> viewModel.postComment(selectedPostForComments!!.id, text) },
                onClose = { viewModel.closeComments() }
            )
        }

        // Active Story Viewer Overlay overlay
        if (activeStoryView != null) {
            StoryViewerOverlay(
                story = activeStoryView,
                onClose = { viewModel.closeStory() }
            )
        }
    }
}

@Composable
fun BottomNavItem(
    selected: Boolean,
    icon: String,
    label: String,
    textColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        val sizeMultiplier = if (selected) 1.2f else 1.0f
        Text(
            text = icon,
            fontSize = (18f * sizeMultiplier).sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            color = if (selected) CyberpunkCyan else Color.Gray,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
        if (selected) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(CyberpunkCyan, shape = CircleShape)
            )
        } else {
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}
