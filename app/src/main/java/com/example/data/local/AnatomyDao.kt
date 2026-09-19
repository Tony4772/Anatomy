package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AnatomyDao {

    // Student Profile
    @Query("SELECT * FROM student_profile WHERE id = 1 LIMIT 1")
    fun getStudentProfile(): Flow<StudentProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: StudentProfileEntity)

    @Update
    suspend fun updateProfile(profile: StudentProfileEntity)

    // Certificates
    @Query("SELECT * FROM certificates ORDER BY issueDate DESC")
    fun getAllCertificates(): Flow<List<CertificateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertificate(certificate: CertificateEntity)

    @Query("SELECT * FROM certificates WHERE folio = :folio LIMIT 1")
    suspend fun getCertificateByFolio(folio: String): CertificateEntity?

    // Quiz History
    @Query("SELECT * FROM quiz_history ORDER BY timestamp DESC LIMIT 20")
    fun getRecentQuizHistory(): Flow<List<QuizHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizHistory(history: QuizHistoryEntity)

    // Forum Posts
    @Query("SELECT * FROM forum_posts ORDER BY upvotes DESC")
    fun getAllForumPosts(): Flow<List<ForumPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForumPost(post: ForumPostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForumPosts(posts: List<ForumPostEntity>)

    @Query("UPDATE forum_posts SET upvotes = upvotes + 1 WHERE id = :postId")
    suspend fun upvotePost(postId: String)

    @Query("UPDATE forum_posts SET commentsJson = :newCommentsJson WHERE id = :postId")
    suspend fun updatePostComments(postId: String, newCommentsJson: String)

    // System Mastery
    @Query("SELECT * FROM system_mastery")
    fun getAllSystemMastery(): Flow<List<SystemMasteryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMastery(list: List<SystemMasteryEntity>)

    @Update
    suspend fun updateMastery(mastery: SystemMasteryEntity)

    // Bookmarks
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE pinId = :pinId")
    suspend fun deleteBookmark(pinId: String)
}
