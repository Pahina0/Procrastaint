# UI Cleanup & Unicode Emoticons Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove obsolete screens (`CalendarScreen`, `UpcomingScreen`, `LibraryScreen`) and their navigation routes after the Agenda revamp, and replace all emoji usage across the app and widgets with dynamically picked Unicode text emoticons from a centralized registry.

**Architecture:** A single-pool `Emoticons` object in `shared` provides a list of Unicode text emoticons and a `random(): String` function, stabilized in Jetpack Compose via a `rememberRandomEmoticon()` helper. `ExternalEventView` is migrated out of `ui/calendar/components/` into `ui/agenda/components/` to preserve Google Calendar integration. All dead screen packages, their Koin ViewModel bindings, dead routes, and dead DataStore calendar preference keys are cleanly purged.

**Tech Stack:** Kotlin Multiplatform (Common, Android), Jetpack Compose, Material 3 Expressive, Jetpack Glance (Widgets), AndroidX DataStore Preferences, Koin DI.

**Spec:** [`docs/superpowers/specs/2026-10-24-ui-cleanup-emoticons-design.md`](file:///Users/pahina/Documents/Procrastaint/docs/superpowers/specs/2026-10-24-ui-cleanup-emoticons-design.md)

## Global Constraints

- Never import `java.time.*`, `java.util.Date`, or `android.*` inside `shared/src/commonMain/`.
- Maintain strict code formatting and pass `./gradlew detektall` cleanly before completing any task.
- Ensure `./gradlew assembleDebug` compiles with zero warnings or errors.
- Do not modify or break `AgendaScreen` or Google Calendar external events rendering.

## Review Focus

1. **Agenda External Event Rendering:** Relocating `ExternalEventView` must preserve its exact rendering behavior for `AgendaFeed` without breaking previews or event styling.
2. **Glance Widget Stability:** Calling `Emoticons.random()` in `UpcomingWidget` must render without crashes or layout truncation in widget views across process updates.
3. **Compose Recomposition Stability:** Any usage of emoticons in Compose must avoid flickering or changing faces across routine UI recompositions.
4. **Clean DI Graph:** Removing `CalendarViewModel`, `UpcomingViewModel`, and `LibraryViewModel` from `AndroidModule.kt` must leave no unresolved Koin bindings.
5. **No Orphaned Persistence Keys:** Removing `CalendarDisplayMode` must leave `PreferenceRepository` in a completely valid compile and runtime state without schema breakage.

---

### Task 1: Create Shared Unicode Emoticons Registry (`Emoticons.kt`) & Common Tests

**Files:**
- Create: `shared/src/commonMain/kotlin/ap/panini/procrastaint/util/Emoticons.kt`
- Test: `shared/src/commonTest/kotlin/ap/panini/procrastaint/util/EmoticonsTest.kt`

**Interfaces:**
- Consumes: None
- Produces:
  ```kotlin
  package ap.panini.procrastaint.util

  object Emoticons {
      val ALL: List<String>
      fun random(): String
  }
  ```

- [ ] **Step 1: Write the failing unit test**

Create `shared/src/commonTest/kotlin/ap/panini/procrastaint/util/EmoticonsTest.kt`:
```kotlin
package ap.panini.procrastaint.util

import kotlin.test.Test
import kotlin.test.assertTrue

class EmoticonsTest {
    @Test
    fun testEmoticonsPoolIsNotEmpty() {
        assertTrue(Emoticons.ALL.isNotEmpty())
        assertTrue(Emoticons.ALL.contains("(•‿•)"))
        assertTrue(Emoticons.ALL.contains(";-;"))
    }

    @Test
    fun testRandomEmoticonReturnsItemFromPool() {
        val emoticon = Emoticons.random()
        assertTrue(Emoticons.ALL.contains(emoticon))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :shared:testDebugUnitTest --tests "ap.panini.procrastaint.util.EmoticonsTest"`
Expected: Compilation failure because `Emoticons` does not exist yet.

- [ ] **Step 3: Implement `Emoticons.kt` in `shared`**

Create `shared/src/commonMain/kotlin/ap/panini/procrastaint/util/Emoticons.kt`:
```kotlin
package ap.panini.procrastaint.util

object Emoticons {
    val ALL: List<String> = listOf(
        "(•‿•)",
        "(^_^)",
        ";-;",
        "(｡◕‿◕｡)",
        "¯\\_(ツ)_/¯",
        "(>_<)",
        "(¬_¬)",
        "(✿◠‿◠)",
        "(ง'̀-'́)ง",
        "(◕‿◕✿)",
        "(o_o)",
        "(^o^)",
        "( ＾◡＾)",
        "(•_•)",
        "(¬‿¬)",
        "(~_~)",
        "(*^▽^*)",
        "(=^･^=)"
    )

    fun random(): String = ALL.random()
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :shared:testDebugUnitTest --tests "ap.panini.procrastaint.util.EmoticonsTest"`
Expected: Tests pass.

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/ap/panini/procrastaint/util/Emoticons.kt shared/src/commonTest/kotlin/ap/panini/procrastaint/util/EmoticonsTest.kt
git commit -m "feat: add multiplatform Unicode emoticons registry"
```

---

### Task 2: Integrate Emoticons into Widget & UI

**Files:**
- Create: `androidApp/src/main/java/ap/panini/procrastaint/ui/components/EmoticonUtil.kt`
- Modify: `androidApp/src/main/java/ap/panini/procrastaint/ui/widget/UpcomingWidget.kt:111-121,213-228`

**Interfaces:**
- Consumes: `ap.panini.procrastaint.util.Emoticons`
- Produces:
  ```kotlin
  package ap.panini.procrastaint.ui.components
  @Composable fun rememberRandomEmoticon(): String
  ```

- [ ] **Step 1: Create `EmoticonUtil.kt` for Compose UI**

Create `androidApp/src/main/java/ap/panini/procrastaint/ui/components/EmoticonUtil.kt`:
```kotlin
package ap.panini.procrastaint.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ap.panini.procrastaint.util.Emoticons

@Composable
fun rememberRandomEmoticon(): String = remember { Emoticons.random() }
```

- [ ] **Step 2: Update `UpcomingWidget.kt` to replace emojis**

In `androidApp/src/main/java/ap/panini/procrastaint/ui/widget/UpcomingWidget.kt`:
Add import:
```kotlin
import ap.panini.procrastaint.util.Emoticons
```
Update `MissedTasksHeader`:
```kotlin
    @Composable
    private fun MissedTasksHeader(count: Int) {
        Text(
            text = "${Emoticons.random()} Missed Tasks ($count)",
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.error
            ),
            modifier = GlanceModifier.padding(top = 10.dp, bottom = 4.dp)
        )
    }
```
Update `NoTasksView`:
```kotlin
    @Composable
    private fun NoTasksView() {
        Column(
            modifier = GlanceModifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = Emoticons.random(),
                style = TextStyle(
                    fontSize = 32.sp,
                    color = GlanceTheme.colors.onSurface
                )
            )
            Text(
                text = "No upcoming tasks",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlanceTheme.colors.onSurfaceVariant
                ),
                modifier = GlanceModifier.padding(top = 4.dp)
            )
        }
    }
```

- [ ] **Step 3: Verify build**

Run: `./gradlew assembleDebug`
Expected: Compilation succeeds.

- [ ] **Step 4: Commit**

```bash
git add androidApp/src/main/java/ap/panini/procrastaint/ui/components/EmoticonUtil.kt androidApp/src/main/java/ap/panini/procrastaint/ui/widget/UpcomingWidget.kt
git commit -m "feat(widget): replace emojis with dynamic unicode emoticons"
```

---

### Task 3: Relocate `ExternalEventView` to Preserve Agenda Integration

**Files:**
- Move: `androidApp/src/main/java/ap/panini/procrastaint/ui/calendar/components/ExternalEventView.kt` -> `androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/components/ExternalEventView.kt`
- Modify: `androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/components/AgendaFeed.kt:41`

**Interfaces:**
- Consumes: `ExternalCalendarEvent`
- Produces:
  ```kotlin
  package ap.panini.procrastaint.ui.agenda.components
  @Composable fun ExternalEventView(event: ExternalCalendarEvent, modifier: Modifier = Modifier)
  ```

- [ ] **Step 1: Move file using git**

Run:
```bash
git mv androidApp/src/main/java/ap/panini/procrastaint/ui/calendar/components/ExternalEventView.kt androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/components/ExternalEventView.kt
```

- [ ] **Step 2: Update package in `ExternalEventView.kt`**

Change package from `ap.panini.procrastaint.ui.calendar.components` to `ap.panini.procrastaint.ui.agenda.components`.

- [ ] **Step 3: Update import in `AgendaFeed.kt`**

In `androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/components/AgendaFeed.kt`:
Update or remove import `ap.panini.procrastaint.ui.calendar.components.ExternalEventView` since it is now in the same package `ap.panini.procrastaint.ui.agenda.components`.

- [ ] **Step 4: Verify build**

Run: `./gradlew assembleDebug`
Expected: Compilation succeeds.

- [ ] **Step 5: Commit**

```bash
git add androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/components/ExternalEventView.kt androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/components/AgendaFeed.kt
git commit -m "refactor(agenda): relocate ExternalEventView from calendar to agenda components"
```

---

### Task 4: Remove Obsolete Screen Files & ViewModels

**Files:**
- Delete: `androidApp/src/main/java/ap/panini/procrastaint/ui/calendar/` (all remaining files)
- Delete: `androidApp/src/main/java/ap/panini/procrastaint/ui/upcoming/` (all files)
- Delete: `androidApp/src/main/java/ap/panini/procrastaint/ui/library/` (all files)
- Modify: `androidApp/src/main/java/ap/panini/procrastaint/di/AndroidModule.kt`

**Interfaces:**
- Consumes: None
- Produces: `androidModule` without obsolete ViewModel bindings.

- [ ] **Step 1: Remove `CalendarViewModel`, `UpcomingViewModel`, and `LibraryViewModel` from `AndroidModule.kt`**

In `androidApp/src/main/java/ap/panini/procrastaint/di/AndroidModule.kt`:
Remove imports:
```kotlin
import ap.panini.procrastaint.ui.calendar.CalendarViewModel
import ap.panini.procrastaint.ui.library.LibraryViewModel
import ap.panini.procrastaint.ui.upcoming.UpcomingViewModel
```
Remove definitions:
```kotlin
viewModel { UpcomingViewModel(get()) }
viewModel { CalendarViewModel(get(), get(), get(), get()) }
viewModel { LibraryViewModel(get()) }
```

- [ ] **Step 2: Delete obsolete UI directories using git**

Run:
```bash
git rm -r androidApp/src/main/java/ap/panini/procrastaint/ui/calendar
git rm -r androidApp/src/main/java/ap/panini/procrastaint/ui/upcoming
git rm -r androidApp/src/main/java/ap/panini/procrastaint/ui/library
```

- [ ] **Step 3: Commit**

```bash
git add androidApp/src/main/java/ap/panini/procrastaint/di/AndroidModule.kt
git commit -m "refactor: remove obsolete calendar, upcoming, and library screens and viewmodels"
```

---

### Task 5: Prune Routes, NavGraph, and FAB Entry Points

**Files:**
- Modify: `androidApp/src/main/java/ap/panini/procrastaint/ui/navigation/Route.kt:15-23`
- Modify: `androidApp/src/main/java/ap/panini/procrastaint/ui/navigation/NavGraph.kt:15-24,46-55`
- Modify: `androidApp/src/main/java/ap/panini/procrastaint/ui/MainActivity.kt:83-89`

**Interfaces:**
- Consumes: None
- Produces: Cleaned `Route` interface and `NavGraph` without dead destinations.

- [ ] **Step 1: Update `Route.kt`**

In `androidApp/src/main/java/ap/panini/procrastaint/ui/navigation/Route.kt`:
Remove:
```kotlin
    @Serializable
    data object Calendar : Route

    @Serializable
    data object Upcoming : Route

    @Serializable
    data object Library : Route
```

- [ ] **Step 2: Update `NavGraph.kt`**

In `androidApp/src/main/java/ap/panini/procrastaint/ui/navigation/NavGraph.kt`:
Remove imports:
```kotlin
import ap.panini.procrastaint.ui.calendar.CalendarScreen
import ap.panini.procrastaint.ui.library.LibraryScreen
import ap.panini.procrastaint.ui.upcoming.UpcomingScreen
```
Remove destination composable blocks:
```kotlin
        composable<Route.Calendar> { CalendarScreen() }

        composable<Route.Upcoming> { UpcomingScreen() }

        composable<Route.Library> {
            LibraryScreen(
                onNavigateToTag = { navController.navigate(it) }
            )
        }
```

- [ ] **Step 3: Update `MainActivity.kt`**

In `androidApp/src/main/java/ap/panini/procrastaint/ui/MainActivity.kt`:
In `showFab`, remove `Route.Upcoming::class`, `Route.Calendar::class`, and `Route.Library::class`:
```kotlin
        val showFab = remember(currentDestination) {
            listOf(
                Route.Agenda::class,
                Route.Tag::class
            ).isEntryIn(currentDestination)
        }
```

- [ ] **Step 4: Verify build**

Run: `./gradlew assembleDebug`
Expected: Compilation succeeds.

- [ ] **Step 5: Commit**

```bash
git add androidApp/src/main/java/ap/panini/procrastaint/ui/navigation/Route.kt androidApp/src/main/java/ap/panini/procrastaint/ui/navigation/NavGraph.kt androidApp/src/main/java/ap/panini/procrastaint/ui/MainActivity.kt
git commit -m "refactor(navigation): prune obsolete calendar, upcoming, and library routes"
```

---

### Task 6: Purge Calendar Display Mode from Shared Preferences & Verify All Builds

**Files:**
- Delete: `shared/src/commonMain/kotlin/ap/panini/procrastaint/ui/calendar/CalendarDisplayMode.kt`
- Modify: `shared/src/commonMain/kotlin/ap/panini/procrastaint/data/repositories/PreferenceRepository.kt`

**Interfaces:**
- Consumes: None
- Produces: `PreferenceRepository` without calendar display preferences.

- [ ] **Step 1: Delete `CalendarDisplayMode.kt` using git**

Run:
```bash
git rm shared/src/commonMain/kotlin/ap/panini/procrastaint/ui/calendar/CalendarDisplayMode.kt
```

- [ ] **Step 2: Clean up `PreferenceRepository.kt`**

In `shared/src/commonMain/kotlin/ap/panini/procrastaint/data/repositories/PreferenceRepository.kt`:
Remove:
```kotlin
import ap.panini.procrastaint.ui.calendar.CalendarDisplayMode
```
Remove constant:
```kotlin
const val CALENDAR_DISPLAY_MODE = "calendar_display_mode"
```
Remove from `stringPreference` defaults:
```kotlin
CALENDAR_DISPLAY_MODE to CalendarDisplayMode.DAILY.name,
```
Remove methods:
```kotlin
    fun getCalendarDisplayMode(): Flow<CalendarDisplayMode> = dataStore.data.map { preferences ->
        val modeName = preferences[stringPreferencesKey(CALENDAR_DISPLAY_MODE)] ?: CalendarDisplayMode.DAILY.name
        CalendarDisplayMode.valueOf(modeName)
    }

    suspend fun setCalendarDisplayMode(displayMode: CalendarDisplayMode) {
        dataStore.edit { settings ->
            settings[stringPreferencesKey(CALENDAR_DISPLAY_MODE)] = displayMode.name
        }
    }
```

- [ ] **Step 3: Run full verification commands**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

Run: `./gradlew detektall`
Expected: BUILD SUCCESSFUL with 0 issues.

Run: `./gradlew allTests` or `./gradlew testDebugUnitTest`
Expected: All tests pass.

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonMain/kotlin/ap/panini/procrastaint/data/repositories/PreferenceRepository.kt
git commit -m "refactor(preferences): remove obsolete CalendarDisplayMode and preference keys"
```
