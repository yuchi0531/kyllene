# Generative UI

Kyllene can draw a reply as live cards instead of plain Markdown. A fenced code block whose language is `ui`
carries a data-only JSON component tree; the app renders it with native Compose on Android and desktop. If the
JSON is invalid or the fence is still streaming, the block shows as an ordinary code block.

The agent side learns the protocol from the skill in `skills/kyllene-genui/SKILL.md`; this page is the client-side
reference.

## Pipeline

1. `shared/designsystem/.../MarkdownText.kt` intercepts every fence through `LocalFenceBlock`
   (`FenceBlockRenderer`). It passes the info string, the body and whether the closing fence has arrived.
2. `shared/ui/.../chat/ChatScreen.kt` provides `LocalFenceBlock` and, for `ui` fences that have closed, calls
   `GenUi.parse(code)` and draws `GenUiCard(root)`.
3. `shared/core/.../chat/GenUi.kt` parses the JSON into `GenUiNode` (type, props, children, id), enforces limits
   and maps actions.
4. `shared/ui/.../chat/GenUiCard.kt` renders the tree with the design system. Form state is `rememberSaveable`
   keyed by content and node id, so it survives scrolling.

Nothing is sent to the gateway for the card itself: it is ordinary message text and survives history replay.

## Protocol

The body is one JSON object, or an array (wrapped in a `stack`). A node is
`{"type": "...", "id"?: "...", "children"?: [...], ...props}`.

Limits: 200 nodes, depth 12, 4,000 characters per string, 50 table rows, 8 table columns, 200 chart points.
Unknown node types render their children, so newer emitters degrade gracefully.

### Components

| Type | Props |
| --- | --- |
| `card` | `title` |
| `stack`, `column` | `children` |
| `row` | `children` |
| `grid` | `columns` (1-6, default 2) |
| `divider` | - |
| `text` | `text`, `tone` |
| `heading` | `text` |
| `callout` | `text`, `title`, `tone` |
| `metric`, `kpi` | `label`, `value`, `note`, `tone` |
| `badge` | `text`, `tone` |
| `table` | `columns`, `rows` |
| `chart` | `kind` (`bar`/`line`), `labels`, `series` (`[{name, values}]`), `unit` |
| `list` | `items` (strings or `{title, detail}`) |
| `progress` | `value`, `max`, `label` |
| `code` | `text`, `language` |
| `button` | `text`, `action` |
| `choice` | `id`, `label`, `options`, `value` |
| `slider` | `id`, `label`, `min`, `max`, `step`, `value` |
| `toggle` | `id`, `label`, `value` |
| `checklist` | `id`, `label`, `options`, `values` |

Aliases are accepted for the common synonyms (`column`, `hstack`, `alert`, `kpi`, `tag`, `radio`, `range`,
`checkbox`, `multiselect`, ...).

### Actions

`button.action` is one of:

- `{"kind": "send", "event": "...", "context": {...}}` — sends the event plus current form `data` as the next
  user message.
- `{"kind": "copy", "text": "..."}`.
- `{"kind": "open", "url": "https://..."}` — http, https and mailto only.

Form values are collected by `id` into `data`; the sent text follows the A2UI tap shape already used by
`phone_show_ui` cards.

## Instruction the agent needs

Kyllene cannot change Hermes's system prompt: `prompt.submit` carries only the user's text. Instead Settings ›
Chat › **Generative UI cards** writes a skill into the gateway itself, through `POST /api/skills`
(`CapabilitiesApi.createSkill`), when the switch is turned on or the install row is tapped. The content ships in
`BundledSkills.GENUI_CONTENT` (`shared/core/.../capabilities/BundledSkills.kt`), which mirrors
`skills/kyllene-genui/SKILL.md`; its frontmatter description must stay within the gateway's 60-character index
limit. Turning the switch off disables the skill on the gateway without deleting it. Without the skill the
renderer still works, but the agent has no reason to emit the fence.

## Tests

- `shared/core/.../GenUiTest.kt` — parsing, limits, actions.
- `shared/designsystem/.../MarkdownFenceTest.kt` — fence language, body and completeness.
- `shared/ui/.../GenUiCardTest.kt` — action message and type/tone mapping.
