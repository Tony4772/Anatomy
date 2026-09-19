package com.example.ui.ar3d

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.data.model.AnatomyStructure
import com.example.data.model.Vector3D
import kotlin.math.*

data class BodyOrganRegion(
    val structureId: String,
    val name: String,
    val systemLabel: String,
    val position3D: Vector3D,
    val color: Color,
    val hitRadius: Float = 55f
)

@Composable
fun HumanBody3DCanvas(
    modifier: Modifier = Modifier,
    structures: List<AnatomyStructure>,
    selectedStructure: AnatomyStructure?,
    onSelectOrgan: (AnatomyStructure) -> Unit
) {
    var yaw by remember { mutableFloatStateOf(10f) }
    var pitch by remember { mutableFloatStateOf(-5f) }
    var zoomScale by remember { mutableFloatStateOf(0.95f) }
    var panOffset by remember { mutableStateOf(Offset(0f, 20f)) }

    // Pulsing animation for selected organ and heartbeat
    val infiniteTransition = rememberInfiniteTransition(label = "bodyOrganPulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val organGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        zoomScale = (zoomScale * zoomChange).coerceIn(0.5f, 2.5f)
        panOffset += panChange
    }

    // Organ anchor nodes mapped to 3D world space coordinates
    // Origin (0,0,0) is approximately the pelvic center.
    // Head: y ~ +220, Thorax: y ~ +120, Abdomen: y ~ +50, Knees: y ~ -140, Feet: y ~ -260
    val organRegions = remember {
        listOf(
            BodyOrganRegion("brain", "Encéfalo / Cerebro", "Sistema Nervioso", Vector3D(0f, 230f, 0f), Color(0xFF64B5F6)),
            BodyOrganRegion("lungs", "Pulmones & Vía Aérea", "Sistema Respiratorio", Vector3D(28f, 135f, 5f), Color(0xFF4DD0E1)),
            BodyOrganRegion("heart", "Corazón y Grandes Vasos", "Sistema Cardiovascular", Vector3D(-16f, 125f, 20f), Color(0xFFFF5252)),
            BodyOrganRegion("digestive", "Hígado y Vías Biliares", "Aparato Digestivo", Vector3D(24f, 55f, 15f), Color(0xFFFFB74D)),
            BodyOrganRegion("femur", "Fémur y Articulación Coxal", "Sistema Esquelético", Vector3D(38f, -75f, 5f), Color(0xFFE0E0E0))
        )
    }

    // Projected 2D screen offsets for tap detection
    val projectedRegions = remember { mutableStateListOf<Pair<BodyOrganRegion, Offset>>() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .transformable(state = transformableState)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    yaw = (yaw + dragAmount.x * 0.45f) % 360f
                    pitch = (pitch - dragAmount.y * 0.35f).coerceIn(-75f, 75f)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    val clicked = projectedRegions.minByOrNull { (_, screenPos) ->
                        (screenPos - tapOffset).getDistance()
                    }
                    if (clicked != null && (clicked.second - tapOffset).getDistance() <= clicked.first.hitRadius * zoomScale) {
                        val matchingStructure = structures.find { it.id == clicked.first.structureId }
                        if (matchingStructure != null) {
                            onSelectOrgan(matchingStructure)
                        }
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f + panOffset.x, size.height / 2f + panOffset.y)
            val focalLength = 700f
            val radYaw = Math.toRadians(yaw.toDouble()).toFloat()
            val radPitch = Math.toRadians(pitch.toDouble()).toFloat()

            // 1. Draw Medical Background Holographic Circles
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x1A4DD8EC), Color.Transparent),
                    center = center,
                    radius = size.minDimension * 0.5f
                ),
                radius = size.minDimension * 0.45f,
                center = center
            )

            // Medical Coordinate Axis Grid
            val gridRadius = 260f * zoomScale
            drawCircle(
                color = Color(0x224DD8EC),
                radius = gridRadius,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // 3D Projection Helper
            fun project(v: Vector3D): Offset? {
                // Yaw rotation (Y axis)
                val cosY = cos(radYaw)
                val sinY = sin(radYaw)
                val x1 = v.x * cosY + v.z * sinY
                val z1 = -v.x * sinY + v.z * cosY

                // Pitch rotation (X axis)
                val cosP = cos(radPitch)
                val sinP = sin(radPitch)
                val y2 = v.y * cosP - z1 * sinP
                val z2 = v.y * sinP + z1 * cosP

                val distance = 600f - z2
                if (distance <= 50f) return null
                val scale = (focalLength / distance) * zoomScale
                return Offset(center.x + x1 * scale, center.y - y2 * scale)
            }

            // 2. Draw Human Silhouette Skeleton Wireframe
            drawHumanBodyWireframe(::project, zoomScale, radYaw)

            // 3. Draw In-Situ Visceral Organ Geometries
            drawInSituOrgans(::project, pulse, organGlowAlpha, selectedStructure?.id)

            // 4. Update projected region hotspots and draw interactive badges
            projectedRegions.clear()
            organRegions.forEach { organ ->
                val p2d = project(organ.position3D)
                if (p2d != null) {
                    projectedRegions.add(Pair(organ, p2d))
                    val isSelected = selectedStructure?.id == organ.structureId

                    // Draw glowing beacon around organ
                    val radius = if (isSelected) 28f * pulse else 18f
                    drawCircle(
                        color = organ.color.copy(alpha = if (isSelected) 0.35f else 0.18f),
                        radius = radius * zoomScale,
                        center = p2d
                    )
                    drawCircle(
                        color = organ.color,
                        radius = (if (isSelected) 10f else 6f) * zoomScale,
                        center = p2d
                    )
                    drawCircle(
                        color = Color.White,
                        radius = (if (isSelected) 4.5f else 2.5f) * zoomScale,
                        center = p2d
                    )

                    // Target crosshair if selected
                    if (isSelected) {
                        val cross = 18f * zoomScale
                        drawLine(
                            color = organ.color,
                            start = Offset(p2d.x - cross, p2d.y),
                            end = Offset(p2d.x + cross, p2d.y),
                            strokeWidth = 2f
                        )
                        drawLine(
                            color = organ.color,
                            start = Offset(p2d.x, p2d.y - cross),
                            end = Offset(p2d.x, p2d.y + cross),
                            strokeWidth = 2f
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawHumanBodyWireframe(
    project: (Vector3D) -> Offset?,
    zoomScale: Float,
    radYaw: Float
) {
    val boneColor = Color(0x66B0BEC5)
    val spineColor = Color(0x884DD8EC)
    val accentCyan = Color(0x5500E5FF)
    val lineStroke = Stroke(width = 1.8f * zoomScale)
    val thinStroke = Stroke(width = 1.0f * zoomScale)

    fun drawLine3D(v1: Vector3D, v2: Vector3D, color: Color, stroke: Stroke = lineStroke) {
        val p1 = project(v1)
        val p2 = project(v2)
        if (p1 != null && p2 != null) {
            drawLine(color, p1, p2, strokeWidth = stroke.width)
        }
    }

    // A. Head / Cranium Sphere Cage
    val headCenter = Vector3D(0f, 230f, 0f)
    val pHead = project(headCenter)
    if (pHead != null) {
        drawCircle(
            color = Color(0x224DD8EC),
            radius = 32f * zoomScale,
            center = pHead
        )
        drawCircle(
            color = accentCyan,
            radius = 32f * zoomScale,
            center = pHead,
            style = Stroke(width = 1.5f * zoomScale)
        )
    }

    // B. Spine (Vertebral Column Axis)
    val spineNodes = listOf(
        Vector3D(0f, 195f, 0f), // Cervical C1-C7
        Vector3D(0f, 165f, 5f), // Thoracic T1
        Vector3D(0f, 130f, 10f), // Mid Thoracic T6
        Vector3D(0f, 90f, 5f),  // Low Thoracic T12
        Vector3D(0f, 50f, 0f),  // Lumbar L3
        Vector3D(0f, 10f, -5f)  // Sacrum S1
    )
    for (i in 0 until spineNodes.size - 1) {
        drawLine3D(spineNodes[i], spineNodes[i + 1], spineColor, lineStroke)
    }

    // C. Shoulder Girdle & Clavicles
    val lShoulder = Vector3D(-65f, 175f, 0f)
    val rShoulder = Vector3D(65f, 175f, 0f)
    val sternumTop = Vector3D(0f, 165f, 15f)
    drawLine3D(sternumTop, lShoulder, boneColor)
    drawLine3D(sternumTop, rShoulder, boneColor)

    // D. Rib Cage / Thoracic Cage (Bilateral arches)
    val ribLevels = listOf(
        Pair(155f, 48f),
        Pair(140f, 56f),
        Pair(125f, 58f),
        Pair(110f, 54f),
        Pair(95f, 45f)
    )
    ribLevels.forEach { (y, w) ->
        val spinePt = Vector3D(0f, y, -5f)
        val lSternum = Vector3D(-w * 0.7f, y, 16f)
        val rSternum = Vector3D(w * 0.7f, y, 16f)
        val lRibExt = Vector3D(-w, y - 4f, 0f)
        val rRibExt = Vector3D(w, y - 4f, 0f)

        drawLine3D(spinePt, lRibExt, boneColor, thinStroke)
        drawLine3D(lRibExt, lSternum, boneColor, thinStroke)
        drawLine3D(spinePt, rRibExt, boneColor, thinStroke)
        drawLine3D(rRibExt, rSternum, boneColor, thinStroke)
    }

    // E. Arms
    // Left Arm
    val lElbow = Vector3D(-80f, 105f, 0f)
    val lWrist = Vector3D(-90f, 45f, 5f)
    drawLine3D(lShoulder, lElbow, boneColor)
    drawLine3D(lElbow, lWrist, boneColor)

    // Right Arm
    val rElbow = Vector3D(80f, 105f, 0f)
    val rWrist = Vector3D(90f, 45f, 5f)
    drawLine3D(rShoulder, rElbow, boneColor)
    drawLine3D(rElbow, rWrist, boneColor)

    // F. Pelvis (Iliac crests, pubic arch)
    val lPelvis = Vector3D(-42f, 15f, 0f)
    val rPelvis = Vector3D(42f, 15f, 0f)
    val pubis = Vector3D(0f, -15f, 10f)
    val sacrum = Vector3D(0f, 10f, -10f)

    drawLine3D(sacrum, lPelvis, boneColor)
    drawLine3D(sacrum, rPelvis, boneColor)
    drawLine3D(lPelvis, pubis, boneColor)
    drawLine3D(rPelvis, pubis, boneColor)

    // G. Lower Limbs (Femurs, Tibias, Feet)
    // Left Leg
    val lHip = Vector3D(-38f, -5f, 0f)
    val lKnee = Vector3D(-42f, -125f, 0f)
    val lAnkle = Vector3D(-38f, -240f, 0f)
    val lFoot = Vector3D(-38f, -250f, 22f)
    drawLine3D(lHip, lKnee, boneColor, lineStroke)
    drawLine3D(lKnee, lAnkle, boneColor, lineStroke)
    drawLine3D(lAnkle, lFoot, boneColor, lineStroke)

    // Right Leg
    val rHip = Vector3D(38f, -5f, 0f)
    val rKnee = Vector3D(42f, -125f, 0f)
    val rAnkle = Vector3D(38f, -240f, 0f)
    val rFoot = Vector3D(38f, -250f, 22f)
    drawLine3D(rHip, rKnee, boneColor, lineStroke)
    drawLine3D(rKnee, rAnkle, boneColor, lineStroke)
    drawLine3D(rAnkle, rFoot, boneColor, lineStroke)
}

private fun DrawScope.drawInSituOrgans(
    project: (Vector3D) -> Offset?,
    pulse: Float,
    glowAlpha: Float,
    selectedId: String?
) {
    // 1. Brain in Cranium
    val bCenter = project(Vector3D(0f, 230f, 0f))
    if (bCenter != null) {
        val isBrain = selectedId == "brain"
        val bColor = if (isBrain) Color(0xFF64B5F6) else Color(0xAA42A5F5)
        drawCircle(
            color = bColor.copy(alpha = if (isBrain) 0.5f else 0.25f),
            radius = 22f,
            center = bCenter
        )
    }

    // 2. Lungs in Thorax (Bilateral lobes)
    val pLungsL = project(Vector3D(-28f, 130f, 5f))
    val pLungsR = project(Vector3D(28f, 130f, 5f))
    val isLungs = selectedId == "lungs"
    val lungColor = if (isLungs) Color(0xFF4DD0E1) else Color(0x6680DEEA)
    if (pLungsL != null) {
        drawOval(
            color = lungColor,
            topLeft = Offset(pLungsL.x - 16f, pLungsL.y - 25f),
            size = androidx.compose.ui.geometry.Size(32f, 50f)
        )
    }
    if (pLungsR != null) {
        drawOval(
            color = lungColor,
            topLeft = Offset(pLungsR.x - 16f, pLungsR.y - 25f),
            size = androidx.compose.ui.geometry.Size(32f, 50f)
        )
    }

    // 3. Heart (Mediastinum) - Pulsing
    val pHeart = project(Vector3D(-14f, 122f, 18f))
    if (pHeart != null) {
        val isHeart = selectedId == "heart"
        val hColor = if (isHeart) Color(0xFFFF1744) else Color(0xFFE53935)
        val hRadius = (16f * pulse)
        drawCircle(
            color = hColor.copy(alpha = if (isHeart) 0.85f else 0.65f),
            radius = hRadius,
            center = pHeart
        )
    }

    // 4. Liver in Right Hypochondrium
    val pLiver = project(Vector3D(24f, 55f, 15f))
    if (pLiver != null) {
        val isDigestive = selectedId == "digestive"
        val lColor = if (isDigestive) Color(0xFFFFB74D) else Color(0xAAFFA726)
        drawOval(
            color = lColor,
            topLeft = Offset(pLiver.x - 22f, pLiver.y - 12f),
            size = androidx.compose.ui.geometry.Size(44f, 24f)
        )
    }

    // 5. Femur & Hip
    val pFemur = project(Vector3D(38f, -70f, 5f))
    if (pFemur != null) {
        val isFemur = selectedId == "femur"
        val fColor = if (isFemur) Color(0xFFECEFF1) else Color(0x99CFD8DC)
        drawLine(
            color = fColor,
            start = Offset(pFemur.x, pFemur.y - 35f),
            end = Offset(pFemur.x + 4f, pFemur.y + 35f),
            strokeWidth = if (isFemur) 8f else 5f
        )
    }
}
