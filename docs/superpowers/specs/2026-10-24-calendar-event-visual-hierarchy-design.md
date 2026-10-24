# Calendar Event Visual Hierarchy & Integration Design

## Intent & The Why
In Procrastaint, actionable tasks are the primary purpose of the application. Currently, external calendar events are rendered as full-size cards with prominent accent colors and tinted containers, competing with or even overshadowing actionable tasks in the agenda feed. The goal is to visually demote calendar events into quiet contextual schedule markers while maintaining strict chronological fidelity, and to allow users to tap an event to view it in their native calendar app.

---

## Goals, Non-Goals, & Anti-Goals

### Goals
- Demote calendar events so actionable tasks remain the unmistakable primary focus of the agenda.
- Retain strict chronological sequence for timed events relative to tasks throughout each day.
- Dock all-day events directly beneath the sticky date header as compact, quiet banners.
- Render timed events as slim, subtle schedule dividers with horizontal hairline gradient rules.
- Redirect users to their respective calendar app when tapping a calendar event via Android intents.
- Adhere strictly to the project-wide No-Emoji Policy across all components, strings, and icons.

### Non-Goals
- Modifying the task synchronization or Google Calendar API integration logic in the shared module.
- Allowing editing or deleting external calendar events directly inside Procrastaint (events remain read-only).
- Altering the iOS UI implementation in this iteration (Android Agenda feed focus).

### Anti-Goals
- Hiding calendar events completely or decoupling them from the chronological flow of the day.
- Using emojis or overly colorful indicators that restore visual dominance to calendar events.

---

## Constraints
- **Strict No-Emoji Policy:** Absolute prohibition on emojis in UI strings, vector fallbacks, logs, or comments (as codified in [`AGENTS.md`](file:///Users/pahina/Documents/Procrastaint/AGENTS.md#L246-L248)). Use standard Material Icons (`Icons.Outlined.*`) or clear text descriptions instead.
- **KMP Boundaries:** Keep Android-specific APIs (such as `android.content.Intent` and `android.provider.CalendarContract`) in `androidApp`.
- **Detekt & Code Style:** Must pass `./gradlew detektall` cleanly.

---

## Approaches Considered

### 1. Component Re-architecture (Selected)
- **Description:** Partition day events into all-day and timed events inside `AgendaFeed`. Display all-day events under the sticky header via an all-day banner, and transform timed events into `ScheduleDivider` rows with low-opacity calendar tints and hairline rules. Provide an `onEventClick` lambda that fires a `CalendarContract` view Intent with a safe fallback.
- **Trade-offs:** Highly focused, low risk, preserves `LazyColumn` item recycling, zero changes to shared models.
- **Decision:** Selected by user for direct effectiveness and clean architecture.

### 2. Full ScheduleItem Timeline Engine Refactor
- **Description:** Unify tasks and events under the shared `ScheduleItem` polymorphic interface and rebuild `AgendaFeed` with dynamic time-gap spacing and continuous timeline tracks.
- **Trade-offs:** Higher complexity, touches shared multiplatform contracts unnecessarily, and introduces extra layout overhead for an Android-specific visual requirement.
- **Decision:** Dropped due to unnecessary scope and churn.

---

## Design & Behavioral Specifications

### 1. Visual Presentation (Option C Pattern)
- **All-Day Events:**
  - Rendered beneath the sticky `AgendaDateHeader` for dates containing all-day events.
  - Subdued container with 8% accent tint, 1px subtle border, rounded corners (8.dp).
  - Displays a small textual `ALL DAY` badge, event title, and calendar source label (`Google` or `Device`), using Material Theme typography (`labelSmall` / `bodySmall`).
- **Timed Events:**
  - Sorted chronologically in the day list alongside tasks.
  - Rendered as a centered pill badge (`ScheduleDivider`) flanked on both sides by 1px gradient horizontal hairline rules fading outward to transparent.
  - Displays time window (e.g., `11:00 AM - 12:00 PM`), a subtle separator (`|`), event title, and optional location string in small muted text.
  - Background has 8%–10% opacity of the calendar accent color with a 1px border at 20% opacity.
  - No checkboxes, no heavy cards.

### 2. Tap Interaction & Intent Redirection
- When the user taps either an all-day event banner or a timed event divider:
  - Attempt to launch an `ACTION_VIEW` intent with `ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId.toLongOrNull() ?: ...)` or `CalendarContract.CONTENT_URI.buildUpon().appendPath("time").appendPath(startTime.toString()).build()`.
  - Wrap in a `try-catch (ActivityNotFoundException / SecurityException)` block to prevent crashes if no calendar app is installed.
  - Fallback: launch calendar app via `Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_CALENDAR)`.

### 3. Empty State Handling
- If a date has external events but no scheduled tasks:
  - Show the date header, event banners/dividers, and a quiet text item indicating `"No tasks scheduled"` to reinforce that tasks are the primary purpose of the view.

---

## What's Left to the Builder
- Fine-tuning exact DP measurements, alpha levels, and typography tokens matching Procrastaint's Material 3 theme.
- Extracting the event click intent logic into an Android utility function (e.g. `CalendarIntentUtils.kt` or directly in `AgendaFeed`).
- Verifying accessibility (content descriptions and tap targets) and running `./gradlew detektall`.
