package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.data.local.CertificateEntity
import com.example.data.local.ForumPostEntity
import com.example.data.local.QuizHistoryEntity
import com.example.data.local.StudentProfileEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

enum class FirebaseSyncStatus {
    INITIALIZING,
    READY_ONLINE,
    STANDBY_LOCAL,
    SYNCING,
    ERROR
}

data class FirebaseBackendInfo(
    val status: FirebaseSyncStatus = FirebaseSyncStatus.INITIALIZING,
    val projectId: String = "",
    val currentUserEmail: String? = null,
    val isAnonymous: Boolean = false,
    val lastSyncTimestamp: Long = 0,
    val statusMessage: String = "Iniciando servicio de backend Firebase..."
)

class FirebaseManager private constructor(private val context: Context) {

    private val _backendInfo = MutableStateFlow(FirebaseBackendInfo())
    val backendInfo: StateFlow<FirebaseBackendInfo> = _backendInfo

    private var firestore: FirebaseFirestore? = null
    private var auth: FirebaseAuth? = null

    init {
        initFirebase()
    }

    private fun initFirebase() {
        try {
            val app = if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            } else {
                FirebaseApp.getInstance()
            }

            if (app != null) {
                firestore = FirebaseFirestore.getInstance()
                auth = FirebaseAuth.getInstance()
                val projId = app.options.projectId ?: "anatomimed-firebase-backend"

                _backendInfo.value = FirebaseBackendInfo(
                    status = FirebaseSyncStatus.READY_ONLINE,
                    projectId = projId,
                    currentUserEmail = auth?.currentUser?.email,
                    isAnonymous = auth?.currentUser?.isAnonymous == true,
                    statusMessage = "Backend Firebase conectado a proyecto '$projId'"
                )
                Log.i(TAG, "Firebase initialized successfully with project: $projId")
            } else {
                _backendInfo.value = FirebaseBackendInfo(
                    status = FirebaseSyncStatus.STANDBY_LOCAL,
                    statusMessage = "Firebase en modo local (sin proyecto activo)"
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase initialization fallback: ${e.message}")
            _backendInfo.value = FirebaseBackendInfo(
                status = FirebaseSyncStatus.STANDBY_LOCAL,
                statusMessage = "Modo Local: Preparado para sincronizar con Firebase cuando se configure el proyecto"
            )
        }
    }

    fun isOnlineReady(): Boolean {
        return firestore != null && _backendInfo.value.status == FirebaseSyncStatus.READY_ONLINE
    }

    suspend fun signInAnonymously(): Result<String> = withContext(Dispatchers.IO) {
        val authInstance = auth ?: return@withContext Result.failure(Exception("Auth no inicializado"))
        try {
            val authResult = authInstance.signInAnonymously().await()
            val uid = authResult.user?.uid ?: "anon_${System.currentTimeMillis()}"
            _backendInfo.value = _backendInfo.value.copy(
                isAnonymous = true,
                statusMessage = "Autenticado en Firebase anónimamente (UID: ${uid.take(8)}...)"
            )
            Result.success(uid)
        } catch (e: Exception) {
            Log.e(TAG, "Error in signInAnonymously: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun syncStudentProfile(profile: StudentProfileEntity): Boolean = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext false
        try {
            val profileDoc = mapOf(
                "name" to profile.name,
                "university" to profile.university,
                "xp" to profile.xp,
                "level" to profile.level,
                "rankTitle" to profile.rankTitle,
                "streakDays" to profile.streakDays,
                "lastUpdated" to System.currentTimeMillis()
            )
            val uid = auth?.currentUser?.uid ?: "user_default"
            db.collection("student_profiles").document(uid).set(profileDoc, SetOptions.merge()).await()
            _backendInfo.value = _backendInfo.value.copy(lastSyncTimestamp = System.currentTimeMillis())
            true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync student profile: ${e.message}")
            false
        }
    }

    suspend fun uploadCertificateToCloud(cert: CertificateEntity): Boolean = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext false
        try {
            val certData = mapOf(
                "folio" to cert.folio,
                "studentName" to cert.studentName,
                "university" to cert.university,
                "examTitle" to cert.examTitle,
                "scorePercentage" to cert.scorePercentage,
                "issueDate" to cert.issueDate,
                "distinction" to cert.distinction,
                "qrCodeData" to cert.qrCodeData,
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("certificates").document(cert.folio).set(certData, SetOptions.merge()).await()
            Log.i(TAG, "Certificate ${cert.folio} uploaded to Firebase Firestore")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to upload certificate: ${e.message}")
            false
        }
    }

    suspend fun verifyCertificateOnline(folio: String): Map<String, Any>? = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext null
        try {
            val doc = db.collection("certificates").document(folio).get().await()
            if (doc.exists()) {
                doc.data
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to verify certificate online: ${e.message}")
            null
        }
    }

    suspend fun uploadForumPost(post: ForumPostEntity): Boolean = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext false
        try {
            val postData = mapOf(
                "id" to post.id,
                "authorName" to post.authorName,
                "authorRole" to post.authorRole,
                "university" to post.university,
                "title" to post.title,
                "category" to post.category,
                "content" to post.content,
                "tagsCsv" to post.tagsCsv,
                "upvotes" to post.upvotes,
                "timestampFormatted" to post.timestampFormatted,
                "isVerifiedDoctor" to post.isVerifiedDoctor,
                "commentsJson" to post.commentsJson,
                "createdAt" to System.currentTimeMillis()
            )
            db.collection("forum_posts").document(post.id).set(postData, SetOptions.merge()).await()
            Log.i(TAG, "Post ${post.id} pushed to Firestore")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to upload forum post: ${e.message}")
            false
        }
    }

    suspend fun upvoteForumPost(postId: String, newUpvotes: Int): Boolean = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext false
        try {
            db.collection("forum_posts").document(postId).update("upvotes", newUpvotes).await()
            true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to upvote on Firestore: ${e.message}")
            false
        }
    }

    suspend fun uploadQuizHistory(history: QuizHistoryEntity): Boolean = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext false
        try {
            val data = mapOf(
                "quizType" to history.quizType,
                "systemName" to history.systemName,
                "score" to history.score,
                "totalQuestions" to history.totalQuestions,
                "xpEarned" to history.xpEarned,
                "timestamp" to history.timestamp
            )
            db.collection("quiz_history").add(data).await()
            true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to upload quiz history: ${e.message}")
            false
        }
    }

    suspend fun fetchCloudForumPosts(): List<ForumPostEntity>? = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext null
        try {
            val snapshot = db.collection("forum_posts").get().await()
            snapshot.documents.mapNotNull { doc ->
                try {
                    ForumPostEntity(
                        id = doc.getString("id") ?: doc.id,
                        authorName = doc.getString("authorName") ?: "Estudiante",
                        authorRole = doc.getString("authorRole") ?: "Médico",
                        university = doc.getString("university") ?: "",
                        title = doc.getString("title") ?: "",
                        category = doc.getString("category") ?: "Discusión",
                        content = doc.getString("content") ?: "",
                        tagsCsv = doc.getString("tagsCsv") ?: "",
                        upvotes = (doc.getLong("upvotes") ?: 1L).toInt(),
                        timestampFormatted = doc.getString("timestampFormatted") ?: "Reciente",
                        isVerifiedDoctor = doc.getBoolean("isVerifiedDoctor") ?: false,
                        commentsJson = doc.getString("commentsJson") ?: "[]"
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch posts from Firestore: ${e.message}")
            null
        }
    }

    companion object {
        private const val TAG = "FirebaseBackend"

        @Volatile
        private var INSTANCE: FirebaseManager? = null

        fun getInstance(context: Context): FirebaseManager {
            return INSTANCE ?: synchronized(this) {
                val instance = FirebaseManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
