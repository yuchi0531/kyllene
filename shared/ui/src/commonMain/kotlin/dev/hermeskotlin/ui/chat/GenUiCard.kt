package dev.hermeskotlin.ui.chat

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import com.composeunstyled.theme.Theme
import dev.hermeskotlin.core.chat.GenUi
import dev.hermeskotlin.core.chat.GenUiAction
import dev.hermeskotlin.core.chat.GenUiNode
import dev.hermeskotlin.core.chat.action
import dev.hermeskotlin.designsystem.accent
import dev.hermeskotlin.designsystem.accentSoft
import dev.hermeskotlin.designsystem.accentText
import dev.hermeskotlin.designsystem.body
import dev.hermeskotlin.designsystem.bodySmall
import dev.hermeskotlin.designsystem.caption
import dev.hermeskotlin.designsystem.code
import dev.hermeskotlin.designsystem.colors
import dev.hermeskotlin.designsystem.components.Button
import dev.hermeskotlin.designsystem.components.ButtonSize
import dev.hermeskotlin.designsystem.components.ButtonVariant
import dev.hermeskotlin.designsystem.components.Chip
import dev.hermeskotlin.designsystem.components.CodeView
import dev.hermeskotlin.designsystem.components.Surface
import dev.hermeskotlin.designsystem.components.Switch
import dev.hermeskotlin.designsystem.components.plainTextClipEntry
import dev.hermeskotlin.designsystem.danger
import dev.hermeskotlin.designsystem.dangerSoft
import dev.hermeskotlin.designsystem.heading
import dev.hermeskotlin.designsystem.label
import dev.hermeskotlin.designsystem.radii
import dev.hermeskotlin.designsystem.radiusMedium
import dev.hermeskotlin.designsystem.stroke
import dev.hermeskotlin.designsystem.success
import dev.hermeskotlin.designsystem.successSoft
import dev.hermeskotlin.designsystem.surface2
import dev.hermeskotlin.designsystem.surface3
import dev.hermeskotlin.designsystem.text as textColor
import dev.hermeskotlin.designsystem.textMuted
import dev.hermeskotlin.designsystem.textSecondary
import dev.hermeskotlin.designsystem.textTertiary
import dev.hermeskotlin.designsystem.typography
import dev.hermeskotlin.designsystem.warning
import dev.hermeskotlin.designsystem.warningSoft
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlin.math.round
import kotlin.math.roundToLong

/**
 * Draws a `ui` fence the model wrote as native Compose: cards, text, metrics, tables, charts and the small
 * form controls whose values a Send button carries back to the agent. Text and every label come from the
 * model's own JSON, so the card adds no words of its own.
 */
@Composable
fun GenUiCard(block: GenUiNode, modifier: Modifier = Modifier) {
    val blockHash = remember(block) { genUiContentHash(block) }
    val values = remember(block) { mutableStateMapOf<String, String>() }
    val send = LocalAgentScreenSend.current
    val clipboard = LocalClipboard.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()

    Surface(modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            GenUiNodeView(
                node = block,
                path = "root",
                blockHash = blockHash,
                values = values,
                onAction = { node, action ->
                    when (action) {
                        is GenUiAction.Send -> send?.invoke(genUiSendText(action, node, values))
                        is GenUiAction.Copy -> action.text?.takeIf { it.isNotBlank() }?.let { text ->
                            scope.launch { clipboard.setClipEntry(plainTextClipEntry(text)) }
                        }
                        is GenUiAction.Open -> openGenUiUrl(action.url, uriHandler)
                    }
                },
            )
        }
    }
}

/** The card's content as one hash, so a form keeps its values while scrolling and starts fresh when the model redraws it. */
internal fun genUiContentHash(node: GenUiNode): Int {
    var hash = node.type.hashCode()
    hash = 31 * hash + node.props.toString().hashCode()
    node.children.forEach { hash = 31 * hash + genUiContentHash(it) }
    return hash
}

/** Component names the renderer knows, with the aliases a model may write for them. */
internal fun genUiType(raw: String): String = when (raw.trim().lowercase()) {
    "stack", "column", "vstack", "vertical", "form", "section" -> "stack"
    "row", "hstack", "horizontal" -> "row"
    "divider", "hr", "separator", "rule" -> "divider"
    "text", "paragraph" -> "text"
    "heading", "header", "title" -> "heading"
    "callout", "alert", "note" -> "callout"
    "metric", "kpi", "stat" -> "metric"
    "badge", "tag", "pill" -> "badge"
    "chart", "graph" -> "chart"
    "list", "items" -> "list"
    "progress", "progressbar", "progress-bar" -> "progress"
    "code", "codeblock", "code-block", "pre" -> "code"
    "button", "action" -> "button"
    "choice", "radio", "select", "segmented" -> "choice"
    "slider", "range" -> "slider"
    "toggle", "switch", "checkbox" -> "toggle"
    "checklist", "checks", "multiselect" -> "checklist"
    else -> raw.trim().lowercase()
}

/** Tones fold to the four the design system tints: neutral, good, warn, bad and info. */
internal fun genUiTone(raw: String?): String = when (raw?.trim()?.lowercase()) {
    "good", "success", "positive", "ok", "pass" -> "good"
    "warn", "warning", "caution" -> "warn"
    "bad", "danger", "error", "negative", "fail" -> "bad"
    "info", "accent", "note" -> "info"
    else -> "neutral"
}

/**
 * The message a Send button posts as the user: the event and the form's current values, in the same shape
 * [screenTapMessage] uses for a tap on an agent screen. A Send with no event sends its own text.
 */
internal fun genUiSendText(action: GenUiAction.Send, node: GenUiNode, values: Map<String, String>): String {
    val event = node.eventName()?.takeIf { it.isNotBlank() }
    if (event == null) return action.text?.takeIf { it.isNotBlank() } ?: "Tapped a button on the card you showed."
    val context = node.props["context"] as? JsonObject
    val data = buildJsonObject {
        (node.props["data"] as? JsonObject)?.forEach { (key, value) -> put(key, value) }
        values.forEach { (key, value) -> if (key.isNotBlank()) put(key, JsonPrimitive(value)) }
    }
    val more = buildJsonObject {
        context?.takeIf { it.isNotEmpty() }?.let { put("context", it) }
        if (data.isNotEmpty()) put("data", data)
    }
    val said = "Tapped “$event” on the card you showed"
    return if (more.isEmpty()) "$said." else "$said: $more"
}

@Composable
private fun GenUiNodeView(
    node: GenUiNode,
    path: String,
    blockHash: Int,
    values: MutableMap<String, String>,
    onAction: (GenUiNode, GenUiAction) -> Unit,
) {
    when (genUiType(node.type)) {
        "card", "stack" -> GenUiStack(node, path, blockHash, values, onAction)
        "row" -> GenUiRow(node, path, blockHash, values, onAction)
        "grid" -> GenUiGrid(node, path, blockHash, values, onAction)
        "divider" -> Box(Modifier.fillMaxWidth().height(1.dp).background(Theme[colors][stroke]))
        "text" -> GenUiText(node)
        "heading" -> GenUiHeading(node)
        "callout" -> GenUiCallout(node)
        "metric" -> GenUiMetric(node)
        "badge" -> GenUiBadge(node)
        "table" -> GenUiTable(node)
        "chart" -> GenUiChart(node)
        "list" -> GenUiList(node)
        "progress" -> GenUiProgress(node)
        "code" -> GenUiCode(node)
        "button" -> GenUiButton(node, onAction)
        "choice" -> GenUiChoice(node, path, blockHash, values)
        "slider" -> GenUiSlider(node, path, blockHash, values)
        "toggle" -> GenUiToggle(node, path, blockHash, values)
        "checklist" -> GenUiChecklist(node, path, blockHash, values)
        else -> GenUiChildren(node, path, blockHash, values, onAction)
    }
}

@Composable
private fun GenUiStack(
    node: GenUiNode,
    path: String,
    blockHash: Int,
    values: MutableMap<String, String>,
    onAction: (GenUiNode, GenUiAction) -> Unit,
) {
    val title = node.propString("title")
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (title != null) {
            Text(title, style = Theme[typography][heading], color = Theme[colors][textColor])
        }
        node.children.forEachIndexed { index, child ->
            GenUiNodeView(child, "$path/$index", blockHash, values, onAction)
        }
    }
}

@Composable
private fun GenUiRow(
    node: GenUiNode,
    path: String,
    blockHash: Int,
    values: MutableMap<String, String>,
    onAction: (GenUiNode, GenUiAction) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        node.children.forEachIndexed { index, child ->
            GenUiNodeView(child, "$path/$index", blockHash, values, onAction)
        }
    }
}

@Composable
private fun GenUiGrid(
    node: GenUiNode,
    path: String,
    blockHash: Int,
    values: MutableMap<String, String>,
    onAction: (GenUiNode, GenUiAction) -> Unit,
) {
    val columns = (node.propInt("columns") ?: 2).coerceIn(1, 6)
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        node.children.chunked(columns).forEachIndexed { rowIndex, cells ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                cells.forEachIndexed { columnIndex, child ->
                    Box(Modifier.weight(1f)) {
                        GenUiNodeView(child, "$path/${rowIndex * columns + columnIndex}", blockHash, values, onAction)
                    }
                }
                repeat(columns - cells.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun GenUiChildren(
    node: GenUiNode,
    path: String,
    blockHash: Int,
    values: MutableMap<String, String>,
    onAction: (GenUiNode, GenUiAction) -> Unit,
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        node.children.forEachIndexed { index, child ->
            GenUiNodeView(child, "$path/$index", blockHash, values, onAction)
        }
    }
}

@Composable
private fun GenUiText(node: GenUiNode) {
    val text = node.propString("text") ?: node.propString("content") ?: return
    Text(text, style = Theme[typography][body], color = toneForeground(node.propString("tone")))
}

@Composable
private fun GenUiHeading(node: GenUiNode) {
    val text = node.propString("text") ?: node.propString("title") ?: return
    Text(text, style = Theme[typography][heading], color = toneForeground(node.propString("tone")))
}

@Composable
private fun GenUiCallout(node: GenUiNode) {
    val text = node.propString("text") ?: node.propString("content") ?: return
    val tone = genUiTone(node.propString("tone"))
    val (fill, foreground) = toneColors(tone)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Theme[radii][radiusMedium]))
            .background(fill)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        node.propString("title")?.let {
            Text(it, style = Theme[typography][label].copy(fontWeight = FontWeight.SemiBold), color = foreground)
        }
        Text(text, style = Theme[typography][body], color = foreground)
    }
}

@Composable
private fun GenUiMetric(node: GenUiNode) {
    val label = node.propString("label")
    val value = node.propString("value") ?: return
    val note = node.propString("note")
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        if (label != null) {
            Text(label, style = Theme[typography][caption], color = Theme[colors][textTertiary])
        }
        Text(value, style = Theme[typography][heading], color = toneForeground(node.propString("tone")))
        if (note != null) {
            Text(note, style = Theme[typography][caption], color = Theme[colors][textMuted])
        }
    }
}

@Composable
private fun GenUiBadge(node: GenUiNode) {
    val text = node.propString("text") ?: node.propString("label") ?: return
    val (fill, foreground) = toneColors(genUiTone(node.propString("tone")))
    Row(Modifier.fillMaxWidth()) {
        Box(Modifier.clip(CircleShape).background(fill).padding(horizontal = 8.dp, vertical = 3.dp)) {
            Text(text, style = Theme[typography][label], color = foreground)
        }
    }
}

@Composable
private fun GenUiTable(node: GenUiNode) {
    val columns = node.stringList("columns").take(GenUi.MAX_TABLE_COLUMNS)
    val rows = node.propArray("rows")
        ?.mapNotNull { it as? JsonArray }
        ?.take(GenUi.MAX_TABLE_ROWS)
        ?: emptyList()
    val width = maxOf(columns.size, rows.maxOfOrNull { it.size } ?: 0)
    if (width == 0) return
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Theme[radii][radiusMedium]))
            .border(1.dp, Theme[colors][stroke], RoundedCornerShape(Theme[radii][radiusMedium])),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        if (columns.isNotEmpty()) {
            GenUiTableRow(columns.map { it }.toList(), header = true, width = width)
        }
        rows.forEachIndexed { index, row ->
            if (index > 0 || columns.isNotEmpty()) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(Theme[colors][stroke]))
            }
            GenUiTableRow(row.map { cellText(it) }, header = false, width = width)
        }
    }
}

@Composable
private fun GenUiTableRow(cells: List<String>, header: Boolean, width: Int) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 7.dp)) {
        repeat(width) { index ->
            Text(
                cells.getOrNull(index).orEmpty(),
                style = if (header) Theme[typography][label] else Theme[typography][bodySmall],
                color = if (header) Theme[colors][textTertiary] else Theme[colors][textColor],
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun GenUiChart(node: GenUiNode) {
    val labels = node.stringList("labels").take(GenUi.MAX_CHART_POINTS)
    val series = remember(node) { node.chartSeries() }
    val line = node.propString("kind")?.trim()?.lowercase() == "line"
    val points = series.maxOfOrNull { it.values.size } ?: 0
    if (series.isEmpty() || points == 0) return
    val seriesColors = listOf(
        Theme[colors][accent],
        Theme[colors][success],
        Theme[colors][warning],
        Theme[colors][danger],
    )
    val trackHeight = 128.dp
    Canvas(Modifier.fillMaxWidth().height(trackHeight)) {
        val max = series.flatMap { it.values }.maxOrNull()?.takeIf { it > 0.0 } ?: 1.0
        val height = size.height
        if (line) {
            val denominator = (points - 1).coerceAtLeast(1)
            series.forEachIndexed { index, s ->
                val path = Path()
                s.values.forEachIndexed { pointIndex, value ->
                    val x = if (points == 1) size.width / 2 else size.width * pointIndex / denominator
                    val y = height - (value / max).toFloat() * height
                    if (pointIndex == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, seriesColors[index % seriesColors.size], style = Stroke(width = 2.dp.toPx()))
            }
        } else {
            val slot = size.width / points
            val bar = (slot / (series.size + 1)).coerceAtLeast(1f)
            series.forEachIndexed { index, s ->
                s.values.forEachIndexed { pointIndex, value ->
                    val barHeight = (value / max).toFloat() * height
                    drawRect(
                        color = seriesColors[index % seriesColors.size],
                        topLeft = Offset(pointIndex * slot + bar * (index + 1), height - barHeight),
                        size = Size(bar, barHeight),
                    )
                }
            }
        }
    }
    if (labels.isNotEmpty()) {
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            labels.forEach { Text(it, style = Theme[typography][caption], color = Theme[colors][textTertiary]) }
        }
    }
    val names = series.mapNotNull { it.name }
    if (names.isNotEmpty()) {
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            names.forEachIndexed { index, name ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(seriesColors[index % seriesColors.size]))
                    Text(name, style = Theme[typography][caption], color = Theme[colors][textTertiary])
                }
            }
        }
    }
}

@Composable
private fun GenUiList(node: GenUiNode) {
    val items = node.propArray("items") ?: node.propArray("children") ?: return
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        items.forEach { item ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                Box(Modifier.padding(top = 5.dp).size(5.dp).clip(CircleShape).background(Theme[colors][accent]))
                when (item) {
                    is JsonObject -> Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        val title = (item["title"] as? JsonPrimitive)?.contentOrNull
                            ?: (item["text"] as? JsonPrimitive)?.contentOrNull
                        val detail = (item["detail"] as? JsonPrimitive)?.contentOrNull
                            ?: (item["note"] as? JsonPrimitive)?.contentOrNull
                        if (title != null) Text(title, style = Theme[typography][body], color = Theme[colors][textColor])
                        if (detail != null) Text(detail, style = Theme[typography][caption], color = Theme[colors][textTertiary])
                    }
                    else -> Text(cellText(item), style = Theme[typography][body], color = Theme[colors][textColor], modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun GenUiProgress(node: GenUiNode) {
    val labelText = node.propString("label")
    val value = node.propDouble("value") ?: 0.0
    val max = (node.propDouble("max") ?: 100.0).takeIf { it > 0.0 } ?: 100.0
    val fraction = (value / max).coerceIn(0.0, 1.0).toFloat()
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (labelText != null) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(labelText, style = Theme[typography][label], color = Theme[colors][textSecondary])
                Text(genUiNumberText(fraction * 100.0) + "%", style = Theme[typography][caption], color = Theme[colors][textTertiary])
            }
        }
        Box(
            Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(Theme[colors][surface3]),
        ) {
            Box(Modifier.fillMaxWidth(fraction).height(6.dp).clip(CircleShape).background(Theme[colors][accent]))
        }
    }
}

@Composable
private fun GenUiCode(node: GenUiNode) {
    val text = node.propString("text") ?: node.propString("code") ?: return
    CodeView(
        text = text,
        language = node.propString("language"),
        modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp),
    )
}

@Composable
private fun GenUiButton(node: GenUiNode, onAction: (GenUiNode, GenUiAction) -> Unit) {
    val action = node.action() ?: return
    val text = node.propString("text") ?: node.propString("label") ?: node.eventName() ?: return
    val variant = when (genUiTone(node.propString("tone"))) {
        "bad" -> ButtonVariant.Danger
        "info" -> ButtonVariant.Primary
        "neutral" -> ButtonVariant.Secondary
        else -> ButtonVariant.Outline
    }
    Button(
        text = text,
        onClick = { onAction(node, action) },
        variant = variant,
        size = ButtonSize.Small,
    )
}

@Composable
private fun GenUiChoice(
    node: GenUiNode,
    path: String,
    blockHash: Int,
    values: MutableMap<String, String>,
) {
    val options = node.options()
    val fieldKey = node.fieldKey(path)
    var selected by rememberSaveable(blockHash, path) {
        mutableStateOf(node.propString("value").orEmpty())
    }
    SideEffect { values[fieldKey] = selected }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        node.propString("label")?.let {
            Text(it, style = Theme[typography][label], color = Theme[colors][textSecondary])
        }
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            options.forEach { option ->
                Chip(
                    text = option.second,
                    selected = selected == option.first,
                    onClick = { selected = option.first },
                )
            }
        }
    }
}

@Composable
private fun GenUiSlider(
    node: GenUiNode,
    path: String,
    blockHash: Int,
    values: MutableMap<String, String>,
) {
    val min = node.propDouble("min") ?: 0.0
    val max = (node.propDouble("max") ?: 100.0).takeIf { it > min } ?: (min + 100.0)
    val step = (node.propDouble("step") ?: 1.0).takeIf { it > 0.0 } ?: 1.0
    val initial = (node.propDouble("value") ?: min).coerceIn(min, max)
    val fieldKey = node.fieldKey(path)
    var value by rememberSaveable(blockHash, path) { mutableStateOf(initial) }
    SideEffect { values[fieldKey] = genUiNumberText(value) }
    fun setFrom(x: Float, width: Int) {
        if (width <= 0) return
        val raw = min + (x / width.toFloat()).coerceIn(0f, 1f).toDouble() * (max - min)
        val snapped = min + ((raw - min) / step).roundToLong() * step
        value = snapped.coerceIn(min, max)
    }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                node.propString("label") ?: "",
                style = Theme[typography][label],
                color = Theme[colors][textSecondary],
            )
            Text(genUiNumberText(value), style = Theme[typography][caption], color = Theme[colors][textTertiary])
        }
        val trackColor = Theme[colors][surface3]
        val accentColor = Theme[colors][accent]
        val thumbColor = Theme[colors][accent]
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(34.dp)
                .pointerInput(min, max, step) {
                    detectDragGestures(
                        onDragStart = { offset -> setFrom(offset.x, size.width) },
                        onDrag = { change, _ ->
                            change.consume()
                            setFrom(change.position.x, size.width)
                        },
                    )
                }
                .pointerInput(min, max, step) {
                    detectTapGestures { offset -> setFrom(offset.x, size.width) }
                },
        ) {
            val track = 6.dp.toPx()
            val centerY = size.height / 2f
            val fraction = ((value - min) / (max - min)).toFloat().coerceIn(0f, 1f)
            drawRoundRect(
                color = trackColor,
                topLeft = Offset(0f, centerY - track / 2),
                size = Size(size.width, track),
                cornerRadius = CornerRadius(track / 2),
            )
            drawRoundRect(
                color = accentColor,
                topLeft = Offset(0f, centerY - track / 2),
                size = Size(size.width * fraction, track),
                cornerRadius = CornerRadius(track / 2),
            )
            drawCircle(thumbColor, radius = 8.dp.toPx(), center = Offset(size.width * fraction, centerY))
        }
    }
}

@Composable
private fun GenUiToggle(
    node: GenUiNode,
    path: String,
    blockHash: Int,
    values: MutableMap<String, String>,
) {
    val fieldKey = node.fieldKey(path)
    var checked by rememberSaveable(blockHash, path) { mutableStateOf(node.propBool("value") ?: false) }
    SideEffect { values[fieldKey] = checked.toString() }
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { checked = !checked }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            node.propString("label") ?: node.propString("text") ?: "",
            style = Theme[typography][body],
            color = Theme[colors][textColor],
            modifier = Modifier.weight(1f),
        )
        Switch(checked = checked)
    }
}

@Composable
private fun GenUiChecklist(
    node: GenUiNode,
    path: String,
    blockHash: Int,
    values: MutableMap<String, String>,
) {
    val options = node.options()
    val fieldKey = node.fieldKey(path)
    var selected by rememberSaveable(blockHash, path) {
        mutableStateOf(node.stringList("values").joinToString(","))
    }
    val chosen = selected.split(',').filter { it.isNotBlank() }.toSet()
    SideEffect { values[fieldKey] = chosen.joinToString(",") }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        node.propString("label")?.let {
            Text(it, style = Theme[typography][label], color = Theme[colors][textSecondary])
        }
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            options.forEach { option ->
                Chip(
                    text = option.second,
                    selected = option.first in chosen,
                    onClick = {
                        selected = if (option.first in chosen) {
                            (chosen - option.first).joinToString(",")
                        } else {
                            (chosen + option.first).joinToString(",")
                        }
                    },
                )
            }
        }
    }
}

private fun GenUiNode.propString(key: String): String? =
    (props[key] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() }

private fun GenUiNode.propInt(key: String): Int? =
    (props[key] as? JsonPrimitive)?.contentOrNull?.toIntOrNull()

private fun GenUiNode.propDouble(key: String): Double? =
    (props[key] as? JsonPrimitive)?.contentOrNull?.toDoubleOrNull()

private fun GenUiNode.propBool(key: String): Boolean? = when ((props[key] as? JsonPrimitive)?.contentOrNull?.lowercase()) {
    "true", "1", "yes", "on" -> true
    "false", "0", "no", "off" -> false
    else -> null
}

private fun GenUiNode.propArray(key: String): JsonArray? = props[key] as? JsonArray

private fun GenUiNode.stringList(key: String): List<String> =
    propArray(key)?.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }.orEmpty()

private fun GenUiNode.eventName(): String? =
    propString("event") ?: ((props["action"] as? JsonObject)?.get("event") as? JsonPrimitive)?.contentOrNull

private fun GenUiNode.fieldKey(path: String): String = nodeIdString() ?: path

private fun GenUiNode.nodeIdString(): String? = id?.takeIf { it.isNotBlank() }

private fun cellText(element: JsonElement): String = when (element) {
    is JsonNull -> ""
    is JsonPrimitive -> element.contentOrNull.orEmpty()
    else -> element.toString()
}

private fun GenUiNode.options(): List<Pair<String, String>> =
    propArray("options")?.mapNotNull { option ->
        when (option) {
            is JsonPrimitive -> option.contentOrNull?.let { it to it }
            is JsonObject -> {
                val value = (option["value"] as? JsonPrimitive)?.contentOrNull
                    ?: (option["id"] as? JsonPrimitive)?.contentOrNull
                    ?: (option["label"] as? JsonPrimitive)?.contentOrNull
                    ?: (option["text"] as? JsonPrimitive)?.contentOrNull
                val label = (option["label"] as? JsonPrimitive)?.contentOrNull
                    ?: (option["text"] as? JsonPrimitive)?.contentOrNull
                    ?: value
                if (value != null && label != null) value to label else null
            }
            else -> null
        }
    }.orEmpty()

private data class GenUiSeries(val name: String?, val values: List<Double>)

private fun GenUiNode.chartSeries(): List<GenUiSeries> {
    val fromSeries = propArray("series")?.mapNotNull { element ->
        when (element) {
            is JsonObject -> {
                val values = (element["values"] as? JsonArray)?.mapNotNull { value ->
                    (value as? JsonPrimitive)?.contentOrNull?.toDoubleOrNull()
                }.orEmpty().take(GenUi.MAX_CHART_POINTS)
                val name = (element["name"] as? JsonPrimitive)?.contentOrNull
                if (values.isEmpty()) null else GenUiSeries(name, values)
            }
            is JsonArray -> {
                val values = element.mapNotNull { value ->
                    (value as? JsonPrimitive)?.contentOrNull?.toDoubleOrNull()
                }.take(GenUi.MAX_CHART_POINTS)
                if (values.isEmpty()) null else GenUiSeries(null, values)
            }
            else -> null
        }
    }.orEmpty()
    if (fromSeries.isNotEmpty()) return fromSeries
    val values = propArray("values")?.mapNotNull { value ->
        (value as? JsonPrimitive)?.contentOrNull?.toDoubleOrNull()
    }.orEmpty().take(GenUi.MAX_CHART_POINTS)
    return if (values.isEmpty()) emptyList() else listOf(GenUiSeries(null, values))
}

private fun openGenUiUrl(url: String?, uriHandler: androidx.compose.ui.platform.UriHandler) {
    val target = url?.trim()?.takeIf { it.isNotBlank() } ?: return
    val scheme = target.substringBefore(':', "").lowercase()
    if (scheme == "http" || scheme == "https" || scheme == "mailto") uriHandler.openUri(target)
}

@Composable
private fun toneForeground(rawTone: String?): Color = when (genUiTone(rawTone)) {
    "good" -> Theme[colors][success]
    "warn" -> Theme[colors][warning]
    "bad" -> Theme[colors][danger]
    "info" -> Theme[colors][accentText]
    else -> Theme[colors][textColor]
}

@Composable
private fun toneColors(tone: String): Pair<Color, Color> = when (tone) {
    "good" -> Theme[colors][successSoft] to Theme[colors][success]
    "warn" -> Theme[colors][warningSoft] to Theme[colors][warning]
    "bad" -> Theme[colors][dangerSoft] to Theme[colors][danger]
    "info" -> Theme[colors][accentSoft] to Theme[colors][accentText]
    else -> Theme[colors][surface2] to Theme[colors][textSecondary]
}

private fun genUiNumberText(value: Double): String {
    val rounded = round(value * 1000.0) / 1000.0
    return if (rounded == rounded.toLong().toDouble()) rounded.toLong().toString() else rounded.toString()
}
