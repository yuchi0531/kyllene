package dev.hermeskotlin.core.chat

import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GenUiTest {

    @Test
    fun aCardTreeParsesWithItsPropsAndChildren() {
        val root = parseRoot(
            """
            {"type":"Card","id":"root","title":"Hello","revision":2,"children":[
              {"type":"Text","text":"Hi"},
              {"type":"Row","children":[{"type":"Button","text":"Go","action":{"kind":"send","text":"go"}}]}
            ]}
            """.trimIndent(),
        )
        assertEquals("card", root.type)
        assertEquals("root", root.id)
        assertEquals("Hello", root.string("title"))
        assertEquals(2, root.int("revision"))
        assertEquals(2, root.children.size)
        assertEquals("text", root.children[0].type)
        assertEquals(GenUiAction.Send("go"), root.children[1].children.single().action())
        assertTrue("type" !in root.props)
        assertTrue("children" !in root.props)
        assertTrue("id" !in root.props)
    }

    @Test
    fun anArrayRootBecomesAStack() {
        val root = parseRoot("""[{"type":"text","text":"one"},{"type":"text","text":"two"}]""")
        assertEquals(GenUi.FENCE_LANGUAGE, "ui")
        assertEquals("stack", root.type)
        assertNull(root.id)
        assertTrue(root.children.all { it.type == "text" })
        assertEquals(listOf("one", "two"), root.children.map { it.string("text") })
    }

    @Test
    fun malformedJsonAndWrongRootsAreErrors() {
        assertError(GenUi.parse("{not json"), "json")
        assertError(GenUi.parse("42"), "root")
        assertError(GenUi.parse("null"), "root")
    }

    @Test
    fun aMissingOrBlankTypeIsAnError() {
        assertError(GenUi.parse("""{"children":[]}"""), "type")
        assertError(GenUi.parse("""{"type":"   "}"""), "type")
        assertError(GenUi.parse("""{"type":3}"""), "type")
        assertError(GenUi.parse("""{"type":"card","children":["not a node"]}"""), "child")
        assertError(GenUi.parse("""{"type":"card","children":{}}"""), "children")
    }

    @Test
    fun depthIsLimited() {
        assertIs<GenUiParseResult.Ok>(GenUi.parse(nested(GenUi.MAX_DEPTH)))
        assertError(GenUi.parse(nested(GenUi.MAX_DEPTH + 1)), "deep")
    }

    @Test
    fun nodeCountIsLimited() {
        assertIs<GenUiParseResult.Ok>(GenUi.parse(flat(GenUi.MAX_NODES - 1)))
        assertError(GenUi.parse(flat(GenUi.MAX_NODES)), "nodes")
    }

    @Test
    fun unknownFieldsAreToleratedAndKept() {
        val root = parseRoot(
            """{"type":"card","future":{"nested":[1,2,3]},"unknown":"kept","children":[]}""",
        )
        assertTrue(root.props.containsKey("future"))
        assertEquals("kept", root.string("unknown"))
    }

    @Test
    fun nodeAccessorsReadTheirTypes() {
        val node = parseRoot(
            """{"type":"card","title":"T","count":3,"ratio":1.5,"enabled":true,"tags":["a","b",7],"list":[{}]}""",
        )
        assertEquals("T", node.string("title"))
        assertEquals(3, node.int("count"))
        assertEquals(1.5, node.double("ratio"))
        assertEquals(true, node.bool("enabled"))
        assertEquals(listOf("a", "b"), node.stringList("tags"))
        assertEquals(1, node.array("list")?.size)
        assertNull(node.string("count"))
        assertNull(node.int("title"))
        assertNull(node.array("title"))
        assertEquals(emptyList<String>(), node.stringList("missing"))
    }

    @Test
    fun actionVariantsAreRead() {
        assertEquals(GenUiAction.Send("Say"), nodeWithAction(""" "send" """).action())
        assertEquals(GenUiAction.Copy("Copy me"), nodeWithAction(""" "copy" """, text = "Copy me").action())
        assertEquals(
            GenUiAction.Open("https://example.com/a"),
            nodeWithAction(""" "open" """, url = "https://example.com/a").action(),
        )
        assertEquals(
            GenUiAction.Send("Hi"),
            nodeWithAction("""{"kind":"send","text":"Hi"}""").action(),
        )
        assertEquals(
            GenUiAction.Copy("Hi"),
            nodeWithAction("""{"kind":"copy","text":"Hi"}""").action(),
        )
        assertEquals(
            GenUiAction.Open("mailto:hi@example.com"),
            nodeWithAction("""{"kind":"open","url":"mailto:hi@example.com"}""").action(),
        )
        assertEquals(GenUiAction.Send("Hi"), nodeWithAction("""{"send":"Hi"}""").action())
        assertEquals(GenUiAction.Copy("Hi"), nodeWithAction("""{"copy":"Hi"}""").action())
        assertEquals(GenUiAction.Open("https://example.com"), nodeWithAction("""{"open":"https://example.com"}""").action())
        assertEquals(GenUiAction.Send("Hi"), nodeWithAction("""{"text":"Hi"}""").action())
        assertEquals(GenUiAction.Send(null), nodeWithAction("""{"kind":"send"}""").action())
        assertNull(nodeWithAction("""{"kind":"unknown"}""").action())
        assertNull(parseRoot("""{"type":"button"}""").action())
    }

    @Test
    fun onlySafeOpenUrlsAreActions() {
        for (url in listOf("javascript:alert(1)", "file:///etc/passwd", "data:text/html,hi", "cmd:calc", "no-scheme")) {
            assertNull(openNode(url).action(), url)
        }
        assertNull(openNode(null).action())
    }

    @Test
    fun stringsAtTheTextLimitAreAcceptedAndLongerOnesAreErrors() {
        val exact = buildJsonObject {
            put("type", "text")
            put("text", "a".repeat(GenUi.MAX_TEXT))
        }
        assertIs<GenUiParseResult.Ok>(GenUi.parse(exact.toString()))

        val tooLong = buildJsonObject {
            put("type", "text")
            put("text", "a".repeat(GenUi.MAX_TEXT + 1))
        }
        assertError(GenUi.parse(tooLong.toString()), "string")

        val nested = buildJsonObject {
            put("type", "card")
            put(
                "future",
                buildJsonObject { put("value", "b".repeat(GenUi.MAX_TEXT + 1)) },
            )
        }
        assertError(GenUi.parse(nested.toString()), "string")
    }

    private fun parseRoot(code: String): GenUiNode = assertIs<GenUiParseResult.Ok>(GenUi.parse(code)).root

    private fun assertError(result: GenUiParseResult, reason: String): GenUiParseResult.Error {
        val error = assertIs<GenUiParseResult.Error>(result)
        assertTrue(reason in error.reason, "expected '$reason' in '${error.reason}'")
        return error
    }

    private fun nodeWithAction(action: String, url: String? = null, text: String = "Say"): GenUiNode {
        val props = buildJsonObject {
            put("type", "button")
            put("text", text)
            if (url != null) put("url", url)
            put("action", kotlinx.serialization.json.Json.parseToJsonElement(action.trim()))
        }
        return parseRoot(props.toString())
    }

    private fun openNode(url: String?): GenUiNode =
        parseRoot(
            buildJsonObject {
                put("type", "button")
                put(
                    "action",
                    buildJsonObject {
                        put("kind", "open")
                        if (url != null) put("url", url)
                    },
                )
            }.toString(),
        )

    private fun nested(depth: Int): String {
        var json = """{"type":"text"}"""
        repeat(depth - 1) { json = """{"type":"box","children":[$json]}""" }
        return json
    }

    private fun flat(children: Int): String =
        buildJsonObject {
            put("type", "stack")
            put(
                "children",
                buildJsonArray {
                    repeat(children) { add(buildJsonObject { put("type", "text") }) }
                },
            )
        }.toString()
}
