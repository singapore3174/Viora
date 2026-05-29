package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles")
    fun getAllProfilesFlow(): Flow<List<UserProfile>>

    @Query("SELECT * FROM user_profiles WHERE username = :username")
    fun getProfileFlow(username: String): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(profiles: List<UserProfile>)

    @Query("UPDATE user_profiles SET isFollowing = :isFollowing, followersCount = followersCount + :followsDiff WHERE username = :username")
    suspend fun updateFollowingState(username: String, isFollowing: Boolean, followsDiff: Int)
}

@Dao
interface PostDao {
    @Query("SELECT * FROM posts WHERE isReel = 0 ORDER BY timestamp DESC")
    fun getAllPostsFlow(): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE isReel = 1 ORDER BY timestamp DESC")
    fun getReelsFlow(): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE isSaved = 1 ORDER BY timestamp DESC")
    fun getSavedPostsFlow(): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE username = :username ORDER BY timestamp DESC")
    fun getProfilePostsFlow(username: String): Flow<List<Post>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<Post>)

    @Query("UPDATE posts SET isLiked = :isLiked, likesCount = likesCount + :likesDiff WHERE id = :postId")
    suspend fun updateLikeState(postId: String, isLiked: Boolean, likesDiff: Int)

    @Query("UPDATE posts SET isSaved = :isSaved WHERE id = :postId")
    suspend fun updateSaveState(postId: String, isSaved: Boolean)

    @Query("UPDATE posts SET commentsCount = commentsCount + :countDiff WHERE id = :postId")
    suspend fun updateCommentCount(postId: String, countDiff: Int)
}

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsForPostFlow(postId: String): Flow<List<Comment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment)
}

@Dao
interface StoryDao {
    @Query("SELECT * FROM stories ORDER BY timestamp DESC")
    fun getAllStoriesFlow(): Flow<List<Story>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: Story)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<Story>)

    @Query("UPDATE stories SET isViewed = 1 WHERE id = :storyId")
    suspend fun markStoryAsViewed(storyId: String)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE chatPartner = :partner ORDER BY timestamp ASC")
    fun getMessagesForChatFlow(partner: String): Flow<List<Message>>

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessagesFlow(): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<Message>)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotificationsFlow(): Flow<List<Notification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<Notification>)
}

@Database(
    entities = [
        UserProfile::class,
        Post::class,
        Comment::class,
        Story::class,
        Message::class,
        Notification::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun postDao(): PostDao
    abstract fun commentDao(): CommentDao
    abstract fun storyDao(): StoryDao
    abstract fun messageDao(): MessageDao
    abstract fun notificationDao(): NotificationDao
}
