package com.example.ui.panels

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.BoxModelMetrics
import com.example.model.DomNodeItem
import com.example.ui.theme.BoxBorderColor
import com.example.ui.theme.BoxContentColor
import com.example.ui.theme.BoxMarginColor
import com.example.ui.theme.BoxPaddingColor
import com.example.ui.theme.DevAmber
import com.example.ui.theme.DevEmerald
import com.example.ui.theme.DevRose
import com.example.ui.theme.DevSlate950
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.SyntaxAttrName
import com.example.ui.theme.SyntaxAttrValue
import com.example.ui.theme.SyntaxTag

@Composable
fun ElementsPanel(
    domNodes: List<DomNodeItem>,
    selectedNode: DomNodeItem?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSelectNode: (DomNodeItem) -> Unit,
    onApplyInlineStyle: (String, String) -> Unit,
    onHideNode: (String) -> Unit,
    onEditNodeText: (String, String) -> Unit,
    onSetAttribute: (String, String, String) -> Unit,
    onRemoveNode: (String) -> Unit,
    onAppendChildNode: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSubTab by remember { mutableIntStateOf(0) } // 0: Styles, 1: Box Model, 2: Computed, 3: DOM Actions
    var isInspectorExpanded by remember { mutableStateOf(true) }
    var editingNodeDialogTarget by remember { mutableStateOf<DomNodeItem?>(null) }
    val clipboardManager = LocalClipboardManager.current
    val cs = MaterialTheme.colorScheme

    Column(modifier = modifier.fillMaxSize()) {
        // Compact CSS Selector Search Bar + Hold-to-edit hint
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cs.surface)
                .padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(cs.background)
                    .border(1.dp, cs.outlineVariant, RoundedCornerShape(14.dp))
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search DOM by CSS selector",
                    tint = cs.onSurfaceVariant,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        color = cs.onSurface
                    ),
                    cursorBrush = SolidColor(cs.primary),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Find by selector (hold element to edit)…",
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dom_selector_search_input")
                )
                if (searchQuery.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear selector filter",
                        tint = cs.onSurfaceVariant,
                        modifier = Modifier
                            .size(13.dp)
                            .clickable { onSearchQueryChange("") }
                    )
                }
            }

            Text(
                text = "${domNodes.size} nodes",
                style = MaterialTheme.typography.labelSmall,
                color = cs.onSurfaceVariant
            )
        }

        HorizontalDivider(thickness = 1.dp, color = cs.outlineVariant)

        // DOM Tree View (Press & hold any element to edit it)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (isInspectorExpanded) 0.48f else 1f)
                .background(cs.background)
                .padding(vertical = 3.dp)
                .testTag("dom_tree_list")
        ) {
            items(domNodes, key = { it.path }) { node ->
                val isSelected = selectedNode?.path == node.path
                DomTreeNodeRow(
                    node = node,
                    isSelected = isSelected,
                    onClick = { onSelectNode(node) },
                    onLongClick = {
                        onSelectNode(node)
                        editingNodeDialogTarget = node
                    }
                )
            }
        }

        // Chrome DevTools Breadcrumb & Quick Actions Bar
        if (selectedNode != null) {
            HorizontalDivider(thickness = 1.dp, color = cs.outlineVariant)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cs.surface)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedNode.cssSelector.ifBlank { selectedNode.tag } + "  == \$0",
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(cs.primaryContainer)
                            .clickable { editingNodeDialogTarget = selectedNode }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .testTag("quick_edit_selected_element_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Element",
                            tint = cs.primary,
                            modifier = Modifier.size(11.dp)
                        )
                        Text("Edit", style = MaterialTheme.typography.labelSmall, color = cs.primary)
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                clipboardManager.setText(
                                    AnnotatedString(selectedNode.cssSelector.ifBlank { selectedNode.tag })
                                )
                            }
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy CSS Selector",
                            tint = cs.onSurfaceVariant,
                            modifier = Modifier.size(11.dp)
                        )
                        Text("Selector", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                clipboardManager.setText(AnnotatedString(selectedNode.outerHtmlPreview))
                            }
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy OuterHTML",
                            tint = cs.onSurfaceVariant,
                            modifier = Modifier.size(11.dp)
                        )
                        Text("HTML", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                    }
                }
            }
        }

        HorizontalDivider(thickness = 1.dp, color = cs.outline)

        // Inspector Sub-Tabs Header (with collapse/expand toggle)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cs.surfaceVariant)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabs = listOf("Styles", "Box Model", "Computed", "DOM Actions")
                tabs.forEachIndexed { idx, title ->
                    val active = activeSubTab == idx && isInspectorExpanded
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (active) cs.primaryContainer else Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = if (active) cs.primary else Color.Transparent,
                                shape = RoundedCornerShape(50)
                            )
                            .clickable {
                                activeSubTab = idx
                                isInspectorExpanded = true
                            }
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                            .testTag("elements_subtab_${title.lowercase().replace(" ", "_")}")
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (active) cs.primary else cs.onSurfaceVariant
                        )
                    }
                }
            }

            Icon(
                imageVector = if (isInspectorExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                contentDescription = "Toggle Inspector Drawer",
                tint = cs.onSurfaceVariant,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { isInspectorExpanded = !isInspectorExpanded }
            )
        }

        if (isInspectorExpanded) {
            HorizontalDivider(thickness = 1.dp, color = cs.outlineVariant)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.52f)
                    .background(cs.surface)
            ) {
                if (selectedNode == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Select any node in the DOM tree above (or press & hold to edit)",
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurfaceVariant
                        )
                    }
                } else {
                    when (activeSubTab) {
                        0 -> StylesLiveEditorSubPane(
                            node = selectedNode,
                            onApplyInlineStyle = onApplyInlineStyle
                        )
                        1 -> BoxModelDiagramSubPane(
                            boxModel = selectedNode.boxModel
                        )
                        2 -> ComputedStylesSubPane(
                            computedStyles = selectedNode.computedStyles
                        )
                        3 -> DomMutationSubPane(
                            node = selectedNode,
                            onHideNode = onHideNode,
                            onEditText = onEditNodeText,
                            onSetAttribute = onSetAttribute,
                            onRemoveNode = onRemoveNode,
                            onAppendChildNode = onAppendChildNode,
                            onHighlightNode = { onSelectNode(selectedNode) }
                        )
                    }
                }
            }
        }
    }

    // Press-and-Hold Edit Element Dialog
    val modalTarget = editingNodeDialogTarget
    if (modalTarget != null) {
        val liveNode = domNodes.find { it.path == modalTarget.path } ?: modalTarget
        EditElementModalDialog(
            node = liveNode,
            onDismiss = { editingNodeDialogTarget = null },
            onSaveEdits = { newText, newStyle, attrKey, attrVal ->
                if (newText != liveNode.textPreview) {
                    onEditNodeText(liveNode.path, newText)
                }
                if (newStyle != liveNode.inlineStyle) {
                    onApplyInlineStyle(liveNode.path, newStyle)
                }
                if (attrKey.isNotBlank()) {
                    onSetAttribute(liveNode.path, attrKey.trim(), attrVal)
                }
                editingNodeDialogTarget = null
            },
            onHideElement = {
                onHideNode(liveNode.path)
                editingNodeDialogTarget = null
            },
            onRemoveElement = {
                onRemoveNode(liveNode.path)
                editingNodeDialogTarget = null
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DomTreeNodeRow(
    node: DomNodeItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val indentDp = (node.depth.coerceAtMost(10) * 12).dp
    val isHidden = node.inlineStyle.contains("display: none", ignoreCase = true) ||
        node.computedStyles["display"] == "none"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) cs.primaryContainer else Color.Transparent)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(start = 6.dp + indentDp, end = 8.dp, top = 3.dp, bottom = 3.dp)
            .testTag("dom_node_${node.path}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (node.childCount > 0) {
            Icon(
                imageVector = if (isSelected) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = cs.onSurfaceVariant,
                modifier = Modifier.size(13.dp)
            )
        } else {
            Spacer(modifier = Modifier.width(13.dp))
        }

        val annotatedDomLine = buildAnnotatedString {
            withStyle(SpanStyle(color = SyntaxTag, fontWeight = FontWeight.SemiBold)) {
                append("<${node.tag}")
            }
            if (node.idAttr.isNotEmpty()) {
                withStyle(SpanStyle(color = SyntaxAttrName)) { append(" id=") }
                withStyle(SpanStyle(color = cs.primary, fontWeight = FontWeight.Bold)) {
                    append("\"${node.idAttr}\"")
                }
            }
            if (node.classAttr.isNotEmpty()) {
                withStyle(SpanStyle(color = SyntaxAttrName)) { append(" class=") }
                withStyle(SpanStyle(color = SyntaxAttrValue)) {
                    append("\"${node.classAttr.take(32)}\"")
                }
            }
            if (node.inlineStyle.isNotEmpty()) {
                withStyle(SpanStyle(color = SyntaxAttrName)) { append(" style=") }
                withStyle(SpanStyle(color = DevEmerald)) {
                    append("\"${node.inlineStyle.take(28)}\"")
                }
            }
            withStyle(SpanStyle(color = SyntaxTag, fontWeight = FontWeight.SemiBold)) {
                append(">")
            }
            if (node.textPreview.isNotEmpty()) {
                withStyle(SpanStyle(color = cs.onSurface)) {
                    append(" ${node.textPreview.take(38)} ")
                }
            }
            withStyle(SpanStyle(color = SyntaxTag)) {
                append("</${node.tag}>")
            }
        }

        Text(
            text = annotatedDomLine,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        if (isHidden) {
            Text(
                text = "hidden",
                style = MaterialTheme.typography.labelSmall,
                color = DevRose,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        if (isSelected) {
            Text(
                text = "== \$0",
                style = MaterialTheme.typography.labelSmall,
                color = DevAmber,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun EditElementModalDialog(
    node: DomNodeItem,
    onDismiss: () -> Unit,
    onSaveEdits: (newText: String, newStyle: String, attrKey: String, attrVal: String) -> Unit,
    onHideElement: () -> Unit,
    onRemoveElement: () -> Unit
) {
    var editedText by remember(node.path) { mutableStateOf(node.textPreview) }
    var editedStyle by remember(node.path) { mutableStateOf(node.inlineStyle) }
    var attrKey by remember(node.path) { mutableStateOf("") }
    var attrVal by remember(node.path) { mutableStateOf("") }
    val cs = MaterialTheme.colorScheme

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = cs.surface,
            shape = RoundedCornerShape(26.dp),
            tonalElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, cs.primary, RoundedCornerShape(26.dp))
                .testTag("edit_element_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Edit Element <${node.tag}>",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = cs.onSurface
                        )
                        Text(
                            text = node.cssSelector.ifBlank { node.tag },
                            style = MaterialTheme.typography.labelSmall,
                            color = cs.primary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Edit Element Dialog",
                        tint = cs.onSurfaceVariant,
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { onDismiss() }
                    )
                }

                OutlinedTextField(
                    value = editedText,
                    onValueChange = { editedText = it },
                    label = { Text("Text Content (innerText)", style = MaterialTheme.typography.labelSmall) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    textStyle = TextStyle(fontFamily = JetBrainsMonoFamily, fontSize = 12.sp, color = cs.onSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_edit_text_input")
                )

                OutlinedTextField(
                    value = editedStyle,
                    onValueChange = { editedStyle = it },
                    label = { Text("Inline Style (element.style)", style = MaterialTheme.typography.labelSmall) },
                    placeholder = { Text("display: none !important; color: red;", style = MaterialTheme.typography.labelSmall) },
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(14.dp),
                    textStyle = TextStyle(fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = cs.onSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_edit_style_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = attrKey,
                        onValueChange = { attrKey = it },
                        label = { Text("Attr Name", style = MaterialTheme.typography.labelSmall) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        textStyle = TextStyle(fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = cs.onSurface),
                        modifier = Modifier.weight(0.45f)
                    )
                    OutlinedTextField(
                        value = attrVal,
                        onValueChange = { attrVal = it },
                        label = { Text("Attr Value", style = MaterialTheme.typography.labelSmall) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        textStyle = TextStyle(fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = cs.onSurface),
                        modifier = Modifier.weight(0.55f)
                    )
                }

                // Quick Hide & Remove Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(DevAmber.copy(alpha = 0.2f))
                            .border(1.dp, DevAmber, RoundedCornerShape(50))
                            .clickable { onHideElement() }
                            .padding(vertical = 9.dp)
                            .testTag("dialog_hide_element_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = DevAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Hide Element",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = DevAmber
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(DevRose.copy(alpha = 0.2f))
                            .border(1.dp, DevRose, RoundedCornerShape(50))
                            .clickable { onRemoveElement() }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                tint = DevRose,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Remove Node",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = DevRose
                            )
                        }
                    }
                }

                // Save Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .background(cs.primary)
                        .clickable { onSaveEdits(editedText, editedStyle, attrKey, attrVal) }
                        .padding(vertical = 11.dp)
                        .testTag("save_edit_element_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Apply Changes",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = cs.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun StylesLiveEditorSubPane(
    node: DomNodeItem,
    onApplyInlineStyle: (String, String) -> Unit
) {
    var styleText by remember(node.path, node.inlineStyle) { mutableStateOf(node.inlineStyle) }
    val cs = MaterialTheme.colorScheme

    LaunchedEffect(node.path, node.inlineStyle) {
        styleText = node.inlineStyle
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "element.style {",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = cs.primary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(DevEmerald)
                        .clickable { onApplyInlineStyle(node.path, styleText) }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .testTag("apply_inline_css_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Apply CSS",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Apply CSS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = styleText,
                onValueChange = { styleText = it },
                placeholder = {
                    Text(
                        "color: #38bdf8; background: #1e293b; padding: 16px;",
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.onSurfaceVariant
                    )
                },
                shape = RoundedCornerShape(14.dp),
                textStyle = TextStyle(
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 11.sp,
                    color = cs.onSurface
                ),
                minLines = 2,
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inline_style_input")
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val presets = listOf(
                    "Accent Border" to "border: 2px solid #38bdf8; background-color: rgba(56, 189, 248, 0.18); padding: 14px; border-radius: 12px;",
                    "Emerald" to "border: 2px solid #10b981; background-color: rgba(16, 185, 129, 0.18); color: #34d399; padding: 14px;",
                    "Hide (!important)" to "display: none !important;",
                    "Reset" to ""
                )
                presets.forEach { (label, css) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(cs.surfaceVariant)
                            .border(1.dp, cs.outline, RoundedCornerShape(50))
                            .clickable {
                                styleText = css
                                onApplyInlineStyle(node.path, css)
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = cs.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxModelDiagramSubPane(boxModel: BoxModelMetrics) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(12.dp))
                .background(BoxMarginColor.copy(alpha = 0.22f))
                .border(1.dp, BoxMarginColor, RoundedCornerShape(12.dp))
                .padding(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("margin", style = MaterialTheme.typography.labelSmall, color = BoxMarginColor)
                Text("${boxModel.marginTop}", style = MaterialTheme.typography.labelSmall, color = BoxMarginColor)
                Spacer(modifier = Modifier.width(28.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${boxModel.marginLeft}", style = MaterialTheme.typography.labelSmall, color = BoxMarginColor)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BoxBorderColor.copy(alpha = 0.20f))
                        .border(1.dp, BoxBorderColor, RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("border", style = MaterialTheme.typography.labelSmall, color = BoxBorderColor)
                        Text("${boxModel.borderTop}", style = MaterialTheme.typography.labelSmall, color = BoxBorderColor)
                        Spacer(modifier = Modifier.width(24.dp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${boxModel.borderLeft}", style = MaterialTheme.typography.labelSmall, color = BoxBorderColor)

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(BoxPaddingColor.copy(alpha = 0.22f))
                                .border(1.dp, BoxPaddingColor, RoundedCornerShape(6.dp))
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("padding", style = MaterialTheme.typography.labelSmall, color = BoxPaddingColor)
                                Text("${boxModel.paddingTop}", style = MaterialTheme.typography.labelSmall, color = BoxPaddingColor)
                                Spacer(modifier = Modifier.width(24.dp))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${boxModel.paddingLeft}", style = MaterialTheme.typography.labelSmall, color = BoxPaddingColor)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(BoxContentColor.copy(alpha = 0.32f))
                                        .border(1.dp, BoxContentColor, RoundedCornerShape(4.dp))
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${boxModel.width} × ${boxModel.height}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = cs.onSurface
                                    )
                                }

                                Text("${boxModel.paddingRight}", style = MaterialTheme.typography.labelSmall, color = BoxPaddingColor)
                            }
                            Text("${boxModel.paddingBottom}", style = MaterialTheme.typography.labelSmall, color = BoxPaddingColor)
                        }

                        Text("${boxModel.borderRight}", style = MaterialTheme.typography.labelSmall, color = BoxBorderColor)
                    }
                    Text("${boxModel.borderBottom}", style = MaterialTheme.typography.labelSmall, color = BoxBorderColor)
                }

                Text("${boxModel.marginRight}", style = MaterialTheme.typography.labelSmall, color = BoxMarginColor)
            }
            Text("${boxModel.marginBottom}", style = MaterialTheme.typography.labelSmall, color = BoxMarginColor)
        }
    }
}

@Composable
private fun ComputedStylesSubPane(computedStyles: Map<String, String>) {
    val entries = computedStyles.entries.toList()
    val cs = MaterialTheme.colorScheme
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(entries, key = { it.key }) { (prop, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(cs.background)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = prop,
                    style = MaterialTheme.typography.labelMedium,
                    color = cs.primary
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium,
                    color = cs.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun DomMutationSubPane(
    node: DomNodeItem,
    onHideNode: (String) -> Unit,
    onEditText: (String, String) -> Unit,
    onSetAttribute: (String, String, String) -> Unit,
    onRemoveNode: (String) -> Unit,
    onAppendChildNode: (String, String, String) -> Unit,
    onHighlightNode: () -> Unit
) {
    var editedText by remember(node.path, node.textPreview) { mutableStateOf(node.textPreview) }
    var attrKey by remember(node.path) { mutableStateOf("data-state") }
    var attrVal by remember(node.path) { mutableStateOf("active") }
    var childTag by remember(node.path) { mutableStateOf("span") }
    var childText by remember(node.path) { mutableStateOf("New Badge") }
    val cs = MaterialTheme.colorScheme

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Hide Element Action (display: none !important;)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(DevAmber.copy(alpha = 0.22f))
                        .border(1.dp, DevAmber, RoundedCornerShape(50))
                        .clickable { onHideNode(node.path) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("dom_action_hide_element")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VisibilityOff,
                            contentDescription = "Hide Element (display: none !important;)",
                            tint = DevAmber,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Hide Element",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = DevAmber
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(cs.primaryContainer)
                        .border(1.dp, cs.primary, RoundedCornerShape(50))
                        .clickable { onHighlightNode() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CenterFocusStrong,
                            contentDescription = null,
                            tint = cs.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text("Scroll into View", style = MaterialTheme.typography.labelSmall, color = cs.primary)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(DevRose.copy(alpha = 0.2f))
                        .border(1.dp, DevRose, RoundedCornerShape(50))
                        .clickable { onRemoveNode(node.path) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("delete_dom_node_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = DevRose,
                            modifier = Modifier.size(13.dp)
                        )
                        Text("Remove Node", style = MaterialTheme.typography.labelSmall, color = DevRose)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = editedText,
                    onValueChange = { editedText = it },
                    label = { Text("innerText", style = MaterialTheme.typography.labelSmall) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    textStyle = TextStyle(fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = cs.onSurface),
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(cs.primary)
                        .clickable { onEditText(node.path, editedText) }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Set Text",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = cs.onPrimary
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = attrKey,
                    onValueChange = { attrKey = it },
                    label = { Text("Attribute", style = MaterialTheme.typography.labelSmall) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    textStyle = TextStyle(fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = cs.onSurface),
                    modifier = Modifier.weight(0.4f)
                )
                OutlinedTextField(
                    value = attrVal,
                    onValueChange = { attrVal = it },
                    label = { Text("Value", style = MaterialTheme.typography.labelSmall) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    textStyle = TextStyle(fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = cs.onSurface),
                    modifier = Modifier.weight(0.4f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(DevEmerald)
                        .clickable { onSetAttribute(node.path, attrKey, attrVal) }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Set Attr",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = childTag,
                    onValueChange = { childTag = it },
                    label = { Text("Child <tag>", style = MaterialTheme.typography.labelSmall) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    textStyle = TextStyle(fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = cs.onSurface),
                    modifier = Modifier.weight(0.35f)
                )
                OutlinedTextField(
                    value = childText,
                    onValueChange = { childText = it },
                    label = { Text("Child Text", style = MaterialTheme.typography.labelSmall) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    textStyle = TextStyle(fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = cs.onSurface),
                    modifier = Modifier.weight(0.45f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(DevAmber)
                        .clickable { onAppendChildNode(node.path, childTag, childText) }
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = DevSlate950, modifier = Modifier.size(12.dp))
                        Text(
                            text = "Child",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = DevSlate950
                        )
                    }
                }
            }
        }
    }
}
