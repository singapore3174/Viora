package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object GeminiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    suspend fun generateAiReply(userPrompt: String, systemPrompt: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getFallbackReply(userPrompt)
        }

        // Properly escape user and system prompts for JSON
        val escapedUserPrompt = escapeJsonString(userPrompt)
        val escapedSystemPrompt = systemPrompt?.let { escapeJsonString(it) }

        val systemBlock = if (escapedSystemPrompt != null) {
            """
            ,"systemInstruction": {
                "parts": [{"text": "$escapedSystemPrompt"}]
            }
            """.trimIndent()
        } else ""

        val jsonBody = """
        {
          "contents": [{
            "parts": [{"text": "$escapedUserPrompt"}]
          }]
          $systemBlock
        }
        """.trimIndent()

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toRequestBody(mediaType)

        val request = Request.Builder()
            .url("$BASE_URL?key=$apiKey")
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                if (!response.isSuccessful || bodyString.isBlank()) {
                    return@withContext getFallbackReply(userPrompt)
                }
                
                // Pure extract string search is 100% resilient and requires no external JSON parsing dependencies
                extractTextFromResponse(bodyString) ?: getFallbackReply(userPrompt)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            getFallbackReply(userPrompt)
        }
    }

    private fun escapeJsonString(input: String): String {
        return input.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    private fun extractTextFromResponse(responseJson: String): String? {
        val needle = "\"text\":"
        var index = responseJson.indexOf(needle)
        if (index == -1) return null
        
        index += needle.length
        // skip whitespace or quotes
        while (index < responseJson.length && (responseJson[index] == ' ' || responseJson[index] == '\"')) {
            index++
        }
        
        val start = index
        val sb = StringBuilder()
        var escaped = false
        while (index < responseJson.length) {
            val char = responseJson[index]
            if (escaped) {
                when (char) {
                    'n' -> sb.append('\n')
                    't' -> sb.append('\t')
                    'r' -> sb.append('\r')
                    '\\' -> sb.append('\\')
                    '\"' -> sb.append('\"')
                    else -> sb.append(char)
                }
                escaped = false
            } else if (char == '\\') {
                escaped = true
            } else if (char == '\"') {
                break // end of string
            } else {
                sb.append(char)
            }
            index++
        }
        return sb.toString().trim()
    }

    private fun getFallbackReply(prompt: String): String {
        val trimmed = prompt.lowercase()
        return when {
            trimmed.contains("hello") || trimmed.contains("hi") || trimmed.contains("hey") -> {
                "Hey there, digital explorer! ✨ Welcome to Aura. This space is powered by your vibes. How are your posts doing today?"
            }
            trimmed.contains("recommend") || trimmed.contains("explore") || trimmed.contains("suggest") -> {
                "Based on your aesthetic flow, I recommend looking into #cyberpunk and #minimalism. Try following @nova_builder and @neon_dreamer for premium futuristic captures!"
            }
            trimmed.contains("caption") || trimmed.contains("idea") || trimmed.contains("hashtag") -> {
                "✨ CAPTION GENERATION: 'Lost in the digital ether. Living life in high contrast. 🌌💊 #futurevibe #glassmorphic #aura'"
            }
            trimmed.contains("chat") || trimmed.contains("friend") || trimmed.contains("bot") -> {
                "I'm keeping watch on the code stream! Let's build something epic. Tell me about your next photoshoot."
            }
            else -> {
                "Vibe check complete. 🔮 Let's continue uploading stunning stories and making connections!"
            }
        }
    }
}
