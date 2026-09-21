package com.example.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnatomicalSystem
import com.example.data.repository.AnatomyRepository
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: AnatomyRepository,
    onNavigateToOrgan: (String) -> Unit,
    onNavigateToQuiz: (AnatomicalSystem?) -> Unit,
    onNavigateToAITutor: (String) -> Unit,
    onNavigateToCertificates: () -> Unit,
    onNavigateToProgress: () -> Unit = {}
) {
    val regions = remember { BodyRegionRepository.regions }
    var selectedRegion by remember { mutableStateOf(regions[0]) } // Default to Head or Thorax
    var isPosteriorView by remember { mutableStateOf(false) }
    var showXRayOrgans by remember { mutableStateOf(true) }
    var showHelpDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MedicalTealPrimary.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, MedicalTealLight),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccessibilityNew,
                                    contentDescription = null,
                                    tint = MedicalTealLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AnatomiMed",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Text(
                                text = "EBYZOM E.I.R.L. · Explorador Anatómico 2D",
                                fontSize = 11.sp,
                                color = MedicalTealLight
                            )
                        }
                    }
                },
                actions = {
                    // Toggle Posterior / Anterior View
                    FilledTonalButton(
                        onClick = { isPosteriorView = !isPosteriorView },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isPosteriorView) Color(0xFF00ACC1) else DarkNavySurfaceVariant,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flip,
                            contentDescription = "Cambiar vista",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPosteriorView) "Dorsal" else "Frontal",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // X-Ray Toggle Icon
                    IconButton(onClick = { showXRayOrgans = !showXRayOrgans }) {
                        Icon(
                            imageVector = if (showXRayOrgans) Icons.Default.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = "Rayos X Órganos",
                            tint = if (showXRayOrgans) MedicalTealLight else Color(0xFF78909C)
                        )
                    }

                    IconButton(onClick = onNavigateToProgress) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Progreso y Firebase",
                            tint = MedicalTealLight
                        )
                    }

                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Ayuda",
                            tint = Color(0xFFB0BEC5)
                        )
                    }
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
                .padding(innerPadding)
                .background(DarkNavyBackground)
        ) {
            // Horizontal Quick Region Selector Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkNavySurface.copy(alpha = 0.5f))
            ) {
                items(regions) { region ->
                    val isSelected = selectedRegion.type == region.type
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedRegion = region },
                        label = {
                            Text(
                                text = region.name,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = region.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = region.primaryColor.copy(alpha = 0.85f),
                            selectedLabelColor = Color.Black,
                            selectedLeadingIconColor = Color.Black,
                            containerColor = DarkNavySurfaceVariant,
                            labelColor = Color(0xFFCFD8DC),
                            iconColor = region.primaryColor
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = if (isSelected) BorderStroke(1.dp, region.primaryColor) else null
                    )
                }
            }

            // Central Main Area: 2D Interactive Body Canvas (takes 52% of screen height)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.05f)
                    .padding(horizontal = 8.dp)
            ) {
                HumanBody2DCanvas(
                    modifier = Modifier.fillMaxSize(),
                    selectedRegion = selectedRegion,
                    isPosteriorView = isPosteriorView,
                    showXRayOrgans = showXRayOrgans,
                    onSelectRegion = { region ->
                        selectedRegion = region
                    }
                )

                // Interactive touch prompt badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xCC0B1626),
                    border = BorderStroke(1.dp, Color(0x334DD8EC)),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = NeuralGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Toca cualquier parte del cuerpo o los puntos clave",
                            fontSize = 11.sp,
                            color = Color(0xFFE0F7FA)
                        )
                    }
                }

                // Region Name Floating Indicator
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = selectedRegion.primaryColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, selectedRegion.primaryColor.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 8.dp, end = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(selectedRegion.primaryColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = selectedRegion.name.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = selectedRegion.primaryColor
                        )
                    }
                }
            }

            // Lower Section: Selected Region Detail & Direct Organ Navigation Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.95f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = DarkNavySurface,
                border = BorderStroke(1.dp, Color(0x334DD8EC)),
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Region Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = selectedRegion.primaryColor.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, selectedRegion.primaryColor.copy(alpha = 0.5f)),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = selectedRegion.icon,
                                    contentDescription = null,
                                    tint = selectedRegion.primaryColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selectedRegion.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = selectedRegion.latinName,
                                    fontSize = 12.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = Color(0xFF90A4AE)
                                )
                            }
                            Text(
                                text = selectedRegion.subtitle,
                                fontSize = 11.sp,
                                color = selectedRegion.primaryColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = selectedRegion.description,
                        fontSize = 12.sp,
                        color = Color(0xFFCFD8DC),
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Clinical Pearl banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x22FFA000),
                        border = BorderStroke(1.dp, Color(0x55FFA000)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = NeuralGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "PERLA CLÍNICA",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeuralGold
                                )
                                Text(
                                    text = selectedRegion.clinicalPearl,
                                    fontSize = 11.sp,
                                    color = Color(0xFFFFECB3),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Contained Organs Section with Direct Navigation
                    Text(
                        text = "SISTEMAS Y ÓRGANOS EN ESTA REGIÓN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MedicalTealLight
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    selectedRegion.organs.forEach { organ ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = DarkNavySurfaceVariant,
                            border = BorderStroke(1.dp, Color(0x334DD8EC)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = selectedRegion.primaryColor.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = organ.badgeText,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = selectedRegion.primaryColor,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = organ.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = organ.shortDescription,
                                        fontSize = 11.sp,
                                        color = Color(0xFFB0BEC5),
                                        lineHeight = 14.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Direct Navigation Button to 3D / AR Viewer
                                Button(
                                    onClick = { onNavigateToOrgan(organ.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MedicalTealPrimary,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ViewInAr,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "3D / RA",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Secondary Action Buttons: AI Tutor and Quiz for this Region
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onNavigateToAITutor("Explícame a fondo la anatomía clínica, irrigación y patologías frecuentes de la región: ${selectedRegion.name} (${selectedRegion.latinName})")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0x664DD8EC)),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MedicalTealLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tutor IA",
                                fontSize = 12.sp,
                                color = MedicalTealLight,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                onNavigateToQuiz(selectedRegion.organs.firstOrNull()?.system)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0x66FFA726)),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Quiz,
                                contentDescription = null,
                                tint = Color(0xFFFFB74D),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Retos Clínicos",
                                fontSize = 12.sp,
                                color = Color(0xFFFFB74D),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    HorizontalDivider(color = Color(0x224DD8EC))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "© ${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)} EBYZOM E.I.R.L.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MedicalTealLight,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "AnatomiMed · Todos los derechos reservados",
                        fontSize = 10.sp,
                        color = Color(0xFF78909C),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    // Help & Usage Dialog
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MedicalTealLight
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guía del Explorador 2D")
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Bienvenido a AnatomiMed:\n\n" +
                                "1. Modelo 2D Interactivo: Toca cualquier región corporal (cabeza, tórax, abdomen o extremidades) para seleccionarla.\n\n" +
                                "2. Puntos Clave (Beacons): Los puntos luminosos pulsan indicando órganos internos vitales.\n\n" +
                                "3. Navegación a 3D / RA: Al tocar el botón '3D / RA' de cada órgano, accederás inmediatamente a su modelo tridimensional con despiece de capas, perlas clínicas y anclaje espacial con Realidad Aumentada.\n\n" +
                                "4. Vistas Frontal y Dorsal: Utiliza el botón superior 'Frontal / Dorsal' para alternar la perspectiva anatómica.",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFFCFD8DC)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Desarrollado por EBYZOM E.I.R.L.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MedicalTealLight
                    )
                    Text(
                        text = "© ${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)} EBYZOM E.I.R.L. Todos los derechos reservados.",
                        fontSize = 11.sp,
                        color = Color(0xFF90A4AE)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("Entendido", color = MedicalTealLight)
                }
            },
            containerColor = DarkNavySurface
        )
    }
}
