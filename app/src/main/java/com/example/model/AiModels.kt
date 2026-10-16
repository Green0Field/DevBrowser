package com.example.model

enum class AiThreatCategory(
    val displayName: String,
    val vtEquivalent: VirusTotalThreatLevel
) {
    SAFE("Safe Site", VirusTotalThreatLevel.SAFE),
    CAREFUL("Careful: Suspicious", VirusTotalThreatLevel.CAREFUL),
    DANGER("Danger! High Risk", VirusTotalThreatLevel.DANGER);

    companion object {
        fun fromString(str: String?): AiThreatCategory {
            val upper = str?.uppercase() ?: ""
            return when {
                upper.contains("DANGER") || upper.contains("MALICIOUS") || upper.contains("HIGH RISK") -> DANGER
                upper.contains("CAREFUL") || upper.contains("SUSPICIOUS") || upper.contains("WARNING") -> CAREFUL
                else -> SAFE
            }
        }
    }
}

enum class GeminiModelChoice(
    val id: String,
    val modelId: String?,
    val displayName: String,
    val description: String
) {
    AUTO(
        id = "auto",
        modelId = null,
        displayName = "Auto (Recommended)",
        description = "Chooses the best model (Gemini 3.5 Flash) and cascades down the list if one fails"
    ),
    GEMINI_3_5_FLASH(
        id = "gemini-3.5-flash",
        modelId = "gemini-3.5-flash",
        displayName = "Gemini 3.5 Flash",
        description = "Fast, state-of-the-art model for text chat, summarization, translations, and safety checks"
    ),
    GEMINI_3_1_PRO(
        id = "gemini-3.1-pro-preview",
        modelId = "gemini-3.1-pro-preview",
        displayName = "Gemini 3.1 Pro Preview",
        description = "Advanced reasoning, complex comprehension, and detailed analysis"
    ),
    GEMINI_3_1_FLASH_LITE(
        id = "gemini-3.1-flash-lite-preview",
        modelId = "gemini-3.1-flash-lite-preview",
        displayName = "Gemini 3.1 Flash Lite Preview",
        description = "Lightweight, ultra low-latency model for rapid responses"
    ),
    GEMINI_FLASH_LATEST(
        id = "gemini-flash-latest",
        modelId = "gemini-flash-latest",
        displayName = "Gemini Flash Latest",
        description = "Latest stable Gemini Flash alias"
    );

    companion object {
        val AUTO_CASCADE_ORDER = listOf(
            "gemini-3.5-flash",
            "gemini-3.1-pro-preview",
            "gemini-3.1-flash-lite-preview",
            "gemini-flash-latest"
        )

        fun fromId(id: String?): GeminiModelChoice {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: AUTO
        }
    }
}

data class AiProtectionScanResult(
    val url: String,
    val domain: String,
    val category: AiThreatCategory,
    val reasoning: String,
    val modelUsed: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class AiChatDialogState(
    val url: String,
    val title: String,
    val webpageContent: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val suggestions: List<String> = listOf("Summarize this webpage", "What is the main topic?", "Extract key takeaways", "Are there any security concerns?"),
    val inputQuery: String = "",
    val modelUsed: String? = null,
    val isGeneratingSuggestions: Boolean = false,
    val isLoadingAnswer: Boolean = false,
    val errorMessage: String? = null
)

data class WebpageTranslationState(
    val url: String,
    val title: String,
    val originalText: String = "",
    val sourceLanguage: String = "Auto-detect",
    val targetLanguage: String = "English",
    val translatedText: String? = null,
    val modelUsed: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showLanguageSelector: Boolean = false,
    val showResultDialog: Boolean = false
)

object SupportedLanguages {
    val POPULAR_LANGUAGES = listOf(
        "English",
        "Spanish",
        "French",
        "German",
        "Chinese (Simplified)",
        "Japanese",
        "Korean",
        "Italian",
        "Portuguese",
        "Russian",
        "Arabic",
        "Hindi",
        "Dutch",
        "Polish",
        "Turkish",
        "Vietnamese",
        "Thai",
        "Indonesian",
        "Greek",
        "Swedish"
    )
}
