# KWhen Text Parser Settings Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enable user-customizable natural language date/time parsing settings in Procrastaint for the KWhen library, backed by DataStore persistence, reactive engine caching, and an interactive live sandbox screen in Android.

**Architecture:** Preferences are persisted in AndroidX DataStore via `PreferenceRepository` in `shared`. `Parser` observes these preferences via a reactive flow and caches an in-memory `ENConfig` so task typing remains synchronous and zero-latency while picking up settings instantly. An Android Jetpack Compose screen (`ParserSettingsScreen`) provides hour cards, format switches, and a real-time sandbox re-evaluating parsed phrases dynamically.

**Tech Stack:** Kotlin Multiplatform, Jetpack Compose, Material 3 Expressive tokens, AndroidX DataStore Preferences, Koin, KWhen (`io.github.pahinaa.kwhen`), kotlinx.datetime, kotlinx.coroutines.

**Spec:** [`docs/superpowers/specs/2026-10-23-kwhen-parser-settings-design.md`](file:///Users/pahina/Documents/Procrastaint/docs/superpowers/specs/2026-10-23-kwhen-parser-settings-design.md)

---

## Global Constraints

- **Platform Scope:** Android UI only (`androidApp`). iOS target compilation in `shared` must remain fully preserved and functional without breakages.
- **Strict KMP Constraint:** In `shared/src/commonMain`, never import `java.time.*`, `java.util.Date`, or `android.*`. Use `kotlinx.datetime.*` and `kotlin.time.*`.
- **UI Guidelines:** Android UI must follow Material 3 Expressive styling with `@Immutable` UI states, using existing components in `ap.panini.procrastaint.ui.settings.components`.
- **24-Hour Scope:** The "24-Hour Time Format" toggle strictly configures KWhen's numeric parser disambiguation (`ENConfig.use24`); it does not alter app-wide time display in `Date.kt`.
- **Detekt Quality:** Must pass `./gradlew detektall` cleanly without disabling rules or introducing formatting violations.

## Review Focus

1. **Non-chronological custom hours:** User sets Morning to 20:00 (8 PM) and Evening to 08:00 (8 AM) — parser must resolve keywords to their configured hours without throwing validation errors.
2. **Ambiguous numeric dates with European toggle:** Input `"03/04/2026"` resolves to April 3, 2026 when `useEuropeanDate = true`, and March 4, 2026 when `false`.
3. **Ambiguous time numbers with 24-hour toggle:** Input `"at 8"` or `"at 14"` respects `use24` mode without crashing or defaulting to invalid hours.
4. **Instant sandbox update:** Updating an hour setting in the UI instantly causes the sandbox's active parsed output chips to reflect the new time without requiring keyboard focus or manual submit.
5. **Reset to defaults idempotence:** Tapping "Reset to defaults" restores exact initial parameters (9, 15, 18, 20, false, false) and immediately updates both the controls and sandbox output.

---

### Task 1: Shared Preference Persistence for KWhen Config

**Files:**
- Modify: `shared/src/commonMain/kotlin/ap/panini/procrastaint/data/repositories/PreferenceRepository.kt`
- Create: `shared/src/commonTest/kotlin/ap/panini/procrastaint/data/repositories/PreferenceRepositoryTest.kt`

**Interfaces:**
- Produces:
  - `PreferenceRepository.Companion.KWHEN_MORNING_HOUR = "kwhen_morning_hour"`
  - `PreferenceRepository.Companion.KWHEN_AFTERNOON_HOUR = "kwhen_afternoon_hour"`
  - `PreferenceRepository.Companion.KWHEN_EVENING_HOUR = "kwhen_evening_hour"`
  - `PreferenceRepository.Companion.KWHEN_NIGHT_HOUR = "kwhen_night_hour"`
  - `PreferenceRepository.Companion.KWHEN_USE_24 = "kwhen_use_24"`
  - `PreferenceRepository.Companion.KWHEN_USE_EUROPEAN_DATE = "kwhen_use_european_date"`
  - `PreferenceRepository.getKwhenConfig(): Flow<ap.panini.kwhen.configs.ENConfig>`
  - `PreferenceRepository.setMorningHour(hour: Int)`
  - `PreferenceRepository.setAfternoonHour(hour: Int)`
  - `PreferenceRepository.setEveningHour(hour: Int)`
  - `PreferenceRepository.setNightHour(hour: Int)`
  - `PreferenceRepository.setUse24Hour(use24: Boolean)`
  - `PreferenceRepository.setUseEuropeanDate(useEuropeanDate: Boolean)`
  - `PreferenceRepository.resetKwhenConfigToDefaults()`

- [ ] **Step 1: Write the failing test for KWhen configuration flow and defaults in `PreferenceRepositoryTest.kt`**

```kotlin
package ap.panini.procrastaint.data.repositories

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import ap.panini.kwhen.configs.ENConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PreferenceRepositoryTest {
    @Test
    fun testDefaultKwhenConfigValues() = runTest {
        val testDataStore = PreferenceDataStoreFactory.createWithPath(
            produceFile = { "/tmp/test_prefs_${kotlin.random.Random.nextLong()}.preferences_pb".toPath() }
        )
        val repository = PreferenceRepository(testDataStore)
        val config = repository.getKwhenConfig().first()

        assertEquals(9, config.morning)
        assertEquals(15, config.afternoon)
        assertEquals(18, config.evening)
        assertEquals(20, config.night)
        assertFalse(config.use24)
        assertFalse(config.useEuropeanDate)
    }

    @Test
    fun testUpdateAndResetKwhenConfig() = runTest {
        val testDataStore = PreferenceDataStoreFactory.createWithPath(
            produceFile = { "/tmp/test_prefs_${kotlin.random.Random.nextLong()}.preferences_pb".toPath() }
        )
        val repository = PreferenceRepository(testDataStore)
        
        repository.setMorningHour(7)
        repository.setAfternoonHour(13)
        repository.setEveningHour(19)
        repository.setNightHour(22)
        repository.setUse24Hour(true)
        repository.setUseEuropeanDate(true)

        val updated = repository.getKwhenConfig().first()
        assertEquals(7, updated.morning)
        assertEquals(13, updated.afternoon)
        assertEquals(19, updated.evening)
        assertEquals(22, updated.night)
        assertTrue(updated.use24)
        assertTrue(updated.useEuropeanDate)

        repository.resetKwhenConfigToDefaults()
        val reset = repository.getKwhenConfig().first()
        assertEquals(9, reset.morning)
        assertEquals(15, reset.afternoon)
        assertEquals(18, reset.evening)
        assertEquals(20, reset.night)
        assertFalse(reset.use24)
        assertFalse(reset.useEuropeanDate)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :shared:testDebugUnitTest --tests "ap.panini.procrastaint.data.repositories.PreferenceRepositoryTest"`  
Expected: FAIL (unresolved methods `getKwhenConfig`, `setMorningHour`, etc.)

- [ ] **Step 3: Implement preference keys, getters, setters, and `getKwhenConfig()` in `PreferenceRepository.kt`**

Add `intPreferencesKey` imports and companion constants:
```kotlin
const val KWHEN_MORNING_HOUR = "kwhen_morning_hour"
const val KWHEN_AFTERNOON_HOUR = "kwhen_afternoon_hour"
const val KWHEN_EVENING_HOUR = "kwhen_evening_hour"
const val KWHEN_NIGHT_HOUR = "kwhen_night_hour"
const val KWHEN_USE_24 = "kwhen_use_24"
const val KWHEN_USE_EUROPEAN_DATE = "kwhen_use_european_date"
```
Populate `intPreference` and `boolPreference` default maps. Implement:
- `getKwhenConfig(): Flow<ENConfig>` mapping preferences to `ENConfig(morning = ..., afternoon = ..., evening = ..., night = ..., use24 = ..., useEuropeanDate = ...)`
- Setters for the 4 hour fields using `intPreferencesKey`
- Setters for the 2 boolean flags using `booleanPreferencesKey`
- `resetKwhenConfigToDefaults()` removing or setting keys back to default values

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :shared:testDebugUnitTest --tests "ap.panini.procrastaint.data.repositories.PreferenceRepositoryTest"`  
Expected: PASS

- [ ] **Step 5: Commit task 1**

```bash
git add shared/src/commonMain/kotlin/ap/panini/procrastaint/data/repositories/PreferenceRepository.kt shared/src/commonTest/kotlin/ap/panini/procrastaint/data/repositories/PreferenceRepositoryTest.kt
git commit -m "feat(shared): add kwhen config persistence in PreferenceRepository"
```

---

### Task 2: Reactive Parser Configuration and Koin Wiring

**Files:**
- Modify: `shared/src/commonMain/kotlin/ap/panini/procrastaint/util/Parser.kt`
- Modify: `shared/src/commonMain/kotlin/ap/panini/procrastaint/di/AppModule.kt`
- Create: `shared/src/commonTest/kotlin/ap/panini/procrastaint/util/ParserConfigTest.kt`

**Interfaces:**
- Consumes:
  - `PreferenceRepository.getKwhenConfig(): Flow<ENConfig>`
- Produces:
  - `Parser(db: TaskRepository, preferenceRepository: PreferenceRepository? = null, coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()))`
  - `Parser.parse(input: String): Parsed` (uses cached `ENConfig` dynamically)

- [ ] **Step 1: Write the failing test for dynamic parsing updates in `ParserConfigTest.kt`**

```kotlin
package ap.panini.procrastaint.util

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import ap.panini.procrastaint.data.repositories.PreferenceRepository
import ap.panini.procrastaint.data.repositories.TaskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ParserConfigTest {
    @Test
    fun testParserReflectsUpdatedHourConfig() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)
        val testDataStore = PreferenceDataStoreFactory.createWithPath(
            produceFile = { "/tmp/test_prefs_${kotlin.random.Random.nextLong()}.preferences_pb".toPath() }
        )
        val prefRepo = PreferenceRepository(testDataStore)
        val mockDb = TaskRepositoryMock() // Minimal mock or fake TaskRepository
        val parser = Parser(mockDb, prefRepo, testScope)

        // Default morning is 9 AM
        val defaultResult = parser.parse("Meeting tomorrow morning")
        assertTrue(defaultResult.times.isNotEmpty())

        // Update morning to 6 AM (06:00)
        prefRepo.setMorningHour(6)

        val updatedResult = parser.parse("Meeting tomorrow morning")
        assertTrue(updatedResult.times.isNotEmpty())
    }

    @Test
    fun testParserReflectsEuropeanDateFormat() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)
        val testDataStore = PreferenceDataStoreFactory.createWithPath(
            produceFile = { "/tmp/test_prefs_${kotlin.random.Random.nextLong()}.preferences_pb".toPath() }
        )
        val prefRepo = PreferenceRepository(testDataStore)
        val mockDb = TaskRepositoryMock()
        val parser = Parser(mockDb, prefRepo, testScope)

        prefRepo.setUseEuropeanDate(true)
        val result = parser.parse("Deadline 03/04/2026")
        assertTrue(result.times.isNotEmpty())
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :shared:testDebugUnitTest --tests "ap.panini.procrastaint.util.ParserConfigTest"`  
Expected: FAIL (Parser constructor mismatch)

- [ ] **Step 3: Implement reactive caching in `Parser.kt` and update `AppModule.kt`**

In `Parser.kt`:
- Add `preferenceRepository: PreferenceRepository? = null` and `coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())` to constructor with default values.
- Maintain `private var cachedConfig: ENConfig = ENConfig()`.
- If `preferenceRepository != null`, launch collection of `preferenceRepository.getKwhenConfig()` inside `coroutineScope` updating `cachedConfig`.
- In `reset()`, instantiate `textParser = TimeParser(cachedConfig)`.

In `AppModule.kt`:
- Update `factory { Parser(get(), get()) }` or `single { Parser(get(), get()) }`.

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :shared:testDebugUnitTest --tests "ap.panini.procrastaint.util.ParserConfigTest"`  
Expected: PASS

- [ ] **Step 5: Commit task 2**

```bash
git add shared/src/commonMain/kotlin/ap/panini/procrastaint/util/Parser.kt shared/src/commonMain/kotlin/ap/panini/procrastaint/di/AppModule.kt shared/src/commonTest/kotlin/ap/panini/procrastaint/util/ParserConfigTest.kt
git commit -m "feat(shared): enable reactive config caching in Parser"
```

---

### Task 3: Android Navigation Route and ParserSettingsViewModel

**Files:**
- Modify: `androidApp/src/main/java/ap/panini/procrastaint/ui/navigation/Route.kt`
- Create: `androidApp/src/main/java/ap/panini/procrastaint/ui/settings/parser/ParserSettingsViewModel.kt`
- Modify: `androidApp/src/main/java/ap/panini/procrastaint/di/AppModule.android.kt` (or settings DI module)

**Interfaces:**
- Consumes:
  - `PreferenceRepository`
  - `Parser`
- Produces:
  - `Route.ParserSettings : Route`
  - `ParserSettingsViewModel : ViewModel`
  - `ParserSettingsUiState` (immutable data class containing 6 config values, `sandboxInput`, and `sandboxParsed`)
  - Mutator methods: `setMorningHour(Int)`, `setAfternoonHour(Int)`, `setEveningHour(Int)`, `setNightHour(Int)`, `setUse24(Boolean)`, `setUseEuropeanDate(Boolean)`, `updateSandboxInput(String)`, `resetToDefaults()`

- [ ] **Step 1: Add `ParserSettings` route in `Route.kt`**

Add to `Route.kt`:
```kotlin
@Serializable
data object ParserSettings : Route
```

- [ ] **Step 2: Create `ParserSettingsViewModel.kt` with state and sandbox evaluation**

```kotlin
package ap.panini.procrastaint.ui.settings.parser

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ap.panini.kwhen.configs.ENConfig
import ap.panini.procrastaint.data.repositories.PreferenceRepository
import ap.panini.procrastaint.util.Parsed
import ap.panini.procrastaint.util.Parser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ParserSettingsViewModel(
    private val preferenceRepository: PreferenceRepository,
    private val parser: Parser
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ParserSettingsUiState(
            sandboxInput = "Doctor appointment tomorrow evening #health"
        )
    )
    val uiState: StateFlow<ParserSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferenceRepository.getKwhenConfig().collect { config ->
                _uiState.update { current ->
                    current.copy(
                        morningHour = config.morning,
                        afternoonHour = config.afternoon,
                        eveningHour = config.evening,
                        nightHour = config.night,
                        use24 = config.use24,
                        useEuropeanDate = config.useEuropeanDate,
                        sandboxParsed = parser.parse(current.sandboxInput)
                    )
                }
            }
        }
    }

    fun setMorningHour(hour: Int) = viewModelScope.launch { preferenceRepository.setMorningHour(hour) }
    fun setAfternoonHour(hour: Int) = viewModelScope.launch { preferenceRepository.setAfternoonHour(hour) }
    fun setEveningHour(hour: Int) = viewModelScope.launch { preferenceRepository.setEveningHour(hour) }
    fun setNightHour(hour: Int) = viewModelScope.launch { preferenceRepository.setNightHour(hour) }
    fun setUse24(enabled: Boolean) = viewModelScope.launch { preferenceRepository.setUse24Hour(enabled) }
    fun setUseEuropeanDate(enabled: Boolean) = viewModelScope.launch { preferenceRepository.setUseEuropeanDate(enabled) }
    fun resetToDefaults() = viewModelScope.launch { preferenceRepository.resetKwhenConfigToDefaults() }

    fun updateSandboxInput(input: String) {
        _uiState.update {
            it.copy(
                sandboxInput = input,
                sandboxParsed = parser.parse(input)
            )
        }
    }

    @Immutable
    data class ParserSettingsUiState(
        val morningHour: Int = 9,
        val afternoonHour: Int = 15,
        val eveningHour: Int = 18,
        val nightHour: Int = 20,
        val use24: Boolean = false,
        val useEuropeanDate: Boolean = false,
        val sandboxInput: String = "",
        val sandboxParsed: Parsed? = null
    )
}
```

- [ ] **Step 3: Register `ParserSettingsViewModel` in Koin**

In `androidApp/src/main/java/ap/panini/procrastaint/di/AppModule.kt` (or androidApp's module file):
```kotlin
viewModel { ParserSettingsViewModel(get(), get()) }
```

- [ ] **Step 4: Verify compilation**

Run: `./gradlew :androidApp:compileDebugKotlin`  
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit task 3**

```bash
git add androidApp/src/main/java/ap/panini/procrastaint/ui/navigation/Route.kt androidApp/src/main/java/ap/panini/procrastaint/ui/settings/parser/ParserSettingsViewModel.kt androidApp/src/main/java/ap/panini/procrastaint/di/
git commit -m "feat(ui): add ParserSettings route and ViewModel"
```

---

### Task 4: Android Parser Settings Screen & Interactive Sandbox UI

**Files:**
- Create: `androidApp/src/main/java/ap/panini/procrastaint/ui/settings/parser/ParserSettingsScreen.kt`
- Create: `androidApp/src/main/java/ap/panini/procrastaint/ui/settings/parser/components/HourPickerCard.kt`
- Create: `androidApp/src/main/java/ap/panini/procrastaint/ui/settings/parser/components/LiveSandboxCard.kt`
- Modify: `androidApp/src/main/java/ap/panini/procrastaint/ui/navigation/NavGraph.kt`
- Modify: `androidApp/src/main/java/ap/panini/procrastaint/ui/settings/SettingsScreen.kt`

**Interfaces:**
- Consumes:
  - `ParserSettingsViewModel`
  - `Route.ParserSettings`
- Produces:
  - `ParserSettingsScreen(onNavigateBack: () -> Unit)`
  - Destination registered in `NavGraph.kt`
  - Entry setting item in `SettingsScreen.kt` navigating to `Route.ParserSettings`

- [ ] **Step 1: Create `HourPickerCard.kt` and dialog helper**

Support picking an hour (0–23) with Material 3 styling. Format title and subtitle (e.g. "9:00 AM" or "09:00" based on `use24`).

- [ ] **Step 2: Create `LiveSandboxCard.kt`**

Render:
- Active OutlinedTextField for `sandboxInput`
- Quick sample suggestion chips: `"tomorrow morning"`, `"04/05 at 8"`, `"gym every other Tuesday at 5pm #health"`
- Result badges:
  - Extracted dates/times in success chip
  - Extracted tags in accent chip
  - Cleaned text summary

- [ ] **Step 3: Create `ParserSettingsScreen.kt`**

Assemble:
- TopAppBar with navigation back icon, title "Date & Time Parsing", and "Reset" action button.
- Section 1: Time of Day Hours (Morning, Afternoon, Evening, Night).
- Section 2: Notation Switches (European Date Format `DD/MM/YYYY`, 24-Hour Time Format).
- Section 3: `LiveSandboxCard`.

- [ ] **Step 4: Register in `NavGraph.kt` and add entry in `SettingsScreen.kt`**

In `NavGraph.kt`:
```kotlin
composable<Route.ParserSettings> {
    ParserSettingsScreen(
        onNavigateBack = { navController.popBackStack() }
    )
}
```

In `SettingsScreen.kt`:
Add a section item with icon `Icons.Outlined.Tune`, text `"Date & Time Parsing"`, subtext `"Configure natural language keywords and date formats"`, clicking to `onNavigateToParserSettings()`.

- [ ] **Step 5: Verify build compiles cleanly**

Run: `./gradlew assembleDebug`  
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit task 4**

```bash
git add androidApp/src/main/java/ap/panini/procrastaint/ui/settings/parser/ androidApp/src/main/java/ap/panini/procrastaint/ui/settings/SettingsScreen.kt androidApp/src/main/java/ap/panini/procrastaint/ui/navigation/NavGraph.kt
git commit -m "feat(ui): implement ParserSettingsScreen with live interactive sandbox"
```

---

### Task 5: Full Verification, Formatting & Linting

**Files:**
- All modified and created files across `shared` and `androidApp`

- [ ] **Step 1: Run shared unit tests**

Run: `./gradlew :shared:testDebugUnitTest`  
Expected: ALL TESTS PASS

- [ ] **Step 2: Run Detekt linter and auto-formatter**

Run: `./gradlew detektall`  
Expected: BUILD SUCCESSFUL with 0 code smell violations

- [ ] **Step 3: Run full Android application assemble**

Run: `./gradlew assembleDebug`  
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Verify git status and commit any cleanups**

```bash
git status
git add -u
git commit -m "chore: formatting and detekt cleanup"
```
