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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.example.data.model.*
import kotlin.math.*

@Composable
fun Anatomy3DCanvas(
    modifier: Modifier = Modifier,
    structure: AnatomyStructure,
    selectedLayer: AnatomyLayer = AnatomyLayer.ALL,
    explodedFactor: Float = 0f,
    bpm: Int = 72,
    isHeartBeating: Boolean = true,
    isARMode: Boolean = false,
    onPinClicked: (AnatomyPin) -> Unit = {}
) {
    var yaw by remember(structure.id) { mutableFloatStateOf(15f) }
    var pitch by remember(structure.id) { mutableFloatStateOf(-10f) }
    var zoomScale by remember(structure.id) { mutableFloatStateOf(structure.defaultScale) }
    var panOffset by remember(structure.id) { mutableStateOf(Offset.Zero) }

    // Heart beat pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "heartBeat")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (structure.bpmSupport && isHeartBeating) 1.08f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (60000 / bpm.coerceIn(40, 160)).toInt(),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Hologram scanner line for AR Mode
    val scanLineProgress by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan"
    )

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        zoomScale = (zoomScale * zoomChange).coerceIn(0.4f, 3.5f)
        panOffset += panChange
    }

    // Cache projected pin locations for tap detection
    val projectedPins = remember { mutableStateListOf<Pair<AnatomyPin, Offset>>() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .transformable(state = transformableState)
            .pointerInput(structure.id) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    yaw = (yaw + dragAmount.x * 0.5f) % 360f
                    pitch = (pitch - dragAmount.y * 0.4f).coerceIn(-85f, 85f)
                }
            }
            .pointerInput(structure.id) {
                detectTapGestures { tapOffset ->
                    val clicked = projectedPins.firstOrNull { (_, offset) ->
                        (offset - tapOffset).getDistance() <= 40f
                    }
                    if (clicked != null) {
                        onPinClicked(clicked.first)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f + panOffset.x, size.height / 2f + panOffset.y)
            val focalLength = 600f
            val radYaw = Math.toRadians(yaw.toDouble()).toFloat()
            val radPitch = Math.toRadians(pitch.toDouble()).toFloat()

            // Draw AR Spatial Floor Grid if in AR Mode
            if (isARMode) {
                drawARGrid(center, size.width, size.height, scanLineProgress)
            }

            // Filter polygons by selected layer
            val filteredPolygons = structure.polygons.filter {
                selectedLayer == AnatomyLayer.ALL || it.layer == selectedLayer || it.layer == AnatomyLayer.ALL
            }

            // Transform vertices with rotation, pulse, and exploded view
            data class TransformedPolygon(
                val screenPoints: List<Offset>,
                val avgZ: Float,
                val shadedColor: Color
            )

            val lightDir = Vector3D(0.4f, 0.7f, 0.6f).let {
                val len = sqrt(it.x * it.x + it.y * it.y + it.z * it.z)
                Vector3D(it.x / len, it.y / len, it.z / len)
            }

            val projectedPolys = mutableListOf<TransformedPolygon>()

            for (poly in filteredPolygons) {
                val transformedVertices = poly.vertices.map { v ->
                    // Exploded view radial push
                    val radialDir = if (poly.layer == AnatomyLayer.VASCULAR || poly.layer == AnatomyLayer.CROSS_SECTION) {
                        Vector3D(v.x * 0.8f, v.y * 0.2f, v.z * 0.8f)
                    } else {
                        Vector3D(0f, 0f, 0f)
                    }
                    val pos = Vector3D(
                        v.x * pulseScale + radialDir.x * explodedFactor,
                        v.y * pulseScale + radialDir.y * explodedFactor,
                        v.z * pulseScale + radialDir.z * explodedFactor
                    )
                    rotate3D(pos, radYaw, radPitch)
                }

                // Normal vector calculation
                val normal = if (transformedVertices.size >= 3) {
                    val a = transformedVertices[1] - transformedVertices[0]
                    val b = transformedVertices[2] - transformedVertices[0]
                    val nx = a.y * b.z - a.z * b.y
                    val ny = a.z * b.x - a.x * b.z
                    val nz = a.x * b.y - a.y * b.x
                    val len = sqrt(nx * nx + ny * ny + nz * nz).coerceAtLeast(0.0001f)
                    Vector3D(nx / len, ny / len, nz / len)
                } else Vector3D(0f, 0f, 1f)

                // Lambertian shading
                val dot = (normal.x * lightDir.x + normal.y * lightDir.y + normal.z * lightDir.z).coerceIn(-0.3f, 1f)
                val intensity = (0.45f + 0.55f * max(0f, dot)).coerceIn(0.25f, 1.0f)

                val shadedColor = Color(
                    red = (poly.baseColor.red * intensity).coerceIn(0f, 1f),
                    green = (poly.baseColor.green * intensity).coerceIn(0f, 1f),
                    blue = (poly.baseColor.blue * intensity).coerceIn(0f, 1f),
                    alpha = if (isARMode) 0.85f else 0.95f
                )

                val screenPoints = transformedVertices.map { v ->
                    val factor = focalLength / (focalLength + v.z + 150f) * zoomScale
                    Offset(
                        x = center.x + v.x * factor * 2.2f,
                        y = center.y - v.y * factor * 2.2f
                    )
                }

                val avgZ = transformedVertices.map { it.z }.average().toFloat()
                projectedPolys.add(TransformedPolygon(screenPoints, avgZ, shadedColor))
            }

            // Painter's algorithm: draw farthest polygons first
            projectedPolys.sortBy { it.avgZ }

            for (poly in projectedPolys) {
                if (poly.screenPoints.size >= 3) {
                    val path = Path().apply {
                        moveTo(poly.screenPoints[0].x, poly.screenPoints[0].y)
                        for (i in 1 until poly.screenPoints.size) {
                            lineTo(poly.screenPoints[i].x, poly.screenPoints[i].y)
                        }
                        close()
                    }
                    drawPath(path, poly.shadedColor)
                    // Edge wireframe highlight
                    drawPath(
                        path,
                        color = if (isARMode) Color(0x664DD8EC) else Color(0x33FFFFFF),
                        style = Stroke(width = 1.2f)
                    )
                }
            }

            // Project Anatomy Hotspots / Pins
            projectedPins.clear()
            val visiblePins = structure.pins.filter {
                selectedLayer == AnatomyLayer.ALL || it.layer == selectedLayer || it.layer == AnatomyLayer.ALL
            }

            for (pin in visiblePins) {
                val rot = rotate3D(pin.position * pulseScale, radYaw, radPitch)
                // Cull pins that are facing away on the back hemisphere
                if (rot.z > -80f) {
                    val factor = focalLength / (focalLength + rot.z + 150f) * zoomScale
                    val pinOffset = Offset(
                        x = center.x + rot.x * factor * 2.2f,
                        y = center.y - rot.y * factor * 2.2f
                    )
                    projectedPins.add(pin to pinOffset)
                    drawHotspotPin(pin, pinOffset, isARMode)
                }
            }
        }
    }
}

private fun rotate3D(v: Vector3D, yaw: Float, pitch: Float): Vector3D {
    // Rotate around Y axis (Yaw)
    val cosY = cos(yaw)
    val sinY = sin(yaw)
    val x1 = v.x * cosY + v.z * sinY
    val y1 = v.y
    val z1 = -v.x * sinY + v.z * cosY

    // Rotate around X axis (Pitch)
    val cosP = cos(pitch)
    val sinP = sin(pitch)
    val x2 = x1
    val y2 = y1 * cosP - z1 * sinP
    val z2 = y1 * sinP + z1 * cosP

    return Vector3D(x2, y2, z2)
}

private fun DrawScope.drawHotspotPin(pin: AnatomyPin, offset: Offset, isARMode: Boolean) {
    val haloColor = if (isARMode) Color(0x5500E5FF) else Color(0x66FFB300)
    val dotColor = if (isARMode) Color(0xFF00E5FF) else Color(0xFFFFB300)

    // Outer pulsating halo ring
    drawCircle(color = haloColor, radius = 20f, center = offset)
    drawCircle(color = Color.White, radius = 9f, center = offset)
    drawCircle(color = dotColor, radius = 6f, center = offset)

    // Directional pin pointer line
    drawLine(
        color = dotColor,
        start = offset,
        end = Offset(offset.x + 18f, offset.y - 18f),
        strokeWidth = 2f
    )
    drawLine(
        color = dotColor,
        start = Offset(offset.x + 18f, offset.y - 18f),
        end = Offset(offset.x + 55f, offset.y - 18f),
        strokeWidth = 2f
    )
}

private fun DrawScope.drawARGrid(center: Offset, width: Float, height: Float, scanProgress: Float) {
    val gridColor = Color(0x2200E5FF)
    val floorY = center.y + 160f

    // Concentric spatial perspective rings
    for (r in listOf(80f, 150f, 220f, 290f)) {
        drawOval(
            color = gridColor,
            topLeft = Offset(center.x - r, floorY - r * 0.35f),
            size = androidx.compose.ui.geometry.Size(r * 2f, r * 0.7f),
            style = Stroke(width = 1.5f)
        )
    }

    // Grid crosslines
    drawLine(
        color = gridColor,
        start = Offset(center.x - 300f, floorY),
        end = Offset(center.x + 300f, floorY),
        strokeWidth = 1.5f
    )

    // Scanning vertical bar
    val scanY = center.y + scanProgress * 200f
    drawLine(
        color = Color(0x4400E5FF),
        start = Offset(center.x - 220f, scanY),
        end = Offset(center.x + 220f, scanY),
        strokeWidth = 2.5f
    )
}
