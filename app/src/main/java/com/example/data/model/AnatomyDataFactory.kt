package com.example.data.model

import androidx.compose.ui.graphics.Color

object AnatomyDataFactory {

    fun getAllStructures(): List<AnatomyStructure> = listOf(
        createHeartModel(),
        createBrainModel(),
        createRespiratoryModel(),
        createSkeletalModel(),
        createDigestiveModel()
    )

    private fun createHeartModel(): AnatomyStructure {
        val polygons = mutableListOf<AnatomyPolygon>()

        // Ventricles (Muscular body - Deep Crimson)
        val vColor = Color(0xFFC0262D)
        val vColorDark = Color(0xFF98181E)

        // Ventricular apex & lower body
        polygons.add(AnatomyPolygon(listOf(Vector3D(0f, -90f, 0f), Vector3D(-45f, -30f, 35f), Vector3D(0f, -20f, 50f)), vColor, AnatomyLayer.MUSCULAR))
        polygons.add(AnatomyPolygon(listOf(Vector3D(0f, -90f, 0f), Vector3D(0f, -20f, 50f), Vector3D(45f, -30f, 35f)), vColor, AnatomyLayer.MUSCULAR))
        polygons.add(AnatomyPolygon(listOf(Vector3D(0f, -90f, 0f), Vector3D(45f, -30f, 35f), Vector3D(40f, -30f, -30f)), vColorDark, AnatomyLayer.MUSCULAR))
        polygons.add(AnatomyPolygon(listOf(Vector3D(0f, -90f, 0f), Vector3D(40f, -30f, -30f), Vector3D(-40f, -30f, -30f)), vColorDark, AnatomyLayer.MUSCULAR))
        polygons.add(AnatomyPolygon(listOf(Vector3D(0f, -90f, 0f), Vector3D(-40f, -30f, -30f), Vector3D(-45f, -30f, 35f)), vColorDark, AnatomyLayer.MUSCULAR))

        // Mid ventricles
        polygons.add(AnatomyPolygon(listOf(Vector3D(-45f, -30f, 35f), Vector3D(-55f, 25f, 30f), Vector3D(0f, 30f, 45f), Vector3D(0f, -20f, 50f)), vColor, AnatomyLayer.MUSCULAR))
        polygons.add(AnatomyPolygon(listOf(Vector3D(0f, -20f, 50f), Vector3D(0f, 30f, 45f), Vector3D(50f, 25f, 30f), Vector3D(45f, -30f, 35f)), vColor, AnatomyLayer.MUSCULAR))
        polygons.add(AnatomyPolygon(listOf(Vector3D(45f, -30f, 35f), Vector3D(50f, 25f, 30f), Vector3D(45f, 20f, -35f), Vector3D(40f, -30f, -30f)), vColorDark, AnatomyLayer.MUSCULAR))
        polygons.add(AnatomyPolygon(listOf(Vector3D(-40f, -30f, -30f), Vector3D(-45f, 20f, -35f), Vector3D(-55f, 25f, 30f), Vector3D(-45f, -30f, 35f)), vColorDark, AnatomyLayer.MUSCULAR))

        // Atria (Upper Heart - lighter crimson / auricles)
        val atriaColor = Color(0xFFE53935)
        polygons.add(AnatomyPolygon(listOf(Vector3D(-55f, 25f, 30f), Vector3D(-45f, 65f, 15f), Vector3D(-15f, 65f, 25f), Vector3D(0f, 30f, 45f)), atriaColor, AnatomyLayer.MUSCULAR))
        polygons.add(AnatomyPolygon(listOf(Vector3D(0f, 30f, 45f), Vector3D(15f, 65f, 25f), Vector3D(45f, 65f, 15f), Vector3D(50f, 25f, 30f)), atriaColor, AnatomyLayer.MUSCULAR))

        // Aorta (Vascular - Bright Arterial Red/Coral)
        val aortaColor = Color(0xFFFF5252)
        polygons.add(AnatomyPolygon(listOf(Vector3D(-10f, 40f, 10f), Vector3D(-12f, 85f, 12f), Vector3D(8f, 95f, 12f), Vector3D(10f, 45f, 10f)), aortaColor, AnatomyLayer.VASCULAR))
        // Aortic Arch
        polygons.add(AnatomyPolygon(listOf(Vector3D(-12f, 85f, 12f), Vector3D(-15f, 110f, 0f), Vector3D(5f, 115f, -5f), Vector3D(8f, 95f, 12f)), aortaColor, AnatomyLayer.VASCULAR))
        polygons.add(AnatomyPolygon(listOf(Vector3D(5f, 115f, -5f), Vector3D(20f, 105f, -20f), Vector3D(18f, 75f, -25f), Vector3D(8f, 95f, 12f)), aortaColor, AnatomyLayer.VASCULAR))

        // Pulmonary Artery & Trunk (Vascular - Cyan / Blue)
        val pulmColor = Color(0xFF1E88E5)
        polygons.add(AnatomyPolygon(listOf(Vector3D(-5f, 35f, 30f), Vector3D(-25f, 75f, 20f), Vector3D(-10f, 80f, 15f), Vector3D(10f, 40f, 25f)), pulmColor, AnatomyLayer.VASCULAR))
        polygons.add(AnatomyPolygon(listOf(Vector3D(-25f, 75f, 20f), Vector3D(-45f, 75f, -5f), Vector3D(-35f, 65f, -10f), Vector3D(-10f, 80f, 15f)), pulmColor, AnatomyLayer.VASCULAR))

        // Superior Vena Cava (Vascular - Deep Blue)
        val venaColor = Color(0xFF1565C0)
        polygons.add(AnatomyPolygon(listOf(Vector3D(30f, 50f, 5f), Vector3D(32f, 100f, 5f), Vector3D(44f, 98f, 0f), Vector3D(42f, 48f, 0f)), venaColor, AnatomyLayer.VASCULAR))

        // Coronary Artery Sulcus (Golden/Red branch)
        val coronaryColor = Color(0xFFFFD54F)
        polygons.add(AnatomyPolygon(listOf(Vector3D(-5f, 25f, 46f), Vector3D(-15f, -5f, 48f), Vector3D(-12f, -5f, 46f), Vector3D(-2f, 25f, 44f)), coronaryColor, AnatomyLayer.VASCULAR))
        polygons.add(AnatomyPolygon(listOf(Vector3D(-15f, -5f, 48f), Vector3D(-5f, -60f, 30f), Vector3D(-3f, -60f, 28f), Vector3D(-12f, -5f, 46f)), coronaryColor, AnatomyLayer.VASCULAR))

        // Internal cross section septum (Cutaway view)
        val septumColor = Color(0xFF880E4F)
        polygons.add(AnatomyPolygon(listOf(Vector3D(-2f, -70f, 5f), Vector3D(-2f, 20f, 10f), Vector3D(2f, 20f, 10f), Vector3D(2f, -70f, 5f)), septumColor, AnatomyLayer.CROSS_SECTION))

        val pins = listOf(
            AnatomyPin(
                id = "pin_aorta",
                title = "Aorta Ascendente y Cayado",
                latinName = "Arcus aortae",
                position = Vector3D(0f, 105f, 5f),
                layer = AnatomyLayer.VASCULAR,
                description = "Principal arteria del cuerpo humano originada en el ventrículo izquierdo, distribuye sangre oxigenada a la circulación sistémica a través del tronco braquiocefálico, carótida común izquierda y subclavia izquierda.",
                clinicalPearl = "La disección aórtica tipo A de Stanford compromete la aorta ascendente y representa una emergencia quirúrgica absoluta con dolor torácico súbito y desgarrante.",
                irrigation = "Ramas coronarias derecha e izquierda nacen de los senos de Valsalva.",
                innervation = "Plexo cardíaco autónomo y barorreceptores del nervio vago (NC X)."
            ),
            AnatomyPin(
                id = "pin_apex",
                title = "Vértice Cardíaco (Ápex)",
                latinName = "Apex cordis",
                position = Vector3D(0f, -85f, 15f),
                layer = AnatomyLayer.MUSCULAR,
                description = "Punta del corazón orientada hacia abajo, adelante y a la izquierda. Formada íntegramente por la porción inferolateral del ventrículo izquierdo.",
                clinicalPearl = "Corresponde al choque de la punta (PMI: Punto de Máximo Impulso), auscultable en el 5º espacio intercostal izquierdo con línea medioclavicular.",
                irrigation = "Arteria interventricular anterior (rama de la descendente anterior) anastomosa con la interventricular posterior.",
                innervation = "Fibras simpáticas del plexo cardíaco superficial (T1-T4)."
            ),
            AnatomyPin(
                id = "pin_coronary",
                title = "Arteria Coronaria Descendente Anterior",
                latinName = "Ramus interventricularis anterior",
                position = Vector3D(-12f, -10f, 47f),
                layer = AnatomyLayer.VASCULAR,
                description = "Rama terminal más importante de la arteria coronaria izquierda; desciende por el surco interventricular anterior hacia el ápex.",
                clinicalPearl = "Conocida como la 'arteria viuda' o arteria de mayor frecuencia en infartos agudos de miocardio (IAM anterior extenso con elevación del ST en derivaciones V1-V4).",
                irrigation = "Irriga los dos tercios anteriores del tabique interventricular, el haz de His y la pared anterior del ventrículo izquierdo.",
                innervation = "Inervación autónoma perivascular simpática."
            ),
            AnatomyPin(
                id = "pin_mitral",
                title = "Válvula Mitral / Bicúspide",
                latinName = "Valva atrioventricularis sinistra",
                position = Vector3D(-25f, 10f, 15f),
                layer = AnatomyLayer.CROSS_SECTION,
                description = "Válvula aurículoventricular izquierda compuesta por dos valvas (anterior y posterior) ancladas a las cuerdas tendinosas y músculos papilares.",
                clinicalPearl = "El prolapso de la válvula mitral es la causa más común de insuficiencia mitral en países desarrollados, produciendo un clic mesosistólico y soplo telesistólico.",
                irrigation = "Avascular en sus valvas; se nutre por difusión desde el torrente intracardíaco.",
                innervation = "Terminaciones nerviosas sensoriales de estiramiento del endocardio."
            ),
            AnatomyPin(
                id = "pin_cava",
                title = "Vena Cava Superior",
                latinName = "Vena cava superior",
                position = Vector3D(35f, 85f, 5f),
                layer = AnatomyLayer.VASCULAR,
                description = "Gran tronco venoso de 7 cm que drena la sangre desoxigenada de la cabeza, cuello, miembros superiores y tórax hacia la aurícula derecha.",
                clinicalPearl = "El Síndrome de Vena Cava Superior se origina con frecuencia por compresión extrínseca por carcinoma broncogénico de pulmón derecho.",
                irrigation = "Retorno venoso sistémico a 0-4 mmHg de presión venosa central.",
                innervation = "Fibras nerviosas vagales y simpáticas torácicas."
            )
        )

        return AnatomyStructure(
            id = "heart",
            name = "Corazón Humano",
            latinName = "Cor humanum",
            system = AnatomicalSystem.CARDIOVASCULAR,
            summary = "Órgano muscular hueco de 4 cavidades que impulsa 5 litros de sangre por minuto a todo el organismo a través de la circulación mayor y menor.",
            defaultScale = 1.0f,
            polygons = polygons,
            pins = pins,
            bpmSupport = true,
            funFact = "El corazón humano late unas 100.000 veces al día y bombea suficiente sangre en una vida para llenar tres superpetroleros."
        )
    }

    private fun createBrainModel(): AnatomyStructure {
        val polygons = mutableListOf<AnatomyPolygon>()

        // Frontal Lobe (Soft Cyan/Teal)
        val fColor = Color(0xFF26A69A)
        polygons.add(AnatomyPolygon(listOf(Vector3D(-45f, 15f, 40f), Vector3D(-15f, 75f, 35f), Vector3D(15f, 75f, 35f), Vector3D(45f, 15f, 40f)), fColor, AnatomyLayer.MUSCULAR))
        polygons.add(AnatomyPolygon(listOf(Vector3D(-45f, 15f, 40f), Vector3D(-55f, 15f, 0f), Vector3D(-25f, 70f, 0f), Vector3D(-15f, 75f, 35f)), fColor, AnatomyLayer.MUSCULAR))
        polygons.add(AnatomyPolygon(listOf(Vector3D(45f, 15f, 40f), Vector3D(15f, 75f, 35f), Vector3D(25f, 70f, 0f), Vector3D(55f, 15f, 0f)), fColor, AnatomyLayer.MUSCULAR))

        // Parietal Lobe (Amber/Neural Orange)
        val pColor = Color(0xFFFFA726)
        polygons.add(AnatomyPolygon(listOf(Vector3D(-45f, 15f, 40f), Vector3D(-35f, -40f, 45f), Vector3D(0f, -45f, 50f), Vector3D(0f, 20f, 45f)), pColor, AnatomyLayer.MUSCULAR))
        polygons.add(AnatomyPolygon(listOf(Vector3D(0f, 20f, 45f), Vector3D(0f, -45f, 50f), Vector3D(35f, -40f, 45f), Vector3D(45f, 15f, 40f)), pColor, AnatomyLayer.MUSCULAR))

        // Temporal Lobe (Soft Blue)
        val tColor = Color(0xFF42A5F5)
        polygons.add(AnatomyPolygon(listOf(Vector3D(-55f, 15f, 0f), Vector3D(-50f, -30f, -5f), Vector3D(-25f, -30f, -5f), Vector3D(-25f, 15f, 10f)), tColor, AnatomyLayer.MUSCULAR))
        polygons.add(AnatomyPolygon(listOf(Vector3D(55f, 15f, 0f), Vector3D(25f, 15f, 10f), Vector3D(25f, -30f, -5f), Vector3D(50f, -30f, -5f)), tColor, AnatomyLayer.MUSCULAR))

        // Occipital Lobe (Violet)
        val oColor = Color(0xFFAB47BC)
        polygons.add(AnatomyPolygon(listOf(Vector3D(-35f, -40f, 45f), Vector3D(-25f, -75f, 15f), Vector3D(0f, -80f, 20f), Vector3D(0f, -45f, 50f)), oColor, AnatomyLayer.MUSCULAR))
        polygons.add(AnatomyPolygon(listOf(Vector3D(0f, -45f, 50f), Vector3D(0f, -80f, 20f), Vector3D(25f, -75f, 15f), Vector3D(35f, -40f, 45f)), oColor, AnatomyLayer.MUSCULAR))

        // Cerebellum (Suboccipital - Deep Coral)
        val cColor = Color(0xFFEF5350)
        polygons.add(AnatomyPolygon(listOf(Vector3D(-35f, -55f, -10f), Vector3D(-25f, -75f, -30f), Vector3D(0f, -78f, -25f), Vector3D(0f, -50f, -5f)), cColor, AnatomyLayer.MUSCULAR))
        polygons.add(AnatomyPolygon(listOf(Vector3D(0f, -50f, -5f), Vector3D(0f, -78f, -25f), Vector3D(25f, -75f, -30f), Vector3D(35f, -55f, -10f)), cColor, AnatomyLayer.MUSCULAR))

        // Brainstem (Pons & Medulla - Ivory)
        val bsColor = Color(0xFFFFEE58)
        polygons.add(AnatomyPolygon(listOf(Vector3D(-12f, -15f, -25f), Vector3D(-10f, -60f, -50f), Vector3D(10f, -60f, -50f), Vector3D(12f, -15f, -25f)), bsColor, AnatomyLayer.MUSCULAR))

        // Circle of Willis (Vascular Ring - Red)
        val willisColor = Color(0xFFFF1744)
        polygons.add(AnatomyPolygon(listOf(Vector3D(-15f, 5f, -20f), Vector3D(0f, 15f, -18f), Vector3D(15f, 5f, -20f), Vector3D(10f, -15f, -22f), Vector3D(-10f, -15f, -22f)), willisColor, AnatomyLayer.VASCULAR))

        val pins = listOf(
            AnatomyPin(
                id = "pin_broca",
                title = "Área de Broca (Pars triangularis & opercularis)",
                latinName = "Area Broca (Brodmann 44 y 45)",
                position = Vector3D(-42f, 40f, 22f),
                layer = AnatomyLayer.MUSCULAR,
                description = "Ubicada en el giro frontal inferior del hemisferio dominante (generalmente izquierdo). Responsable de la programación motora del lenguaje articulado.",
                clinicalPearl = "La lesión isquémica en la división superior de la arteria cerebral media izquierda causa Afasia de Broca (motora, no fluente, con comprensión preservada pero frustración del paciente).",
                irrigation = "Ramas corticales de la arteria cerebral media (ACM).",
                innervation = "Conectada al área de Wernicke a través del fascículo arqueado."
            ),
            AnatomyPin(
                id = "pin_wernicke",
                title = "Área de Wernicke",
                latinName = "Area Wernicke (Brodmann 22)",
                position = Vector3D(-45f, -25f, 15f),
                layer = AnatomyLayer.MUSCULAR,
                description = "Ubicada en el giro temporal superior posterior. Es el centro primario para la comprensión e interpretación del lenguaje hablado y escrito.",
                clinicalPearl = "Su lesión causa Afasia de Wernicke (fluente pero incomprensible, llena de parafasias fonémicas y semánticas, con anosognosia).",
                irrigation = "División inferior de la arteria cerebral media.",
                innervation = "Corteza de asociación auditiva y visual."
            ),
            AnatomyPin(
                id = "pin_willis",
                title = "Polígono de Willis",
                latinName = "Circulus arteriosus cerebri",
                position = Vector3D(0f, 5f, -19f),
                layer = AnatomyLayer.VASCULAR,
                description = "Anillo arterial anastomótico en la base del encéfalo que conecta la circulación anterior (carotídea interna) con la posterior (vertebrobasilar).",
                clinicalPearl = "Sitio de predilección para aneurismas saculares 'en baya', especialmente en la bifurcación de la comunicante anterior. Su ruptura provoca hemorragia subaracnoidea ('la peor cefalea de la vida').",
                irrigation = "Formado por carótidas internas, cerebrales anteriores, comunicante anterior, comunicantes posteriores y cerebrales posteriores.",
                innervation = "Plexo carotídeo simpático periarterial."
            ),
            AnatomyPin(
                id = "pin_cerebellum",
                title = "Cerebelo",
                latinName = "Cerebellum",
                position = Vector3D(0f, -68f, -20f),
                layer = AnatomyLayer.MUSCULAR,
                description = "Estructura situada en la fosa craneal posterior. Coordina los movimientos voluntarios finos, el tono muscular, el equilibrio y la postura corporal.",
                clinicalPearl = "El síndrome cerebeloso se manifiesta con la tétrada: ataxia de la marcha, dismetría (prueba dedo-nariz alterada), disdiadococinesia y temblor intencional cinético.",
                irrigation = "Arterias cerebelosas: PICA (cerebelosa posteroinferior), AICA y SUCA (cerebelosa superior).",
                innervation = "Vías espinocerebelosas, vestibulares y corticopontocerebelosas."
            )
        )

        return AnatomyStructure(
            id = "brain",
            name = "Encéfalo y Cerebro",
            latinName = "Encephalon",
            system = AnatomicalSystem.NERVOUS,
            summary = "Centro del sistema nervioso central integrado por el telencéfalo, diencéfalo, cerebelo y tronco del encéfalo, coordinando funciones motoras, cognitivas y vitales.",
            defaultScale = 1.0f,
            polygons = polygons,
            pins = pins,
            bpmSupport = false,
            funFact = "El cerebro consume el 20% del oxígeno y glucosa de todo el cuerpo humano a pesar de representar solo el 2% del peso corporal."
        )
    }

    private fun createRespiratoryModel(): AnatomyStructure {
        val polygons = mutableListOf<AnatomyPolygon>()

        // Trachea & Cartilages (Light Cyan)
        val trColor = Color(0xFF80DEEA)
        polygons.add(AnatomyPolygon(listOf(Vector3D(-10f, 90f, 5f), Vector3D(-8f, 30f, 5f), Vector3D(8f, 30f, 5f), Vector3D(10f, 90f, 5f)), trColor, AnatomyLayer.ALL))

        // Main Bronchi (Cyan)
        val brColor = Color(0xFF26C6DA)
        // Right bronchus (more vertical & wider)
        polygons.add(AnatomyPolygon(listOf(Vector3D(0f, 30f, 5f), Vector3D(25f, 5f, 0f), Vector3D(35f, 0f, -5f), Vector3D(8f, 25f, 5f)), brColor, AnatomyLayer.ALL))
        // Left bronchus (longer, more horizontal)
        polygons.add(AnatomyPolygon(listOf(Vector3D(0f, 30f, 5f), Vector3D(-8f, 25f, 5f), Vector3D(-38f, 10f, -5f), Vector3D(-30f, 15f, 0f)), brColor, AnatomyLayer.ALL))

        // Right Lung (3 lobes - Soft Pink/Rose)
        val rlColor = Color(0xFFF48FB1)
        polygons.add(AnatomyPolygon(listOf(Vector3D(20f, 65f, 10f), Vector3D(65f, 25f, 15f), Vector3D(70f, -40f, 10f), Vector3D(25f, -45f, 5f)), rlColor, AnatomyLayer.MUSCULAR))
        polygons.add(AnatomyPolygon(listOf(Vector3D(65f, 25f, 15f), Vector3D(60f, 60f, -10f), Vector3D(20f, 65f, 10f)), rlColor, AnatomyLayer.MUSCULAR))

        // Left Lung (2 lobes with cardiac notch - Soft Pink/Rose)
        val llColor = Color(0xFFF06292)
        polygons.add(AnatomyPolygon(listOf(Vector3D(-20f, 65f, 10f), Vector3D(-65f, 25f, 15f), Vector3D(-68f, -40f, 10f), Vector3D(-25f, -45f, 5f)), llColor, AnatomyLayer.MUSCULAR))
        // Cardiac notch cutaway
        polygons.add(AnatomyPolygon(listOf(Vector3D(-25f, -45f, 5f), Vector3D(-20f, -10f, 12f), Vector3D(-40f, -15f, 10f)), Color(0xFFC2185B), AnatomyLayer.CROSS_SECTION))

        val pins = listOf(
            AnatomyPin(
                id = "pin_carina",
                title = "Carina Traqueal",
                latinName = "Carina tracheae",
                position = Vector3D(0f, 30f, 6f),
                layer = AnatomyLayer.ALL,
                description = "Cresta cartilaginosa anteroposterior en la bifurcación traqueal a nivel de T4-T5 (ángulo esternal de Louis), dividiendo en bronquios principales.",
                clinicalPearl = "Posee la mucosa con mayor densidad de receptores de tos de todo el árbol respiratorio. La distorsión o ensanchamiento de la carina en broncoscopía sugiere metástasis en ganglios subcarínicos.",
                irrigation = "Arterias bronquiales nacientes de la aorta torácica descendente.",
                innervation = "Ramos del nervio vago y cadena simpática torácica."
            ),
            AnatomyPin(
                id = "pin_bronchus_right",
                title = "Bronquio Principal Derecho",
                latinName = "Bronchus principalis dexter",
                position = Vector3D(22f, 8f, 2f),
                layer = AnatomyLayer.ALL,
                description = "Más ancho, corto (2.5 cm) y orientado más verticalmente que el bronquio izquierdo respecto al eje traqueal.",
                clinicalPearl = "Por su disposición verticalizada, es el sitio más frecuente de alojamiento de cuerpos extraños aspirados accidentalmente en niños y adultos inconscientes.",
                irrigation = "Arteria bronquial derecha (rama de la 3ª intercostal posterior o bronquial izquierda).",
                innervation = "Plexo pulmonar anterior y posterior."
            )
        )

        return AnatomyStructure(
            id = "respiratory",
            name = "Árbol Traqueobronquial y Pulmones",
            latinName = "Systema respiratorium",
            system = AnatomicalSystem.RESPIRATORY,
            summary = "Estructura conductora y de intercambio gaseoso compuesta por la tráquea, bronquios lobares, segmentarios y los campos pulmonares derecho e izquierdo.",
            defaultScale = 1.0f,
            polygons = polygons,
            pins = pins,
            bpmSupport = false,
            funFact = "Si se desplegaran todos los alvéolos de los pulmones humanos sobre una superficie plana, cubrirían el tamaño de una cancha de tenis (alrededor de 75 m²)."
        )
    }

    private fun createSkeletalModel(): AnatomyStructure {
        val polygons = mutableListOf<AnatomyPolygon>()
        val boneColor = Color(0xFFFAF0E6)
        val cartilageColor = Color(0xFFB0BEC5)

        // Femur Head (Ivory sphere approximation)
        polygons.add(AnatomyPolygon(listOf(Vector3D(-25f, 75f, 10f), Vector3D(-45f, 85f, 15f), Vector3D(-45f, 65f, -10f), Vector3D(-25f, 55f, -5f)), cartilageColor, AnatomyLayer.ALL))

        // Neck (Colum femoris)
        polygons.add(AnatomyPolygon(listOf(Vector3D(-25f, 75f, 10f), Vector3D(0f, 65f, 10f), Vector3D(0f, 45f, -5f), Vector3D(-25f, 55f, -5f)), boneColor, AnatomyLayer.ALL))

        // Greater Trochanter
        polygons.add(AnatomyPolygon(listOf(Vector3D(0f, 65f, 10f), Vector3D(25f, 70f, 5f), Vector3D(20f, 40f, -10f), Vector3D(0f, 45f, -5f)), boneColor, AnatomyLayer.ALL))

        // Femoral Diaphysis / Shaft
        polygons.add(AnatomyPolygon(listOf(Vector3D(-10f, 45f, 8f), Vector3D(12f, 45f, 8f), Vector3D(10f, -40f, 8f), Vector3D(-10f, -40f, 8f)), boneColor, AnatomyLayer.ALL))
        polygons.add(AnatomyPolygon(listOf(Vector3D(12f, 45f, 8f), Vector3D(10f, 45f, -8f), Vector3D(8f, -40f, -8f), Vector3D(10f, -40f, 8f)), Color(0xFFECE0D1), AnatomyLayer.ALL))

        // Distal Condyles (Medial & Lateral)
        polygons.add(AnatomyPolygon(listOf(Vector3D(-10f, -40f, 8f), Vector3D(-28f, -70f, 12f), Vector3D(0f, -75f, 10f), Vector3D(0f, -40f, 8f)), cartilageColor, AnatomyLayer.ALL))
        polygons.add(AnatomyPolygon(listOf(Vector3D(0f, -40f, 8f), Vector3D(0f, -75f, 10f), Vector3D(28f, -70f, 12f), Vector3D(10f, -40f, 8f)), cartilageColor, AnatomyLayer.ALL))

        val pins = listOf(
            AnatomyPin(
                id = "pin_femur_neck",
                title = "Cuello Anatómico Femoral",
                latinName = "Collum femoris",
                position = Vector3D(-15f, 60f, 8f),
                layer = AnatomyLayer.ALL,
                description = "Segmento óseo piramidal que une la cabeza esférica con la diáfisis femoral en un ángulo cérvico-diafisario promedio de 126°.",
                clinicalPearl = "Las fracturas intracapsulares del cuello femoral en pacientes ancianos lesionan las arterias retinaculares circunflejas femorales, con alto riesgo de necrosis avascular de la cabeza femoral.",
                irrigation = "Arteria circunfleja femoral medial y lateral (ramas de la femoral profunda).",
                innervation = "Ramos articulares del nervio femoral y nervio obturador."
            ),
            AnatomyPin(
                id = "pin_trochanter_major",
                title = "Trocánter Mayor",
                latinName = "Trochanter major",
                position = Vector3D(18f, 60f, 6f),
                layer = AnatomyLayer.ALL,
                description = "Gran eminencia ósea cuadrilátera lateral donde se insertan los principales músculos abductores y rotadores de la cadera (glúteo medio, glúteo menor, piriforme).",
                clinicalPearl = "La debilidad del glúteo medio (lesión del nervio glúteo superior) produce la 'marcha de Trendelenburg' o caída de la hemipelvis contralateral al pararse sobre una pierna.",
                irrigation = "Ramas perforantes y anastomosis trocantérica.",
                innervation = "Inervado periostealmente por el nervio del músculo cuadrado femoral y glúteo superior."
            )
        )

        return AnatomyStructure(
            id = "femur",
            name = "Fémur y Articulación Coxofemoral",
            latinName = "Os femoris",
            system = AnatomicalSystem.SKELETAL,
            summary = "El hueso más largo, pesado y resistente del esqueleto humano; soporta el peso axial corporal y transmite las cargas a la rodilla.",
            defaultScale = 1.0f,
            polygons = polygons,
            pins = pins,
            bpmSupport = false,
            funFact = "El fémur humano es más resistente que una viga de concreto macizo del mismo tamaño y soporta hasta 30 veces el peso corporal durante un salto."
        )
    }

    private fun createDigestiveModel(): AnatomyStructure {
        val polygons = mutableListOf<AnatomyPolygon>()

        // Liver (Hepatic lobes - Reddish Brown)
        val lColor = Color(0xFF795548)
        polygons.add(AnatomyPolygon(listOf(Vector3D(-50f, 35f, 10f), Vector3D(40f, 40f, 15f), Vector3D(50f, 5f, 10f), Vector3D(-30f, 0f, 5f)), lColor, AnatomyLayer.MUSCULAR))
        polygons.add(AnatomyPolygon(listOf(Vector3D(40f, 40f, 15f), Vector3D(55f, 25f, -10f), Vector3D(50f, 5f, 10f)), Color(0xFF5D4037), AnatomyLayer.MUSCULAR))

        // Stomach (C-shape - Warm Beige/Coral)
        val sColor = Color(0xFFFF7043)
        polygons.add(AnatomyPolygon(listOf(Vector3D(-25f, 20f, 15f), Vector3D(-55f, -5f, 12f), Vector3D(-35f, -45f, 8f), Vector3D(0f, -30f, 10f)), sColor, AnatomyLayer.MUSCULAR))

        // Gallbladder (Green pyriform sac)
        val gbColor = Color(0xFF43A047)
        polygons.add(AnatomyPolygon(listOf(Vector3D(10f, 5f, 12f), Vector3D(20f, -15f, 15f), Vector3D(12f, -25f, 12f), Vector3D(4f, -5f, 10f)), gbColor, AnatomyLayer.ALL))

        // Pancreas (Duodenal C loop - Gold/Yellow)
        val pColor = Color(0xFFFFCA28)
        polygons.add(AnatomyPolygon(listOf(Vector3D(0f, -25f, -5f), Vector3D(-45f, -30f, -10f), Vector3D(-40f, -38f, -8f), Vector3D(5f, -35f, -5f)), pColor, AnatomyLayer.CROSS_SECTION))

        val pins = listOf(
            AnatomyPin(
                id = "pin_gallbladder",
                title = "Vesícula Biliar y Vía Biliar",
                latinName = "Vesica biliaris",
                position = Vector3D(15f, -10f, 14f),
                layer = AnatomyLayer.ALL,
                description = "Reservorio piriforme de 50 ml ubicado en la fosa cística de la cara visceral del hígado, drena a través del conducto cístico hacia el colédoco.",
                clinicalPearl = "La impactación de un lito en el bacinete o cístico produce el Signo de Murphy positivo (interrupción dolorosa de la inspiración profunda al palpar el hipocondrio derecho, clásico de colecistitis aguda).",
                irrigation = "Arteria cística (rama de la arteria hepática propia dentro del triángulo hepatobiliar de Calot).",
                innervation = "Fibras del plexo celíaco y nervio frénico derecho (dolor referido a hombro derecho)."
            ),
            AnatomyPin(
                id = "pin_stomach_pylorus",
                title = "Píloro e Incisura Angular",
                latinName = "Pylorus ventriculi",
                position = Vector3D(-5f, -35f, 9f),
                layer = AnatomyLayer.MUSCULAR,
                description = "Esfínter muscular liso que regula el paso del quimo gástrico hacia la primera porción del duodeno.",
                clinicalPearl = "La estenosis hipertrófica del píloro en neonatos se manifiesta entre la 3ª y 6ª semana con vómitos no biliosos 'en proyectil' y palpación de la oliva pilórica en epigastrio.",
                irrigation = "Arteria gástrica derecha y gastroomental derecha.",
                innervation = "Ramos del vago anterior y posterior (nervio de Latarjet)."
            )
        )

        return AnatomyStructure(
            id = "digestive",
            name = "Complejo Hepatobiliar y Gástrico",
            latinName = "Systema digestorium",
            system = AnatomicalSystem.DIGESTIVE,
            summary = "Estructura funcional clave para el procesamiento nutricional, secreción biliar y metabolismo químico, incluyendo el hígado, vesícula biliar y estómago.",
            defaultScale = 1.0f,
            polygons = polygons,
            pins = pins,
            bpmSupport = false,
            funFact = "El hígado es el único órgano visceral humano con capacidad de regenerar masa celular completa a partir de tan solo el 25% del tejido remanente."
        )
    }
}
