package com.example.ui.pages.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.launch
import com.example.model.BrowserTab
import com.example.model.UserScript
import com.example.ui.theme.DevEmerald
import com.example.ui.theme.DevRose

class JavaScriptSyntaxVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val string = text.text
        val builder = AnnotatedString.Builder(string)

        // Comments
        val commentRegex = Regex("//.*|/\\*[\\s\\S]*?\\*/")
        commentRegex.findAll(string).forEach { match ->
            builder.addStyle(
                SpanStyle(color = Color(0xFF6A9955), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                match.range.first,
                match.range.last + 1
            )
        }

        // Strings
        val stringRegex = Regex("\"(\\\\.|[^\"])*\"|'(\\\\.|[^'])*'`(\\\\.|[^`])*`")
        stringRegex.findAll(string).forEach { match ->
            builder.addStyle(
                SpanStyle(color = Color(0xFFCE9178)),
                match.range.first,
                match.range.last + 1
            )
        }

        // Keywords
        val keywords = listOf("function", "var", "let", "const", "return", "if", "else", "for", "while", "do", "switch", "case", "break", "continue", "async", "await", "try", "catch", "new", "typeof", "in", "of", "import", "export", "default")
        val keywordRegex = Regex("\\b(${keywords.joinToString("|")})\\b")
        keywordRegex.findAll(string).forEach { match ->
            builder.addStyle(
                SpanStyle(color = Color(0xFF569CD6), fontWeight = FontWeight.Bold),
                match.range.first,
                match.range.last + 1
            )
        }

        // Metadata tags (@name, @match, etc.)
        val metaRegex = Regex("@\\w+")
        metaRegex.findAll(string).forEach { match ->
            builder.addStyle(
                SpanStyle(color = Color(0xFF4EC9B0), fontWeight = FontWeight.Bold),
                match.range.first,
                match.range.last + 1
            )
        }

        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}

fun formatJavaScriptCode(rawCode: String): String {
    val lines = rawCode.lines()
    val formatted = StringBuilder()
    var indentLevel = 0
    val indentStr = "    "

    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) {
            formatted.append("\n")
            continue
        }

        val startsWithClose = trimmed.startsWith("}") || trimmed.startsWith("];") || trimmed.startsWith(")")
        if (startsWithClose) {
            indentLevel = maxOf(0, indentLevel - 1)
        }

        repeat(indentLevel) {
            formatted.append(indentStr)
        }
        formatted.append(trimmed).append("\n")

        val openCount = trimmed.count { it == '{' || it == '[' || it == '(' }
        val closeCount = trimmed.count { it == '}' || it == ']' || it == ')' }
        if (openCount > closeCount) {
            indentLevel++
        }
    }
    return formatted.toString().trimEnd()
}

fun handleCodeChangeWithAutoTab(
    oldValue: TextFieldValue,
    newValue: TextFieldValue
): TextFieldValue {
    val oldText = oldValue.text
    val newText = newValue.text

    if (oldText == newText) return newValue

    val oldStart = minOf(oldValue.selection.start, oldValue.selection.end)
    val oldEnd = maxOf(oldValue.selection.start, oldValue.selection.end)
    val oldSelectionLen = oldEnd - oldStart

    val addedChars = newText.length - (oldText.length - oldSelectionLen)
    val isSingleNewline = (addedChars == 1 && newText.getOrNull(oldStart) == '\n') ||
            (addedChars == 2 && oldStart + 2 <= newText.length && newText.substring(oldStart, oldStart + 2) == "\r\n")

    if (!isSingleNewline) {
        return newValue
    }

    val lineStart = oldText.lastIndexOf('\n', (oldStart - 1).coerceAtLeast(0)).let {
        if (it == -1) 0 else it + 1
    }
    val lineUpToCursor = oldText.substring(lineStart, oldStart)
    val leadingIndent = lineUpToCursor.takeWhile { it == ' ' || it == '\t' }

    val trimmedLine = lineUpToCursor.trimEnd()
    val isOpenBrace = trimmedLine.endsWith('{') || trimmedLine.endsWith('(') || trimmedLine.endsWith('[')

    val indentUnit = when {
        leadingIndent.contains('\t') -> "\t"
        leadingIndent.startsWith("  ") && leadingIndent.length % 4 != 0 && leadingIndent.length % 2 == 0 -> "  "
        else -> "    "
    }

    val extraIndent = if (isOpenBrace) indentUnit else ""
    val totalIndent = leadingIndent + extraIndent

    if (totalIndent.isEmpty()) {
        return newValue
    }

    val insertPos = if (addedChars == 2) oldStart + 2 else oldStart + 1
    val nextChar = newText.getOrNull(insertPos)
    val isBetweenBraces = (trimmedLine.endsWith('{') && nextChar == '}') ||
            (trimmedLine.endsWith('(') && nextChar == ')') ||
            (trimmedLine.endsWith('[') && nextChar == ']')

    return if (isBetweenBraces) {
        val closingIndent = "\n" + leadingIndent
        val resultText = newText.substring(0, insertPos) + totalIndent + closingIndent + newText.substring(insertPos)
        val newCursor = insertPos + totalIndent.length
        TextFieldValue(resultText, TextRange(newCursor, newCursor))
    } else {
        val resultText = newText.substring(0, insertPos) + totalIndent + newText.substring(insertPos)
        val newCursor = insertPos + totalIndent.length
        TextFieldValue(resultText, TextRange(newCursor, newCursor))
    }
}

data class ScriptSyntaxIssue(
    val line: Int,
    val column: Int = 1,
    val message: String,
    val isError: Boolean = true
)

fun validateUserscriptSyntax(code: String): List<ScriptSyntaxIssue> {
    val issues = mutableListOf<ScriptSyntaxIssue>()

    // Check userscript metadata block
    val hasHeaderStart = code.contains("// ==UserScript==")
    val hasHeaderEnd = code.contains("// ==/UserScript==")
    if (hasHeaderStart && !hasHeaderEnd) {
        val startLine = code.lineSequence().indexOfFirst { it.contains("// ==UserScript==") } + 1
        issues.add(
            ScriptSyntaxIssue(
                line = if (startLine > 0) startLine else 1,
                message = "Missing closing '// ==/UserScript==' in metadata header"
            )
        )
    }

    class Delimiter(val char: Char, val line: Int, val col: Int)
    val stack = mutableListOf<Delimiter>()
    var inSingleQuote = false
    var singleQuoteStartLine = 1
    var inDoubleQuote = false
    var doubleQuoteStartLine = 1
    var inTemplateLiteral = false
    var templateLiteralStartLine = 1
    var inLineComment = false
    var inBlockComment = false
    var blockCommentStartLine = 1
    var currentLine = 1
    var currentCol = 0

    var i = 0
    val len = code.length
    while (i < len) {
        val ch = code[i]
        currentCol++

        if (ch == '\n') {
            if (inSingleQuote) {
                val prevChar = if (i > 0) code[i - 1] else ' '
                if (prevChar != '\\') {
                    issues.add(ScriptSyntaxIssue(singleQuoteStartLine, 1, "Unterminated single-quote string literal"))
                    inSingleQuote = false
                }
            }
            if (inDoubleQuote) {
                val prevChar = if (i > 0) code[i - 1] else ' '
                if (prevChar != '\\') {
                    issues.add(ScriptSyntaxIssue(doubleQuoteStartLine, 1, "Unterminated double-quote string literal"))
                    inDoubleQuote = false
                }
            }
            inLineComment = false
            currentLine++
            currentCol = 0
            i++
            continue
        }

        if (inBlockComment) {
            if (ch == '*' && i + 1 < len && code[i + 1] == '/') {
                inBlockComment = false
                i += 2
                currentCol++
                continue
            }
            i++
            continue
        }

        if (inLineComment) {
            i++
            continue
        }

        if ((inSingleQuote || inDoubleQuote || inTemplateLiteral) && ch == '\\') {
            i += 2
            currentCol++
            continue
        }

        if (inSingleQuote) {
            if (ch == '\'') inSingleQuote = false
            i++
            continue
        }
        if (inDoubleQuote) {
            if (ch == '"') inDoubleQuote = false
            i++
            continue
        }
        if (inTemplateLiteral) {
            if (ch == '`') inTemplateLiteral = false
            i++
            continue
        }

        if (ch == '/' && i + 1 < len) {
            val next = code[i + 1]
            if (next == '/') {
                inLineComment = true
                i += 2
                currentCol++
                continue
            } else if (next == '*') {
                inBlockComment = true
                blockCommentStartLine = currentLine
                i += 2
                currentCol++
                continue
            }
        }

        if (ch == '\'') {
            inSingleQuote = true
            singleQuoteStartLine = currentLine
            i++
            continue
        }
        if (ch == '"') {
            inDoubleQuote = true
            doubleQuoteStartLine = currentLine
            i++
            continue
        }
        if (ch == '`') {
            inTemplateLiteral = true
            templateLiteralStartLine = currentLine
            i++
            continue
        }

        when (ch) {
            '{', '(', '[' -> {
                stack.add(Delimiter(ch, currentLine, currentCol))
            }
            '}' -> {
                if (stack.isNotEmpty() && stack.last().char == '{') {
                    stack.removeAt(stack.lastIndex)
                } else {
                    issues.add(ScriptSyntaxIssue(currentLine, currentCol, "Unexpected closing brace '}'"))
                }
            }
            ')' -> {
                if (stack.isNotEmpty() && stack.last().char == '(') {
                    stack.removeAt(stack.lastIndex)
                } else {
                    issues.add(ScriptSyntaxIssue(currentLine, currentCol, "Unexpected closing parenthesis ')'"))
                }
            }
            ']' -> {
                if (stack.isNotEmpty() && stack.last().char == '[') {
                    stack.removeAt(stack.lastIndex)
                } else {
                    issues.add(ScriptSyntaxIssue(currentLine, currentCol, "Unexpected closing bracket ']'"))
                }
            }
        }

        i++
    }

    if (inSingleQuote) {
        issues.add(ScriptSyntaxIssue(singleQuoteStartLine, 1, "Unterminated single-quote string literal"))
    }
    if (inDoubleQuote) {
        issues.add(ScriptSyntaxIssue(doubleQuoteStartLine, 1, "Unterminated double-quote string literal"))
    }
    if (inTemplateLiteral) {
        issues.add(ScriptSyntaxIssue(templateLiteralStartLine, 1, "Unterminated template literal"))
    }
    if (inBlockComment) {
        issues.add(ScriptSyntaxIssue(blockCommentStartLine, 1, "Unclosed block comment (missing '*/')"))
    }
    for (unclosed in stack) {
        val closing = when (unclosed.char) {
            '{' -> "}"
            '(' -> ")"
            '[' -> "]"
            else -> ""
        }
        issues.add(ScriptSyntaxIssue(unclosed.line, unclosed.col, "Unclosed '${unclosed.char}' (missing '$closing')"))
    }

    return issues.sortedBy { it.line }
}

fun getLineAndColumn(text: String, charIndex: Int): Pair<Int, Int> {
    val clamped = charIndex.coerceIn(0, text.length)
    var line = 1
    var lastNewlinePos = -1
    for (i in 0 until clamped) {
        if (text[i] == '\n') {
            line++
            lastNewlinePos = i
        }
    }
    val col = (clamped - lastNewlinePos).coerceAtLeast(1)
    return Pair(line, col)
}

fun getCharIndexForLine(text: String, targetLine: Int): Int {
    if (targetLine <= 1) return 0
    var currentLine = 1
    for (i in text.indices) {
        if (text[i] == '\n') {
            currentLine++
            if (currentLine == targetLine) {
                return (i + 1).coerceAtMost(text.length)
            }
        }
    }
    return text.length
}

@Composable
fun UserscriptEditorPage(
    currentUrl: String,
    userScripts: List<UserScript>,
    onNavigate: (String) -> Unit,
    onAddScript: (UserScript) -> Unit,
    onUpdateScript: (UserScript) -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme

    // Extract file param from URL: devbrowser:settings/userscripts/edit?file=...
    val fileParam = runCatching {
        val uri = android.net.Uri.parse(currentUrl)
        uri.getQueryParameter("file")
    }.getOrNull() ?: run {
        if (currentUrl.contains("file=")) {
            currentUrl.substringAfter("file=").substringBefore("&")
        } else null
    }

    val existingScript = userScripts.firstOrNull { it.id == fileParam || it.name.equals(fileParam, ignoreCase = true) }
    
    var isFullScreen by remember { mutableStateOf(false) }
    var showGoToLineDialog by remember { mutableStateOf(false) }
    var goToLineInput by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    var scriptName by remember(fileParam, existingScript?.name) {
        mutableStateOf(existingScript?.name ?: "New Userscript")
    }
    var scriptUpdateUrl by remember(fileParam, existingScript?.updateUrl) {
        mutableStateOf(existingScript?.updateUrl ?: "")
    }
    var codeFieldState by remember(fileParam, existingScript?.code) {
        mutableStateOf(TextFieldValue(existingScript?.code ?: UserScript.createDefaultTemplate().code))
    }

    val lineCount = remember(codeFieldState.text) {
        maxOf(1, codeFieldState.text.count { it == '\n' } + 1)
    }

    val (currentLine, currentCol) = remember(codeFieldState.text, codeFieldState.selection) {
        getLineAndColumn(codeFieldState.text, codeFieldState.selection.start)
    }

    val syntaxIssues = remember(codeFieldState.text) {
        validateUserscriptSyntax(codeFieldState.text)
    }

    val issuesByLine = remember(syntaxIssues) {
        syntaxIssues.groupBy { it.line }
    }
    var currentIssueIndex by remember { mutableIntStateOf(0) }

    val density = LocalDensity.current
    val lineHeightDp = with(density) { 18.sp.toDp() }
    val lineHeightPx = with(density) { 18.sp.toPx() }

    BackHandler {
        onNavigate(BrowserTab.URL_SETTINGS_USERSCRIPTS)
    }

    if (showGoToLineDialog) {
        AlertDialog(
            onDismissRequest = { showGoToLineDialog = false },
            title = {
                Text("Go to Line", style = MaterialTheme.typography.titleMedium)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Enter line number (1 to $lineCount):",
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = goToLineInput,
                        onValueChange = { input -> goToLineInput = input.filter { it.isDigit() } },
                        placeholder = { Text("e.g. $currentLine") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_goto_line_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = goToLineInput.toIntOrNull()?.coerceIn(1, lineCount) ?: currentLine
                        val targetOffset = getCharIndexForLine(codeFieldState.text, num)
                        codeFieldState = codeFieldState.copy(selection = TextRange(targetOffset, targetOffset))
                        coroutineScope.launch {
                            val targetY = ((num - 1) * lineHeightPx).toInt().coerceAtLeast(0)
                            verticalScrollState.animateScrollTo(targetY)
                        }
                        showGoToLineDialog = false
                        goToLineInput = ""
                    },
                    modifier = Modifier.testTag("dialog_goto_line_confirm")
                ) {
                    Text("Go")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showGoToLineDialog = false },
                    modifier = Modifier.testTag("dialog_goto_line_cancel")
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    val snippets = listOf(
        "    ",
        "// ==UserScript==",
        "// @name New Script",
        "// @match *://*/*",
        "// @grant none",
        "// ==/UserScript==\n",
        "console.log('Userscript loaded');",
        "document.querySelector('')",
        "window.addEventListener('load', () => {\n    \n});",
        "setTimeout(() => {\n    \n}, 1000);"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(cs.background)
            .testTag("page_userscript_editor"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = cs.surfaceVariant,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .clickable { onNavigate(BrowserTab.URL_SETTINGS_USERSCRIPTS) }
                            .testTag("editor_back_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = cs.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (existingScript != null) "Edit Userscript" else "New Userscript",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = cs.onBackground
                        )
                        Text(
                            text = currentUrl,
                            style = MaterialTheme.typography.labelSmall,
                            color = cs.primary
                        )
                    }
                }

                Button(
                    onClick = {
                        val parsed = UserScript.parseMetadata(codeFieldState.text, scriptName)
                        if (existingScript != null) {
                            val updated = existingScript.copy(
                                name = scriptName.ifBlank { parsed.name },
                                code = codeFieldState.text,
                                updateUrl = scriptUpdateUrl.trim(),
                                matchPatterns = parsed.matchPatterns,
                                lastUpdated = System.currentTimeMillis()
                            )
                            onUpdateScript(updated)
                        } else {
                            val newScript = UserScript(
                                name = scriptName.ifBlank { parsed.name },
                                code = codeFieldState.text,
                                updateUrl = scriptUpdateUrl.trim(),
                                matchPatterns = parsed.matchPatterns,
                                enabled = true
                            )
                            onAddScript(newScript)
                        }
                        onNavigate(BrowserTab.URL_SETTINGS_USERSCRIPTS)
                    },
                    modifier = Modifier.testTag("editor_save_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save")
                }
            }

            // Metadata Fields (Hidden in Full Screen for maximum workspace)
            if (!isFullScreen) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = scriptName,
                        onValueChange = { scriptName = it },
                        label = { Text("Script Name") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("editor_script_name")
                    )

                    OutlinedTextField(
                        value = scriptUpdateUrl,
                        onValueChange = { scriptUpdateUrl = it },
                        label = { Text("Update URL (Optional)") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("editor_update_url")
                    )
                }

                // Toolbar (Format Button & Auto-Complete Snippets Bar)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            val formatted = formatJavaScriptCode(codeFieldState.text)
                            codeFieldState = TextFieldValue(formatted, TextRange(formatted.length))
                        },
                        modifier = Modifier.testTag("editor_format_button")
                    ) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Format Code")
                    }

                    Text(
                        text = "Syntax Coloring & Auto-Complete active",
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.onSurfaceVariant
                    )
                }

                // Quick Snippets / Auto-complete Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    snippets.forEach { snippet ->
                        Surface(
                            color = cs.surfaceVariant.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant),
                            modifier = Modifier
                                .clickable {
                                    val currentText = codeFieldState.text
                                    val selection = codeFieldState.selection
                                    val newText = currentText.substring(0, selection.start) + snippet + currentText.substring(selection.end)
                                    val newCursor = selection.start + snippet.length
                                    codeFieldState = TextFieldValue(newText, TextRange(newCursor))
                                }
                                .testTag("snippet_${snippet.take(10).replace(Regex("[^A-Za-z0-9]"), "_")}")
                        ) {
                            Text(
                                text = if (snippet == "    ") "Tab ⇥" else snippet.replace("\n", " "),
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = cs.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Full Screen & Navigation Header Bar right above the code box
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isFullScreen) "Editor (Full Screen)" else "Code Editor",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = cs.onSurfaceVariant
                    )

                    // Cursor Position & Line Navigator Button
                    Surface(
                        color = cs.surfaceVariant.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .clickable {
                                goToLineInput = currentLine.toString()
                                showGoToLineDialog = true
                            }
                            .testTag("editor_cursor_pos_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Ln $currentLine, Col $currentCol",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = cs.primary
                            )
                            Text(
                                text = "($lineCount lines)",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = cs.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val cur = codeFieldState
                            val start = minOf(cur.selection.start, cur.selection.end)
                            val end = maxOf(cur.selection.start, cur.selection.end)
                            val tabStr = "    "
                            val newText = cur.text.substring(0, start) + tabStr + cur.text.substring(end)
                            val newPos = start + tabStr.length
                            codeFieldState = TextFieldValue(newText, TextRange(newPos, newPos))
                        },
                        modifier = Modifier.testTag("editor_tab_button"),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "Tab ⇥",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    OutlinedButton(
                        onClick = { isFullScreen = !isFullScreen },
                        modifier = Modifier.testTag("editor_fullscreen_button"),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = cs.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isFullScreen) "Exit" else "Full")
                    }
                }
            }

            // Real-time Syntax / Error Identification Banner
            if (syntaxIssues.isNotEmpty()) {
                val activeIssue = syntaxIssues[currentIssueIndex.coerceIn(0, syntaxIssues.lastIndex)]
                Surface(
                    color = Color(0xFF2C1618),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B2525)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("editor_syntax_error_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = Color(0xFFF87171),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Line ${activeIssue.line}: ${activeIssue.message}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFFFD2D2)
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (syntaxIssues.size > 1) {
                                Text(
                                    text = "${(currentIssueIndex % syntaxIssues.size) + 1}/${syntaxIssues.size}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                    color = Color(0xFFE2A4A4)
                                )
                            }
                            Surface(
                                color = Color(0xFF5C1D24),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .clickable {
                                        val issue = syntaxIssues[currentIssueIndex % syntaxIssues.size]
                                        val targetOffset = getCharIndexForLine(codeFieldState.text, issue.line)
                                        codeFieldState = codeFieldState.copy(selection = TextRange(targetOffset, targetOffset))
                                        coroutineScope.launch {
                                            val targetY = ((issue.line - 1) * lineHeightPx).toInt().coerceAtLeast(0)
                                            verticalScrollState.animateScrollTo(targetY)
                                        }
                                        currentIssueIndex = (currentIssueIndex + 1) % syntaxIssues.size
                                    }
                                    .testTag("editor_jump_to_error_button")
                            ) {
                                Text(
                                    text = if (syntaxIssues.size > 1) "Next ➔" else "Jump ➔",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFFFF1F1),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Code Editor Box with Line Number Gutter & Syntax Highlighting
            Surface(
                color = Color(0xFF1E1E1E), // VS Code dark editor background
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val containerHeight = maxHeight
                    val gutterWidth = (maxOf(3, lineCount.toString().length) * 9 + 28).dp

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = containerHeight)
                            .verticalScroll(verticalScrollState)
                    ) {
                        // Line Number Gutter on the left
                        Box(
                            modifier = Modifier
                                .width(gutterWidth)
                                .heightIn(min = containerHeight)
                                .background(Color(0xFF161616))
                                .drawBehind {
                                    // Subtle vertical divider between gutter and code
                                    drawLine(
                                        color = Color(0xFF2D2D2D),
                                        start = Offset(size.width, 0f),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                }
                                .testTag("editor_line_gutter")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp, bottom = 12.dp, start = 4.dp, end = 6.dp)
                            ) {
                                for (lineNum in 1..lineCount) {
                                    val isCurrent = lineNum == currentLine
                                    val lineIssues = issuesByLine[lineNum]
                                    val hasError = lineIssues?.any { it.isError } == true
                                    val hasWarning = lineIssues?.any { !it.isError } == true

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(lineHeightDp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(
                                                when {
                                                    hasError -> Color(0xFF451A1A)
                                                    isCurrent -> Color(0xFF282828)
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .clickable {
                                                val target = getCharIndexForLine(codeFieldState.text, lineNum)
                                                codeFieldState = codeFieldState.copy(selection = TextRange(target, target))
                                                coroutineScope.launch {
                                                    val targetY = ((lineNum - 1) * lineHeightPx).toInt().coerceAtLeast(0)
                                                    verticalScrollState.animateScrollTo(targetY)
                                                }
                                            }
                                            .testTag("gutter_line_$lineNum"),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        if (hasError) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(Color(0xFFF87171), CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                        } else if (hasWarning) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(Color(0xFFFBBF24), CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                        }

                                        Text(
                                            text = "$lineNum",
                                            style = TextStyle(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 13.sp,
                                                lineHeight = 18.sp,
                                                color = when {
                                                    hasError -> Color(0xFFF87171)
                                                    hasWarning -> Color(0xFFFBBF24)
                                                    isCurrent -> Color.White
                                                    else -> Color(0xFF6E7681)
                                                },
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                                textAlign = TextAlign.End
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Code Text Field on the right
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = containerHeight)
                                .horizontalScroll(horizontalScrollState)
                                .padding(top = 12.dp, bottom = 12.dp, start = 12.dp, end = 16.dp)
                        ) {
                            BasicTextField(
                                value = codeFieldState,
                                onValueChange = { newVal ->
                                    codeFieldState = handleCodeChangeWithAutoTab(codeFieldState, newVal)
                                },
                                textStyle = TextStyle(
                                    color = Color(0xFFD4D4D4),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                ),
                                visualTransformation = JavaScriptSyntaxVisualTransformation(),
                                cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFF569CD6)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("editor_code_textarea")
                            )
                        }
                    }
                }
            }
        }
    }
}
