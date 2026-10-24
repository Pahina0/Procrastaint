# Generalized Date & Time Settings Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Generalize date & time configuration so user preferences (12h/24h, US/European date order, and "Follow System") control both natural language text parsing and app-wide date/time display formatting, with an in-screen visual diff comparison.

**Architecture:** A tri-state preference model (`SYSTEM`, `H12`/`MDY`, `H24`/`DMY`) persisted in DataStore; a platform bridge queries Android's `DateFormat.is24HourFormat` and `getDateFormatOrder`; a reactive in-memory configuration cache in `Date.kt` drives formatting across all UI screens, calendar views, and widgets with zero latency; `Parser.kt` synchronizes parsing heuristics in lockstep; the settings UI provides 3-way segmented buttons, an interactive "Before & After" diff comparison card, and sandbox.

**Tech Stack:** Kotlin Multiplatform (Common, Android, iOS), Jetpack Compose, Material 3, AndroidX DataStore Preferences, kotlinx-datetime, KWhen.

**Spec:** [`docs/superpowers/specs/2026-10-24-generalized-datetime-settings-design.md`](file:///Users/pahina/Documents/Procrastaint/docs/superpowers/specs/2026-10-24-generalized-datetime-settings-design.md)

## Global Constraints
* Never import `android.*` or `java.time.*` in `shared/src/commonMain`.
* All date/time calculations must use `kotlinx.datetime` and `kotlin.time`.
* Display formatting and natural language parsing must be 100% synchronized when options are selected.
* Reset to defaults must restore `SYSTEM`, `SYSTEM`, and keyword hours `9, 15, 18, 20`.

## Review Focus
* `SYSTEM` mode on a 24-hour device parses and displays as 24-hour (`"14:00"`).
* `SYSTEM` mode on a 12-hour device parses and displays as 12-hour AM/PM (`"2:00 PM"`).
* European date mode (`DMY`) reorders Month+Day to Day-first (`"5 Apr"`) and full dates to `"DD/MM/YYYY"`.
* US date mode (`MDY`) formats Month-first (`"Apr 5"`) and full dates to `"MM/DD/YYYY"`.
* Baseline diff comparison in settings highlights exact date/time changes when settings are edited.

---

### Task 1: Platform & Repository - Tri-State Preference Management & System Detection

**Files:**
- Create: `shared/src/commonMain/kotlin/ap/panini/procrastaint/data/models/DateTimeFormatModes.kt`
- Create: `shared/src/commonMain/kotlin/ap/panini/procrastaint/util/SystemDateTimeProvider.kt`
- Create: `shared/src/androidMain/kotlin/ap/panini/procrastaint/util/SystemDateTimeProvider.android.kt`
- Create: `shared/src/iosMain/kotlin/ap/panini/procrastaint/util/SystemDateTimeProvider.ios.kt`
- Modify: `shared/src/commonMain/kotlin/ap/panini/procrastaint/data/repositories/PreferenceRepository.kt`
- Test: `shared/src/commonTest/kotlin/ap/panini/procrastaint/data/repositories/PreferenceRepositoryTest.kt`

**Interfaces:**
- Produces:
  - `enum class TimeFormatMode { SYSTEM, H12, H24 }`
  - `enum class DateFormatMode { SYSTEM, MDY, DMY }`
  - `data class ResolvedDateTimeFormat(val use24Hour: Boolean, val useEuropeanDate: Boolean)`
  - `expect class SystemDateTimeProvider { fun is24HourFormat(): Boolean; fun isEuropeanDateFormat(): Boolean }`
  - In `PreferenceRepository`:
    - `fun getTimeFormatMode(): Flow<TimeFormatMode>`
    - `fun getDateFormatMode(): Flow<DateFormatMode>`
    - `suspend fun setTimeFormatMode(mode: TimeFormatMode)`
    - `suspend fun setDateFormatMode(mode: DateFormatMode)`
    - `fun getResolvedDateTimeFormat(): Flow<ResolvedDateTimeFormat>`

- [ ] **Step 1: Write the failing tests in `PreferenceRepositoryTest.kt`**

```kotlin
@Test
fun testTimeAndDateFormatModesDefaultsToSystem() = runBlocking {
    val repo = createTestRepository()
    val timeMode = repo.getTimeFormatMode().first()
    val dateMode = repo.getDateFormatMode().first()
    assertEquals(TimeFormatMode.SYSTEM, timeMode)
    assertEquals(DateFormatMode.SYSTEM, dateMode)
}

@Test
fun testSettingExplicitFormatModes() = runBlocking {
    val repo = createTestRepository()
    repo.setTimeFormatMode(TimeFormatMode.H24)
    repo.setDateFormatMode(DateFormatMode.DMY)
    assertEquals(TimeFormatMode.H24, repo.getTimeFormatMode().first())
    assertEquals(DateFormatMode.DMY, repo.getDateFormatMode().first())
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :shared:testDebugUnitTest --tests "ap.panini.procrastaint.data.repositories.PreferenceRepositoryTest"`
Expected: Compilation failure due to missing types and methods.

- [ ] **Step 3: Implement models, expect/actual provider, and repository methods**
  - Define `TimeFormatMode`, `DateFormatMode`, and `ResolvedDateTimeFormat` in `shared/src/commonMain/kotlin/ap/panini/procrastaint/data/models/DateTimeFormatModes.kt`.
  - Define `expect class SystemDateTimeProvider` in `commonMain`.
  - Implement `actual class SystemDateTimeProvider` for Android using `android.text.format.DateFormat.is24HourFormat` and `getDateFormatOrder` checking if `'d'` precedes `'M'`.
  - Implement `actual class SystemDateTimeProvider` for iOS fallback (`false`, `false`).
  - In `PreferenceRepository.kt`, add preference keys `"time_format_mode"` and `"date_format_mode"`, wire getters/setters, implement `getResolvedDateTimeFormat()`, and update `getKwhenConfig()` to combine keyword hours with resolved flags.

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :shared:testDebugUnitTest --tests "ap.panini.procrastaint.data.repositories.PreferenceRepositoryTest"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add shared/src/
git commit -m "feat(shared): add tri-state time and date format preferences with device detection"
```

---

### Task 2: Shared Display Formatting Engine & Reactive Synchronization

**Files:**
- Modify: `shared/src/commonMain/kotlin/ap/panini/procrastaint/util/Date.kt`
- Modify: `shared/src/commonMain/kotlin/ap/panini/procrastaint/di/AppModule.kt`
- Test: `shared/src/commonTest/kotlin/ap/panini/procrastaint/util/DateFormattingTest.kt`

**Interfaces:**
- Consumes:
  - `ResolvedDateTimeFormat` from Task 1
- Produces:
  - `Date.displayConfig: ResolvedDateTimeFormat`
  - `Long.formatMilliseconds(known: Set<Time>, smart: Boolean, useAbbreviated: Boolean): String` (reacts to `displayConfig`)
  - `LocalDate.formatCustom(useEuropean: Boolean = Date.displayConfig.useEuropeanDate): String`

- [ ] **Step 1: Write the failing tests in `DateFormattingTest.kt`**

```kotlin
class DateFormattingTest {
    @Test
    fun test12HourVs24HourTimeFormatting() {
        // 2:30 PM (14:30)
        val epochMs = 1775313000000L // arbitrary fixed time
        Date.displayConfig = ResolvedDateTimeFormat(use24Hour = false, useEuropeanDate = false)
        val formatted12 = with(Date) { epochMs.formatMilliseconds(setOf(Time.HOUR, Time.MINUTE), smart = false) }
        assertTrue(formatted12.contains("PM") || formatted12.contains("AM"))

        Date.displayConfig = ResolvedDateTimeFormat(use24Hour = true, useEuropeanDate = false)
        val formatted24 = with(Date) { epochMs.formatMilliseconds(setOf(Time.HOUR, Time.MINUTE), smart = false) }
        assertFalse(formatted24.contains("PM") || formatted24.contains("AM"))
    }

    @Test
    fun testUSVsEuropeanDateReordering() {
        val epochMs = 1775313000000L // e.g. April 4th or 5th
        Date.displayConfig = ResolvedDateTimeFormat(use24Hour = false, useEuropeanDate = false)
        val usDate = with(Date) { epochMs.formatMilliseconds(setOf(Time.MONTH, Time.DAY), smart = false, useAbbreviated = true) }
        assertTrue(usDate.matches(Regex("[A-Za-z]+ \\d+")), "Expected Month Day in US mode, got $usDate")

        Date.displayConfig = ResolvedDateTimeFormat(use24Hour = false, useEuropeanDate = true)
        val euDate = with(Date) { epochMs.formatMilliseconds(setOf(Time.MONTH, Time.DAY), smart = false, useAbbreviated = true) }
        assertTrue(euDate.matches(Regex("\\d+ [A-Za-z]+")), "Expected Day Month in EU mode, got $euDate")
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :shared:testDebugUnitTest --tests "ap.panini.procrastaint.util.DateFormattingTest"`
Expected: FAIL

- [ ] **Step 3: Implement display configuration in `Date.kt` and Koin lifecycle wiring in `AppModule.kt`**
  - Add `@Volatile var displayConfig: ResolvedDateTimeFormat` to `object Date`.
  - Update `formatDate(...)` in `Date.kt`: if `displayConfig.useEuropeanDate` is true, reorder to Day before Month (e.g. `"$day $month"` instead of `"$month $day"`), and format full dates as `"DD/MM/YYYY"`.
  - Update `formatTime(...)` in `Date.kt`: if `displayConfig.use24Hour` is true, format 24-hour padded hours/minutes without AM/PM marker.
  - Implement `LocalDate.formatCustom(useEuropean: Boolean)`.
  - In `AppModule.kt`, launch coroutine collection from `PreferenceRepository.getResolvedDateTimeFormat()` updating `Date.displayConfig`.

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :shared:testDebugUnitTest --tests "ap.panini.procrastaint.util.DateFormattingTest"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add shared/src/
git commit -m "feat(shared): support 24-hour and European date reordering in Date formatters"
```

---

### Task 3: Parser Synchronization Verification

**Files:**
- Modify: `shared/src/commonTest/kotlin/ap/panini/procrastaint/util/ParserConfigTest.kt`
- Modify: `shared/src/commonMain/kotlin/ap/panini/procrastaint/util/Parser.kt`

**Interfaces:**
- Consumes:
  - `getKwhenConfig()` with resolved flags from Task 1

- [ ] **Step 1: Write test in `ParserConfigTest.kt` verifying tri-state mode propagates to Parser**

```kotlin
@Test
fun testTriStateModePropagatesToParser() = runBlocking {
    val repo = createTestRepository()
    val parser = Parser(preferenceRepository = repo)

    repo.setDateFormatMode(DateFormatMode.DMY)
    waitForCondition { parser.cachedConfig.useEuropeanDate }
    val parsed = parser.parse("Doctor visit 03/04/2026")
    val startTime = parsed.times.first().startTimes.first()
    val localTime = Instant.fromEpochMilliseconds(startTime).toLocalDateTime(TimeZone.currentSystemDefault())
    assertEquals(Month.APRIL, localTime.month)
    assertEquals(3, localTime.day)
}
```

- [ ] **Step 2: Run test to verify it passes or fails**

Run: `./gradlew :shared:testDebugUnitTest --tests "ap.panini.procrastaint.util.ParserConfigTest"`
Expected: PASS (or fix any timing / propagation nuances in `Parser.kt`).

- [ ] **Step 3: Commit**

```bash
git add shared/src/
git commit -m "test(shared): verify parser synchronizes with tri-state date and time preferences"
```

---

### Task 4: Settings ViewModel & Diff Calculation Engine

**Files:**
- Modify: `androidApp/src/main/java/ap/panini/procrastaint/ui/settings/parser/ParserSettingsViewModel.kt`

**Interfaces:**
- Produces:
  - `data class ComparisonDiffItem(val label: String, val input: String, val previousOutput: String, val currentOutput: String, val hasChanged: Boolean)`
  - `ParserSettingsUiState` updated with:
    - `timeFormatMode: TimeFormatMode`
    - `dateFormatMode: DateFormatMode`
    - `is24HourResolved: Boolean`
    - `isEuropeanDateResolved: Boolean`
    - `comparisonDiffs: List<ComparisonDiffItem>`
  - View model methods:
    - `setTimeFormatMode(mode: TimeFormatMode)`
    - `setDateFormatMode(mode: DateFormatMode)`

- [ ] **Step 1: Define comparison diff models and capture baseline config on VM creation**
  - Store `baselineConfig` captured on initial collection.
  - Maintain comparison preset phrases:
    - `"Gym tomorrow morning"`
    - `"Doctor 04/05 at 8"`
    - `"Meeting every Tuesday at 17:00"`
  - For each phrase, compute parsed output using baseline settings vs current draft settings, setting `hasChanged = previousOutput != currentOutput`.

- [ ] **Step 2: Add tri-state setters in `ParserSettingsViewModel`**
  - Add `setTimeFormatMode(mode: TimeFormatMode)` launching `preferenceRepository.setTimeFormatMode(mode)`.
  - Add `setDateFormatMode(mode: DateFormatMode)` launching `preferenceRepository.setDateFormatMode(mode)`.

- [ ] **Step 3: Build verification**

Run: `./gradlew :androidApp:compileDebugKotlin`
Expected: SUCCESS

- [ ] **Step 4: Commit**

```bash
git add androidApp/src/main/java/ap/panini/procrastaint/ui/settings/parser/ParserSettingsViewModel.kt
git commit -m "feat(ui): add comparison diff calculations and tri-state modes to ParserSettingsViewModel"
```

---

### Task 5: Settings UI & Comparison Component

**Files:**
- Create: `androidApp/src/main/java/ap/panini/procrastaint/ui/settings/parser/components/ComparisonDiffCard.kt`
- Modify: `androidApp/src/main/java/ap/panini/procrastaint/ui/settings/parser/ParserSettingsScreen.kt`
- Modify: `androidApp/src/main/java/ap/panini/procrastaint/ui/settings/SettingsScreen.kt`
- Modify: `androidApp/src/main/java/ap/panini/procrastaint/ui/calendar/weekly/ScrollableWeekView.kt`

**Interfaces:**
- Consumes:
  - `ComparisonDiffItem` and `ParserSettingsUiState` from Task 4

- [ ] **Step 1: Create `ComparisonDiffCard.kt`**
  - Renders a styled Material 3 Card titled **"Before & After Comparison"** with subtitle *"Visual changes compared to your previous settings"*.
  - Displays each diff phrase: input text, previous parsed result, and current parsed result.
  - If `hasChanged == true`, highlight with `MaterialTheme.colorScheme.primaryContainer` and a `"Changed"` badge.

- [ ] **Step 2: Update `ParserSettingsScreen.kt`**
  - Screen title updated to **"Date & Time"**.
  - Replace binary switches with Material 3 `SingleChoiceSegmentedButtonRow`:
    - Time Format: `[ System | 12-Hour | 24-Hour ]` with resolved explanation subtext.
    - Date Format: `[ System | MM/DD/YYYY | DD/MM/YYYY ]` with resolved explanation subtext.
  - Insert `ComparisonDiffCard` above the `LiveSandboxCard`.
  - Wire TopAppBar Reset button to restore defaults.

- [ ] **Step 3: Update `SettingsScreen.kt` and `ScrollableWeekView.kt`**
  - In `SettingsScreen.kt`, update entry item title to `"Date & Time"` and subtext to `"Configure time and date formats, keywords, and display"`.
  - In `ScrollableWeekView.kt`, update `amPmFormatter` to display 24-hour time when `Date.displayConfig.use24Hour` is true.

- [ ] **Step 4: Verify full build and tests**

Run: `./gradlew :shared:testDebugUnitTest assembleDebug detektall`
Expected: ALL PASS, 0 detekt warnings.

- [ ] **Step 5: Commit**

```bash
git add androidApp/
git commit -m "feat(ui): implement Date & Time screen with segmented controls and comparison diff card"
```
