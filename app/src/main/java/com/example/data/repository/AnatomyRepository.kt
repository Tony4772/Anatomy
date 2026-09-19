package com.example.data.repository

import android.content.Context
import com.example.data.firebase.FirebaseBackendInfo
import com.example.data.firebase.FirebaseManager
import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class AnatomyRepository(
    private val dao: AnatomyDao,
    private val firebaseManager: FirebaseManager? = null
) {

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialDataIfNeeded()
            // Try fetching latest cloud forum discussions if Firebase is online
            try {
                val cloudPosts = firebaseManager?.fetchCloudForumPosts()
                if (!cloudPosts.isNullOrEmpty()) {
                    dao.insertForumPosts(cloudPosts)
                }
            } catch (e: Exception) {
                // Ignore offline
            }
        }
    }

    val firebaseInfo: StateFlow<FirebaseBackendInfo>? = firebaseManager?.backendInfo
    val studentProfile: Flow<StudentProfileEntity?> = dao.getStudentProfile()
    val allCertificates: Flow<List<CertificateEntity>> = dao.getAllCertificates()
    val recentQuizHistory: Flow<List<QuizHistoryEntity>> = dao.getRecentQuizHistory()
    val allForumPosts: Flow<List<ForumPostEntity>> = dao.getAllForumPosts()
    val systemMastery: Flow<List<SystemMasteryEntity>> = dao.getAllSystemMastery()
    val bookmarks: Flow<List<BookmarkEntity>> = dao.getAllBookmarks()

    suspend fun updateStudentName(name: String, university: String) {
        val current = dao.getStudentProfile().firstOrNull() ?: StudentProfileEntity()
        val updated = current.copy(name = name, university = university)
        dao.insertProfile(updated)
        CoroutineScope(Dispatchers.IO).launch {
            firebaseManager?.syncStudentProfile(updated)
        }
    }

    suspend fun addExperience(points: Int) {
        val current = dao.getStudentProfile().firstOrNull() ?: StudentProfileEntity()
        val newXp = current.xp + points
        val newLevel = calculateLevel(newXp)
        val newRank = calculateRank(newLevel)
        val updated = current.copy(
            xp = newXp,
            level = newLevel,
            rankTitle = newRank,
            lastStudyEpoch = System.currentTimeMillis()
        )
        dao.insertProfile(updated)
        CoroutineScope(Dispatchers.IO).launch {
            firebaseManager?.syncStudentProfile(updated)
        }
    }

    suspend fun saveQuizResult(quizType: String, system: String, score: Int, total: Int, xpEarned: Int) {
        val historyItem = QuizHistoryEntity(
            quizType = quizType,
            systemName = system,
            score = score,
            totalQuestions = total,
            xpEarned = xpEarned
        )
        dao.insertQuizHistory(historyItem)
        addExperience(xpEarned)

        CoroutineScope(Dispatchers.IO).launch {
            firebaseManager?.uploadQuizHistory(historyItem)
        }

        // Update system mastery
        val percentage = ((score.toFloat() / total.coerceAtLeast(1)) * 100).toInt()
        val currentMastery = dao.getAllSystemMastery().firstOrNull()?.find { it.systemKey == system }
        if (currentMastery != null) {
            val updatedPercentage = ((currentMastery.masteryPercentage * currentMastery.questionsAnswered + percentage * total) / (currentMastery.questionsAnswered + total)).coerceIn(0, 100)
            dao.updateMastery(
                currentMastery.copy(
                    masteryPercentage = updatedPercentage,
                    questionsAnswered = currentMastery.questionsAnswered + total,
                    correctAnswers = currentMastery.correctAnswers + score
                )
            )
        }
    }

    suspend fun issueCertificate(studentName: String, university: String, examTitle: String, scorePercentage: Int): CertificateEntity {
        val dateFormat = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("es", "ES"))
        val currentDate = dateFormat.format(Date())
        val randomSuffix = UUID.randomUUID().toString().take(8).uppercase()
        val folio = "MED-CERT-2026-$randomSuffix"
        val distinction = when {
            scorePercentage >= 95 -> "Summa Cum Laude (Excelencia Sobresaliente)"
            scorePercentage >= 90 -> "Magna Cum Laude (Distinción Honorífica)"
            else -> "Aprobación Médica Meritoria"
        }
        val qrData = "VERIFY:https://medacademy.edu/verify?folio=$folio&score=$scorePercentage&student=${studentName.replace(" ", "%20")}"

        val cert = CertificateEntity(
            folio = folio,
            studentName = studentName,
            university = university,
            examTitle = examTitle,
            scorePercentage = scorePercentage,
            issueDate = currentDate,
            distinction = distinction,
            qrCodeData = qrData
        )
        dao.insertCertificate(cert)
        addExperience(250) // Big XP boost for certification

        // Push to Firebase Firestore cloud registry for online verification
        CoroutineScope(Dispatchers.IO).launch {
            firebaseManager?.uploadCertificateToCloud(cert)
        }
        return cert
    }

    suspend fun upvotePost(postId: String) {
        dao.upvotePost(postId)
        CoroutineScope(Dispatchers.IO).launch {
            val post = dao.getAllForumPosts().firstOrNull()?.find { it.id == postId }
            if (post != null) {
                firebaseManager?.upvoteForumPost(postId, post.upvotes)
            }
        }
    }

    suspend fun createForumPost(title: String, category: String, content: String, tags: List<String>) {
        val current = dao.getStudentProfile().firstOrNull() ?: StudentProfileEntity()
        val id = "post_${System.currentTimeMillis()}"
        val dateFormat = SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault())
        val formattedDate = dateFormat.format(Date())

        val post = ForumPostEntity(
            id = id,
            authorName = current.name,
            authorRole = current.rankTitle,
            university = current.university,
            title = title,
            category = category,
            content = content,
            tagsCsv = tags.joinToString(","),
            upvotes = 1,
            timestampFormatted = formattedDate,
            isVerifiedDoctor = false,
            commentsJson = "[]"
        )
        dao.insertForumPost(post)
        addExperience(30)

        CoroutineScope(Dispatchers.IO).launch {
            firebaseManager?.uploadForumPost(post)
        }
    }

    suspend fun syncAllWithFirebase(): Boolean = withContext(Dispatchers.IO) {
        val fb = firebaseManager ?: return@withContext false
        try {
            dao.getStudentProfile().firstOrNull()?.let { fb.syncStudentProfile(it) }
            dao.getAllCertificates().firstOrNull()?.forEach { fb.uploadCertificateToCloud(it) }
            dao.getAllForumPosts().firstOrNull()?.forEach { fb.uploadForumPost(it) }
            val cloudPosts = fb.fetchCloudForumPosts()
            if (!cloudPosts.isNullOrEmpty()) {
                dao.insertForumPosts(cloudPosts)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun authenticateWithFirebase(): Result<String> {
        return firebaseManager?.signInAnonymously() ?: Result.failure(Exception("Firebase Backend no inicializado"))
    }

    suspend fun addCommentToPost(postId: String, commentText: String) {
        val current = dao.getStudentProfile().firstOrNull() ?: StudentProfileEntity()
        val posts = dao.getAllForumPosts().firstOrNull() ?: return
        val target = posts.find { it.id == postId } ?: return

        val jsonArray = try {
            JSONArray(target.commentsJson)
        } catch (e: Exception) {
            JSONArray()
        }

        val commentObj = JSONObject().apply {
            put("id", "c_${System.currentTimeMillis()}")
            put("authorName", current.name)
            put("authorRole", current.rankTitle)
            put("text", commentText)
            put("timestamp", "Justo ahora")
            put("isKeyAnswer", false)
        }
        jsonArray.put(commentObj)

        dao.updatePostComments(postId, jsonArray.toString())
        addExperience(15)
    }

    suspend fun toggleBookmark(pin: AnatomyPin, structureName: String) {
        val existing = dao.getAllBookmarks().firstOrNull()?.find { it.pinId == pin.id }
        if (existing != null) {
            dao.deleteBookmark(pin.id)
        } else {
            dao.insertBookmark(
                BookmarkEntity(
                    pinId = pin.id,
                    structureName = structureName,
                    title = pin.title,
                    clinicalPearl = pin.clinicalPearl
                )
            )
        }
    }

    private fun calculateLevel(xp: Int): Int {
        return when {
            xp >= 2500 -> 5
            xp >= 1500 -> 4
            xp >= 800 -> 3
            xp >= 300 -> 2
            else -> 1
        }
    }

    private fun calculateRank(level: Int): String {
        return when (level) {
            5 -> "Especialista Anatomista & Cirujano"
            4 -> "Residente de Cirugía General"
            3 -> "Médico Pasante de Servicio Social"
            2 -> "Interno de Pregrado Clínico"
            else -> "Estudiante Inicial de Medicina"
        }
    }

    private suspend fun seedInitialDataIfNeeded() {
        val existingProfile = dao.getStudentProfile().firstOrNull()
        if (existingProfile == null) {
            dao.insertProfile(
                StudentProfileEntity(
                    id = 1,
                    name = "Dr. Alejandro Morales",
                    university = "Facultad de Medicina y Ciencias de la Salud",
                    xp = 420,
                    level = 2,
                    rankTitle = "Interno de Pregrado Clínico",
                    streakDays = 5,
                    lastStudyEpoch = System.currentTimeMillis()
                )
            )
        }

        val existingMastery = dao.getAllSystemMastery().firstOrNull()
        if (existingMastery.isNullOrEmpty()) {
            dao.insertAllMastery(
                listOf(
                    SystemMasteryEntity("Cardiovascular", "Sistema Cardiovascular", 78, 45, 35),
                    SystemMasteryEntity("Neuroanatomía", "Neuroanatomía Central", 62, 38, 24),
                    SystemMasteryEntity("Respiratorio", "Sistema Respiratorio", 85, 30, 26),
                    SystemMasteryEntity("Esquelético", "Osteología y Articulaciones", 55, 20, 11),
                    SystemMasteryEntity("Digestivo", "Aparato Digestivo y Vías", 70, 28, 20)
                )
            )
        }

        val existingPosts = dao.getAllForumPosts().firstOrNull()
        if (existingPosts.isNullOrEmpty()) {
            val initialPosts = listOf(
                ForumPostEntity(
                    id = "post_1",
                    authorName = "Dra. Sofía Navarro",
                    authorRole = "Residente Cirugía R2",
                    university = "Hospital Clínico San Borja",
                    title = "¿Cómo recordar las relaciones del Triángulo de Calot en colecistectomía?",
                    category = "Debate Quirúrgico",
                    content = "¡Hola a todos! Para los que entran a quirófano este semestre, recordar los límites del triángulo hepatobiliar de Calot es vital para no lesionar la vía biliar principal. Límites: Conducto cístico (inferior), conducto hepático común (medial) y borde inferior del hígado. Contenido clave: ¡la arteria cística y el ganglio de Mascagni!",
                    tagsCsv = "Cirugia,ViasBiliares,AnatomiaQuirurgica",
                    upvotes = 34,
                    timestampFormatted = "Hace 2 horas",
                    isVerifiedDoctor = true,
                    commentsJson = """
                        [
                          {"id":"c1","authorName":"Carlos Ramos (Med R1)","authorRole":"Interno","text":"¡Excelente perla quirúrgica Dra! Siempre preguntan la variación anatómica de la cística en pases de guardia.","timestamp":"Hace 1 hora","isKeyAnswer":true},
                          {"id":"c2","authorName":"Valeria Mendez","authorRole":"Estudiante 3er Año","text":"¿Qué porcentaje de pacientes presenta la arteria cística anterior al conducto hepático común?","timestamp":"Hace 45 min","isKeyAnswer":false}
                        ]
                    """.trimIndent()
                ),
                ForumPostEntity(
                    id = "post_2",
                    authorName = "Ignacio Herrera",
                    authorRole = "Estudiante 2º Año",
                    university = "Univ. Nacional de Medicina",
                    title = "Mnemotecnia infalible para los Pares Craneales (Sensitivos / Motores / Mixtos)",
                    category = "Mnemotecnias",
                    content = "Para saber rápidamente si un par craneal es Sensitivo (S), Motor (M) o Both/Mixto (B):\n\n'Some Say Marry Money, But My Brother Says Big Brains Matter More'\n\nI: S, II: S, III: M, IV: M, V: B, VI: M, VII: B, VIII: S, IX: B, X: B, XI: M, XII: M. ¡Me salvó el examen de neuroanatomía!",
                    tagsCsv = "Neuro,ParesCraneales,Mnemotecnia",
                    upvotes = 58,
                    timestampFormatted = "Ayer",
                    isVerifiedDoctor = false,
                    commentsJson = """
                        [
                          {"id":"c3","authorName":"Lucía Pavez","authorRole":"Ayudante de Cátedra","text":"¡Clásica y súper efectiva! Recuerden que el VII, IX y X también llevan fibras parasimpáticas secretomotoras.","timestamp":"Hace 5 horas","isKeyAnswer":true}
                        ]
                    """.trimIndent()
                ),
                ForumPostEntity(
                    id = "post_3",
                    authorName = "Dr. Mateo Valdés",
                    authorRole = "Médico Cardiólogo",
                    university = "Instituto Nacional del Tórax",
                    title = "Caso Clínico: IAM con elevación del ST en V1-V4 y soplo nuevo holosistólico",
                    category = "Casos Clínicos",
                    content = "Paciente de 62 años con dolor precordial opresivo. En el eco se observa ruptura del tabique interventricular. Anatómicamente, ¿cuál es la arteria culpable y qué porción del septo irriga predominantemente?",
                    tagsCsv = "Cardio,IAM,Coronarias,CasoClinico",
                    upvotes = 42,
                    timestampFormatted = "Hace 1 día",
                    isVerifiedDoctor = true,
                    commentsJson = """
                        [
                          {"id":"c4","authorName":"Martina Gómez","authorRole":"Médico Pasante","text":"Arteria Descendente Anterior (DA), irriga los 2/3 anteriores del tabique interventricular. La ruptura septal es una complicación mecánica mortal.","timestamp":"Hace 18 horas","isKeyAnswer":true}
                        ]
                    """.trimIndent()
                )
            )
            dao.insertForumPosts(initialPosts)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AnatomyRepository? = null

        fun getInstance(context: Context): AnatomyRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AnatomyDatabase.getInstance(context)
                val fbManager = try {
                    FirebaseManager.getInstance(context)
                } catch (e: Exception) {
                    null
                }
                val instance = AnatomyRepository(db.anatomyDao(), fbManager)
                INSTANCE = instance
                instance
            }
        }
    }
}
