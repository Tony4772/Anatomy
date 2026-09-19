package com.example.ui.quiz

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.AnatomyRepository
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun QuizScreen(
    repository: AnatomyRepository,
    onNavigateToExam: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var selectedMode by remember { mutableStateOf(QuizMode.SPOTTER) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var comboStreak by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var hasAnswered by remember { mutableStateOf(false) }
    var isQuizCompleted by remember { mutableStateOf(false) }
    var totalXpEarned by remember { mutableIntStateOf(0) }

    val questions = remember(selectedMode) {
        when (selectedMode) {
            QuizMode.SPOTTER -> AnatomyQuestionBank.getSpotterQuestions()
            QuizMode.CLINICAL_CASES -> AnatomyQuestionBank.getClinicalCases()
            QuizMode.FLASHCARDS -> AnatomyQuestionBank.getSpotterQuestions()
        }
    }

    val currentQuestion = questions.getOrNull(currentQuestionIndex)

    fun resetQuiz(newMode: QuizMode) {
        selectedMode = newMode
        currentQuestionIndex = 0
        score = 0
        comboStreak = 0
        selectedOption = null
        hasAnswered = false
        isQuizCompleted = false
        totalXpEarned = 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavyBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Mode Selector Tab Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(DarkNavySurfaceVariant)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuizMode.entries.forEach { mode ->
                val isCurrent = selectedMode == mode
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { resetQuiz(mode) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isCurrent) MedicalTealPrimary else Color.Transparent
                ) {
                    Text(
                        text = when (mode) {
                            QuizMode.SPOTTER -> "Spotter"
                            QuizMode.CLINICAL_CASES -> "Casos MIR"
                            QuizMode.FLASHCARDS -> "Tarjetas"
                        },
                        color = if (isCurrent) Color.White else Color(0xFFB0BEC5),
                        fontSize = 12.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Header Status (Question counter, Streak flames, XP)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Questions Progress Badge
            Surface(
                color = DarkNavySurface,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x334DD8EC))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pregunta ${currentQuestionIndex + 1}/${questions.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MedicalTealLight
                    )
                }
            }

            // Combo Streak Badge
            Surface(
                color = if (comboStreak >= 2) Color(0x44D32F2F) else DarkNavySurface,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (comboStreak >= 2) ArterialCrimsonLight else Color(0x334DD8EC)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = "Racha",
                        tint = if (comboStreak >= 2) ArterialCrimsonLight else NeuralGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (comboStreak > 1) "Racha x$comboStreak" else "Racha: $comboStreak",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (comboStreak >= 2) ArterialCrimsonLight else NeuralGold
                    )
                }
            }

            // Total Score
            Surface(
                color = DarkNavySurface,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x334DD8EC))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Stars,
                        contentDescription = "Puntos",
                        tint = NeuralGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+$totalXpEarned XP",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeuralGold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Linear Progress Indicator
        LinearProgressIndicator(
            progress = { (currentQuestionIndex + 1f) / questions.size.coerceAtLeast(1) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MedicalTealLight,
            trackColor = Color(0x22FFFFFF)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!isQuizCompleted && currentQuestion != null) {
            // Clinical Vignette Card if present
            if (currentQuestion.clinicalVignette != null) {
                Surface(
                    color = Color(0xFF16253D),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x444DD8EC)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Medication,
                            contentDescription = null,
                            tint = MedicalTealLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = currentQuestion.clinicalVignette,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFB2EBF2)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Question Text Card
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x224DD8EC)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Surface(
                        color = MedicalTealPrimary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = currentQuestion.system.displayName.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedicalTealLight,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentQuestion.question,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 22.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Options List
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                currentQuestion.options.forEachIndexed { index, optionText ->
                    val isSelected = selectedOption == index
                    val isCorrect = index == currentQuestion.correctIndex

                    val borderColor = when {
                        hasAnswered && isCorrect -> MedicalGreenSuccess
                        hasAnswered && isSelected && !isCorrect -> ArterialCrimson
                        isSelected -> MedicalTealLight
                        else -> Color(0x224DD8EC)
                    }

                    val containerColor = when {
                        hasAnswered && isCorrect -> Color(0x3300A86B)
                        hasAnswered && isSelected && !isCorrect -> Color(0x33D32F2F)
                        isSelected -> Color(0x22007A8C)
                        else -> DarkNavySurface
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !hasAnswered) {
                                selectedOption = index
                                hasAnswered = true
                                if (isCorrect) {
                                    score++
                                    comboStreak++
                                    val multiplier = if (comboStreak >= 3) 2 else 1
                                    totalXpEarned += currentQuestion.xpReward * multiplier
                                } else {
                                    comboStreak = 0
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = containerColor,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(
                                        when {
                                            hasAnswered && isCorrect -> MedicalGreenSuccess
                                            hasAnswered && isSelected && !isCorrect -> ArterialCrimson
                                            else -> DarkNavySurfaceVariant
                                        },
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ('A' + index).toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = optionText,
                                fontSize = 14.sp,
                                color = Color(0xFFECEFF4),
                                modifier = Modifier.weight(1f)
                            )
                            if (hasAnswered) {
                                if (isCorrect) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Correcto",
                                        tint = MedicalGreenSuccess,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = "Incorrecto",
                                        tint = ArterialCrimson,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Explanation & Next Button if answered
            if (hasAnswered) {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF17233B)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3300B4D8)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircleOutline,
                                contentDescription = null,
                                tint = MedicalTealLight,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Fundamento y Explicación Anatómica",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MedicalTealLight
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentQuestion.explanation,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFFECEFF4)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        // Clinical pearl inside explanation
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x22FFB300)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = NeuralGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Perla MIR: ${currentQuestion.clinicalPearl}",
                                    fontSize = 12.sp,
                                    color = Color(0xFFFFD54F)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (currentQuestionIndex + 1 < questions.size) {
                            currentQuestionIndex++
                            selectedOption = null
                            hasAnswered = false
                        } else {
                            isQuizCompleted = true
                            scope.launch {
                                repository.saveQuizResult(
                                    quizType = selectedMode.title,
                                    system = selectedStructureSystemName(questions),
                                    score = score,
                                    total = questions.size,
                                    xpEarned = totalXpEarned
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (currentQuestionIndex + 1 < questions.size) "Siguiente Pregunta" else "Ver Resultados y XP",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else if (isQuizCompleted) {
            // Completion / Results Card
            val percentage = ((score.toFloat() / questions.size.coerceAtLeast(1)) * 100).toInt()

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF4DD8EC)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(
                                if (percentage >= 70) Color(0x3300A86B) else Color(0x33FFB300),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (percentage >= 70) Icons.Filled.WorkspacePremium else Icons.Filled.MilitaryTech,
                            contentDescription = null,
                            tint = if (percentage >= 70) MedicalGreenSuccess else NeuralGold,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (percentage >= 70) "¡Excelente Desempeño!" else "¡Buen Intento de Repaso!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Has completado la sesión de ${selectedMode.title}",
                        fontSize = 13.sp,
                        color = Color(0xFFB0BEC5)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Aciertos", fontSize = 11.sp, color = Color(0xFF90A4AE))
                            Text("$score/${questions.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Precisión", fontSize = 11.sp, color = Color(0xFF90A4AE))
                            Text("$percentage%", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MedicalTealLight)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("XP Ganado", fontSize = 11.sp, color = Color(0xFF90A4AE))
                            Text("+$totalXpEarned", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NeuralGold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { resetQuiz(selectedMode) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Repetir Cuestionario", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onNavigateToExam,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeuralGold)
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, tint = NeuralGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Rendir Examen para Certificado Digital", color = NeuralGold, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun selectedStructureSystemName(questions: List<QuizQuestion>): String {
    return questions.firstOrNull()?.system?.displayName ?: "Cardiovascular"
}
