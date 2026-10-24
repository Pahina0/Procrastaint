# UI Revamp & Unified Agenda (Material 3 Expressive) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Consolidate Android Calendar, Upcoming, and Library into a modern, slick, edge-to-edge **Timeline Agenda** with Material 3 Expressive elements, guaranteed visibility for missed tasks, inline tag filters, and home screen widget parity.

**Architecture:** Create a unified `AgendaViewModel` managing reactive data streams (overdue tasks, rolling 30-day schedule, external calendar events, tag chips, and search query) into an immutable `AgendaUiState`. Build an edge-to-edge `AgendaScreen` featuring a collapsible week-to-month calendar header, pinned "Needs Attention" overdue card, and a continuous multi-day feed with faded completed items, while removing the bottom bar and updating the Glance widget.

**Tech Stack:** Kotlin Multiplatform, Jetpack Compose, Material 3 Expressive (`@OptIn(ExperimentalMaterial3ExpressiveApi::class)`), AndroidX Room, Koin DI, Jetpack Glance.

**Spec:** `docs/superpowers/specs/2026-10-24-ui-revamp-agenda-design.md`

## Global Constraints
* No `java.time.*` or `android.*` in `shared/src/commonMain`; use `kotlinx.datetime` and `kotlin.time`.
* All Android UI components use Material 3 / Material 3 Expressive tokens.
* Clean edge-to-edge canvas with zero bottom navigation bar clutter.
* Code must pass repository linting via `./gradlew detektall`.

## Review Focus
1. **Clock Rollover at Midnight:** Past-due tasks must immediately surface in the "Needs Attention" overdue box rather than disappearing from the feed.
2. **Recurring Task "Move to Today":** Rescheduling a missed recurrence to today must not corrupt or shift future recurring frequencies.
3. **Empty Date Handling:** Dates with zero tasks in the rolling 30-day window must render cleanly without broken sticky headers.
4. **Glance Widget Overdue Synchronization:** Checking off an overdue task in the Glance widget must update Room and reflect immediately in the widget UI.
5. **Tag Filter Isolation:** Selecting an inline tag chip must filter both the active day and future days in the continuous timeline stream without crashing when an empty tag result occurs.

---

### Task 1: Overdue & Multi-Day Task Query Extensions in Shared Repository

**Files:**
- Modify: `shared/src/commonMain/kotlin/ap/panini/procrastaint/data/repositories/TaskRepository.kt`
- Test: `shared/src/commonTest/kotlin/ap/panini/procrastaint/data/repositories/TaskRepositoryTest.kt`

**Interfaces:**
- Consumes: `TaskRepository`, `TaskDao`
- Produces: 
  - `fun getOverdueTasks(before: Long): Flow<List<TaskSingle>>`
  - `suspend fun moveOverdueTasksToDate(tasks: List<TaskSingle>, targetDate: Long)`

- [ ] **Step 1: Write failing unit test for overdue queries**

```kotlin
@Test
fun testGetOverdueTasksReturnsIncompleteTasksBeforeTimestamp() = runTest {
    // Insert task due yesterday, incomplete
    // Insert task due yesterday, completed
    // Insert task due tomorrow, incomplete
    val overdue = repository.getOverdueTasks(todayStart).first()
    assertEquals(1, overdue.size)
    assertEquals("Past incomplete task", overdue.first().title)
}
```

- [ ] **Step 2: Run test to verify failure**

Run: `./gradlew :shared:testDebugUnitTest --tests "ap.panini.procrastaint.data.repositories.TaskRepositoryTest.testGetOverdueTasksReturnsIncompleteTasksBeforeTimestamp"`
Expected: FAIL with compilation error (unresolved reference `getOverdueTasks`).

- [ ] **Step 3: Implement `getOverdueTasks` and `moveOverdueTasksToDate` in `TaskRepository`**

Filter uncompleted tasks where `currentEventTime < before`. For `moveOverdueTasksToDate`, update non-recurring task timestamps to `targetDate`, and for recurring task occurrences, insert a completion record for the missed recurrence and create an occurrence record on `targetDate`.

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :shared:testDebugUnitTest --tests "ap.panini.procrastaint.data.repositories.TaskRepositoryTest.testGetOverdueTasksReturnsIncompleteTasksBeforeTimestamp"`
Expected: PASS.

- [ ] **Step 5: Commit changes**

```bash
git add shared/src/commonMain/kotlin/ap/panini/procrastaint/data/repositories/TaskRepository.kt shared/src/commonTest/
git commit -m "feat(shared): add overdue tasks query and reschedule logic"
```

---

### Task 2: Reactive `AgendaViewModel` & State Hub

**Files:**
- Create: `androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/AgendaViewModel.kt`
- Create: `androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/AgendaUiState.kt`
- Modify: `shared/src/commonMain/kotlin/ap/panini/procrastaint/di/AppModule.kt`
- Test: `androidApp/src/test/java/ap/panini/procrastaint/ui/agenda/AgendaViewModelTest.kt`

**Interfaces:**
- Consumes: `TaskRepository`, `PreferenceRepository`
- Produces: `AgendaViewModel` extending `CheckableTaskViewModel`, exposing `uiState: StateFlow<AgendaUiState>`.

- [ ] **Step 1: Write failing unit tests for `AgendaViewModel`**

```kotlin
@Test
fun testAgendaViewModelCombinesOverdueAndScheduledTasks() = runTest {
    val viewModel = AgendaViewModel(fakeTaskRepository, fakePrefRepository)
    val state = viewModel.uiState.first()
    assertNotNull(state)
    assertEquals(1, state.overdueTasks.size)
}

@Test
fun testMoveAllOverdueToTodayInvokesRepository() = runTest {
    val viewModel = AgendaViewModel(fakeTaskRepository, fakePrefRepository)
    viewModel.moveAllOverdueToToday()
    // Verify overdue tasks rescheduled to today's start
}
```

- [ ] **Step 2: Run test to verify failure**

Run: `./gradlew :androidApp:testDebugUnitTest --tests "ap.panini.procrastaint.ui.agenda.AgendaViewModelTest"`
Expected: FAIL (unresolved class `AgendaViewModel`).

- [ ] **Step 3: Implement `AgendaUiState` and `AgendaViewModel`**

Define `AgendaUiState` with `focusedDate`, `isMonthExpanded`, `overdueTasks`, `scheduledTasksByDate`, `externalEventsByDate`, `monthTasksByDate`, `tags`, `selectedTagId`, `searchQuery`, `recentlyCompleted`. Implement `selectTag`, `setSearchQuery`, `setFocusedDate`, `toggleMonthExpansion`, `moveAllOverdueToToday`, `checkTask`. Register in `AppModule.kt`.

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :androidApp:testDebugUnitTest --tests "ap.panini.procrastaint.ui.agenda.AgendaViewModelTest"`
Expected: PASS.

- [ ] **Step 5: Commit changes**

```bash
git add androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/ shared/src/commonMain/kotlin/ap/panini/procrastaint/di/AppModule.kt androidApp/src/test/
git commit -m "feat(agenda): implement AgendaViewModel and reactive state pipeline"
```

---

### Task 3: Collapsible Week & Month Calendar Header

**Files:**
- Create: `androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/components/CollapsibleCalendarHeader.kt`
- Create: `androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/components/WeekStrip.kt`
- Create: `androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/components/MonthCalendarGrid.kt`

**Interfaces:**
- Consumes: `focusedDate: Long`, `isMonthExpanded: Boolean`, `monthTasksByDate: Map<Long, List<TaskSingle>>`
- Produces: 
  - `onSelectDate: (Long) -> Unit`
  - `onToggleMonthExpansion: () -> Unit`

- [ ] **Step 1: Implement `WeekStrip.kt`**

Render a 7-day horizontal strip with day abbreviations and day numbers. Highlight `focusedDate` with a Material 3 Expressive pill container. Support horizontal swiping to shift weeks.

- [ ] **Step 2: Implement `MonthCalendarGrid.kt`**

Render a 7-column month grid showing date numbers and task indicator dots (colored dots matching primary tag colors). Tapping a date invokes `onSelectDate(date)`.

- [ ] **Step 3: Assemble `CollapsibleCalendarHeader.kt` with accordion motion**

Use `AnimatedVisibility` with spring easing to transition between the compact `WeekStrip` and expanded `MonthCalendarGrid`. Include a subtle chevron toggle button next to the month name.

- [ ] **Step 4: Verify rendering and build compilation**

Run: `./gradlew :androidApp:compileDebugKotlin`
Expected: SUCCESS.

- [ ] **Step 5: Commit changes**

```bash
git add androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/components/
git commit -m "feat(agenda): create collapsible week and month calendar header"
```

---

### Task 4: Pinned "Needs Attention" Card & Tag Filter Chips

**Files:**
- Create: `androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/components/NeedsAttentionCard.kt`
- Create: `androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/components/TagChipRow.kt`

**Interfaces:**
- Consumes: `overdueTasks: List<TaskSingle>`, `tags: List<TagItemData>`, `selectedTagId: Long?`
- Produces:
  - `onMoveAllToToday: () -> Unit`
  - `onCheckTask: (TaskSingle) -> Unit`
  - `onSelectTag: (Long?) -> Unit`
  - `onEditTag: (TagItemData) -> Unit`
  - `onAddTag: () -> Unit`

- [ ] **Step 1: Implement `NeedsAttentionCard.kt`**

Card container styled with Material 3 Expressive error/warning container tokens and 16dp rounded corners. Displays `🚨 MISSED TASKS` badge, a 1-tap "Move All to Today" button, and list items with checkboxes and tap-to-edit.

- [ ] **Step 2: Implement `TagChipRow.kt`**

Horizontal scrollable row starting with "All" chip, followed by user tag chips (`#work (3)`) with leading color dots, ending with a "+ Tag" chip. Tapping toggles `selectedTagId`. Combined clickable: long-press triggers `onEditTag(tag)`.

- [ ] **Step 3: Verify compilation**

Run: `./gradlew :androidApp:compileDebugKotlin`
Expected: SUCCESS.

- [ ] **Step 4: Commit changes**

```bash
git add androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/components/NeedsAttentionCard.kt androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/components/TagChipRow.kt
git commit -m "feat(agenda): add NeedsAttentionCard and TagChipRow components"
```

---

### Task 5: Continuous Multi-Day Feed & Faded Task Items

**Files:**
- Create: `androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/components/AgendaFeed.kt`
- Modify: `androidApp/src/main/java/ap/panini/procrastaint/ui/components/TaskView.kt`

**Interfaces:**
- Consumes: `scheduledTasksByDate: Map<Long, List<TaskSingle>>`, `externalEventsByDate: Map<Long, List<ExternalEvent>>`, `recentlyCompleted: Set<Pair<Long, Long>>`
- Produces:
  - `onCheckTask: (TaskSingle) -> Unit`
  - `onEditTask: (TaskSingle) -> Unit`

- [ ] **Step 1: Update `TaskView.kt` for faded completed items**

When `isDone == true`, set card alpha to `0.5f`, apply `TextDecoration.LineThrough` to title, and show a checked icon with bouncy spring animation.

- [ ] **Step 2: Implement `AgendaFeed.kt` with `LazyColumn`**

Use `stickyHeader` for date labels (`Today`, `Tomorrow`, `Tuesday, Oct 28`, etc.). Under each date header, render external calendar events first with calendar pill badges, followed by tasks. Display expressive empty illustration if no tasks exist in window.

- [ ] **Step 3: Attach programmatic list scroll to `focusedDate`**

Observe changes in `focusedDate` and invoke `lazyListState.animateScrollToItem(dateIndex)` smoothly.

- [ ] **Step 4: Verify compilation**

Run: `./gradlew :androidApp:compileDebugKotlin`
Expected: SUCCESS.

- [ ] **Step 5: Commit changes**

```bash
git add androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/components/AgendaFeed.kt androidApp/src/main/java/ap/panini/procrastaint/ui/components/TaskView.kt
git commit -m "feat(agenda): implement continuous multi-day feed with faded completed items"
```

---

### Task 6: Main `AgendaScreen` Assembly & Edge-to-Edge Navigation

**Files:**
- Create: `androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/AgendaScreen.kt`
- Modify: `androidApp/src/main/java/ap/panini/procrastaint/ui/navigation/Route.kt`
- Modify: `androidApp/src/main/java/ap/panini/procrastaint/ui/navigation/NavGraph.kt`
- Modify: `androidApp/src/main/java/ap/panini/procrastaint/ui/MainActivity.kt`

**Interfaces:**
- Consumes: `AgendaViewModel`, `Route.Agenda`
- Produces: Primary top-level home experience with edge-to-edge canvas.

- [ ] **Step 1: Add `Route.Agenda` to `Route.kt` and update `NavGraph.kt`**

```kotlin
@Serializable
data object Agenda : Route
```
Register `composable<Route.Agenda>` as root in `NavGraph.kt`. Remove `Route.Upcoming` and `Route.Library` from top-level routes.

- [ ] **Step 2: Assemble `AgendaScreen.kt`**

Include Material 3 Expressive Top App Bar with app title, date subtitle, inline search toggle, and Settings gear icon. Embed `CollapsibleCalendarHeader`, `TagChipRow`, `NeedsAttentionCard`, and `AgendaFeed`.

- [ ] **Step 3: Update `MainActivity.kt` for edge-to-edge canvas**

Set `startDestination = Route.Agenda`. Remove bottom navigation bar when navigating `Route.Agenda`. Position `MediumFloatingActionButton` in bottom-right corner triggering `viewModel.onShow()`.

- [ ] **Step 4: Verify navigation compilation and route wiring**

Run: `./gradlew :androidApp:compileDebugKotlin`
Expected: SUCCESS.

- [ ] **Step 5: Commit changes**

```bash
git add androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/AgendaScreen.kt androidApp/src/main/java/ap/panini/procrastaint/ui/navigation/ androidApp/src/main/java/ap/panini/procrastaint/ui/MainActivity.kt
git commit -m "feat(agenda): assemble AgendaScreen and establish edge-to-edge navigation canvas"
```

---

### Task 7: Glance Home Screen Widget Parity for Missed Tasks

**Files:**
- Modify: `androidApp/src/main/java/ap/panini/procrastaint/ui/widget/UpcomingWidgetViewModel.kt`
- Modify: `androidApp/src/main/java/ap/panini/procrastaint/ui/widget/UpcomingWidget.kt`
- Test: `androidApp/src/test/java/ap/panini/procrastaint/ui/widget/UpcomingWidgetViewModelTest.kt`

**Interfaces:**
- Consumes: `TaskRepository`
- Produces: `UpcomingWidget` rendering overdue missed items with `🚨 Missed Tasks` header before the upcoming list.

- [ ] **Step 1: Write test for `UpcomingWidgetViewModel` overdue inclusion**

```kotlin
@Test
fun testWidgetViewModelIncludesOverdueTasks() = runTest {
    // Verify tasks due prior to today are included in widget tasks stream
}
```

- [ ] **Step 2: Update `UpcomingWidgetViewModel.kt`**

Adjust query range to fetch incomplete tasks from before today so missed tasks are included in `upcomingTasks` flow.

- [ ] **Step 3: Update `UpcomingWidget.kt` layout**

Add a dedicated `🚨 Missed Tasks` section with distinct warning styling above the upcoming tasks list in the Glance widget.

- [ ] **Step 4: Run widget tests**

Run: `./gradlew :androidApp:testDebugUnitTest --tests "ap.panini.procrastaint.ui.widget.UpcomingWidgetViewModelTest"`
Expected: PASS.

- [ ] **Step 5: Commit changes**

```bash
git add androidApp/src/main/java/ap/panini/procrastaint/ui/widget/ androidApp/src/test/
git commit -m "feat(widget): add overdue task parity to UpcomingWidget"
```

---

### Task 8: Complete Verification, Formatting, and Linting

**Files:**
- Whole repository

- [ ] **Step 1: Run all unit tests**

Run: `./gradlew testDebugUnitTest`
Expected: ALL TESTS PASS.

- [ ] **Step 2: Run Detekt static analysis and formatting**

Run: `./gradlew detektall`
Expected: 0 errors/violations.

- [ ] **Step 3: Verify debug APK assembly**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit any remaining cleanups**

```bash
git add .
git commit -m "chore: clean up styling and pass detekt checks"
```
