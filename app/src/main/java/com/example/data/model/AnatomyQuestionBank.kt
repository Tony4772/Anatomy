package com.example.data.model

object AnatomyQuestionBank {

    fun getSpotterQuestions(): List<QuizQuestion> = listOf(
        QuizQuestion(
            id = "spot_1",
            system = AnatomicalSystem.CARDIOVASCULAR,
            question = "¿Qué estructura anatómica se origina en el seno aórtico izquierdo y desciende por el surco interventricular anterior?",
            clinicalVignette = "Durante una angiografía coronaria de urgencia por infarto agudo:",
            options = listOf(
                "Arteria circunfleja",
                "Arteria descendente anterior izquierda (DA)",
                "Arteria coronaria derecha",
                "Arteria marginal aguda"
            ),
            correctIndex = 1,
            explanation = "La arteria descendente anterior (rama interventricular anterior de la coronaria izquierda) desciende por el surco interventricular anterior hacia el vértice del corazón.",
            clinicalPearl = "Su oclusión es la causa más común de muerte súbita por infarto miocárdico ('arteria viuda').",
            xpReward = 60
        ),
        QuizQuestion(
            id = "spot_2",
            system = AnatomicalSystem.NERVOUS,
            question = "¿En qué giro y lóbulo cerebral se localiza el área motora del lenguaje de Broca?",
            clinicalVignette = "Paciente que comprende todo lo que escucha pero no puede emitir palabras fluidas:",
            options = listOf(
                "Giro temporal superior (área 22)",
                "Giro frontal inferior (áreas 44 y 45 de Brodmann)",
                "Giro precentral (área 4)",
                "Giro angular (área 39)"
            ),
            correctIndex = 1,
            explanation = "El área de Broca se ubica en las porciones opercular y triangular del giro frontal inferior en el hemisferio dominante.",
            clinicalPearl = "Irrigada por la rama cortical superior de la arteria cerebral media.",
            xpReward = 60
        ),
        QuizQuestion(
            id = "spot_3",
            system = AnatomicalSystem.RESPIRATORY,
            question = "¿A qué nivel vertebral se localiza la bifurcación traqueal (carina) en un adulto en decúbito supino?",
            clinicalVignette = "Revisión de radiografía post-intubación endotraqueal:",
            options = listOf(
                "C6 (borde inferior del cartílago cricoides)",
                "T2 (escotadura yugular)",
                "T4-T5 (plano del ángulo esternal de Louis)",
                "T8 (hiato de la vena cava)"
            ),
            correctIndex = 2,
            explanation = "La carina traqueal se encuentra a nivel del disco intervertebral T4-T5, coincidiendo con el ángulo esternal de Louis.",
            clinicalPearl = "El tubo endotraqueal debe quedar colocado a 2-4 cm por encima de la carina para ventilar ambos pulmones equitativamente.",
            xpReward = 60
        ),
        QuizQuestion(
            id = "spot_4",
            system = AnatomicalSystem.SKELETAL,
            question = "¿Qué arterias aportan la irrigación principal a la cabeza femoral y corren gran riesgo de rotura en fracturas del cuello femoral?",
            clinicalVignette = "Paciente de 82 años con caída de su propia altura y dolor agudo en cadera:",
            options = listOf(
                "Arterias retinaculares de la circunfleja femoral medial",
                "Arteria obturatriz del ligamento redondo",
                "Arteria ilíaca interna",
                "Arteria glútea inferior"
            ),
            correctIndex = 0,
            explanation = "Las ramas retinaculares de la arteria circunfleja femoral medial atraviesan la cápsula articular para nutrir la cabeza; en fracturas intracapsulares se desgarran provocando osteonecrosis.",
            clinicalPearl = "En niños, la arteria del ligamento redondo aporta un flujo significativo, pero en adultos es insuficiente para prevenir necrosis.",
            xpReward = 60
        ),
        QuizQuestion(
            id = "spot_5",
            system = AnatomicalSystem.DIGESTIVE,
            question = "¿Cuáles son los límites anatómicos del Triángulo de Calot (hepatobiliar)?",
            clinicalVignette = "Durante una colecistectomía laparoscópica:",
            options = listOf(
                "Conducto cístico, conducto hepático común y borde inferior del hígado",
                "Conducto colédoco, duodeno y vena porta",
                "Vena cava inferior, ligamento falciforme y estómago",
                "Arteria hepática propia, píloro y páncreas"
            ),
            correctIndex = 0,
            explanation = "El triángulo de Calot está delimitado por el conducto cístico (inferior), el conducto hepático común (medial) y la cara inferior del lóbulo derecho del hígado (superior).",
            clinicalPearl = "Su identificación y disección crítica previene la lesión iatrogénica de la vía biliar principal.",
            xpReward = 60
        )
    )

    fun getClinicalCases(): List<QuizQuestion> = listOf(
        QuizQuestion(
            id = "case_1",
            system = AnatomicalSystem.CARDIOVASCULAR,
            question = "Varón de 58 años hipertenso ingresa con dolor retroesternal urente irradiado a la mandíbula y brazo izquierdo. El ECG muestra supradesnivel del ST en DII, DIII y aVF. ¿Qué arteria coronaria es la responsable con mayor probabilidad y qué estructura del sistema de conducción corre riesgo?",
            clinicalVignette = "Caso de Guardia: Síndrome Coronario Agudo con compromiso inferior",
            options = listOf(
                "Arteria coronaria derecha (ACD) y nodo auriculoventricular (AV)",
                "Arteria descendente anterior y rama derecha del haz de His",
                "Arteria circunfleja y músculo papilar anterolateral",
                "Arteria mamaria interna y nodo sinusal"
            ),
            correctIndex = 0,
            explanation = "Las derivaciones DII, DIII y aVF evalúan la cara diafragmática o inferior del corazón, irrigada en el 85-90% de los casos (dominancia derecha) por la arteria coronaria derecha, la cual da la arteria del nodo AV.",
            clinicalPearl = "Vigilar la aparición de bradicardia severa o bloqueo AV de tercer grado.",
            xpReward = 75
        ),
        QuizQuestion(
            id = "case_2",
            system = AnatomicalSystem.NERVOUS,
            question = "Mujer de 64 años con fibrilación auricular presenta súbitamente desviación de la comisura labial hacia la izquierda, hemiplejía fascio-braquio-crural derecha y dificultad severa para comprender el habla. La angiografía revela oclusión embólica. ¿Qué vaso cerebral está afectado?",
            clinicalVignette = "Caso Neurovascular: ACV Isquémico Agudo",
            options = listOf(
                "Arteria cerebral media izquierda (segmento M1)",
                "Arteria cerebral anterior derecha",
                "Arteria basilar en la protuberancia",
                "Arteria cerebral posterior izquierda"
            ),
            correctIndex = 0,
            explanation = "La ACM izquierda irriga la corteza motora y sensitiva de la cara y miembro superior, así como las áreas del lenguaje (Wernicke en temporal superior y Broca en frontal inferior).",
            clinicalPearl = "La afasia global con hemiplejía derecha es el sello clínico del infarto total de la arteria cerebral media dominante.",
            xpReward = 75
        ),
        QuizQuestion(
            id = "case_3",
            system = AnatomicalSystem.SKELETAL,
            question = "Joven motociclista de 22 años sufre caída sobre el hombro derecho en abducción forzada. Presenta pérdida de la sensibilidad en la cara lateral del muñón del hombro (área del 'escudete') y debilidad para iniciar la abducción. ¿Qué nervio del plexo braquial está lesionado?",
            clinicalVignette = "Traumatología de Urgencias: Luxación anterior glenohumeral",
            options = listOf(
                "Nervio axilar (circunflejo)",
                "Nervio musculocutáneo",
                "Nervio radial",
                "Nervio supraescapular"
            ),
            correctIndex = 0,
            explanation = "El nervio axilar rodea el cuello quirúrgico del húmero junto a los vasos circunflejos humerales posteriores e inerva el músculo deltoides y la piel suprayacente.",
            clinicalPearl = "Siempre explorar la sensibilidad táctil del área deltoidea antes y después de reducir una luxación anterior de hombro.",
            xpReward = 75
        )
    )

    fun getFinalCertificationExam(): List<QuizQuestion> = listOf(
        QuizQuestion(
            id = "cert_1",
            system = AnatomicalSystem.CARDIOVASCULAR,
            question = "¿Dónde se encuentra anatómicamente el nodo sinoauricular (marcapasos fisiológico del corazón)?",
            options = listOf(
                "En la unión de la vena cava superior y la aurícula derecha",
                "En el triángulo de Koch en el tabique interauricular",
                "En la cúspide de la válvula tricúspide",
                "En el tabique interventricular membranoso"
            ),
            correctIndex = 0,
            explanation = "El nodo sinusal (Keith-Flack) se ubica subepicárdicamente en la pared posterolateral alta de la aurícula derecha, en la desembocadura de la VCS.",
            clinicalPearl = "Irrigado por la arteria del nodo sinusal (60% rama de ACD, 40% de circunfleja).",
            xpReward = 80
        ),
        QuizQuestion(
            id = "cert_2",
            system = AnatomicalSystem.NERVOUS,
            question = "¿Cuál es el vaso que comunica la arteria basilar con la arteria carótida interna para cerrar el polígono de Willis?",
            options = listOf(
                "Arteria comunicante posterior",
                "Arteria cerebral anterior",
                "Arteria comunicante anterior",
                "Arteria laberíntica"
            ),
            correctIndex = 0,
            explanation = "La arteria comunicante posterior se extiende desde la carótida interna hasta la arteria cerebral posterior, uniendo la circulación carotídea con la vertebrobasilar.",
            clinicalPearl = "Los aneurismas de la arteria comunicante posterior causan parálisis del III par craneal (motor ocular común) con midriasis precoz.",
            xpReward = 80
        ),
        QuizQuestion(
            id = "cert_3",
            system = AnatomicalSystem.RESPIRATORY,
            question = "¿Cuál de las siguientes afirmaciones sobre la pleura parietal es correcta?",
            options = listOf(
                "Posee inervación somática dolorosa exquisita a través de nervios intercostales y frénicos",
                "Es completamente insensible al dolor",
                "Cubre estrechamente el parénquima pulmonar entrando en las cisuras",
                "No produce líquido pleural"
            ),
            correctIndex = 0,
            explanation = "La pleura parietal es ricamente inervada por nervios somáticos (dolor pleurítico punzante en inspiración); la pleura visceral carece de inervación para el dolor.",
            clinicalPearl = "La irritación de la pleura diafragmática central inervada por el nervio frénico (C3-C5) causa dolor referido al hombro.",
            xpReward = 80
        ),
        QuizQuestion(
            id = "cert_4",
            system = AnatomicalSystem.DIGESTIVE,
            question = "¿Qué vaso sanguíneo discurre inmediatamente posterior al cuello del páncreas y se forma por la confluencia de la vena mesentérica superior y la vena esplénica?",
            options = listOf(
                "Vena Porta Hepática",
                "Vena Cava Inferior",
                "Vena Renal Izquierda",
                "Vena Ácigos"
            ),
            correctIndex = 0,
            explanation = "La vena porta se forma detrás del cuello pancreático por la unión de la VMS y la vena esplénica.",
            clinicalPearl = "Los tumores del cuello pancreático invaden precozmente la vena porta, determinando su irresecabilidad quirúrgica.",
            xpReward = 80
        ),
        QuizQuestion(
            id = "cert_5",
            system = AnatomicalSystem.SKELETAL,
            question = "¿Qué ligamento intracapsular de la rodilla previene el desplazamiento anterior de la tibia sobre el fémur?",
            options = listOf(
                "Ligamento Cruzado Anterior (LCA)",
                "Ligamento Cruzado Posterior (LCP)",
                "Ligamento Colateral Medial",
                "Ligamento Patelar"
            ),
            correctIndex = 0,
            explanation = "El LCA se inserta en el área intercondílea anterior tibial y en la cara medial del cóndilo femoral lateral. Se evalúa con la prueba de Lachman.",
            clinicalPearl = "La tríada desgraciada de O'Donoghue incluye lesión del LCA, ligamento colateral medial y menisco medial.",
            xpReward = 80
        )
    )
}
