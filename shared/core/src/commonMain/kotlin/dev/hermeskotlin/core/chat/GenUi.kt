package dev.hermeskotlin.core.chat

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull

sealed interface GenUiParseResult {
    data class Ok(val root: GenUiNode) : GenUiParseResult
    data class Error(val reason: String) : GenUiParseResult
}

/**
 * One component of a ```ui fence. Data only: every property except [type], [children] and [id] is kept in
 * [props] untouched, so renderers can add component types without changing the parser.
 */
class GenUiNode internal constructor(
    val type: String,
    val props: JsonObject,
    val children: List<GenUiNode>,
    val id: String?,
)

object GenUi {
    const val FENCE_LANGUAGE = "ui"
    const val MAX_DEPTH = 12
    const val MAX_NODES = 200
    const val MAX_TEXT = 4_000
    const val MAX_TABLE_ROWS = 50
    const val MAX_TABLE_COLUMNS = 8
    const val MAX_CHART_POINTS = 200

    /** Parses one JSON value: a component object, or an array wrapped in a `stack` root. */
    fun parse(code: String): GenUiParseResult {
        val root = runCatching { Json.parseToJsonElement(code) }.getOrNull()
            ?: return GenUiParseResult.Error("malformed json")
        return try {
            GenUiParseResult.Ok(Parser().root(root))
        } catch (error: ParseError) {
            GenUiParseResult.Error(error.reason)
        }
    }

    private class ParseError(val reason: String) : RuntimeException(reason)

    private class Parser {
        private var nodes = 0

        fun root(element: JsonElement): GenUiNode = when (element) {
            is JsonObject -> node(element, depth = 1)
            is JsonArray -> {
                count()
                GenUiNode("stack", JsonObject(emptyMap()), element.map { child(childObject(it), depth = 2) }, null)
            }
            else -> fail("root must be an object or array")
        }

        private fun node(obj: JsonObject, depth: Int): GenUiNode {
            if (depth > MAX_DEPTH) fail("too deep")
            count()
            val type = (obj["type"] as? JsonPrimitive)?.takeIf { it.isString }?.contentOrNull
                ?.trim()?.takeIf { it.isNotEmpty() }
                ?: fail("missing type")
            checkText(type)
            val id = when (val raw = obj["id"]) {
                null -> null
                is JsonPrimitive -> if (raw.isString) raw.content else fail("id must be a string")
                else -> fail("id must be a string")
            }
            id?.let(::checkText)
            val children = when (val raw = obj["children"]) {
                null -> emptyList()
                is JsonArray -> raw.map { child(childObject(it), depth + 1) }
                else -> fail("children must be an array")
            }
            val props = JsonObject(obj.filterKeys { it != "type" && it != "children" && it != "id" })
            checkStrings(props)
            return GenUiNode(type.lowercase(), props, children, id)
        }

        private fun childObject(element: JsonElement): JsonObject =
            element as? JsonObject ?: fail("child is not an object")

        private fun child(obj: JsonObject, depth: Int): GenUiNode = node(obj, depth)

        private fun count() {
            if (++nodes > MAX_NODES) fail("too many nodes")
        }

        private fun checkStrings(element: JsonElement) {
            when (element) {
                is JsonPrimitive -> if (element.isString) checkText(element.content)
                is JsonArray -> element.forEach(::checkStrings)
                is JsonObject -> element.forEach { (_, value) -> checkStrings(value) }
            }
        }

        private fun checkText(text: String) {
            if (text.length > MAX_TEXT) fail("string too long")
        }

        private fun fail(reason: String): Nothing = throw ParseError(reason)
    }
}

/** A string-valued property; numbers and booleans are not strings. */
fun GenUiNode.string(key: String): String? =
    (props[key] as? JsonPrimitive)?.takeIf { it.isString }?.contentOrNull

fun GenUiNode.int(key: String): Int? = (props[key] as? JsonPrimitive)?.intOrNull

fun GenUiNode.double(key: String): Double? = (props[key] as? JsonPrimitive)?.doubleOrNull

fun GenUiNode.bool(key: String): Boolean? = (props[key] as? JsonPrimitive)?.booleanOrNull

/** The string elements of an array property; non-strings are skipped. */
fun GenUiNode.stringList(key: String): List<String> =
    array(key)?.mapNotNull { (it as? JsonPrimitive)?.takeIf { primitive -> primitive.isString }?.contentOrNull }
        .orEmpty()

fun GenUiNode.array(key: String): JsonArray? = props[key] as? JsonArray

sealed interface GenUiAction {
    data class Send(val text: String?) : GenUiAction
    data class Copy(val text: String?) : GenUiAction
    data class Open(val url: String) : GenUiAction
}

/**
 * `action` is either a kind string ("send", "copy", "open") using the node's `text`/`url`, or an object:
 * `{"kind":"send","text":"..."}`, `{"kind":"copy","text":"..."}`, `{"kind":"open","url":"..."}`.
 * Action objects may also use the `send`/`copy`/`open` shorthand keys, and `{"text":"..."}` means send.
 */
fun GenUiNode.action(): GenUiAction? {
    val raw = props["action"] ?: return null
    return when (raw) {
        is JsonPrimitive -> if (raw.isString) actionForKind(raw.content) else null
        is JsonObject -> actionObject(raw)
        else -> null
    }
}

private fun GenUiNode.actionForKind(value: String): GenUiAction? =
    when (value.trim().lowercase()) {
        "send" -> GenUiAction.Send(string("text"))
        "copy" -> GenUiAction.Copy(string("text"))
        "open" -> safeOpen(string("url"))
        else -> null
    }

private fun actionObject(obj: JsonObject): GenUiAction? {
    when (obj.string("kind")?.trim()?.lowercase()) {
        "send" -> return GenUiAction.Send(obj.string("text") ?: obj.string("send"))
        "copy" -> return GenUiAction.Copy(obj.string("text") ?: obj.string("copy"))
        "open" -> return safeOpen(obj.string("url") ?: obj.string("open"))
        null -> Unit
        else -> return null
    }
    obj["send"]?.let { return GenUiAction.Send(shorthandText(it, obj)) }
    obj["copy"]?.let { return GenUiAction.Copy(shorthandText(it, obj)) }
    obj["open"]?.let { return safeOpen(shorthandText(it, obj, "url")) }
    return obj.string("text")?.let { GenUiAction.Send(it) }
}

private fun shorthandText(value: JsonElement, obj: JsonObject, key: String = "text"): String? =
    (value as? JsonPrimitive)?.takeIf { it.isString }?.contentOrNull ?: obj.string(key)

private fun safeOpen(url: String?): GenUiAction.Open? {
    val trimmed = url?.trim().orEmpty()
    val colon = trimmed.indexOf(':')
    if (colon <= 0) return null
    if (trimmed.substring(0, colon).lowercase() !in SAFE_URL_SCHEMES) return null
    if (trimmed.any { it.code < 0x20 || it.code == 0x7F }) return null
    return GenUiAction.Open(trimmed)
}

private val SAFE_URL_SCHEMES = setOf("http", "https", "mailto")
