# Design Document: UI Revamp & Unified Agenda (Material 3 Expressive)

**Date:** 2026-10-24  
**Status:** Ready for User Approval  
**Target:** Android Application (`androidApp`), Glance AppWidget (`UpcomingWidget`), and Shared Architecture  

---

## 1. Intent & The "Why"
The current user interface divides the core daily experience across three isolated destinations: **Calendar** (day/week/month grids), **Upcoming** (a flat chronological task list), and **Library** (a tag card browser). Users have to constantly hop between tabs to understand their day, missed tasks are easily lost when their due date passes, and having a dedicated tab just to display a list of tags feels heavy and fragmented.

The goal of this revamp is to eliminate UI clunkiness and deliver a slick, modern, Material 3 Expressive experience. By consolidating Calendar, Upcoming, and Library into a unified **Timeline Agenda**, users get an instant, edge-to-edge view of what remains to be done today, prominent protection against missed/overdue tasks, seamless calendar event integration, and frictionless task entry both inside the app and via the home screen widget.

---

## 2. Goals, Non-Goals, and Anti-Goals

### Goals
* **Continuous Multi-Day Stream:** Unify upcoming tasks and external calendar events (Google & device calendars) into a single fluid, chronological stream with sticky date headers (`Today`, `Tomorrow`, `Tuesday, Oct 28`, etc.).
* **Faded Completed Tasks:** Completed tasks remain in their respective date sections with lower opacity (`0.5f`), struck-through text, and animated checkmarks, celebrating daily achievements without cluttering remaining to-dos.
* **Guaranteed Visibility for Missed Items:** Overdue/missed tasks from past dates are prominently displayed in a pinned "Needs Attention" banner at the very top of the agenda with a 1-tap "Move All to Today" action and individual checkoffs.
* **Inline Tag Filters & Management:** Replace the separate Library tab with interactive, horizontal tag filter chips on the main screen. Tapping filters the stream instantly; long-pressing opens `TagBottomSheet` for tag editing/deletion; "+ Tag" creates new tags.
* **Edge-to-Edge M3 Expressive Canvas:** Remove the bottom navigation bar to maximize vertical space. Place Settings (`⚙️`) and Search/Filter (`🔍`) in the top app bar, and a Material 3 Expressive `MediumFloatingActionButton` in the bottom-right corner.
* **Collapsible Interactive Calendar:** A compact 7-day week strip at the top that can be swiped horizontally. Tapping a day scrolls the list directly to that date. Pulling down or tapping the calendar header expands the strip into a full-month grid with task indicator dots; tapping a month date auto-collapses back to week view and jumps to that date.
* **Home Screen Widget Parity:** Update the Jetpack Glance `UpcomingWidget` so that overdue tasks are queried and surfaced at the top with a missed-task indicator, ensuring the widget never hides missed tasks.

### Non-Goals
* **Rewriting the KWhen natural language parsing engine or Room schema:** The underlying data persistence (`TaskRepository`, Room entities) and natural language parsing (`Parser`, `ENConfig`) remain intact.
* **iOS UI Redesign:** This project specifically targets the Android application and Android widgets. (iOS continues using its existing SwiftUI views).
* **Replacing Settings Sub-Screens:** Sub-screens like `SyncScreen`, `ParserSettingsScreen`, and `AboutLibrariesScreen` remain as navigations from `SettingsScreen`.

### Anti-Goals
* **Hiding or Silently Forgetting Overdue Tasks:** Past-due tasks must never disappear from view when the clock rolls over past midnight.
* **Multi-ViewModel Spaghetti:** Avoid binding three disparate ViewModels (`CalendarViewModel`, `UpcomingViewModel`, `LibraryViewModel`) in a single composable without a consolidated data state.
* **Crowded Navigation:** Do not retain unused bottom bar tabs; the home interface must remain an uncluttered, focused canvas.

---

## 3. Constraints
* **Kotlin Multiplatform & Compose Multiplatform Guidelines:** Keep common business logic clean in `shared`; keep Android-specific UI and Glance widget code in `androidApp`.
* **Material 3 Expressive:** Utilize `@OptIn(ExperimentalMaterial3ExpressiveApi::class)`, M3 Expressive shapes (pills, squircle corners), motion schemes (springs/bounces), and expressive FAB tokens.
* **Detekt & Code Style:** All code must conform to repository linting rules via `./gradlew detektall`.

---

## 4. Architectural Approaches

### Approach 1: Unified `AgendaViewModel` & Reactive State Hub *(Selected)*
Create a dedicated `AgendaScreen.kt` and `AgendaViewModel.kt` that cleanly merges task queries and calendar event streams into a single reactive UI state (`AgendaUiState`).
* **Why Selected:** Eliminates multi-ViewModel coordination overhead, centralizes overdue task handling, provides a single source of truth for all schedule items, and easily synchronizes with the Glance widget.
* **Tradeoffs:** Requires deprecating older standalone `UpcomingViewModel` and `LibraryScreen` routes in favor of the new consolidated screen.

### Approach 2: Compose-Layer Multi-ViewModel Composition *(Rejected)*
Keep `CalendarViewModel`, `UpcomingViewModel`, and `LibraryViewModel` intact, injecting all three into the composable.
* **Why Rejected:** Clunky cross-ViewModel synchronization in Compose; makes reactive tag filtering and date changes fragile and prone to UI jank.

### Approach 3: Incremental In-Place Mutation of `CalendarScreen` *(Rejected)*
Attach the task list below the existing calendar grid and delete unused routes.
* **Why Rejected:** Carries legacy technical debt and rigid top-bar sheets instead of a purpose-built M3 Expressive agenda.

---

## 5. Detailed Design & Architecture

### 5.1 UI & Screen Architecture (`AgendaScreen.kt`)
* **Top App Bar:**
  * Left: App Title ("Procrastaint") with date subtitle (e.g., "Friday, Oct 24").
  * Right actions:
    * Search/Filter icon (`Icons.Outlined.Search`): Toggles an inline real-time filter field for quick task title and tag filtering without full-screen navigation.
    * Settings gear icon (`Icons.Outlined.Settings`): Navigates to `Route.Settings` (where calendar sync and preferences live).
* **Collapsible Calendar Strip:**
  * Default state: 7-day horizontal strip displaying weekday initials and day numbers. Today's date is accented with an expressive pill indicator. Swiping left/right shifts the week. Tapping a day smoothly scrolls the agenda feed to that date's sticky header.
  * Expanded state: Accordion expansion pushing the feed down to reveal a full-month calendar grid with task indicator dots. Tapping any day selects it, auto-collapses back to the week strip, and scrolls to that day.
* **Tag Filter Chips:**
  * Horizontal scrollable row directly below the calendar strip.
  * Chips: "All" (active by default), followed by active user tags with counts (`#work (3)`, `#health (1)`), plus a "+ Tag" button that opens `TagBottomSheet` to create tags.
  * Interaction: Tapping a tag chip filters the stream in real time. Long-pressing any tag chip opens `TagBottomSheet` populated with the tag for instant editing or deletion.
* **Pinned "Needs Attention" Box:**
  * Displayed conditionally if `overdueTasks.isNotEmpty()`.
  * Highlighted container (warning container colors with 16dp rounded corners).
  * Shows count of missed tasks with a prominent "Move All to Today" button.
  * Each item has a checkbox (checking marks task complete) and tap-to-edit.
* **Continuous Chronological Schedule Feed:**
  * Merged items grouped under sticky date headers (`Today`, `Tomorrow`, `Tuesday, Oct 28`, etc.) spanning a rolling 30-day window.
  * Integrates Google / Device calendar events with clear calendar badges.
  * Shows tasks with checkboxes, tag pills, and time indicators.
  * Checked tasks remain visible in place with lowered opacity (`0.5f`) and struck-through text with smooth spring animations.
  * Empty state: Friendly, expressive illustration when no tasks or events exist in the upcoming window.
* **Floating Action Button:**
  * Expressive `MediumFloatingActionButton` in bottom-right corner.
  * Tapping invokes `MainActivityViewModel.onShow()` to open `TaskBottomSheet` for natural language task input.

### 5.2 Reactive ViewModel (`AgendaViewModel.kt`)
* Exposes `uiState: StateFlow<AgendaUiState>`:
  ```kotlin
  data class AgendaUiState(
      val focusedDate: Long = Date.getTodayStart(),
      val isMonthExpanded: Boolean = false,
      val overdueTasks: List<TaskSingle> = emptyList(),
      val scheduledTasksByDate: Map<Long, List<TaskSingle>> = emptyMap(),
      val externalEventsByDate: Map<Long, List<ExternalEvent>> = emptyMap(),
      val monthTasksByDate: Map<Long, List<TaskSingle>> = emptyMap(),
      val tags: List<TagItemData> = emptyList(),
      val selectedTagId: Long? = null,
      val searchQuery: String = "",
      val recentlyCompleted: Set<Pair<Long, Long>> = emptySet(),
      val hasCalendarPermission: Boolean = false,
      val isGoogleLoggedIn: Boolean = false
  )
  ```
* **Core Operations:**
  * `setFocusedDate(date: Long)`: Sets active date and signals the list to scroll to that section.
  * `toggleMonthExpansion()`: Toggles between week strip and month grid.
  * `selectTag(tagId: Long?)`: Filters tasks by tag.
  * `setSearchQuery(query: String)`: Filters tasks in-memory by text query.
  * `moveAllOverdueToToday()`: Moves overdue tasks to today. For standard tasks, updates `startTime` to today's start; for recurring task occurrences, marks the missed recurrence completed and generates an instance for today.
  * `checkTask(task: TaskSingle)`: Toggles task completion state with optimistic UI feedback.

### 5.3 Widget Parity (`UpcomingWidgetViewModel.kt` & `UpcomingWidget.kt`)
* Modify task query in `UpcomingWidgetViewModel`:
  * Query tasks from `from = 0` (or `from = todayStart - 30.days`) to fetch incomplete overdue tasks alongside upcoming tasks.
* In `UpcomingWidget.kt`:
  * If overdue tasks exist, display an alert section with `🚨 Missed Tasks` header before the upcoming task list.
  * Provides parity so the home screen widget immediately shows missed obligations.

### 5.4 Navigation Restructuring (`NavGraph.kt` & `MainActivity.kt`)
* In `Route.kt`:
  * Add `@Serializable data object Agenda : Route` as the primary home screen.
  * Remove `Route.Upcoming` and `Route.Library` from top-level navigation (keeping `Route.Tag` for tag details if needed).
* In `MainActivity.kt`:
  * Set `startDestination = Route.Agenda`.
  * Remove bottom navigation bar when on `Route.Agenda`, establishing the clean edge-to-edge canvas.

---

## 6. Engineering Calls Settled by Review
1. **Feed Scope:** Continuous multi-day stream with sticky date headers. Week strip taps jump to the selected date.
2. **Completed Tasks:** Faded (`0.5f` opacity) with strikethrough text and animated checkbox, remaining in their respective date section.
3. **Month Calendar Expansion:** Accordion push-down animation. Month grid features task indicator dots. Tapping any date auto-collapses back to the week strip and scrolls to that day.
4. **Tag Management:** Long-press on any tag chip opens `TagBottomSheet` for edit/delete; "+ Tag" creates new tags.
5. **Search Scope:** Fast, inline search bar directly on the Agenda screen filtering tasks and tags in real time without navigating away.
6. **"Move All to Today" Semantics:** Non-recurring overdue tasks update their due date to today's start; recurring task missed occurrences are marked completed and scheduled for today to protect the future repetition pattern.
