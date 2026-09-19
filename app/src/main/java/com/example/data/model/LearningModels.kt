package com.example.data.model

data class QuizQuestion(
    val id: String,
    val system: AnatomicalSystem,
    val question: String,
    val clinicalVignette: String? = null,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val clinicalPearl: String,
    val xpReward: Int = 50
)

enum class QuizMode(val title: String, val subtitle: String) {
    SPOTTER("Spotter Anatómico", "Reconoce estructuras y reparos con límite de tiempo"),
    CLINICAL_CASES("Casos Clínicos MIR/USMLE", "Aplica anatomía en diagnóstico y resolución médica"),
    FLASHCARDS("Tarjetas Repetición Espaciada", "Mnemotecnias y conceptos de alto rendimiento")
}

data class ExamQuestionResult(
    val questionId: String,
    val selectedIndex: Int,
    val isCorrect: Boolean
)

data class CertificateInfo(
    val folio: String,
    val studentName: String,
    val university: String,
    val examTitle: String,
    val scorePercentage: Int,
    val issueDate: String,
    val distinction: String,
    val qrVerificationCode: String
)

data class ForumPost(
    val id: String,
    val authorName: String,
    val authorRole: String,
    val university: String,
    val title: String,
    val category: String, // "Casos Clínicos", "Mnemotecnias", "Dudas de Cátedra", "Debate Quirúrgico"
    val content: String,
    val tags: List<String>,
    val upvotes: Int,
    val timestamp: String,
    val isVerifiedDoctor: Boolean = false,
    val comments: List<ForumComment> = emptyList()
)

data class ForumComment(
    val id: String,
    val authorName: String,
    val authorRole: String,
    val text: String,
    val timestamp: String,
    val isKeyAnswer: Boolean = false
)
