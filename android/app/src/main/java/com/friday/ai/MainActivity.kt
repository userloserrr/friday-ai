package com.friday.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

enum class FaceMood {
    HAPPY,
    NEUTRAL,
    SAD,
    SURPRISED
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FridayScreen()
        }
    }
}

@Composable
fun FridayScreen() {
    var prompt by remember { mutableStateOf("Merhaba Friday, bugün ne yapabiliriz?") }
    var reply by remember { mutableStateOf("Hazırlanıyorum...") }
    var loading by remember { mutableStateOf(false) }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var faceMood by remember { mutableStateOf(FaceMood.NEUTRAL) }

    val animatedMood = if (loading) FaceMood.SURPRISED else faceMood

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B1020))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Friday AI",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1D2A43), RoundedCornerShape(28.dp))
                .border(1.dp, Color(0xFF8FE3FF).copy(alpha = 0.6f), RoundedCornerShape(28.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FaceAvatar(mood = animatedMood, talking = loading)
            Text(
                text = when (animatedMood) {
                    FaceMood.HAPPY -> "Mutlu ve hazır"
                    FaceMood.NEUTRAL -> "Duygusal olarak dengeli"
                    FaceMood.SAD -> "Biraz düşünceli"
                    FaceMood.SURPRISED -> "Dinliyor / düşünüyor"
                },
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Kullanıcı komutu") },
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    loading = true
                    faceMood = moodFromPrompt(prompt)
                    CoroutineScope(Dispatchers.IO).launch {
                        val result = sendToBackend(prompt)
                        reply = result
                        loading = false
                    }
                },
                enabled = !loading,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (loading) "Bekleniyor..." else "Gönder")
            }

            Button(
                onClick = {
                    loading = true
                    faceMood = FaceMood.SURPRISED
                    CoroutineScope(Dispatchers.IO).launch {
                        val result = sendImageRequest(prompt)
                        imageUrl = result
                        loading = false
                    }
                },
                enabled = !loading,
                modifier = Modifier.weight(1f)
            ) {
                Text("Görsel üret")
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF111827), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Text(text = "Friday yanıtı", color = Color(0xFF8FE3FF))
            Text(text = reply, color = Color.White)
        }

        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Friday generated image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
        }
    }
}

@Composable
fun FaceAvatar(mood: FaceMood, talking: Boolean) {
    val eyeScale by animateFloatAsState(targetValue = if (talking) 1.4f else 1f, label = "eyes")
    val browLift = when (mood) {
        FaceMood.HAPPY -> 8f
        FaceMood.SAD -> -8f
        FaceMood.SURPRISED -> 12f
        FaceMood.NEUTRAL -> 0f
    }

    Canvas(modifier = Modifier.size(220.dp).padding(8.dp)) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radius = size.minDimension / 2.2f

        drawCircle(
            color = Color(0xFF9EE7FF),
            radius = radius,
            center = Offset(centerX, centerY)
        )

        drawCircle(
            color = Color(0xFF1D2A43),
            radius = radius * 1.02f,
            center = Offset(centerX, centerY),
            style = Stroke(width = 10f)
        )

        val eyeY = centerY - radius * 0.35f
        val eyeWidth = radius * 0.26f
        val eyeHeight = radius * (0.16f * eyeScale)
        val eyeLeftX = centerX - radius * 0.38f
        val eyeRightX = centerX + radius * 0.38f

        drawOval(
            color = Color.White,
            topLeft = Offset(eyeLeftX - eyeWidth, eyeY - eyeHeight),
            size = Size(eyeWidth * 2f, eyeHeight * 2f)
        )
        drawOval(
            color = Color.White,
            topLeft = Offset(eyeRightX - eyeWidth, eyeY - eyeHeight),
            size = Size(eyeWidth * 2f, eyeHeight * 2f)
        )

        drawOval(
            color = Color(0xFF1A1A1A),
            topLeft = Offset(eyeLeftX - eyeWidth * 0.35f, eyeY - eyeHeight * 0.35f),
            size = Size(eyeWidth * 0.7f, eyeHeight * 0.7f)
        )
        drawOval(
            color = Color(0xFF1A1A1A),
            topLeft = Offset(eyeRightX - eyeWidth * 0.35f, eyeY - eyeHeight * 0.35f),
            size = Size(eyeWidth * 0.7f, eyeHeight * 0.7f)
        )

        val browColor = Color(0xFF2F3C5C)
        val browLength = radius * 0.42f
        val browYLeft = eyeY - eyeHeight * 1.6f + browLift
        val browYRight = eyeY - eyeHeight * 1.6f + browLift

        drawLine(
            color = browColor,
            start = Offset(eyeLeftX - browLength, browYLeft),
            end = Offset(eyeLeftX + browLength, browYLeft + browLift * 0.15f),
            strokeWidth = 8f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = browColor,
            start = Offset(eyeRightX - browLength, browYRight),
            end = Offset(eyeRightX + browLength, browYRight + browLift * 0.15f),
            strokeWidth = 8f,
            cap = StrokeCap.Round
        )

        val mouthTop = centerY + radius * 0.26f
        val mouthBot = centerY + radius * 0.62f
        val mouthPath = Path().apply {
            when (mood) {
                FaceMood.HAPPY -> {
                    moveTo(centerX - radius * 0.25f, mouthTop)
                    quadraticBezierTo(
                        centerX,
                        mouthBot + 10f,
                        centerX + radius * 0.25f,
                        mouthTop
                    )
                }
                FaceMood.SAD -> {
                    moveTo(centerX - radius * 0.25f, mouthBot)
                    quadraticBezierTo(
                        centerX,
                        mouthTop - 10f,
                        centerX + radius * 0.25f,
                        mouthBot
                    )
                }
                FaceMood.SURPRISED -> {
                    drawArc(
                        color = Color(0xFF630B0B),
                        startAngle = 200f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(centerX - radius * 0.32f, mouthTop - 10f),
                        size = Size(radius * 0.64f, radius * 0.44f),
                        style = Stroke(width = 10f, cap = StrokeCap.Round)
                    )
                    return@Canvas
                }
                FaceMood.NEUTRAL -> {
                    drawLine(
                        color = Color(0xFF630B0B),
                        start = Offset(centerX - radius * 0.25f, centerY + radius * 0.55f),
                        end = Offset(centerX + radius * 0.25f, centerY + radius * 0.55f),
                        strokeWidth = 10f,
                        cap = StrokeCap.Round
                    )
                    return@Canvas
                }
            }
        }

        if (mood != FaceMood.SURPRISED && mood != FaceMood.NEUTRAL) {
            drawPath(path = mouthPath, color = Color(0xFF630B0B), style = Stroke(width = 10f, cap = StrokeCap.Round))
        }
    }
}

private fun moodFromPrompt(message: String): FaceMood {
    val lower = message.lowercase()
    return when {
        lower.contains("merhaba") || lower.contains("selam") || lower.contains("günaydın") -> FaceMood.HAPPY
        lower.contains("üzgün") || lower.contains("problem") || lower.contains("hayal kırıklığı") -> FaceMood.SAD
        lower.contains("görsel") || lower.contains("resim") || lower.contains("fotoğraf") -> FaceMood.SURPRISED
        else -> FaceMood.NEUTRAL
    }
}

private fun sendToBackend(message: String): String {
    val client = OkHttpClient()
    val json = JSONObject().apply { put("message", message) }.toString()
    val request = Request.Builder()
        .url("http://10.0.2.2:8000/chat")
        .post(json.toRequestBody("application/json; charset=utf-8".toMediaType()))
        .build()

    return try {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return "Sunucu hatası: ${response.code}"
            }
            val body = response.body?.string() ?: ""
            val obj = JSONObject(body)
            obj.optString("reply", "Yanıt gelmedi")
        }
    } catch (e: IOException) {
        "Bağlantı hatası. Backend çalışıyor mu? ${e.message}"
    }
}

private fun sendImageRequest(prompt: String): String {
    val client = OkHttpClient()
    val json = JSONObject().apply {
        put("prompt", prompt)
        put("style", "cinematic")
    }.toString()
    val request = Request.Builder()
        .url("http://10.0.2.2:8000/generate-image")
        .post(json.toRequestBody("application/json; charset=utf-8".toMediaType()))
        .build()

    return try {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return "Görsel üretme hatası: ${response.code}"
            }
            val body = response.body?.string() ?: ""
            val obj = JSONObject(body)
            obj.optString("image_url", "")
        }
    } catch (e: IOException) {
        "Görsel üretim bağlantı hatası: ${e.message}"
    }
}
