package com.example.ui.ar3d

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.model.*
import com.example.data.repository.AnatomyRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ARViewerScreen(
    repository: AnatomyRepository,
    initialStructureId: String? = null,
    initialFullBody: Boolean = true,
    onBackToHome: (() -> Unit)? = null,
    onConsultAITutor: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val structures = remember { AnatomyDataFactory.getAllStructures() }
    var isFullBodyView by remember(initialStructureId, initialFullBody) {
        mutableStateOf(if (initialStructureId != null) initialFullBody else true)
    }
    var selectedStructure by remember(initialStructureId) {
        mutableStateOf(
            if (initialStructureId != null) {
                structures.find { it.id.equals(initialStructureId, ignoreCase = true) } ?: structures[0]
            } else {
                structures[0]
            }
        )
    }
    var selectedLayer by remember { mutableStateOf(AnatomyLayer.ALL) }
    var isARMode by remember { mutableStateOf(false) }
    var explodedFactor by remember { mutableFloatStateOf(0f) }
    var bpm by remember { mutableIntStateOf(72) }
    var isHeartBeating by remember { mutableStateOf(true) }
    var activePin by remember { mutableStateOf<AnatomyPin?>(null) }
    var showHelpDialog by remember { mutableStateOf(false) }

    // Camera permission for AR mode
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted) isARMode = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (!isFullBodyView) {
                        IconButton(onClick = { isFullBodyView = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver al cuerpo humano completo",
                                tint = MedicalTealLight
                            )
                        }
                    } else if (onBackToHome != null) {
                        IconButton(onClick = onBackToHome) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver al explorador 2D",
                                tint = MedicalTealLight
                            )
                        }
                    }
                },
                title = {
                    Column {
                        Text(
                            text = if (isFullBodyView) {
                                "Cuerpo Humano Completo"
                            } else if (isARMode) {
                                "Visor RA • ${selectedStructure.name}"
                            } else {
                                selectedStructure.name
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = if (isFullBodyView) {
                                "Toca un órgano o región para explorar en 3D / RA"
                            } else {
                                "${selectedStructure.latinName} • ${selectedStructure.system.displayName}"
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    if (isFullBodyView) {
                        OutlinedButton(
                            onClick = { isFullBodyView = false },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x554DD8EC))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = MedicalTealLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ver Órgano", fontSize = 12.sp, color = MedicalTealLight)
                        }
                    } else {
                        // AR Mode Toggle Switch Button
                        FilledTonalButton(
                            onClick = {
                                if (!isARMode) {
                                    if (hasCameraPermission) {
                                        isARMode = true
                                    } else {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                } else {
                                    isARMode = false
                                }
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isARMode) MedicalTealLight else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isARMode) DarkNavyBackground else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isARMode) Icons.Default.ViewInAr else Icons.Outlined.ViewInAr,
                                contentDescription = "Cambiar modo AR",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isARMode) "Modo 3D" else "Modo RA",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(Icons.Outlined.Info, contentDescription = "Instrucciones de uso")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isARMode && !isFullBodyView) Color(0xCC091322) else MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(if (isARMode && !isFullBodyView) Color.Black else DarkNavyBackground)
        ) {
            if (isFullBodyView) {
                // ==========================================
                // 1. VISTA DE CUERPO HUMANO COMPLETO EN 3D
                // ==========================================
                HumanBody3DCanvas(
                    modifier = Modifier.fillMaxSize(),
                    structures = structures,
                    selectedStructure = selectedStructure,
                    onSelectOrgan = { structure ->
                        selectedStructure = structure
                    }
                )

                // Selector horizontal de órganos sobre el cuerpo humano
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = true,
                                onClick = { /* Already in full body */ },
                                label = { Text("🧍 Cuerpo Completo", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MedicalTealPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        items(structures) { item ->
                            val isSelected = item.id == selectedStructure.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedStructure = item
                                },
                                label = { Text(item.name, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF00ACC1),
                                    selectedLabelColor = Color.White,
                                    containerColor = DarkNavySurfaceVariant,
                                    labelColor = Color(0xFFECEFF4)
                                )
                            )
                        }
                    }

                    // Floating guidance chip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xAA0B1626),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x334DD8EC)),
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Text(
                            text = "💡 Arrastra para rotar en 360° • Toca los puntos brillantes para inspeccionar cada órgano",
                            fontSize = 11.sp,
                            color = Color(0xFFB0BEC5),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Tarjeta Inferior de Órgano Seleccionado en el Cuerpo
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(12.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = DarkNavySurface.copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x444DD8EC)),
                    tonalElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = MedicalTealPrimary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = selectedStructure.system.displayName.uppercase(),
                                        color = MedicalTealLight,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = selectedStructure.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = selectedStructure.latinName,
                                    fontSize = 12.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = Color(0xFFB0BEC5)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = selectedStructure.summary,
                            fontSize = 12.sp,
                            color = Color(0xFFCFD8DC),
                            lineHeight = 16.sp,
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    isFullBodyView = false
                                    explodedFactor = 0f
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MedicalTealPrimary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Explorar en 3D / RA", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    onConsultAITutor("Explícame en detalle la anatomía, irrigación y perlas de: ${selectedStructure.name} (${selectedStructure.latinName})")
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x664DD8EC))
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MedicalTealLight, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tutor IA", color = MedicalTealLight, fontSize = 12.sp)
                            }
                        }
                    }
                }

            } else {
                // ====================================================
                // 2. VISTA DETALLADA DEL ÓRGANO ESPECÍFICO (3D / RA)
                // ====================================================
                // Background: Camera Preview when in AR Mode
                if (isARMode && hasCameraPermission) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // AR Medical HUD Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0x77000000), Color.Transparent, Color(0x99000000))
                                )
                            )
                    )

                    // AR Reticle Center Indicator
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .align(Alignment.Center)
                            .border(1.dp, Color(0x6600E5FF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF00E5FF), CircleShape)
                        )
                    }

                    // AR Status Badge
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xDD09182C),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x8800E5FF))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF00E5FF), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "RA ACTIVA • Anclaje espacial 1:1 • Arrastre para rotar",
                                color = Color(0xFFE0F7FA),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // 3D / AR Interactive Canvas
                Anatomy3DCanvas(
                    modifier = Modifier.fillMaxSize(),
                    structure = selectedStructure,
                    selectedLayer = selectedLayer,
                    explodedFactor = explodedFactor,
                    bpm = bpm,
                    isHeartBeating = isHeartBeating,
                    isARMode = isARMode,
                    onPinClicked = { pin -> activePin = pin }
                )

                // Top Systems Selector Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = false,
                                onClick = { isFullBodyView = true },
                                label = { Text("← 🧍 Cuerpo", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xCC0D47A1),
                                    labelColor = Color.White
                                )
                            )
                        }
                        items(structures) { item ->
                            val isSelected = item.id == selectedStructure.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedStructure = item
                                    explodedFactor = 0f
                                },
                                label = { Text(item.name, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MedicalTealPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = if (isARMode) Color(0xBB101F33) else DarkNavySurfaceVariant,
                                    labelColor = Color(0xFFECEFF4)
                                )
                            )
                        }
                    }

                    // Layer selection chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        AnatomyLayer.entries.forEach { layer ->
                            val isCurrentLayer = selectedLayer == layer
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isCurrentLayer) Color(0xFF00B4D8) else Color(0x88121D31),
                                modifier = Modifier.clickable { selectedLayer = layer }
                            ) {
                                Text(
                                    text = layer.displayName,
                                    fontSize = 10.sp,
                                    fontWeight = if (isCurrentLayer) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrentLayer) DarkNavyBackground else Color(0xFFB0BEC5),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Bottom Tool Controls (Exploded view & BPM for heart)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(12.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = if (isARMode) Color(0xEE091322) else DarkNavySurface.copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x334DD8EC)),
                    tonalElevation = 6.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Exploded View Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.OpenWith,
                                contentDescription = null,
                                tint = MedicalTealLight,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Despiece Anatómico: ${(explodedFactor * 100).toInt()}%",
                                fontSize = 12.sp,
                                color = Color(0xFFCFD8DC),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Slider(
                            value = explodedFactor,
                            onValueChange = { explodedFactor = it },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = MedicalTealLight,
                                activeTrackColor = MedicalTealPrimary,
                                inactiveTrackColor = Color(0x44FFFFFF)
                            )
                        )

                        // If Heart Model, show BPM pulse controls
                        if (selectedStructure.bpmSupport) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { isHeartBeating = !isHeartBeating },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isHeartBeating) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                            contentDescription = "Latido",
                                            tint = ArterialCrimsonLight
                                        )
                                    }
                                    Text(
                                        text = if (isHeartBeating) "$bpm BPM (Latido Activo)" else "Latido Pausado",
                                        fontSize = 12.sp,
                                        color = Color(0xFFFF859F),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(onClick = { bpm = (bpm - 10).coerceAtLeast(50) }) {
                                        Text("-10", color = MedicalTealLight)
                                    }
                                    TextButton(onClick = { bpm = (bpm + 10).coerceAtMost(140) }) {
                                        Text("+10", color = MedicalTealLight)
                                    }
                                }
                            }
                        }

                        // Interactive Tip Banner
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.TouchApp,
                                contentDescription = null,
                                tint = NeuralGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Toca los puntos dorados para abrir las Perlas Clínicas e irrigación.",
                                fontSize = 11.sp,
                                color = Color(0xFFFFD54F)
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal BottomSheet for Anatomy Pin Details & Clinical Pearl
    if (activePin != null) {
        val pin = activePin!!
        var isBookmarked by remember { mutableStateOf(false) }

        ModalBottomSheet(
            onDismissRequest = { activePin = null },
            containerColor = DarkNavySurface,
            contentColor = Color(0xFFECEFF4)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            color = MedicalTealPrimary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "ESTRUCTURA ANATÓMICA",
                                color = MedicalTealLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = pin.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = pin.latinName,
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            color = Color(0xFFB0BEC5)
                        )
                    }

                    IconButton(
                        onClick = {
                            isBookmarked = !isBookmarked
                            scope.launch {
                                repository.toggleBookmark(pin, selectedStructure.name)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Guardar marcador",
                            tint = NeuralGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = pin.description,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFFCFD8DC)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // High-Yield Clinical Pearl Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E283D)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Lightbulb,
                                contentDescription = null,
                                tint = NeuralGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Perla Clínica & Relevancia MIR/USMLE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = NeuralGold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = pin.clinicalPearl,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFFFFF8E1)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Neurovascular details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF162238)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Irrigación",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = ArterialCrimsonLight
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = pin.irrigation,
                                fontSize = 11.sp,
                                color = Color(0xFFECEFF4)
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF162238)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Inervación",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = NeuralGold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = pin.innervation,
                                fontSize = 11.sp,
                                color = Color(0xFFECEFF4)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons: Ask AI Tutor
                Button(
                    onClick = {
                        activePin = null
                        onConsultAITutor("Explícame la anatomía quirúrgica y perlas clínicas de: ${pin.title} (${pin.latinName})")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MedicalTealPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Profundizar con el Tutor IA Clínico", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Help & Gestures Guide Dialog
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("Controles del Visor 3D y RA", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Rotación: Desliza un dedo sobre la pantalla para rotar en 360°.")
                    Text("• Zoom y Escala: Pellizca con dos dedos para acercar o alejar la estructura.")
                    Text("• Puntos de Interés: Toca cualquier marcador dorado para abrir sus relaciones anatómicas y perlas clínicas.")
                    Text("• Modo RA (Realidad Aumentada): Activa la cámara para proyectar el modelo holográfico sobre una superficie física.")
                    Text("• Despiece: Usa el deslizador inferior para separar las capas y vasos sanguíneos.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("Entendido", color = MedicalTealPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
