# Calendar Event Visual Hierarchy Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Demote external calendar events in the agenda feed to subtle schedule dividers and all-day banners, keeping tasks as the primary visual focus, and enable tapping events to view them in the native calendar app.

**Architecture:** Partition external events into all-day and timed events inside `AgendaFeed`. Display all-day events directly beneath the date header via `AgendaAllDayBanner`, render timed events inline as pill-badge `AgendaScheduleDivider` items between tasks, and route clicks to an Android `Intent` handler via `CalendarIntentUtils`.

**Tech Stack:** Kotlin Multiplatform, Jetpack Compose, Material 3, AndroidX Room, Detekt.

**Spec:** [`docs/superpowers/specs/2026-10-24-calendar-event-visual-hierarchy-design.md`](file:///Users/pahina/Documents/Procrastaint/docs/superpowers/specs/2026-10-24-calendar-event-visual-hierarchy-design.md)

## Global Constraints
- **Strict No-Emoji Policy:** ABSOLUTELY DO NOT USE EMOJIS anywhere in the app UI, components, vector fallbacks, notifications, logs, or documentation (codified in `AGENTS.md`).
- **KMP Boundaries:** Keep Android-specific APIs (`android.content.Intent`, `android.provider.CalendarContract`) in `androidApp`.
- **Detekt & Code Style:** Must pass `./gradlew detektall` cleanly with zero warnings/errors.

## Review Focus
1. Event ID parsing failure: Event ID is a non-numeric string (e.g. Google Calendar alphanumeric string) when launching calendar intent. Expected behavior: safely fall back to time-based calendar view intent rather than crashing.
2. Missing calendar application on device: User taps an event but no application handles the view intent. Expected behavior: gracefully catch `ActivityNotFoundException` and fall back to `CATEGORY_APP_CALENDAR` or safely do nothing without crashing.
3. Day with only external events and no tasks: Day has events but zero actionable tasks. Expected behavior: header and events render properly, accompanied by a quiet `"No tasks scheduled"` message rather than showing nothing.
4. Overlapping or multiple all-day events: Multiple all-day events on the same date. Expected behavior: render stacked or wrapped in `AgendaAllDayBanner` cleanly without layout clipping.
5. High-contrast / dark theme readability: Tinted divider rules and text maintain WCAG AA contrast ratio against `surface` and `surfaceContainer` colors in both light and dark themes.

---

### Task 1: Calendar Intent Launcher Utility & Unit Tests

**Files:**
- Create: `androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/util/CalendarIntentUtils.kt`
- Test: `androidApp/src/test/java/ap/panini/procrastaint/ui/agenda/util/CalendarIntentUtilsTest.kt`

**Interfaces:**
- Consumes: `ExternalCalendarEvent` from `ap.panini.procrastaint.data.entities.ExternalCalendarEvent`
- Produces:
  - `fun createViewEventIntent(event: ExternalCalendarEvent): Intent`
  - `fun launchCalendarEvent(context: Context, event: ExternalCalendarEvent): Boolean`

- [ ] **Step 1: Write the failing unit tests for Intent construction**

```kotlin
package ap.panini.procrastaint.ui.agenda.util

import android.content.Intent
import android.provider.CalendarContract
import ap.panini.procrastaint.data.entities.CalendarSource
import ap.panini.procrastaint.data.entities.ExternalCalendarEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CalendarIntentUtilsTest {

    @Test
    fun testCreateViewEventIntentWithNumericId() {
        val event = ExternalCalendarEvent(
            id = "12345",
            title = "Team Standup",
            startTime = 1700000000000L,
            endTime = 1700003600000L,
            source = CalendarSource.DEVICE
        )
        val intent = CalendarIntentUtils.createViewEventIntent(event)
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertTrue(intent.data.toString().contains("content://com.android.calendar/events/12345"))
    }

    @Test
    fun testCreateViewEventIntentWithAlphanumericIdFallsBackToTime() {
        val event = ExternalCalendarEvent(
            id = "google_cal_alphanumeric_id",
            title = "All Hands",
            startTime = 1700000000000L,
            endTime = 1700003600000L,
            source = CalendarSource.GOOGLE
        )
        val intent = CalendarIntentUtils.createViewEventIntent(event)
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertTrue(intent.data.toString().contains("content://com.android.calendar/time/1700000000000"))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :androidApp:testDebugUnitTest --tests "ap.panini.procrastaint.ui.agenda.util.CalendarIntentUtilsTest"`
Expected: FAIL with "Unresolved reference: CalendarIntentUtils"

- [ ] **Step 3: Implement `CalendarIntentUtils`**

Implement `CalendarIntentUtils.kt` with `createViewEventIntent` and `launchCalendarEvent`, catching `ActivityNotFoundException`, `SecurityException`, and general exceptions, falling back to time-based URI and generic calendar launch.

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :androidApp:testDebugUnitTest --tests "ap.panini.procrastaint.ui.agenda.util.CalendarIntentUtilsTest"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/util/CalendarIntentUtils.kt androidApp/src/test/java/ap/panini/procrastaint/ui/agenda/util/CalendarIntentUtilsTest.kt
git commit -m "feat(agenda): add CalendarIntentUtils for opening external events"
```

---

### Task 2: AgendaScheduleDivider and AgendaAllDayBanner Composables

**Files:**
- Create: `androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/components/AgendaScheduleDivider.kt`
- Create: `androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/components/AgendaAllDayBanner.kt`
- Modify: `androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/components/ExternalEventView.kt`

**Interfaces:**
- Consumes: `ExternalCalendarEvent`
- Produces:
  - `@Composable fun AgendaScheduleDivider(event: ExternalCalendarEvent, onEventClick: (ExternalCalendarEvent) -> Unit, modifier: Modifier = Modifier)`
  - `@Composable fun AgendaAllDayBanner(event: ExternalCalendarEvent, onEventClick: (ExternalCalendarEvent) -> Unit, modifier: Modifier = Modifier)`

- [ ] **Step 1: Implement `AgendaScheduleDivider` composable**

Build the centered pill badge flanked by horizontal hairline gradient rules:
- Pill badge with `event.color` / source color at 8% alpha background, 20% alpha border.
- Displays time range (e.g. `10:00 AM - 11:30 AM`), separator `|`, title, and location if present.
- Strictly NO emojis: use `Icons.Outlined.LocationOn` or text.
- Clickable with `onClick = { onEventClick(event) }`.

- [ ] **Step 2: Implement `AgendaAllDayBanner` composable**

Build the compact horizontal banner for all-day events:
- Muted rounded container (8.dp) with 8% accent tint.
- `ALL DAY` text pill badge (`labelSmall`, bold, uppercase).
- Event title (`bodyMedium`, semi-bold) and source label (`Google` or `Device Calendar`).
- Strictly NO emojis.
- Clickable with `onClick = { onEventClick(event) }`.

- [ ] **Step 3: Update `ExternalEventView.kt` for compatibility**

Refactor `ExternalEventView.kt` to delegate to `AgendaScheduleDivider` so any existing callers remain functional without regression.

- [ ] **Step 4: Verify build and compile**

Run: `./gradlew :androidApp:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/components/AgendaScheduleDivider.kt androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/components/AgendaAllDayBanner.kt androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/components/ExternalEventView.kt
git commit -m "feat(agenda): add AgendaScheduleDivider and AgendaAllDayBanner composables"
```

---

### Task 3: AgendaFeed Integration & Date Item Partitioning

**Files:**
- Modify: `androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/components/AgendaFeed.kt`
- Modify: `androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/AgendaScreen.kt`

**Interfaces:**
- Consumes:
  - `AgendaScheduleDivider` from Task 2
  - `AgendaAllDayBanner` from Task 2
  - `CalendarIntentUtils.launchCalendarEvent` from Task 1
- Produces: Updated `AgendaFeed` rendering all-day banners under sticky headers, schedule dividers for timed events in `dayItems`, and handling event clicks.

- [ ] **Step 1: Update `AgendaFeed` event partitioning**

In `AgendaFeed.kt`:
- Add `onEventClick: ((ExternalCalendarEvent) -> Unit)? = null` parameter (defaulting to null, where null triggers `CalendarIntentUtils.launchCalendarEvent(context, it)`).
- For each date in `distinctDates`:
  - Split `externalEventsByDate[date]` into `allDayEvents = eventsForDate.filter { it.isAllDay }` and `timedEvents = eventsForDate.filter { !it.isAllDay }`.
  - In `stickyHeader(key = "header_$date")`: directly after `AgendaDateHeader`, render each all-day event using `AgendaAllDayBanner(event = it, onEventClick = handleEventClick)`.
  - In `dayItems`: only add `AgendaDayItem.Event` for items in `timedEvents`.
  - When rendering `AgendaDayItem.Event`: invoke `AgendaScheduleDivider(event = item.event, onEventClick = handleEventClick)`.
  - In empty state logic: if `tasksForDate.isEmpty()` and `timedEvents.isNotEmpty()`, render a subtle `"No tasks scheduled"` item in the feed to reinforce that tasks are the primary purpose of the view.

- [ ] **Step 2: Connect context and event launch in `AgendaScreen`**

In `AgendaScreen.kt`:
- Ensure `AgendaFeed` is wired with a safe event click handler that uses `LocalContext.current` to launch external events via `CalendarIntentUtils.launchCalendarEvent`.

- [ ] **Step 3: Run all Android unit tests**

Run: `./gradlew :androidApp:testDebugUnitTest`
Expected: ALL TESTS PASS

- [ ] **Step 4: Commit**

```bash
git add androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/components/AgendaFeed.kt androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/AgendaScreen.kt
git commit -m "feat(agenda): integrate event dividers and all-day banners into AgendaFeed"
```

---

### Task 4: Static Analysis, Formatting & Final Verification

**Files:**
- Modify (if formatting adjustments needed): all touched files

- [ ] **Step 1: Run Detekt on entire project**

Run: `./gradlew detektall`
Expected: BUILD SUCCESSFUL with 0 code smell issues.

- [ ] **Step 2: Verify No-Emoji rule compliance**

Check all newly added/modified code files for any accidental emojis or unicode symbols.
Run: `git diff origin/master -- androidApp/ | grep -E "[^\x00-\x7F]"` to inspect non-ASCII characters and ensure zero emojis are present.

- [ ] **Step 3: Full debug build verification**

Run: `./gradlew :androidApp:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit (if any styling tweaks were made)**

```bash
git commit -am "chore: format and clean up agenda event components"
```
