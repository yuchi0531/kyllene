---
name: kyllene-genui
description: Draw interactive cards from ```ui JSON blocks in Kyllene.
tags:
  - ui
  - genui
  - kyllene
---

# Kyllene generative UI

Kyllene renders a fenced code block whose language is `ui` as native cards when the JSON is valid. If it is
not valid, Kyllene shows the block as ordinary code, so a mistake is harmless.

## Rules

- Emit one ` ```ui ` fence. The body is a single JSON object (a component tree) or a JSON array (Kyllene wraps it
  in a vertical stack).
- Data only: never HTML, scripts, SVG or SQL. No expressions, variables or template syntax. Every value is a
  literal JSON value you compute yourself.
- Put the card after the sentence that introduces it. Do not repeat the data in prose.
- Keep it compact: at most 200 components, depth 12, table 50 rows / 8 columns, charts 200 points, strings 4,000
  characters. The parser rejects anything larger.
- Prefer a card when the reply would otherwise be a table, a comparison, a set of numbers, a form, or a set of
  next-step buttons.

## Envelope

```ui
{
  "type": "card",
  "title": "Estimate",
  "children": [
    {"type": "metric", "label": "Total", "value": "$464"},
    {"type": "table", "columns": ["Item", "Cost"], "rows": [["Seats", "$290"], ["Add-ons", "$174"]]},
    {"type": "button", "text": "Ask about the Pro plan", "action": {"kind": "send", "event": "ask", "context": {"plan": "Pro"}}}
  ]
}
```

## Components

Layout: `card` (title), `stack`/`column`, `row`, `grid` (columns 1-6, default 2), `divider`.
Content: `text`, `heading`, `callout` (text, title, tone), `metric`/`kpi` (label, value, note), `badge` (text),
`table` (columns, rows), `chart` (kind bar|line, labels, series `[{name, values}]`, unit), `list` (items:
strings or `{title, detail}`), `progress` (value, max, label), `code` (text, language).
Inputs: `choice` (id, label, options, value), `slider` (id, label, min, max, step, value), `toggle` (id, label,
value), `checklist` (id, label, options, values).
Actions: `button` (text, action).

Tones: `neutral`, `good`, `warn`, `bad`, `info`.

## Actions

A button's `action` is one of:

- `{"kind": "send", "event": "ask", "context": {"plan": "Pro"}}` — sends the event name plus the form values
  back as the next user message. Use this for "ask a follow-up", "apply this filter", "choose this option".
- `{"kind": "copy", "text": "..."}` — copies literal text to the clipboard.
- `{"kind": "open", "url": "https://..."}` — opens an http, https or mailto URL.

When a button is clicked, Kyllene sends a message shaped like:

```
Tapped "ask" on the card you showed: {"context":{"plan":"Pro"},"data":{"seats":12}}
```

`data` holds the current values of the card's sliders, choices, toggles and checklists, keyed by their `id`.

Note: a `send` action becomes the next user message; it does not block the current turn like a tool call would.

## Style

- Use `metric` for one or two headline numbers, `table` for up to a dozen rows, `chart` for a trend, and
  `list` for short option sets.
- Keep labels short. Do not put a whole paragraph in a card; keep prose above it.
- A card with buttons should offer the next actions the user is likely to take, not every possibility.
