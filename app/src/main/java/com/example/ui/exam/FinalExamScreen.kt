package com.example.ui.exam

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.CertificateEntity
import com.example.data.model.AnatomyQuestionBank
import com.example.data.repository.AnatomyRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinalExamScreen(
    repository: AnatomyRepository
) {
    val scope = rememberCoroutineScope()
    val studentProfile by repository.studentProfile.collectAsStateWithLifecycle(initialValue = null)
    val certificates by repository.allCertificates.collectAsStateWithLifecycle(initialValue = emptyList())

    val examQuestions = remember { AnatomyQuestionBank.getFinalCertificationExam() }

    var isExamStarted by remember { mutableStateOf(false) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    val selectedAnswers = remember { mutableStateMapOf<Int, Int>() }
    var isExamFinished by remember { mutableStateOf(false) }
    var finalScorePercentage by remember { mutableIntStateOf(0) }
    var activeCertificateView by remember { mutableStateOf<CertificateEntity?>(null) }

    // If student is viewing an individual certificate
    if (activeCertificateView != null) {
        DigitalCertificateView(
            certificate = activeCertificateView!!,
            onBack = { activeCertificateView = null }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Examen de Certificación Médica",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkNavySurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkNavyBackground)
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (!isExamStarted && !isExamFinished) {
                // Exam Overview & Rules Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x334DD8EC)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0x33007A8C), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.School,
                                    contentDescription = null,
                                    tint = MedicalTealLight,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Examen General de Anatomía",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Certificación Oficial de Competencias",
                                    fontSize = 12.sp,
                                    color = MedicalTealLight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Demuestra tu dominio en neuroanatomía, cardiovascular, respiratorio y osteología clínica para obtener el Diploma Digital de Excelencia con Folio Oficial y Código QR verificable.",
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = Color(0xFFCFD8DC)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Requirements Checklist
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MedicalGreenSuccess, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("5 Preguntas de Correlación Clínica & Anatómica", fontSize = 12.sp, color = Color(0xFFECEFF4))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MedicalGreenSuccess, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Nota mínima aprobatoria: 80% (4/5 aciertos)", fontSize = 12.sp, color = Color(0xFFECEFF4))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MedicalGreenSuccess, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Diploma Oficial con Sello Dorado y Verificación QR", fontSize = 12.sp, color = Color(0xFFECEFF4))
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                selectedAnswers.clear()
                                currentQuestionIndex = 0
                                isExamStarted = true
                                isExamFinished = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Comenzar Evaluación Oficial", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Existing Certificates Section
                Text(
                    text = "Tus Certificados Médicos Obtenidos (${certificates.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (certificates.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = DarkNavySurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x22FFFFFF)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.WorkspacePremium,
                                contentDescription = null,
                                tint = Color(0xFF78909C),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Aún no tienes diplomas registrados.",
                                fontSize = 13.sp,
                                color = Color(0xFFB0BEC5)
                            )
                            Text(
                                text = "Aprueba la evaluación para conseguir tu primera certificación.",
                                fontSize = 12.sp,
                                color = Color(0xFF78909C)
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        certificates.forEach { cert ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeuralGold.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { activeCertificateView = cert }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(Color(0x33FFB300), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.WorkspacePremium,
                                            contentDescription = null,
                                            tint = NeuralGold,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = cert.examTitle,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "${cert.distinction} • ${cert.scorePercentage}%",
                                            fontSize = 12.sp,
                                            color = NeuralGold
                                        )
                                        Text(
                                            text = "Folio: ${cert.folio}",
                                            fontSize = 10.sp,
                                            color = Color(0xFF90A4AE)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Ver",
                                        tint = Color(0xFFB0BEC5)
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (isExamStarted && !isExamFinished) {
                // Active Exam Questions Form
                val currentQ = examQuestions[currentQuestionIndex]

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pregunta ${currentQuestionIndex + 1} de ${examQuestions.size}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MedicalTealLight
                    )
                    Text(
                        text = "${((currentQuestionIndex + 1f) / examQuestions.size * 100).toInt()}%",
                        fontSize = 13.sp,
                        color = Color(0xFFB0BEC5)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { (currentQuestionIndex + 1f) / examQuestions.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MedicalTealLight,
                    trackColor = Color(0x22FFFFFF)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x334DD8EC)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Surface(
                            color = MedicalTealPrimary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = currentQ.system.displayName.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MedicalTealLight,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentQ.question,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 22.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    currentQ.options.forEachIndexed { optIndex, optText ->
                        val isSelected = selectedAnswers[currentQuestionIndex] == optIndex

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedAnswers[currentQuestionIndex] = optIndex
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0x33007A8C) else DarkNavySurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) MedicalTealLight else Color(0x224DD8EC)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(
                                            if (isSelected) MedicalTealLight else DarkNavySurfaceVariant,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = ('A' + optIndex).toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) DarkNavyBackground else Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = optText,
                                    fontSize = 14.sp,
                                    color = Color(0xFFECEFF4)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (currentQuestionIndex > 0) {
                        OutlinedButton(
                            onClick = { currentQuestionIndex-- },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Anterior", color = Color(0xFFECEFF4))
                        }
                    } else {
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Button(
                        onClick = {
                            if (currentQuestionIndex + 1 < examQuestions.size) {
                                currentQuestionIndex++
                            } else {
                                // Calculate score
                                var correctCount = 0
                                examQuestions.forEachIndexed { i, q ->
                                    if (selectedAnswers[i] == q.correctIndex) {
                                        correctCount++
                                    }
                                }
                                val pct = ((correctCount.toFloat() / examQuestions.size) * 100).toInt()
                                finalScorePercentage = pct
                                isExamFinished = true

                                if (pct >= 80) {
                                    val name = studentProfile?.name ?: "Dr. Estudiante de Medicina"
                                    val uni = studentProfile?.university ?: "Facultad de Medicina"
                                    scope.launch {
                                        val cert = repository.issueCertificate(
                                            studentName = name,
                                            university = uni,
                                            examTitle = "Diploma de Excelencia en Anatomía Humana y Correlaciones Clínicas",
                                            scorePercentage = pct
                                        )
                                        activeCertificateView = cert
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary),
                        shape = RoundedCornerShape(12.dp),
                        enabled = selectedAnswers.containsKey(currentQuestionIndex)
                    ) {
                        Text(
                            text = if (currentQuestionIndex + 1 < examQuestions.size) "Siguiente" else "Entregar Examen",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else if (isExamFinished) {
                // Post Exam Assessment Summary
                val isPassed = finalScorePercentage >= 80

                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isPassed) NeuralGold else ArterialCrimson
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    if (isPassed) Color(0x33FFB300) else Color(0x33D32F2F),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPassed) Icons.Filled.WorkspacePremium else Icons.Filled.Warning,
                                contentDescription = null,
                                tint = if (isPassed) NeuralGold else ArterialCrimsonLight,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (isPassed) "¡Felicidades! Examen Aprobado" else "Examen No Aprobado",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Calificación Obtenida: $finalScorePercentage% (Mínimo requerido: 80%)",
                            fontSize = 13.sp,
                            color = if (isPassed) NeuralGold else Color(0xFFFF859F)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        if (isPassed) {
                            Text(
                                text = "Tu certificado oficial ha sido emitido con éxito y guardado en tu portafolio académico.",
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = Color(0xFFCFD8DC)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    val cert = certificates.firstOrNull()
                                    if (cert != null) activeCertificateView = cert
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeuralGold),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.School, contentDescription = null, tint = DarkNavyBackground)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ver Mi Diploma Digital", color = DarkNavyBackground, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text(
                                text = "Te sugerimos repasar las estructuras en el visor 3D/RA y resolver los cuestionarios por sistemas antes de reintentar.",
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = Color(0xFFCFD8DC)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                isExamStarted = false
                                isExamFinished = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Volver al Inicio de Exámenes", color = Color(0xFFECEFF4))
                        }
                    }
                }
            }
        }
    }
}
