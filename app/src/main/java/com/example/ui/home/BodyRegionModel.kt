package com.example.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.model.AnatomicalSystem

enum class BodyRegionType {
    HEAD,
    THORAX,
    ABDOMEN,
    UPPER_LIMBS,
    LOWER_LIMBS,
    SPINE
}

data class RegionOrganItem(
    val id: String,
    val name: String,
    val latinName: String,
    val system: AnatomicalSystem,
    val badgeText: String,
    val shortDescription: String
)

data class BodyRegionInfo(
    val type: BodyRegionType,
    val name: String,
    val latinName: String,
    val subtitle: String,
    val description: String,
    val clinicalPearl: String,
    val irrigation: String,
    val innervation: String,
    val primaryColor: Color,
    val accentColor: Color,
    val icon: ImageVector,
    val organs: List<RegionOrganItem>,
    // Normalized Y coordinate range for tap hit testing (0.0 to 1.0)
    val minY: Float,
    val maxY: Float,
    // Normalized X coordinate range for tap hit testing
    val minX: Float,
    val maxX: Float
)

object BodyRegionRepository {

    val regions: List<BodyRegionInfo> = listOf(
        BodyRegionInfo(
            type = BodyRegionType.HEAD,
            name = "Cabeza y Cuello",
            latinName = "Caput et Collum",
            subtitle = "Encéfalo, Bóveda Craneal y Pares Craneales",
            description = "Alberga el sistema nervioso central superior, centros vitales de regulación cardiorrespiratoria, órganos sensoriales especializados y las vías aero-digestivas superiores.",
            clinicalPearl = "La tríada de Cushing (bradicardia, hipertensión y respiración irregular) señala hipertensión endocraneal con riesgo inminente de herniación cerebral.",
            irrigation = "Arterias carótidas internas y sistema vértebro-basilar (Polígono de Willis).",
            innervation = "12 pares craneales (I al XII) y plexo cervical (C1-C4).",
            primaryColor = Color(0xFF00E5FF),
            accentColor = Color(0xFF64B5F6),
            icon = Icons.Default.Psychology,
            organs = listOf(
                RegionOrganItem(
                    id = "brain",
                    name = "Encéfalo y Tronco Encefálico",
                    latinName = "Encephalon",
                    system = AnatomicalSystem.NERVOUS,
                    badgeText = "Neuroanatomía",
                    shortDescription = "Corteza telencefálica, diencéfalo, tallo cerebral y cerebelo con líquido cefalorraquídeo."
                )
            ),
            minY = 0.04f,
            maxY = 0.20f,
            minX = 0.35f,
            maxX = 0.65f
        ),
        BodyRegionInfo(
            type = BodyRegionType.THORAX,
            name = "Tórax y Mediastino",
            latinName = "Thorax et Mediastinum",
            subtitle = "Aparato Cardiovascular y Vía Aérea Pulmonar",
            description = "Caja osteocartilaginosa que protege los órganos vitales hemodinámicos y ventilatorios: el corazón en el mediastino medio y ambos pulmones en las cavidades pleurales.",
            clinicalPearl = "El foco aórtico se ausculta en el 2do espacio intercostal derecho paraesternal; el foco mitral en el 5to espacio intercostal izquierdo, línea medioclavicular.",
            irrigation = "Arteria aorta torácica, arterias pulmonares, arterias coronarias y mamarias internas.",
            innervation = "Nervio frénico (C3-C5 para el diafragma), nervios vagos (X) y cadena simpática torácica.",
            primaryColor = Color(0xFFFF5252),
            accentColor = Color(0xFF4DD0E1),
            icon = Icons.Default.Favorite,
            organs = listOf(
                RegionOrganItem(
                    id = "heart",
                    name = "Corazón y Grandes Vasos",
                    latinName = "Cor et Vasa Sanguinea",
                    system = AnatomicalSystem.CARDIOVASCULAR,
                    badgeText = "Cardiovascular",
                    shortDescription = "Bomba muscular de 4 cavidades con arterias coronarias, aorta ascendente y tronco pulmonar."
                ),
                RegionOrganItem(
                    id = "lungs",
                    name = "Pulmones y Árbol Traqueobronquial",
                    latinName = "Pulmones et Arbor Bronchialis",
                    system = AnatomicalSystem.RESPIRATORY,
                    badgeText = "Respiratorio",
                    shortDescription = "3 lóbulos derechos y 2 izquierdos con carina, bronquios fuentes y alvéolos para hematosis."
                )
            ),
            minY = 0.20f,
            maxY = 0.38f,
            minX = 0.28f,
            maxX = 0.72f
        ),
        BodyRegionInfo(
            type = BodyRegionType.ABDOMEN,
            name = "Abdomen y Pelvis",
            latinName = "Abdomen et Pelvis",
            subtitle = "Aparato Digestivo, Hepático y Peritoneo",
            description = "Contiene la glándula más grande del cuerpo (hígado), vías biliares, estómago, bazo, páncreas, intestinos y el sistema venoso portal.",
            clinicalPearl = "El signo de Murphy positivo (detención inspiratoria a la palpación del hipocondrio derecho) es altamente sensible para colecistitis aguda litiásica.",
            irrigation = "Tronco celíaco (hepática, gástrica izquierda, esplénica), mesentérica superior e inferior y vena porta hepática.",
            innervation = "Plexo celíaco, nervios esplácnicos mayores/menores y sistema nervioso entérico.",
            primaryColor = Color(0xFFFFB74D),
            accentColor = Color(0xFFFF9800),
            icon = Icons.Default.MedicalServices,
            organs = listOf(
                RegionOrganItem(
                    id = "digestive",
                    name = "Hígado y Vías Biliares",
                    latinName = "Hepar et Viae Biliares",
                    system = AnatomicalSystem.DIGESTIVE,
                    badgeText = "Digestivo",
                    shortDescription = "Lóbulos derecho e izquierdo divididos por el ligamento falciforme, con vesícula y conducto colédoco."
                )
            ),
            minY = 0.38f,
            maxY = 0.54f,
            minX = 0.30f,
            maxX = 0.70f
        ),
        BodyRegionInfo(
            type = BodyRegionType.UPPER_LIMBS,
            name = "Miembros Superiores",
            latinName = "Membrum Superius",
            subtitle = "Cintura Escapular, Brazo, Antebrazo y Mano",
            description = "Estructuras esqueléticas y musculares diseñadas para la prensión y manipulación de precisión, inervadas por los ramos terminales del plexo braquial.",
            clinicalPearl = "La lesión del nervio radial a nivel del canal de torsión humeral produce la clásica 'mano caída' por parálisis de los músculos extensores.",
            irrigation = "Arteria subclavia, arteria axilar, braquial, radial y cubital formando los arcos palmares.",
            innervation = "Plexo braquial (C5-T1): nervios radial, mediano, cubital, musculocutáneo y axilar.",
            primaryColor = Color(0xFFBA68C8),
            accentColor = Color(0xFFCE93D8),
            icon = Icons.Default.PanTool,
            organs = listOf(
                RegionOrganItem(
                    id = "femur", // Using skeletal system representation
                    name = "Aparato Musculoesquelético Superior",
                    latinName = "Systema Musculosceletale",
                    system = AnatomicalSystem.SKELETAL,
                    badgeText = "Esquelético",
                    shortDescription = "Clavícula, escápula, húmero, cúbito, radio y complejos articulares del hombro y carpo."
                )
            ),
            minY = 0.22f,
            maxY = 0.55f,
            minX = 0.08f,
            maxX = 0.92f
        ),
        BodyRegionInfo(
            type = BodyRegionType.LOWER_LIMBS,
            name = "Miembros Inferiores",
            latinName = "Membrum Inferius",
            subtitle = "Cadera, Fémur, Rodilla, Pierna y Pie",
            description = "Diseñados para la bipedestación, soporte del peso corporal y la marcha biomecánica. Incluye el hueso más largo y resistente del cuerpo humano: el fémur.",
            clinicalPearl = "En fracturas intracapsulares de cuello de fémur existe alto riesgo de necrosis avascular de la cabeza femoral por interrupción de las arterias retinaculares.",
            irrigation = "Arteria ilíaca externa, femoral común, femoral profunda, poplítea, tibiales anterior/posterior y pedíaca.",
            innervation = "Plexo lumbosacro (L1-S4): nervios femoral, obturador y ciático mayor.",
            primaryColor = Color(0xFF00E676),
            accentColor = Color(0xFF69F0AE),
            icon = Icons.AutoMirrored.Filled.DirectionsWalk,
            organs = listOf(
                RegionOrganItem(
                    id = "femur",
                    name = "Fémur y Articulación Coxofemoral",
                    latinName = "Femur et Articulatio Coxae",
                    system = AnatomicalSystem.SKELETAL,
                    badgeText = "Esquelético",
                    shortDescription = "Cabeza, cuello, trocánteres mayor y menor, diáfisis compacta y cóndilos femorales de la rodilla."
                )
            ),
            minY = 0.54f,
            maxY = 0.98f,
            minX = 0.20f,
            maxX = 0.80f
        )
    )

    fun getRegionByType(type: BodyRegionType): BodyRegionInfo {
        return regions.firstOrNull { it.type == type } ?: regions[0]
    }

    fun findRegionByCoordinates(normalizedX: Float, normalizedY: Float): BodyRegionInfo? {
        // Priority order for overlapping or adjacent boundaries
        // 1. Head
        if (normalizedY in 0.03f..0.20f && normalizedX in 0.32f..0.68f) {
            return regions.first { it.type == BodyRegionType.HEAD }
        }
        // 2. Upper Limbs (lateral arms)
        if (normalizedY in 0.20f..0.56f) {
            if (normalizedX < 0.28f || normalizedX > 0.72f) {
                return regions.first { it.type == BodyRegionType.UPPER_LIMBS }
            }
        }
        // 3. Thorax
        if (normalizedY in 0.20f..0.38f && normalizedX in 0.28f..0.72f) {
            return regions.first { it.type == BodyRegionType.THORAX }
        }
        // 4. Abdomen
        if (normalizedY in 0.38f..0.54f && normalizedX in 0.28f..0.72f) {
            return regions.first { it.type == BodyRegionType.ABDOMEN }
        }
        // 5. Lower Limbs
        if (normalizedY in 0.54f..0.98f && normalizedX in 0.18f..0.82f) {
            return regions.first { it.type == BodyRegionType.LOWER_LIMBS }
        }
        return null
    }
}
