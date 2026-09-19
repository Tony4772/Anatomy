package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class AnatomicalSystem(val displayName: String, val iconName: String) {
    CARDIOVASCULAR("Cardiovascular", "favorite"),
    NERVOUS("Neuroanatomía", "psychology"),
    RESPIRATORY("Respiratorio", "air"),
    SKELETAL("Esquelético", "accessibility_new"),
    DIGESTIVE("Digestivo", "medical_services")
}

enum class AnatomyLayer(val displayName: String) {
    ALL("Completo"),
    VASCULAR("Vascular"),
    MUSCULAR("Muscular/Órgano"),
    CROSS_SECTION("Corte Interno")
}

data class Vector3D(
    val x: Float,
    val y: Float,
    val z: Float
) {
    operator fun plus(other: Vector3D) = Vector3D(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vector3D) = Vector3D(x - other.x, y - other.y, z - other.z)
    operator fun times(factor: Float) = Vector3D(x * factor, y * factor, z * factor)
}

data class AnatomyPolygon(
    val vertices: List<Vector3D>,
    val baseColor: Color,
    val layer: AnatomyLayer = AnatomyLayer.ALL,
    val normal: Vector3D = Vector3D(0f, 0f, 1f)
)

data class AnatomyPin(
    val id: String,
    val title: String,
    val latinName: String,
    val position: Vector3D,
    val layer: AnatomyLayer,
    val description: String,
    val clinicalPearl: String,
    val irrigation: String,
    val innervation: String
)

data class AnatomyStructure(
    val id: String,
    val name: String,
    val latinName: String,
    val system: AnatomicalSystem,
    val summary: String,
    val defaultScale: Float = 1.0f,
    val polygons: List<AnatomyPolygon>,
    val pins: List<AnatomyPin>,
    val bpmSupport: Boolean = false,
    val funFact: String
)
