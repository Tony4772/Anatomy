package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student_profile")
data class StudentProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Estudiante de Medicina",
    val university: String = "Facultad de Medicina",
    val xp: Int = 320,
    val level: Int = 2,
    val rankTitle: String = "Interno de Pregrado",
    val streakDays: Int = 4,
    val lastStudyEpoch: Long = System.currentTimeMillis()
)

@Entity(tableName = "certificates")
data class CertificateEntity(
    @PrimaryKey val folio: String,
    val studentName: String,
    val university: String,
    val examTitle: String,
    val scorePercentage: Int,
    val issueDate: String,
    val distinction: String,
    val qrCodeData: String
)

@Entity(tableName = "quiz_history")
data class QuizHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quizType: String,
    val systemName: String,
    val score: Int,
    val totalQuestions: Int,
    val xpEarned: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "forum_posts")
data class ForumPostEntity(
    @PrimaryKey val id: String,
    val authorName: String,
    val authorRole: String,
    val university: String,
    val title: String,
    val category: String,
    val content: String,
    val tagsCsv: String,
    val upvotes: Int,
    val timestampFormatted: String,
    val isVerifiedDoctor: Boolean = false,
    val commentsJson: String = "[]"
)

@Entity(tableName = "system_mastery")
data class SystemMasteryEntity(
    @PrimaryKey val systemKey: String,
    val displayName: String,
    val masteryPercentage: Int,
    val questionsAnswered: Int,
    val correctAnswers: Int
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val pinId: String,
    val structureName: String,
    val title: String,
    val clinicalPearl: String,
    val timestamp: Long = System.currentTimeMillis()
)
