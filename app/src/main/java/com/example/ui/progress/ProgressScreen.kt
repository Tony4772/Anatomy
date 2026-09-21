package com.example.ui.progress

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
import com.example.data.firebase.FirebaseBackendInfo
import com.example.data.firebase.FirebaseSyncStatus
import com.example.data.repository.AnatomyRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    repository: AnatomyRepository,
    onNavigateToCertificates: () -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val profile by repository.studentProfile.collectAsStateWithLifecycle(initialValue = null)
    val masteryList by repository.systemMastery.collectAsStateWithLifecycle(initialValue = emptyList())
    val quizHistory by repository.recentQuizHistory.collectAsStateWithLifecycle(initialValue = emptyList())
    val bookmarks by repository.bookmarks.collectAsStateWithLifecycle(initialValue = emptyList())
    val firebaseInfo by (repository.firebaseInfo?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(FirebaseBackendInfo()) })

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var isSyncingToCloud by remember { mutableStateOf(false) }
    var syncSuccessBanner by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    }
                },
                title = {
                    Text(
                        text = "Progreso y Perfil Académico",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                actions = {
                    IconButton(onClick = { showEditProfileDialog = true }) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Editar perfil")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavySurface)
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
            // Profile Card with Level & XP
            val xp = profile?.xp ?: 0
            val level = profile?.level ?: 1
            val nextLevelXp = when (level) {
                1 -> 300
                2 -> 800
                3 -> 1500
                4 -> 2500
                else -> 4000
            }
            val levelProgress = (xp.toFloat() / nextLevelXp).coerceIn(0f, 1f)

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x334DD8EC)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    androidx.compose.ui.graphics.Brush.linearGradient(
                                        listOf(MedicalTealPrimary, Color(0xFF00B4D8))
                                    ),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (profile?.name ?: "D").take(1),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile?.name ?: "Dr. Estudiante de Medicina",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = profile?.rankTitle ?: "Interno de Pregrado",
                                fontSize = 12.sp,
                                color = MedicalTealLight,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = profile?.university ?: "Facultad de Medicina",
                                fontSize = 11.sp,
                                color = Color(0xFF90A4AE)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Streak and Level Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            color = Color(0x22D32F2F),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ArterialCrimsonLight)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = ArterialCrimsonLight,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${profile?.streakDays ?: 1} Días de Racha",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArterialCrimsonLight
                                )
                            }
                        }

                        Surface(
                            color = Color(0x22FFB300),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeuralGold)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MilitaryTech,
                                    contentDescription = null,
                                    tint = NeuralGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Nivel $level ($xp XP)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeuralGold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Level Progress
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Progreso a Nivel ${level + 1}", fontSize = 11.sp, color = Color(0xFFB0BEC5))
                        Text("$xp / $nextLevelXp XP", fontSize = 11.sp, color = Color(0xFFB0BEC5))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { levelProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MedicalTealLight,
                        trackColor = Color(0x22FFFFFF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Firebase Cloud Backend Architecture Card
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FF9800)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0x33FF9800),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CloudSync,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB74D),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Firebase Cloud Backend",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Firestore DB • Auth • Cloud Verification",
                                    fontSize = 11.sp,
                                    color = Color(0xFFFFB74D)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x224CAF50),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF81C784))
                        ) {
                            Text(
                                text = "CONFIGURADO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF81C784),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = firebaseInfo.statusMessage,
                        fontSize = 12.sp,
                        color = Color(0xFFB0BEC5),
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    isSyncingToCloud = true
                                    val ok = repository.syncAllWithFirebase()
                                    isSyncingToCloud = false
                                    syncSuccessBanner = if (ok) "¡Datos y certificaciones respaldados en Firebase Firestore!" else "Sincronización en cola (modo local activo)"
                                }
                            },
                            enabled = !isSyncingToCloud,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isSyncingToCloud) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sincronizando...", fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sincronizar Nube", fontSize = 12.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    repository.authenticateWithFirebase()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x55FF9800)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color(0xFFFFB74D), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sesión Firebase", fontSize = 12.sp, color = Color(0xFFFFB74D))
                        }
                    }

                    if (syncSuccessBanner != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x2200E676),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x6600E676)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = syncSuccessBanner ?: "",
                                fontSize = 11.sp,
                                color = Color(0xFF69F0AE),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // System Mastery Breakdown
            Text(
                text = "Dominio por Sistemas Anatómicos",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x224DD8EC)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    masteryList.forEach { m ->
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = m.displayName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFECEFF4)
                                )
                                Text(
                                    text = "${m.masteryPercentage}% (${m.correctAnswers}/${m.questionsAnswered})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (m.masteryPercentage >= 75) MedicalTealLight else NeuralGold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { m.masteryPercentage / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (m.masteryPercentage >= 75) MedicalTealLight else NeuralGold,
                                trackColor = Color(0x22FFFFFF)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Recent Sessions
            Text(
                text = "Historial Reciente de Evaluaciones",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (quizHistory.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkNavySurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Aún no has completado cuestionarios. ¡Inicia un Spotter o Caso Clínico!",
                        fontSize = 12.sp,
                        color = Color(0xFF90A4AE),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    quizHistory.take(5).forEach { h ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = DarkNavySurface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${h.quizType} • ${h.systemName}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Aciertos: ${h.score}/${h.totalQuestions}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF90A4AE)
                                    )
                                }
                                Text(
                                    text = "+${h.xpEarned} XP",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeuralGold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bookmarked Clinical Pearls
            if (bookmarks.isNotEmpty()) {
                Text(
                    text = "Perlas Clínicas Guardadas (${bookmarks.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    bookmarks.forEach { b ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFB300)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "${b.title} (${b.structureName})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeuralGold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = b.clinicalPearl,
                                    fontSize = 12.sp,
                                    color = Color(0xFFCFD8DC)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Créditos / Copyright
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x224DD8EC)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Acerca de AnatomiMed",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Desarrollado por EBYZOM E.I.R.L.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MedicalTealLight
                    )
                    Text(
                        text = "© ${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)} EBYZOM E.I.R.L. Todos los derechos reservados.",
                        fontSize = 11.sp,
                        color = Color(0xFF90A4AE),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "AnatomiMed y su contenido son propiedad de EBYZOM E.I.R.L.",
                        fontSize = 11.sp,
                        color = Color(0xFF78909C),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(profile?.name ?: "") }
        var editUni by remember { mutableStateOf(profile?.university ?: "") }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            containerColor = DarkNavySurface,
            title = { Text("Personalizar Datos Médicos", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Estos datos se imprimirán en tus diplomas y certificados digitales.",
                        fontSize = 12.sp,
                        color = Color(0xFFB0BEC5)
                    )
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nombre Completo con Título") },
                        placeholder = { Text("Ej. Dra. Camila Valenzuela") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editUni,
                        onValueChange = { editUni = it },
                        label = { Text("Universidad u Hospital") },
                        placeholder = { Text("Ej. Facultad de Medicina") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.updateStudentName(editName, editUni)
                        }
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary)
                ) {
                    Text("Guardar Cambios")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancelar", color = Color(0xFFB0BEC5))
                }
            }
        )
    }
}
