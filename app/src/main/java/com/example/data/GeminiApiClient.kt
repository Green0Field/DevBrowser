package com.example.data

import android.util.Log
import com.example.model.AiProtectionScanResult
import com.example.model.AiThreatCategory
import com.example.model.GeminiModelChoice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

object GeminiApiClient {

    private const val TAG = "GeminiApiClient"
    private const val TIMEOUT_MILLIS = 45_000

    data class GeminiCallResult(
        val rawResponse: String,
        val text: String,
        val modelUsed: String
    )

    private fun buildEndpointUrl(modelName: String, apiKey: String): String {
        return "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
    }

    /**
     * Executes the Gemini request with the requested model or Auto fallback cascade.
     * When AUTO is chosen: starts with the top model (gemini-3.5-flash) and cascades down
     * the priority list if an error or rejection occurs until one succeeds.
     */
    private fun executeWithModelFallback(
        apiKey: String,
        modelChoice: GeminiModelChoice,
        body: JSONObject
    ): GeminiCallResult {
        val modelsToTry = if (modelChoice == GeminiModelChoice.AUTO) {
            GeminiModelChoice.AUTO_CASCADE_ORDER
        } else {
            listOfNotNull(modelChoice.modelId ?: "gemini-3.5-flash")
        }

        var lastException: Exception? = null
        val attemptedModels = mutableListOf<String>()

        for (modelName in modelsToTry) {
            attemptedModels.add(modelName)
            try {
                Log.d(TAG, "Attempting Gemini call with model: $modelName")
                val rawResponse = executeHttpCall(apiKey, modelName, body)
                val text = parseGeminiText(rawResponse)
                if (text.isNotBlank()) {
                    Log.i(TAG, "Successfully executed request using model: $modelName")
                    return GeminiCallResult(
                        rawResponse = rawResponse,
                        text = text,
                        modelUsed = modelName
                    )
                } else {
                    throw Exception("Empty response parts from model: $modelName")
                }
            } catch (e: Exception) {
                lastException = e
                Log.w(TAG, "Gemini model '$modelName' failed: ${e.message}. Proceeding to next fallback if available.")
            }
        }

        throw lastException ?: Exception("All candidate models failed: ${attemptedModels.joinToString(", ")}")
    }

    private fun executeHttpCall(apiKey: String, modelName: String, body: JSONObject): String {
        val endpoint = buildEndpointUrl(modelName, apiKey)
        val url = URL(endpoint)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = TIMEOUT_MILLIS
            readTimeout = TIMEOUT_MILLIS
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            setRequestProperty("User-Agent", "DevBrowser-Android")
        }

        OutputStreamWriter(connection.outputStream, StandardCharsets.UTF_8).use { writer ->
            writer.write(body.toString())
            writer.flush()
        }

        val responseCode = connection.responseCode
        val inputStream = if (responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream ?: throw Exception("HTTP $responseCode from Gemini API for model $modelName")
        }

        val response = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)).use { reader ->
            reader.readText()
        }

        if (responseCode !in 200..299) {
            val errorMessage = try {
                val errorObj = JSONObject(response).optJSONObject("error")
                errorObj?.optString("message") ?: "HTTP $responseCode"
            } catch (_: Exception) {
                "HTTP $responseCode: $response"
            }
            throw Exception("Model $modelName returned HTTP $responseCode: $errorMessage")
        }

        return response
    }

    /**
     * Test whether the provided Gemini API key is valid using the selected model or auto cascade.
     * Returns the name of the working model.
     */
    suspend fun testApiKey(
        apiKey: String,
        modelChoice: GeminiModelChoice = GeminiModelChoice.AUTO
    ): Result<String> = withContext(Dispatchers.IO) {
        val trimmed = apiKey.trim()
        if (trimmed.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("API key cannot be empty"))
        }

        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "Respond strictly with the word 'OK'")
                        })
                    })
                })
            })
        }

        try {
            val callResult = executeWithModelFallback(trimmed, modelChoice, requestJson)
            Result.success(callResult.modelUsed)
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API key test failed", e)
            Result.failure(e)
        }
    }

    /**
     * Send URL to Gemini to evaluate security risk and categorize into SAFE, CAREFUL, or DANGER
     * with brief reasoning and model identification.
     */
    suspend fun analyzeUrlSecurity(
        apiKey: String,
        targetUrl: String,
        domain: String,
        modelChoice: GeminiModelChoice = GeminiModelChoice.AUTO
    ): AiProtectionScanResult = withContext(Dispatchers.IO) {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isEmpty()) {
            return@withContext AiProtectionScanResult(
                url = targetUrl,
                domain = domain,
                category = AiThreatCategory.SAFE,
                reasoning = "Gemini API key is not configured. Defaulting to standard navigation.",
                modelUsed = null
            )
        }

        val prompt = """
            You are a web safety and security analyzer for an Android developer browser.
            Analyze the following web target for threats like phishing, malware, malicious downloads, deceptive identity, or scam behavior:
            URL: $targetUrl
            Domain: $domain

            Categorize the website into exactly ONE of the following 3 threat categories:
            1. SAFE: Benign, legitimate, widely known, trusted, or standard web content.
            2. CAREFUL: Questionable reputation, unverified brand new domain, aggressive tracking, or suspicious characteristics.
            3. DANGER: Known or apparent malware, phishing, active scam, credential harvesting, or severe risk.

            Output your answer strictly in valid JSON format with keys "category" and "reasoning":
            {"category": "SAFE", "reasoning": "Explain the safety assessment concisely in 1 to 2 sentences."}
        """.trimIndent()

        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
        }

        try {
            val callResult = executeWithModelFallback(trimmedKey, modelChoice, requestJson)
            val (category, reasoning) = parseCategoryAndReasoning(callResult.text, domain)

            AiProtectionScanResult(
                url = targetUrl,
                domain = domain,
                category = category,
                reasoning = reasoning,
                modelUsed = callResult.modelUsed,
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "AI Protection scan failed for $targetUrl", e)
            AiProtectionScanResult(
                url = targetUrl,
                domain = domain,
                category = AiThreatCategory.SAFE,
                reasoning = "AI safety check could not reach Gemini: ${e.message ?: "Network error"}",
                modelUsed = null,
                timestamp = System.currentTimeMillis()
            )
        }
    }

    /**
     * Send chat query along with webpage context to Gemini AI.
     * Returns Pair<AnswerText, ModelUsed>
     */
    suspend fun askAiChat(
        apiKey: String,
        title: String,
        url: String,
        webpageContent: String,
        chatHistory: List<Pair<String, Boolean>>, // Pair<text, isUser>
        userQuery: String,
        modelChoice: GeminiModelChoice = GeminiModelChoice.AUTO
    ): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isEmpty()) {
            return@withContext Result.failure(IllegalStateException("Gemini API key is missing. Please configure it in Settings > Ai Features."))
        }

        val truncated = if (webpageContent.length > 12000) webpageContent.take(12000) + "…" else webpageContent

        val contentsArray = JSONArray().apply {
            // Context system instruction / initial prompt part
            val contextPrompt = """
                You are an expert AI browser assistant embedded in an Android browser.
                The user is currently viewing the following webpage and may ask you questions about it or general questions.
                
                Webpage Title: $title
                Webpage URL: $url
                Webpage Content:
                $truncated
            """.trimIndent()

            put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", contextPrompt) })
                })
            })
            put(JSONObject().apply {
                put("role", "model")
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", "Understood. I am ready to answer questions about this webpage.") })
                })
            })

            // Conversation history
            for ((text, isUser) in chatHistory) {
                put(JSONObject().apply {
                    put("role", if (isUser) "user" else "model")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", text) })
                    })
                })
            }

            // Current query
            put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", userQuery) })
                })
            })
        }

        val requestJson = JSONObject().apply {
            put("contents", contentsArray)
        }

        try {
            val callResult = executeWithModelFallback(trimmedKey, modelChoice, requestJson)
            Result.success(Pair(callResult.text, callResult.modelUsed))
        } catch (e: Exception) {
            Log.e(TAG, "AI Chat query failed", e)
            Result.failure(e)
        }
    }

    /**
     * Auto-generate relevant chat suggestions based on the webpage content.
     * Guaranteed to include "Summarize this webpage" as one of the suggestions.
     */
    suspend fun generateChatSuggestions(
        apiKey: String,
        title: String,
        url: String,
        webpageContent: String,
        modelChoice: GeminiModelChoice = GeminiModelChoice.AUTO
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isEmpty()) {
            return@withContext Result.success(listOf("Summarize this webpage", "What is the main topic?", "Extract key takeaways"))
        }

        val truncated = if (webpageContent.length > 4000) webpageContent.take(4000) else webpageContent
        val prompt = """
            Analyze this webpage and generate exactly 3 short, helpful follow-up questions or prompt suggestions that a user might want to ask an AI about this page.
            Webpage Title: $title
            URL: $url
            Content:
            $truncated

            Rules:
            - Provide ONLY the 3 suggestions, separated by newlines (no numbering, no bullet points, no extra text).
            - Keep each suggestion under 8 words.
        """.trimIndent()

        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            })
        }

        try {
            val callResult = executeWithModelFallback(trimmedKey, modelChoice, requestJson)
            val lines = callResult.text.lines()
                .map { it.trim().removePrefix("-").removePrefix("1.").removePrefix("2.").removePrefix("3.").trim() }
                .filter { it.isNotBlank() }
                .take(3)

            val mutableSuggestions = mutableListOf("Summarize this webpage")
            for (line in lines) {
                if (!mutableSuggestions.any { it.equals(line, ignoreCase = true) } && mutableSuggestions.size < 4) {
                    mutableSuggestions.add(line)
                }
            }
            Result.success(mutableSuggestions)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to generate AI suggestions, falling back to defaults", e)
            Result.success(listOf("Summarize this webpage", "What is the main topic?", "Extract key takeaways", "Are there any security concerns?"))
        }
    }

    /**
     * Detect the language of a text snippet using Gemini.
     */
    suspend fun detectLanguage(
        apiKey: String,
        sampleText: String,
        modelChoice: GeminiModelChoice = GeminiModelChoice.AUTO
    ): Result<String> = withContext(Dispatchers.IO) {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isEmpty()) {
            return@withContext Result.success("English")
        }

        val sample = if (sampleText.length > 500) sampleText.take(500) else sampleText
        if (sample.isBlank()) {
            return@withContext Result.success("English")
        }

        val prompt = """
            Identify the natural language of the following text sample.
            Respond ONLY with the name of the language (e.g., English, Spanish, French, German, Japanese, Chinese, etc.).
            Do not include any other words or punctuation.

            Sample:
            $sample
        """.trimIndent()

        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
        }

        try {
            val callResult = executeWithModelFallback(trimmedKey, modelChoice, requestJson)
            val detected = callResult.text.trim().replace(".", "")
            if (detected.isNotBlank()) {
                Result.success(detected)
            } else {
                Result.success("English")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Language detection fallback to English", e)
            Result.success("English")
        }
    }

    /**
     * Translate webpage content into target language.
     * Returns Pair<TranslatedText, ModelUsed>
     */
    suspend fun translateContent(
        apiKey: String,
        content: String,
        sourceLang: String,
        targetLang: String,
        modelChoice: GeminiModelChoice = GeminiModelChoice.AUTO
    ): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isEmpty()) {
            return@withContext Result.failure(IllegalStateException("Gemini API key is missing. Please configure it in Settings > Ai Features."))
        }

        val truncated = if (content.length > 12000) content.take(12000) + "…" else content
        val prompt = """
            Translate the following webpage content from $sourceLang into $targetLang.
            Maintain the formatting, structure, and readability of the original text.
            Do not add conversational commentary.

            Content to translate:
            $truncated
        """.trimIndent()

        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
        }

        try {
            val callResult = executeWithModelFallback(trimmedKey, modelChoice, requestJson)
            Result.success(Pair(callResult.text, callResult.modelUsed))
        } catch (e: Exception) {
            Log.e(TAG, "Webpage translation failed", e)
            Result.failure(e)
        }
    }

    private fun parseGeminiText(rawResponse: String): String {
        return try {
            val root = JSONObject(rawResponse)
            val candidates = root.optJSONArray("candidates") ?: return ""
            val firstCandidate = candidates.optJSONObject(0) ?: return ""
            val content = firstCandidate.optJSONObject("content") ?: return ""
            val parts = content.optJSONArray("parts") ?: return ""
            val sb = java.lang.StringBuilder()
            for (i in 0 until parts.length()) {
                val part = parts.optJSONObject(i) ?: continue
                val text = part.optString("text", "")
                if (text.isNotEmpty()) sb.append(text)
            }
            sb.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse Gemini response text", e)
            ""
        }
    }

    private fun parseCategoryAndReasoning(rawText: String, domain: String): Pair<AiThreatCategory, String> {
        val cleaned = rawText.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        try {
            val jsonStart = cleaned.indexOf('{')
            val jsonEnd = cleaned.lastIndexOf('}')
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                val jsonSnippet = cleaned.substring(jsonStart, jsonEnd + 1)
                val obj = JSONObject(jsonSnippet)
                val catStr = obj.optString("category", "SAFE")
                val reasoning = obj.optString("reasoning", "Site verified by Gemini safety analysis.")
                return Pair(AiThreatCategory.fromString(catStr), reasoning)
            }
        } catch (_: Exception) {
        }

        // Fallback textual search
        val category = AiThreatCategory.fromString(cleaned)
        val reasoning = if (cleaned.length > 10) cleaned.take(200) else "Domain $domain evaluated by Gemini AI."
        return Pair(category, reasoning)
    }
}
