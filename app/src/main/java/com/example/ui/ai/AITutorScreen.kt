package com.example.ui.ai

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.BuildConfig
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class ChatMessage(
    val id: String,
    val sender: String, // "user" or "ai"
    val message: String,
    val timestamp: String = "Justo ahora"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AITutorScreen(
    initialPrompt: String? = null
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf(initialPrompt ?: "") }
    var isGenerating by remember { mutableStateOf(false) }

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                id = "m_init",
                sender = "ai",
                message = "¡Hola, futuro colega médico! Soy el **Dr. AnatomiMed**, tu asesor especializado en anatomía humana y correlaciones clínico-quirúrgicas. ¿Qué estructura, triángulo anatómico, vía nerviosa o caso clínico deseas explorar hoy?"
            )
        )
    }

    val quickQuestions = listOf(
        "Polígono de Willis y aneurismas",
        "Triángulo de Calot y colecistectomía",
        "Irrigación del infarto de miocardio",
        "Pares craneales y mnemotecnias",
        "Fractura de cuello femoral y necrosis"
    )

    fun sendMessage(query: String) {
        if (query.isBlank() || isGenerating) return

        messages.add(ChatMessage(id = "user_${System.currentTimeMillis()}", sender = "user", message = query))
        inputText = ""
        isGenerating = true

        scope.launch {
            val responseText = queryGeminiOrOffline(query)
            messages.add(
                ChatMessage(
                    id = "ai_${System.currentTimeMillis()}",
                    sender = "ai",
                    message = responseText
                )
            )
            isGenerating = false
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(initialPrompt) {
        if (!initialPrompt.isNullOrBlank()) {
            sendMessage(initialPrompt)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0x3300B4D8), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MedicalTealLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Dr. AnatomiMed (Tutor IA)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Anatomía quirúrgica y correlación clínica",
                                fontSize = 11.sp,
                                color = MedicalTealLight
                            )
                        }
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
        ) {
            // Quick question pills
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                items(quickQuestions) { q ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = DarkNavySurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x334DD8EC)),
                        modifier = Modifier.clickable { sendMessage(q) }
                    ) {
                        Text(
                            text = q,
                            fontSize = 11.sp,
                            color = Color(0xFFE0F7FA),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Chat Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubble(message = msg)
                }

                if (isGenerating) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MedicalTealLight,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "El Dr. AnatomiMed está analizando la consulta clínica...",
                                fontSize = 12.sp,
                                color = Color(0xFF90A4AE)
                            )
                        }
                    }
                }
            }

            // Chat Input Bar
            Surface(
                color = DarkNavySurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x224DD8EC)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Pregunta sobre reparos, irrigación o clínica...", fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MedicalTealLight,
                            unfocusedBorderColor = Color(0x334DD8EC)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { sendMessage(inputText) },
                        enabled = inputText.isNotBlank() && !isGenerating,
                        modifier = Modifier
                            .background(
                                if (inputText.isNotBlank() && !isGenerating) MedicalTealPrimary else Color(0x334DD8EC),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Enviar",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.sender == "user"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) MedicalTealPrimary else DarkNavySurface,
            border = if (!isUser) androidx.compose.foundation.BorderStroke(1.dp, Color(0x224DD8EC)) else null,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (!isUser) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalHospital,
                            contentDescription = null,
                            tint = MedicalTealLight,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Dr. AnatomiMed • Asesor Clínico",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedicalTealLight
                        )
                    }
                }
                Text(
                    text = message.message,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = Color.White
                )
            }
        }
    }
}

private suspend fun queryGeminiOrOffline(prompt: String): String = withContext(Dispatchers.IO) {
    val apiKey = try {
        BuildConfig.GEMINI_API_KEY
    } catch (e: Exception) {
        ""
    }

    if (apiKey.isNotBlank() && apiKey != "YOUR_GEMINI_API_KEY") {
        try {
            val urlString = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 8000
            conn.readTimeout = 12000

            val systemInstruction = "Eres el Dr. AnatomiMed, un eminente profesor y cirujano experto en anatomía humana aplicada y educación médica. Responde siempre a los estudiantes de medicina en español con terminología anatómica precisa (incluye nombres en latín de la Terminología Anatómica Internacional), correlaciones clínicas de alto rendimiento para exámenes como MIR / USMLE / ENARM, perlas quirúrgicas y mnemotecnias mnemónicas útiles."

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "$systemInstruction\n\nConsulta del estudiante: $prompt")
                            })
                        })
                    })
                })
            }

            OutputStreamWriter(conn.outputStream).use { it.write(jsonBody.toString()) }

            if (conn.responseCode == 200) {
                val response = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                val jsonObj = JSONObject(response)
                val candidates = jsonObj.getJSONArray("candidates")
                if (candidates.length() > 0) {
                    val parts = candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts")
                    return@withContext parts.getJSONObject(0).getString("text")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // High-Yield Offline Clinical Knowledge Engine fallback
    val lower = prompt.lowercase()
    when {
        lower.contains("willis") || lower.contains("cerebro") || lower.contains("aneurisma") -> {
            """
            🧠 **Polígono de Willis (Circulus arteriosus cerebri)**:
            
            • **Composición Anatómica**:
              1. **Arterias Carótidas Internas**: Aportan el flujo carotídeo anterior.
              2. **Arterias Cerebrales Anteriores (ACA)**: Unidas por la arteria comunicante anterior.
              3. **Arterias Comunicantes Posteriores**: Unen la carótida interna con la cerebral posterior.
              4. **Arterias Cerebrales Posteriores (ACP)**: Bifurcación terminal de la arteria basilar.
            
            • **Perla Clínica / MIR**:
              El sitio más frecuente de **aneurismas saculares intracraneales** (85-90%) es en la circulación anterior, específicamente en la unión de la comunicante anterior con la cerebral anterior. Su rotura produce **Hemorragia Subaracnoidea (HSA)** espontánea, descrita clásicamente como *"la peor cefalea de toda mi vida"*, acompañada de rigidez de nuca y punción lumbar con líquido xantocrómico.
            """.trimIndent()
        }
        lower.contains("calot") || lower.contains("vesícula") || lower.contains("biliar") -> {
            """
            🔬 **Triángulo Hepatobiliar de Calot (Anatomía Quirúrgica)**:
            
            • **Límites Clásicos**:
              - **Inferior / Lateral**: Conducto cístico.
              - **Medial**: Conducto hepático común.
              - **Superior**: Cara visceral del lóbulo derecho del hígado.
            
            • **Contenido Crítico**:
              - **Arteria Cística** (habitualmente rama de la arteria hepática derecha).
              - **Ganglio linfático de Calot / Mascagni**.
            
            • **Relevancia Quirúrgica**:
              En toda colecistectomía laparoscópica es obligatorio obtener la **Visión Crítica de Seguridad de Strasberg**: disecar el fondo del triángulo de Calot hasta observar únicamente 2 estructuras entrando a la vesícula (el conducto cístico y la arteria cística) antes de grapar o seccionar. Esto previene la temida lesión iatrogénica de la vía biliar principal.
            """.trimIndent()
        }
        lower.contains("corazón") || lower.contains("infarto") || lower.contains("coronaria") -> {
            """
            ❤️ **Irrigación Coronaria y Correlación de Infarto (IAM)**:
            
            • **Arteria Descendente Anterior (DA / Coronaria Izquierda)**:
              - Irriga pared anterior del ventrículo izquierdo y 2/3 anteriores del tabique interventricular.
              - ECG: Supradesnivel del ST en V1, V2, V3 y V4 (IAM anterior extenso).
              - Apodo: *"The Widow Maker"* por su alta mortalidad súbita.
            
            • **Arteria Coronaria Derecha (ACD)**:
              - Irriga ventrículo derecho, cara diafragmática inferior y nodo AV (90%).
              - ECG: Supradesnivel del ST en DII, DIII y aVF (IAM inferior).
              - Riesgo: Bloqueos auriculoventriculares y bradicardia refleja de Bezold-Jarisch.
            
            • **Arteria Circunfleja (Cx)**:
              - Irriga pared lateral del ventrículo izquierdo.
              - ECG: Supradesnivel en DI, aVL, V5 y V6.
            """.trimIndent()
        }
        lower.contains("femur") || lower.contains("cadera") || lower.contains("fractura") -> {
            """
            🦴 **Fémur Proximal y Vascularización de la Cabeza Femoral**:
            
            • **Vascularización Principal**:
              Proviene de las **arterias retinaculares** derivadas de la **arteria circunfleja femoral medial** (rama de la femoral profunda), las cuales perforan la cápsula articular en la base del cuello femoral.
            
            • **Correlación Traumatológica**:
              - **Fracturas Intracapsulares (Subcapitales / Trascervicales)**: Desgarran los vasos retinaculares. Tienen una tasa altísima de **necrosis avascular (osteonecrosis)** y pseudoartrosis. En ancianos suele requerir artroplastia protésica de cadera.
              - **Fracturas Extracapsulares (Intertrocantéricas)**: Preservan la irrigación; suelen consolidar bien con fijación interna (clavos endomedulares).
            """.trimIndent()
        }
        else -> {
            """
            📚 **Respuesta Anatómica y Correlación Clínica**:
            
            Estimado estudiante, respecto a tu consulta sobre **$prompt**:
            
            1. **Disposición Anatómica General**:
               La anatomía descriptiva y topográfica clasifica los órganos según su origen embriológico, relaciones compartimentales y fascias limitantes que delimitan espacios quirúrgicos avasculares.
            
            2. **Neurovascularización de Alto Rendimiento**:
               - El drenaje linfático de los órganos viscerales sigue el trayecto retrógrado de las arterias principales hasta los troncos celíaco, mesentéricos o paraaórticos.
               - La inervación autónoma simpática modula vasoconstricción y respuesta nociceptiva visceral referida (dermatomos T1-L2), mientras que el parasimpático (Nervio Vago NC X y esplácnicos pélvicos S2-S4) controla el tono muscular y la secreción.
            
            3. **Mnemotecnia & Perla de Examen**:
               En evaluaciones clínicas tipo MIR/USMLE, siempre evalúa primero la estabilidad de la vía aérea y el compromiso hemodinámico antes de la confirmación anatómica por imágenes (TAC/AngioTAC).
            """.trimIndent()
        }
    }
}
