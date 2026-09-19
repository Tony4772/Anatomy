package com.example.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import kotlin.math.sqrt

data class BodyBeaconPoint(
    val regionType: BodyRegionType,
    val normalizedX: Float,
    val normalizedY: Float,
    val label: String,
    val color: Color
)

@Composable
fun HumanBody2DCanvas(
    modifier: Modifier = Modifier,
    selectedRegion: BodyRegionInfo?,
    isPosteriorView: Boolean = false,
    showXRayOrgans: Boolean = true,
    onSelectRegion: (BodyRegionInfo) -> Unit
) {
    // Infinite pulse animations for active beacon rings and organ vitality
    val infiniteTransition = rememberInfiniteTransition(label = "humanBody2DAnimation")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beaconPulse"
    )

    val beaconAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beaconAlpha"
    )

    val heartBeatPulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heartBeatPulse"
    )

    // Interactive beacon anchor nodes
    val beacons = remember(isPosteriorView) {
        listOf(
            BodyBeaconPoint(BodyRegionType.HEAD, 0.50f, 0.11f, "Encéfalo", Color(0xFF00E5FF)),
            BodyBeaconPoint(BodyRegionType.THORAX, 0.44f, 0.28f, "Corazón", Color(0xFFFF5252)),
            BodyBeaconPoint(BodyRegionType.THORAX, 0.58f, 0.27f, "Pulmones", Color(0xFF4DD0E1)),
            BodyBeaconPoint(BodyRegionType.ABDOMEN, 0.53f, 0.45f, "Hígado", Color(0xFFFFB74D)),
            BodyBeaconPoint(BodyRegionType.UPPER_LIMBS, 0.19f, 0.36f, "Brazo Izq.", Color(0xFFBA68C8)),
            BodyBeaconPoint(BodyRegionType.UPPER_LIMBS, 0.81f, 0.36f, "Brazo Der.", Color(0xFFBA68C8)),
            BodyBeaconPoint(BodyRegionType.LOWER_LIMBS, 0.38f, 0.72f, "Fémur Izq.", Color(0xFF00E676)),
            BodyBeaconPoint(BodyRegionType.LOWER_LIMBS, 0.62f, 0.72f, "Fémur Der.", Color(0xFF00E676))
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(isPosteriorView) {
                detectTapGestures { tapOffset ->
                    val normX = tapOffset.x / size.width
                    val normY = tapOffset.y / size.height

                    // 1. Check if tap is close to any beacon
                    var closestBeacon: BodyBeaconPoint? = null
                    var minDistance = Float.MAX_VALUE
                    for (beacon in beacons) {
                        val beaconPx = Offset(beacon.normalizedX * size.width, beacon.normalizedY * size.height)
                        val dist = (beaconPx - tapOffset).getDistance()
                        if (dist < 80f && dist < minDistance) {
                            minDistance = dist
                            closestBeacon = beacon
                        }
                    }

                    if (closestBeacon != null) {
                        val reg = BodyRegionRepository.getRegionByType(closestBeacon.regionType)
                        onSelectRegion(reg)
                        return@detectTapGestures
                    }

                    // 2. Check geographical coordinates
                    val region = BodyRegionRepository.findRegionByCoordinates(normX, normY)
                    if (region != null) {
                        onSelectRegion(region)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // Calculate uniform bounds centered in canvas preserving body aspect ratio ~ 1:2.4
            val bodyHeight = canvasH * 0.92f
            val bodyWidth = (bodyHeight * 0.44f).coerceAtMost(canvasW * 0.88f)
            val left = (canvasW - bodyWidth) / 2f
            val top = canvasH * 0.04f

            fun toScreen(nx: Float, ny: Float): Offset {
                return Offset(left + nx * bodyWidth, top + ny * bodyHeight)
            }

            // 1. Holographic Medical Background Grid & Target Rings
            drawMedicalGrid(canvasW, canvasH, toScreen(0.5f, 0.5f))

            // 2. Highlight Aura for currently selected region
            if (selectedRegion != null) {
                drawRegionHighlightAura(
                    selectedRegion = selectedRegion,
                    toScreen = ::toScreen,
                    bodyWidth = bodyWidth,
                    bodyHeight = bodyHeight,
                    pulse = pulseScale
                )
            }

            // 3. Human Body 2D Silhouette
            drawHumanBodySilhouette(
                toScreen = ::toScreen,
                bodyWidth = bodyWidth,
                bodyHeight = bodyHeight,
                isPosterior = isPosteriorView
            )

            // 4. X-Ray Internal Visceral Organs (Anterior View)
            if (showXRayOrgans && !isPosteriorView) {
                drawInternalOrgansXRay(
                    toScreen = ::toScreen,
                    bodyWidth = bodyWidth,
                    bodyHeight = bodyHeight,
                    heartBeatPulse = heartBeatPulse,
                    selectedRegionType = selectedRegion?.type
                )
            } else if (showXRayOrgans && isPosteriorView) {
                // Posterior: Vertebral Column and Spinal Axis
                drawSpineAndPosteriorLandmarks(
                    toScreen = ::toScreen,
                    bodyWidth = bodyWidth,
                    bodyHeight = bodyHeight
                )
            }

            // 5. Region Boundaries & Medical Calibration Ticks
            drawCalibrationGuidelines(
                toScreen = ::toScreen,
                bodyWidth = bodyWidth,
                selectedType = selectedRegion?.type
            )

            // 6. Interactive Glowing Beacon Points
            beacons.forEach { beacon ->
                val center = toScreen(
                    if (isPosteriorView) (1f - beacon.normalizedX) else beacon.normalizedX,
                    beacon.normalizedY
                )
                val isRegionActive = selectedRegion?.type == beacon.regionType
                val radius = if (isRegionActive) 16f * pulseScale else 9f

                // Outer pulsing ring
                drawCircle(
                    color = beacon.color.copy(alpha = if (isRegionActive) 0.35f else 0.15f),
                    radius = radius * 1.6f,
                    center = center
                )
                // Mid solid glowing circle
                drawCircle(
                    color = beacon.color.copy(alpha = if (isRegionActive) beaconAlpha else 0.7f),
                    radius = radius,
                    center = center
                )
                // Inner bright white pinpoint
                drawCircle(
                    color = Color.White,
                    radius = if (isRegionActive) 4.5f else 2.5f,
                    center = center
                )

                // Active crosshair indicator
                if (isRegionActive) {
                    val crossLen = 14f
                    drawLine(
                        color = beacon.color,
                        start = Offset(center.x - crossLen, center.y),
                        end = Offset(center.x + crossLen, center.y),
                        strokeWidth = 1.5f
                    )
                    drawLine(
                        color = beacon.color,
                        start = Offset(center.x, center.y - crossLen),
                        end = Offset(center.x, center.y + crossLen),
                        strokeWidth = 1.5f
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawMedicalGrid(w: Float, h: Float, center: Offset) {
    // Subtle radial gradient backdrop
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x1400E5FF), Color(0x0600E5FF), Color.Transparent),
            center = center,
            radius = w * 0.65f
        ),
        radius = w * 0.65f,
        center = center
    )

    // Concentric medical measurement circles
    drawCircle(
        color = Color(0x124DD8EC),
        radius = w * 0.42f,
        center = center,
        style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f)))
    )
    drawCircle(
        color = Color(0x0A4DD8EC),
        radius = w * 0.58f,
        center = center,
        style = Stroke(width = 1f)
    )

    // Horizontal scanner guide line
    drawLine(
        color = Color(0x184DD8EC),
        start = Offset(center.x - w * 0.45f, center.y),
        end = Offset(center.x + w * 0.45f, center.y),
        strokeWidth = 1f
    )
}

private fun DrawScope.drawRegionHighlightAura(
    selectedRegion: BodyRegionInfo,
    toScreen: (Float, Float) -> Offset,
    bodyWidth: Float,
    bodyHeight: Float,
    pulse: Float
) {
    val (startY, endY) = when (selectedRegion.type) {
        BodyRegionType.HEAD -> Pair(0.04f, 0.20f)
        BodyRegionType.THORAX -> Pair(0.20f, 0.38f)
        BodyRegionType.ABDOMEN -> Pair(0.38f, 0.54f)
        BodyRegionType.UPPER_LIMBS -> Pair(0.22f, 0.55f)
        BodyRegionType.LOWER_LIMBS -> Pair(0.54f, 0.98f)
        BodyRegionType.SPINE -> Pair(0.18f, 0.56f)
    }

    val topPos = toScreen(0.5f, startY)
    val bottomPos = toScreen(0.5f, endY)
    val zoneHeight = bottomPos.y - topPos.y
    val zoneCenterY = (topPos.y + bottomPos.y) / 2f
    val zoneCenterX = topPos.x

    val zoneWidth = when (selectedRegion.type) {
        BodyRegionType.UPPER_LIMBS -> bodyWidth * 0.98f
        BodyRegionType.HEAD -> bodyWidth * 0.42f
        else -> bodyWidth * 0.78f
    }

    // Glowing rounded capsule
    val auraColor = selectedRegion.primaryColor.copy(alpha = 0.12f * pulse)
    val borderColor = selectedRegion.primaryColor.copy(alpha = 0.45f)

    drawRoundRect(
        color = auraColor,
        topLeft = Offset(zoneCenterX - zoneWidth / 2f, zoneCenterY - zoneHeight / 2f),
        size = Size(zoneWidth, zoneHeight),
        cornerRadius = CornerRadius(24f, 24f)
    )

    drawRoundRect(
        color = borderColor,
        topLeft = Offset(zoneCenterX - zoneWidth / 2f, zoneCenterY - zoneHeight / 2f),
        size = Size(zoneWidth, zoneHeight),
        cornerRadius = CornerRadius(24f, 24f),
        style = Stroke(
            width = 1.8f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f))
        )
    )
}

private fun DrawScope.drawHumanBodySilhouette(
    toScreen: (Float, Float) -> Offset,
    bodyWidth: Float,
    bodyHeight: Float,
    isPosterior: Boolean
) {
    val bodyFillBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0x2E162D4A),
            Color(0x3819385C),
            Color(0x2812243A)
        )
    )
    val strokeColor = Color(0x994DD8EC)
    val innerLineColor = Color(0x334DD8EC)
    val silhouetteStroke = Stroke(width = 2.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val thinStroke = Stroke(width = 1.2f)

    // Complete anatomical body outline path
    val path = Path().apply {
        // Start at cranium top vertex
        val headTop = toScreen(0.50f, 0.045f)
        moveTo(headTop.x, headTop.y)

        // Right Cranium & Temple
        val rTemple = toScreen(0.59f, 0.08f)
        val rJaw = toScreen(0.56f, 0.15f)
        val rChin = toScreen(0.52f, 0.175f)
        val chinCenter = toScreen(0.50f, 0.18f)
        cubicTo(rTemple.x, rTemple.y - 12f, rTemple.x, rTemple.y + 10f, rJaw.x, rJaw.y)
        quadraticTo(rChin.x, rChin.y, chinCenter.x, chinCenter.y)

        // Left Chin & Cranium
        val lChin = toScreen(0.48f, 0.175f)
        val lJaw = toScreen(0.44f, 0.15f)
        val lTemple = toScreen(0.41f, 0.08f)
        quadraticTo(lChin.x, lChin.y, lJaw.x, lJaw.y)
        cubicTo(lTemple.x, lTemple.y + 10f, lTemple.x, lTemple.y - 12f, headTop.x, headTop.y)
        close()
    }

    // Torso and Limbs Integrated Path
    val torsoAndLimbs = Path().apply {
        // Neck Right
        val rNeck = toScreen(0.55f, 0.18f)
        moveTo(rNeck.x, rNeck.y)

        // Clavicle & Shoulder Right
        val rShoulder = toScreen(0.68f, 0.22f)
        val rDeltoid = toScreen(0.74f, 0.24f)
        quadraticTo(rShoulder.x, rShoulder.y - 4f, rDeltoid.x, rDeltoid.y)

        // Right Arm (Lateral)
        val rBicep = toScreen(0.79f, 0.32f)
        val rElbow = toScreen(0.82f, 0.40f)
        val rForearm = toScreen(0.85f, 0.47f)
        val rWrist = toScreen(0.87f, 0.52f)
        val rHand = toScreen(0.88f, 0.56f)
        cubicTo(rBicep.x, rBicep.y, rElbow.x, rElbow.y, rForearm.x, rForearm.y)
        quadraticTo(rWrist.x, rWrist.y, rHand.x, rHand.y)

        // Right Hand fingertip curve
        val rHandTip = toScreen(0.865f, 0.575f)
        quadraticTo(rHand.x - 2f, rHand.y + 10f, rHandTip.x, rHandTip.y)

        // Right Arm (Medial)
        val rInnerWrist = toScreen(0.83f, 0.52f)
        val rInnerElbow = toScreen(0.75f, 0.40f)
        val rAxilla = toScreen(0.67f, 0.28f)
        quadraticTo(rInnerWrist.x, rInnerWrist.y, rInnerElbow.x, rInnerElbow.y)
        quadraticTo(rAxilla.x + 8f, rAxilla.y + 20f, rAxilla.x, rAxilla.y)

        // Right Torso / Ribs / Waist
        val rWaist = toScreen(0.65f, 0.42f)
        val rIliac = toScreen(0.67f, 0.52f)
        val rHip = toScreen(0.68f, 0.56f)
        quadraticTo(rWaist.x, rWaist.y, rIliac.x, rIliac.y)
        quadraticTo(rHip.x, rHip.y, rHip.x, rHip.y)

        // Right Leg (Lateral)
        val rThigh = toScreen(0.67f, 0.65f)
        val rKnee = toScreen(0.65f, 0.73f)
        val rCalf = toScreen(0.64f, 0.82f)
        val rAnkle = toScreen(0.62f, 0.92f)
        val rFoot = toScreen(0.64f, 0.97f)
        cubicTo(rThigh.x, rThigh.y, rKnee.x, rKnee.y, rCalf.x, rCalf.y)
        quadraticTo(rAnkle.x, rAnkle.y, rFoot.x, rFoot.y)

        // Right Foot Sole & Heel
        val rToe = toScreen(0.60f, 0.98f)
        val rInnerAnkle = toScreen(0.56f, 0.92f)
        quadraticTo(rFoot.x - 6f, rFoot.y + 4f, rToe.x, rToe.y)
        quadraticTo(rInnerAnkle.x + 4f, rInnerAnkle.y + 8f, rInnerAnkle.x, rInnerAnkle.y)

        // Right Leg (Medial)
        val rInnerCalf = toScreen(0.57f, 0.82f)
        val rInnerKnee = toScreen(0.57f, 0.73f)
        val rGroin = toScreen(0.52f, 0.56f)
        val perineum = toScreen(0.50f, 0.55f)
        quadraticTo(rInnerCalf.x, rInnerCalf.y, rInnerKnee.x, rInnerKnee.y)
        quadraticTo(rGroin.x, rGroin.y, perineum.x, perineum.y)

        // Left Leg (Medial)
        val lGroin = toScreen(0.48f, 0.56f)
        val lInnerKnee = toScreen(0.43f, 0.73f)
        val lInnerCalf = toScreen(0.43f, 0.82f)
        val lInnerAnkle = toScreen(0.44f, 0.92f)
        val lToe = toScreen(0.40f, 0.98f)
        val lFoot = toScreen(0.36f, 0.97f)
        quadraticTo(lGroin.x, lGroin.y, lInnerKnee.x, lInnerKnee.y)
        quadraticTo(lInnerCalf.x, lInnerCalf.y, lInnerAnkle.x, lInnerAnkle.y)
        quadraticTo(lToe.x + 6f, lToe.y + 4f, lFoot.x, lFoot.y)

        // Left Leg (Lateral)
        val lAnkle = toScreen(0.38f, 0.92f)
        val lCalf = toScreen(0.36f, 0.82f)
        val lKnee = toScreen(0.35f, 0.73f)
        val lThigh = toScreen(0.33f, 0.65f)
        val lHip = toScreen(0.32f, 0.56f)
        val lIliac = toScreen(0.33f, 0.52f)
        quadraticTo(lAnkle.x, lAnkle.y, lCalf.x, lCalf.y)
        cubicTo(lCalf.x, lCalf.y, lKnee.x, lKnee.y, lThigh.x, lThigh.y)
        quadraticTo(lHip.x, lHip.y, lIliac.x, lIliac.y)

        // Left Torso / Waist / Axilla
        val lWaist = toScreen(0.35f, 0.42f)
        val lAxilla = toScreen(0.33f, 0.28f)
        quadraticTo(lWaist.x, lWaist.y, lAxilla.x, lAxilla.y)

        // Left Arm (Medial to Lateral)
        val lInnerElbow = toScreen(0.25f, 0.40f)
        val lInnerWrist = toScreen(0.17f, 0.52f)
        val lHandTip = toScreen(0.135f, 0.575f)
        val lHand = toScreen(0.12f, 0.56f)
        val lWrist = toScreen(0.13f, 0.52f)
        val lForearm = toScreen(0.15f, 0.47f)
        val lElbow = toScreen(0.18f, 0.40f)
        val lBicep = toScreen(0.21f, 0.32f)
        val lDeltoid = toScreen(0.26f, 0.24f)
        val lShoulder = toScreen(0.32f, 0.22f)
        val lNeck = toScreen(0.45f, 0.18f)

        quadraticTo(lAxilla.x - 8f, lAxilla.y + 20f, lInnerElbow.x, lInnerElbow.y)
        quadraticTo(lInnerWrist.x, lInnerWrist.y, lHandTip.x, lHandTip.y)
        quadraticTo(lHand.x + 2f, lHand.y + 10f, lHand.x, lHand.y)
        quadraticTo(lWrist.x, lWrist.y, lForearm.x, lForearm.y)
        cubicTo(lForearm.x, lForearm.y, lElbow.x, lElbow.y, lBicep.x, lBicep.y)
        quadraticTo(lDeltoid.x, lDeltoid.y, lShoulder.x, lShoulder.y)
        quadraticTo(lShoulder.x, lShoulder.y - 4f, lNeck.x, lNeck.y)
        close()
    }

    // Fill head and body
    drawPath(path, bodyFillBrush)
    drawPath(torsoAndLimbs, bodyFillBrush)

    // Stroke contours
    drawPath(path, strokeColor, style = silhouetteStroke)
    drawPath(torsoAndLimbs, strokeColor, style = silhouetteStroke)

    // Inner anatomical guidelines (Clavicles, Sternum, Rib arches)
    val clavicleL = toScreen(0.37f, 0.22f)
    val sternumTop = toScreen(0.50f, 0.23f)
    val clavicleR = toScreen(0.63f, 0.22f)
    drawLine(innerLineColor, sternumTop, clavicleL, strokeWidth = thinStroke.width)
    drawLine(innerLineColor, sternumTop, clavicleR, strokeWidth = thinStroke.width)

    if (!isPosterior) {
        // Sternum line
        val sternumBottom = toScreen(0.50f, 0.33f)
        drawLine(innerLineColor, sternumTop, sternumBottom, strokeWidth = thinStroke.width)

        // Subcostal costal margins (inverted V)
        val ribL = toScreen(0.42f, 0.38f)
        val ribR = toScreen(0.58f, 0.38f)
        drawLine(innerLineColor, sternumBottom, ribL, strokeWidth = thinStroke.width)
        drawLine(innerLineColor, sternumBottom, ribR, strokeWidth = thinStroke.width)

        // Umbilicus landmark
        val umbilicus = toScreen(0.50f, 0.46f)
        drawCircle(color = innerLineColor, radius = 3f, center = umbilicus)

        // Patellas (Knee caps)
        val patellaL = toScreen(0.39f, 0.73f)
        val patellaR = toScreen(0.61f, 0.73f)
        drawCircle(color = innerLineColor, radius = 6f, center = patellaL, style = thinStroke)
        drawCircle(color = innerLineColor, radius = 6f, center = patellaR, style = thinStroke)
    }
}

private fun DrawScope.drawInternalOrgansXRay(
    toScreen: (Float, Float) -> Offset,
    bodyWidth: Float,
    bodyHeight: Float,
    heartBeatPulse: Float,
    selectedRegionType: BodyRegionType?
) {
    // 1. Brain Silhouette inside Cranium
    val isHeadActive = selectedRegionType == BodyRegionType.HEAD
    val brainColor = if (isHeadActive) Color(0xFF00E5FF) else Color(0xAA42A5F5)
    val brainCenter = toScreen(0.50f, 0.11f)
    drawOval(
        color = brainColor.copy(alpha = if (isHeadActive) 0.45f else 0.22f),
        topLeft = Offset(brainCenter.x - 22f, brainCenter.y - 20f),
        size = Size(44f, 40f)
    )
    drawOval(
        color = brainColor,
        topLeft = Offset(brainCenter.x - 22f, brainCenter.y - 20f),
        size = Size(44f, 40f),
        style = Stroke(width = 1.4f)
    )

    // 2. Lungs inside Thorax
    val isThoraxActive = selectedRegionType == BodyRegionType.THORAX
    val lungColor = if (isThoraxActive) Color(0xFF4DD0E1) else Color(0x8880DEEA)
    val lungL = toScreen(0.44f, 0.27f)
    val lungR = toScreen(0.56f, 0.27f)
    val lungSize = Size(bodyWidth * 0.16f, bodyHeight * 0.10f)

    drawRoundRect(
        color = lungColor.copy(alpha = if (isThoraxActive) 0.35f else 0.18f),
        topLeft = Offset(lungL.x - lungSize.width / 2f, lungL.y - lungSize.height / 2f),
        size = lungSize,
        cornerRadius = CornerRadius(14f, 14f)
    )
    drawRoundRect(
        color = lungColor,
        topLeft = Offset(lungL.x - lungSize.width / 2f, lungL.y - lungSize.height / 2f),
        size = lungSize,
        cornerRadius = CornerRadius(14f, 14f),
        style = Stroke(width = 1.2f)
    )

    drawRoundRect(
        color = lungColor.copy(alpha = if (isThoraxActive) 0.35f else 0.18f),
        topLeft = Offset(lungR.x - lungSize.width / 2f, lungR.y - lungSize.height / 2f),
        size = lungSize,
        cornerRadius = CornerRadius(14f, 14f)
    )
    drawRoundRect(
        color = lungColor,
        topLeft = Offset(lungR.x - lungSize.width / 2f, lungR.y - lungSize.height / 2f),
        size = lungSize,
        cornerRadius = CornerRadius(14f, 14f),
        style = Stroke(width = 1.2f)
    )

    // 3. Heart with Heartbeat Animation
    val heartColor = if (isThoraxActive) Color(0xFFFF1744) else Color(0xFFD32F2F)
    val heartPos = toScreen(0.48f, 0.29f)
    val heartRadius = 14f * (if (isThoraxActive) heartBeatPulse else 1f)

    drawCircle(
        color = heartColor.copy(alpha = if (isThoraxActive) 0.85f else 0.65f),
        radius = heartRadius,
        center = heartPos
    )
    drawCircle(
        color = Color(0xFFFF8A80),
        radius = heartRadius,
        center = heartPos,
        style = Stroke(width = 1.5f)
    )

    // Aorta arch curve
    val aortaPath = Path().apply {
        moveTo(heartPos.x, heartPos.y - heartRadius)
        quadraticTo(heartPos.x - 6f, heartPos.y - heartRadius - 14f, heartPos.x + 8f, heartPos.y - heartRadius - 16f)
    }
    drawPath(aortaPath, Color(0xFFFF5252), style = Stroke(width = 3.5f, cap = StrokeCap.Round))

    // 4. Liver in Right Hypochondrium
    val isAbdomenActive = selectedRegionType == BodyRegionType.ABDOMEN
    val liverColor = if (isAbdomenActive) Color(0xFFFFB74D) else Color(0x99FFA726)
    val liverPos = toScreen(0.53f, 0.42f)
    val liverSize = Size(bodyWidth * 0.20f, bodyHeight * 0.055f)

    drawRoundRect(
        color = liverColor.copy(alpha = if (isAbdomenActive) 0.55f else 0.25f),
        topLeft = Offset(liverPos.x - liverSize.width / 2f, liverPos.y - liverSize.height / 2f),
        size = liverSize,
        cornerRadius = CornerRadius(10f, 10f)
    )
    drawRoundRect(
        color = liverColor,
        topLeft = Offset(liverPos.x - liverSize.width / 2f, liverPos.y - liverSize.height / 2f),
        size = liverSize,
        cornerRadius = CornerRadius(10f, 10f),
        style = Stroke(width = 1.4f)
    )

    // 5. Femur Bone Axes down the thighs
    val isLowerActive = selectedRegionType == BodyRegionType.LOWER_LIMBS
    val boneColor = if (isLowerActive) Color(0xFFECEFF1) else Color(0x77CFD8DC)
    val boneStroke = Stroke(width = if (isLowerActive) 4f else 2.5f, cap = StrokeCap.Round)

    val rHip = toScreen(0.57f, 0.58f)
    val rKnee = toScreen(0.61f, 0.73f)
    drawLine(boneColor, rHip, rKnee, strokeWidth = boneStroke.width)

    val lHip = toScreen(0.43f, 0.58f)
    val lKnee = toScreen(0.39f, 0.73f)
    drawLine(boneColor, lHip, lKnee, strokeWidth = boneStroke.width)
}

private fun DrawScope.drawSpineAndPosteriorLandmarks(
    toScreen: (Float, Float) -> Offset,
    bodyWidth: Float,
    bodyHeight: Float
) {
    val spineColor = Color(0xAA4DD8EC)
    val spineStroke = Stroke(width = 2.5f, cap = StrokeCap.Round)

    // Vertebral line from cervical C1 to Sacrum
    val spineTop = toScreen(0.50f, 0.16f)
    val spineSacrum = toScreen(0.50f, 0.53f)
    drawLine(spineColor, spineTop, spineSacrum, strokeWidth = spineStroke.width)

    // Vertebral segments
    for (i in 0..12) {
        val yNorm = 0.17f + i * 0.027f
        val pt = toScreen(0.50f, yNorm)
        drawLine(
            color = spineColor,
            start = Offset(pt.x - 8f, pt.y),
            end = Offset(pt.x + 8f, pt.y),
            strokeWidth = 2f
        )
    }

    // Scapulae (Shoulder blades)
    val scapL = toScreen(0.40f, 0.27f)
    val scapR = toScreen(0.60f, 0.27f)
    drawOval(
        color = Color(0x334DD8EC),
        topLeft = Offset(scapL.x - 14f, scapL.y - 18f),
        size = Size(28f, 36f)
    )
    drawOval(
        color = Color(0x334DD8EC),
        topLeft = Offset(scapR.x - 14f, scapR.y - 18f),
        size = Size(28f, 36f)
    )
}

private fun DrawScope.drawCalibrationGuidelines(
    toScreen: (Float, Float) -> Offset,
    bodyWidth: Float,
    selectedType: BodyRegionType?
) {
    val guidelineColor = Color(0x224DD8EC)
    val lineStroke = Stroke(
        width = 1f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f))
    )

    // Horizontal partition lines between anatomical regions
    val lineYs = listOf(0.20f, 0.38f, 0.54f)
    lineYs.forEach { yNorm ->
        val pLeft = toScreen(0.15f, yNorm)
        val pRight = toScreen(0.85f, yNorm)
        drawLine(guidelineColor, pLeft, pRight, strokeWidth = lineStroke.width)
    }
}
