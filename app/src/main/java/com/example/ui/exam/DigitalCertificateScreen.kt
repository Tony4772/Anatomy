package com.example.ui.exam

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.CertificateEntity
import com.example.ui.theme.*

@Composable
fun DigitalCertificateView(
    certificate: CertificateEntity,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavyBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Navigation / Back header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = Color.White)
            }
            Text(
                text = "Certificación Médica Digital",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
            IconButton(
                onClick = {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "¡He obtenido mi ${certificate.examTitle} en AnatomiMed! Calificación: ${certificate.scorePercentage}% - Folio: ${certificate.folio}"
                        )
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Compartir Certificado"))
                }
            ) {
                Icon(Icons.Outlined.Share, contentDescription = "Compartir", tint = NeuralGold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Certificate Parchment Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFBF7)),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(4.dp, Brush.linearGradient(listOf(Color(0xFFD4AF37), Color(0xFFFFDF73), Color(0xFF996515)))),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Background watermarked decorative border lines
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawRect(
                        color = Color(0x11D4AF37),
                        topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(size.width - 8.dp.toPx(), size.height - 8.dp.toPx()),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Caduceus / Academy Banner Header
                    Text(
                        text = "ACADEMIA INTERNACIONAL DE ANATOMÍA CLÍNICA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                        color = Color(0xFF5A4400),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "DIVISIÓN DE EDUCACIÓN MÉDICA CONTINUA",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7A6840),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "CERTIFICADO DE EXCELENCIA ACADÉMICA",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B2A45),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Se otorga la presente distinción a:",
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFF555555)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Student Name
                    Text(
                        text = certificate.studentName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00363F),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = certificate.university,
                        fontSize = 12.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Por haber aprobado con éxito riguroso la evaluación teórico-práctica en:",
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF444444)
                    )

                    Text(
                        text = certificate.examTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF007A8C),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Distinction & Score Pill
                    Surface(
                        color = Color(0xFFFFF9E6),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD4AF37))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = certificate.distinction,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB38600)
                            )
                            Text(
                                text = "Calificación Obtenida: ${certificate.scorePercentage}% / 100%",
                                fontSize = 11.sp,
                                color = Color(0xFF333333)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Golden Seal & QR Code Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Official Golden Academy Seal
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.cert_seal_gold),
                                contentDescription = "Sello dorado oficial",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Folio and verification metadata
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "FOLIO OFICIAL ÚNICO",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF888888)
                            )
                            Text(
                                text = certificate.folio,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF1B2A45)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Fecha: ${certificate.issueDate}",
                                fontSize = 10.sp,
                                color = Color(0xFF666666)
                            )
                        }

                        // Simulated QR Verification Code Graphic
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCode2,
                                    contentDescription = "Código QR de verificación",
                                    tint = Color.Black,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Action Buttons: Share & Save
        Button(
            onClick = {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Diplomado Oficial en Anatomía Humana AnatomiMed.\nTitular: ${certificate.studentName}\nFolio: ${certificate.folio}\nCalificación: ${certificate.scorePercentage}%\nVerificado por la Academia Médica de Anatomía Clínica."
                    )
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(shareIntent, "Compartir Diploma"))
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Compartir Diploma con Colegas", fontWeight = FontWeight.Bold)
        }
    }
}
