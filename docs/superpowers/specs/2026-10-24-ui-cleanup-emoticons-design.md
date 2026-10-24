# UI Cleanup: Unicode Emoticons & Obsolete Screens Removal Design

## 1. Intent & Context
Following the recent UI revamp that introduced the unified edge-to-edge `AgendaScreen` as the default destination, several screens (`UpcomingScreen`, `LibraryScreen`, and `CalendarScreen`) were left orphaned without any user navigation entry points. Additionally, leftover emoji glyphs (such as the `🚨` alert and `✨` spark in `UpcomingWidget`) should be replaced with retro/playful Unicode text emoticons (kaomoji), picked dynamically at runtime from a centralized pool.

The goal of this cleanup is to:
1. Establish a cross-platform Unicode emoticon registry with a single pool of emoticons and randomized selection.
2. Remove all emojis from the app and widgets, replacing them with randomized emoticons from this pool.
3. Cleanly delete all orphaned screens, subviews, viewmodels, navigation routes, and dead preference keys without disrupting the remaining features (specifically preserving `ExternalEventView` used by `AgendaFeed`).

---

## 2. Goals, Non-Goals, & Anti-Goals

### Goals
* **Single Emoticon Registry:** Provide a centralized `Emoticons` object in `shared/src/commonMain/` that provides a curated list of text emoticons and a `random(): String` selector.
* **Replace Emojis:** Replace all emoji usage in widgets (and any UI strings) with dynamic Unicode text emoticons.
* **Preserve Agenda External Event Rendering:** Move `ExternalEventView` out of `ui.calendar.components` so `AgendaFeed` continues to render Google Calendar events.
* **Complete Screen Purge:** Delete all files under `ui/calendar/`, `ui/upcoming/`, and `ui/library/`, including screen composables, child components, paging sources, and `CalendarViewModel`.
* **Prune Navigation & DI:** Remove dead routes (`Route.Calendar`, `Route.Upcoming`, `Route.Library`) from `Route.kt`, `NavGraph.kt`, and `MainActivity.kt`, and remove `CalendarViewModel` from Koin `AndroidModule.kt`.
* **Clean Data Persistence:** Remove `CalendarDisplayMode` and its preference methods from `PreferenceRepository.kt` in `shared`.

### Non-Goals
* Categorizing emoticons into distinct moods or contexts (the user decided on a single pool).
* Redesigning or modifying the active `AgendaScreen`, `TagScreen`, `SettingsScreen`, or onboarding flows.
* Adding new navigation routes or bottom bar tabs.

### Anti-Goals
* Breaking external Google Calendar event integration in `AgendaFeed`.
* Leaving behind dead preference keys, unused imports, or orphaned routes.
* Recomposition flickering in Compose UI when displaying a randomized emoticon (must be stabilized using `remember`).

---

## 3. Approaches Considered & Decision

### Approach 1: Shared Multiplatform Registry + Clean Repository Pruning (Selected)
* **Design:**
  * Define `Emoticons` in `shared/src/commonMain/kotlin/ap/panini/procrastaint/util/Emoticons.kt`.
  * Move `ExternalEventView.kt` to `androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/components/` (or `ui/components/`).
  * Delete all files in `ui/calendar/`, `ui/upcoming/`, and `ui/library/`.
  * Prune `CalendarDisplayMode` and its preferences from `PreferenceRepository.kt`.
  * Remove `CalendarViewModel` from Koin `AndroidModule.kt`.
  * Prune routes from `Route.kt`, `NavGraph.kt`, and `MainActivity.kt`.
* **Tradeoffs:** Touches both `shared` and `androidApp`, but results in a completely clean codebase with zero orphaned code or dead persistence keys.
* **Why Chosen:** The user explicitly selected Approach 1.

### Approach 2: Android-Local Utility + Shallow Screen Removal
* **Design:** Keep `Emoticons.kt` strictly inside `androidApp`, delete the UI screens, but leave `CalendarDisplayMode` and its DataStore preference keys intact inside `shared`.
* **Tradeoffs:** Avoids editing `shared`, but leaves behind dead persistence keys, dead enum classes, and denies multiplatform access to the emoticon pool.
* **Why Dropped:** Rejected in favor of the clean, complete pruning in Approach 1.

---

## 4. Detailed Specification

### 4.1 Unicode Emoticons Registry (`Emoticons.kt`)
Located at `shared/src/commonMain/kotlin/ap/panini/procrastaint/util/Emoticons.kt`:
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

In `androidApp`, provide a Compose utility (e.g. in `ui/components/Emoticons.kt` or inline):
```kotlin
@Composable
fun rememberRandomEmoticon(): String = remember { Emoticons.random() }
```

### 4.2 Widget & UI Integration
In `androidApp/src/main/java/ap/panini/procrastaint/ui/widget/UpcomingWidget.kt`:
1. **Missed Tasks Header:**
   Replace:
   ```kotlin
   text = "🚨 Missed Tasks ($count)"
   ```
   With:
   ```kotlin
   text = "${Emoticons.random()} Missed Tasks ($count)"
   ```
2. **No Tasks (Empty State):**
   Replace:
   ```kotlin
   text = "✨"
   ```
   With:
   ```kotlin
   text = Emoticons.random()
   ```

### 4.3 Preservation & Relocation of `ExternalEventView`
* Move `androidApp/src/main/java/ap/panini/procrastaint/ui/calendar/components/ExternalEventView.kt` to `androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/components/ExternalEventView.kt`.
* Update package declaration to `ap.panini.procrastaint.ui.agenda.components`.
* Update import in `androidApp/src/main/java/ap/panini/procrastaint/ui/agenda/components/AgendaFeed.kt`.

### 4.4 Files and Packages to Delete
* Entire directory `androidApp/src/main/java/ap/panini/procrastaint/ui/upcoming/`
* Entire directory `androidApp/src/main/java/ap/panini/procrastaint/ui/library/`
* Entire directory `androidApp/src/main/java/ap/panini/procrastaint/ui/calendar/` (after relocating `ExternalEventView.kt`)
* File `shared/src/commonMain/kotlin/ap/panini/procrastaint/ui/calendar/CalendarDisplayMode.kt`

### 4.5 Navigation & DI Updates
1. **`Route.kt`:**
   Remove:
   - `data object Calendar : Route`
   - `data object Upcoming : Route`
   - `data object Library : Route`
2. **`NavGraph.kt`:**
   Remove composable registrations for `Route.Calendar`, `Route.Upcoming`, and `Route.Library`.
   Remove corresponding screen imports.
3. **`MainActivity.kt`:**
   In `showFab`, remove `Route.Upcoming::class`, `Route.Calendar::class`, `Route.Library::class`.
4. **`AndroidModule.kt`:**
   Remove `viewModel { CalendarViewModel(get(), get(), get(), get()) }` and its import.

### 4.6 Preference Persistence Cleanup
In `shared/src/commonMain/kotlin/ap/panini/procrastaint/data/repositories/PreferenceRepository.kt`:
* Remove `const val CALENDAR_DISPLAY_MODE = "calendar_display_mode"`.
* Remove `CALENDAR_DISPLAY_MODE to CalendarDisplayMode.DAILY.name` from defaults.
* Remove `fun getCalendarDisplayMode(): Flow<CalendarDisplayMode>`.
* Remove `suspend fun setCalendarDisplayMode(displayMode: CalendarDisplayMode)`.
* Remove `import ap.panini.procrastaint.ui.calendar.CalendarDisplayMode`.

---

## 5. Verification & Quality Assurance
1. **Compilation:** Execute `./gradlew assembleDebug` to verify common and Android targets compile cleanly.
2. **Static Analysis & Linting:** Run `./gradlew detektall` to ensure no lint, formatting, or unused import violations.
3. **Widget Verification:** Verify `UpcomingWidget` compiles and references `Emoticons.random()`.
4. **Agenda Verification:** Verify that `AgendaScreen` and `AgendaFeed` build and reference the relocated `ExternalEventView`.
