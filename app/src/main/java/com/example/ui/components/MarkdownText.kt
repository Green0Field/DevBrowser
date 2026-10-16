package com.example.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Lightweight Markdown renderer supporting headers (# / ## / ###),
 * bullet points (* / -), bold (**text**), italic (*text*), and inline code (`code`).
 */
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = 14.sp,
    lineHeight: TextUnit = 20.sp
) {
    val defaultColor = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color
    val annotatedString = parseMarkdownToAnnotatedString(text, defaultColor, fontSize)

    Text(
        text = annotatedString,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = fontSize,
            lineHeight = lineHeight,
            color = defaultColor
        )
    )
}

fun parseMarkdownToAnnotatedString(raw: String, defaultColor: Color, defaultSize: TextUnit): AnnotatedString {
    return buildAnnotatedString {
        val lines = raw.lines()
        for ((index, line) in lines.withIndex()) {
            if (index > 0) append("\n")

            val trimmed = line.trimStart()
            when {
                trimmed.startsWith("### ") -> {
                    val headerText = trimmed.removePrefix("### ").trim()
                    val startPos = this.length
                    append(headerText)
                    val endPos = this.length
                    addStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = (defaultSize.value * 1.05f).sp,
                            color = defaultColor
                        ),
                        startPos,
                        endPos
                    )
                }
                trimmed.startsWith("## ") -> {
                    val headerText = trimmed.removePrefix("## ").trim()
                    val startPos = this.length
                    append(headerText)
                    val endPos = this.length
                    addStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = (defaultSize.value * 1.15f).sp,
                            color = defaultColor
                        ),
                        startPos,
                        endPos
                    )
                }
                trimmed.startsWith("# ") -> {
                    val headerText = trimmed.removePrefix("# ").trim()
                    val startPos = this.length
                    append(headerText)
                    val endPos = this.length
                    addStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = (defaultSize.value * 1.25f).sp,
                            color = defaultColor
                        ),
                        startPos,
                        endPos
                    )
                }
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    append("• ")
                    appendInlineMarkdown(trimmed.substring(2))
                }
                else -> {
                    appendInlineMarkdown(line)
                }
            }
        }
    }
}

private fun AnnotatedString.Builder.appendInlineMarkdown(lineText: String) {
    var currentIndex = 0
    val textLen = lineText.length

    while (currentIndex < textLen) {
        val boldIdx = lineText.indexOf("**", currentIndex)
        val italicIdx = lineText.indexOf("*", currentIndex)
        val codeIdx = lineText.indexOf("`", currentIndex)

        // Find the earliest delimiter
        val candidateMatches = mutableListOf<Triple<Int, Int, String>>()
        if (boldIdx >= 0) candidateMatches.add(Triple(boldIdx, 2, "BOLD"))
        if (italicIdx >= 0 && italicIdx != boldIdx) candidateMatches.add(Triple(italicIdx, 1, "ITALIC"))
        if (codeIdx >= 0) candidateMatches.add(Triple(codeIdx, 1, "CODE"))

        candidateMatches.sortBy { it.first }

        if (candidateMatches.isEmpty()) {
            append(lineText.substring(currentIndex))
            break
        }

        val (matchIdx, delimLen, formatType) = candidateMatches.first()
        if (matchIdx > currentIndex) {
            append(lineText.substring(currentIndex, matchIdx))
        }

        val closingDelim = when (formatType) {
            "BOLD" -> "**"
            "ITALIC" -> "*"
            else -> "`"
        }
        val closingIdx = lineText.indexOf(closingDelim, matchIdx + delimLen)
        if (closingIdx < 0) {
            // Unmatched delimiter, append the rest verbatim
            append(lineText.substring(matchIdx))
            break
        }

        val innerContent = lineText.substring(matchIdx + delimLen, closingIdx)
        val startPos = this.length
        append(innerContent)
        val endPos = this.length

        when (formatType) {
            "BOLD" -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), startPos, endPos)
            "ITALIC" -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), startPos, endPos)
            "CODE" -> addStyle(
                SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    background = Color.Gray.copy(alpha = 0.2f)
                ),
                startPos,
                endPos
            )
        }

        currentIndex = closingIdx + delimLen
    }
}
