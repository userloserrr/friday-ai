package com.friday.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

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

        // Simple avatar placeholder
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1D2A43), RoundedCornerShape(24.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "◉", color = Color(0xFF8FE3FF), style = MaterialTheme.typography.displayLarge)
            Text(
                text = "Yüz animasyonu burada çalışacak",
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
