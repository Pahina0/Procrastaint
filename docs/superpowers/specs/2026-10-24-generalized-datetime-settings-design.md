# Generalized Date & Time Settings Design Document

## 1. Intent & Overview

Users need a unified place to configure how dates and times are handled throughout **Procrastaint**. Previously, the settings only influenced natural language text parsing via KWhen. This design generalizes the settings screen into **"Date & Time"**, ensuring that time formats (12-hour vs 24-hour) and date formats (`MM/DD/YYYY` vs `DD/MM/YYYY`) govern both:
1. **Natural Language Parsing**: How freeform text (e.g. `"Tomorrow at 8"`, `"Doctor 04/05"`) is resolved.
2. **App-wide Display Formatting**: How timestamps and calendar grids appear across the app (task cards, upcoming view, calendar screens, widgets, and notifications).

Furthermore, users can choose **"Follow System"** for both time and date notation so the app automatically reflects their device settings, and can visually inspect a **"Comparison & Diff"** card that highlights differences between their previous settings (before the edit) and the newly configured values.

---

## 2. Goals, Non-Goals & Anti-Goals

### Goals
* **Tri-State Time Format**: Support `Follow System`, `12-Hour (AM/PM)`, and `24-Hour`.
* **Tri-State Date Format**: Support `Follow System`, `Month / Day / Year (MM/DD/YYYY)`, and `Day / Month / Year (DD/MM/YYYY)`.
* **Synchronized Display & Parsing**: When a preference is changed (or resolved from the system), both KWhen's parser heuristics and `Date.kt` display formatters immediately reflect the choice.
* **Device Auto-Detection**: When set to `Follow System`, query the host device (`DateFormat.is24HourFormat(context)` and locale date ordering) and reactively propagate the resolved values.
* **Interactive Comparison & Diff**: Provide a comparison card in the settings screen that compares the baseline settings (captured upon entering the screen) against current edits. Changed values in sample phrases (e.g. *"Gym tomorrow morning"*, *"Doctor 04/05 at 8"*) are highlighted with accent containers and diff badges.
* **Configurable Keyword Hours**: Retain full freedom for Morning, Afternoon, Evening, and Night hours (0–23).
* **Reset to Defaults**: Restore all keyword hours to defaults (9, 15, 18, 20) and format modes to `Follow System`.

### Non-Goals
* Modifying Room database schema or storage: Timestamps in the database remain UTC epoch milliseconds.
* Complex localized date strings with non-Gregorian calendars or arbitrary custom format syntax strings.

### Anti-Goals
* Disconnecting display formats from parser formats (e.g., displaying in 24-hour while parsing assumes 12-hour).
* Re-querying blocking disk I/O on every frame or keystroke during formatting or parsing.

---

## 3. Constraints
* **Kotlin Multiplatform Strictness**: `shared/src/commonMain` cannot import Android framework classes (`android.text.format.DateFormat`) or `java.time.*`. Platform-specific device resolution must use an `expect`/`actual` bridge or Android-provided repository implementations.
* **Non-blocking & Zero-latency**: Date formatting occurs frequently in list scrolling and typing. Formatting must read from an in-memory cached `@Volatile` configuration.

---

## 4. Approaches Considered

### Approach 1: Reactive Global Display Config + Tri-State Preferences *(Selected)*
* Introduce `TimeFormatMode` (`SYSTEM`, `H12`, `H24`) and `DateFormatMode` (`SYSTEM`, `MDY`, `DMY`).
* A platform helper resolves `SYSTEM` to device clock and locale conventions.
* `Date.kt` maintains a `@Volatile` display config cache updated reactively from `PreferenceRepository`, instantly updating `formatMilliseconds()` and `formatToMMDDYYYY()` across all views without refactoring dozens of Compose call sites.
* `Parser.kt` receives the resolved flags in lockstep.
* **Why chosen:** Cleanest architecture, completely satisfies user requirement for synchronized parsing and display, and works in background widgets/notifications.

### Approach 2: Compose `CompositionLocal` State Hoisting
* Hoist display formatting through Compose `CompositionLocalProvider`.
* **Why rejected:** Requires updating every single composable call site displaying a date/time; cannot format background Glance app widgets or system notifications without duplicate formatting logic.

### Approach 3: Separate Preference Keys for Parser vs Display
* Provide separate toggles for "Display 24h" and "Parser 24h".
* **Why rejected:** Violates user instruction that display and parsing must synchronize automatically.

---

## 5. Detailed Architecture & Implementation Specification

### 5.1 Enums & Data Models (`shared/src/commonMain`)

```kotlin
package ap.panini.procrastaint.data.models

enum class TimeFormatMode {
    SYSTEM,
    H12,
    H24
}

enum class DateFormatMode {
    SYSTEM,
    MDY,
    DMY
}

data class ResolvedDateTimeFormat(
    val use24Hour: Boolean,
    val useEuropeanDate: Boolean
)
```

### 5.2 Device Settings Resolution (`expect` / `actual` or Platform Provider)

In `shared`:
```kotlin
expect class SystemDateTimeProvider {
    fun is24HourFormat(): Boolean
    fun isEuropeanDateFormat(): Boolean
}
```
* On **Android** (`actual class SystemDateTimeProvider`):
  * `is24HourFormat()`: Calls `android.text.format.DateFormat.is24HourFormat(context)`.
  * `isEuropeanDateFormat()`: Inspects `android.text.format.DateFormat.getDateFormatOrder(context)` (checks if `'d'` precedes `'M'`).
* On **iOS / Native**:
  * Provides matching locale inspection or standard fallback.

### 5.3 Shared Preference Persistence (`PreferenceRepository.kt`)
* Preference Keys:
  * `TIME_FORMAT_MODE` (`String`: `"SYSTEM"`, `"H12"`, `"H24"`) -> default `"SYSTEM"`
  * `DATE_FORMAT_MODE` (`String`: `"SYSTEM"`, `"MDY"`, `"DMY"`) -> default `"SYSTEM"`
  * `KWHEN_MORNING_HOUR` (`Int`) -> default `9`
  * `KWHEN_AFTERNOON_HOUR` (`Int`) -> default `15`
  * `KWHEN_EVENING_HOUR` (`Int`) -> default `18`
  * `KWHEN_NIGHT_HOUR` (`Int`) -> default `20`
* Reactive Flows:
  * `getTimeFormatMode(): Flow<TimeFormatMode>`
  * `getDateFormatMode(): Flow<DateFormatMode>`
  * `getResolvedDateTimeFormat(): Flow<ResolvedDateTimeFormat>`: Combines stored mode with `SystemDateTimeProvider`.
  * `getKwhenConfig(): Flow<ENConfig>`: Combines keyword hours with `ResolvedDateTimeFormat`.

### 5.4 Display Formatting Engine (`Date.kt`)
* Add in-memory cached configuration:
  ```kotlin
  @Volatile
  var displayConfig: ResolvedDateTimeFormat = ResolvedDateTimeFormat(use24Hour = false, useEuropeanDate = false)
      internal set
  ```
* Update `formatDate`:
  * **Month & Day ordering**: When `useEuropeanDate == false` (US standard), formats Month then Day (e.g. `"Apr 5"`). When `useEuropeanDate == true` (European standard), reorders to Day then Month (e.g. `"5 Apr"`).
  * **Full date with Year**: When `useEuropeanDate == false`, formats as `"MM/DD/YYYY"` (or Month Day, Year). When `useEuropeanDate == true`, formats as `"DD/MM/YYYY"`.
  * *Note: Calendar grid first-day-of-week remains as currently defined.*
* Update `formatTime`:
  * When `use24Hour == false`: Formats as 12-hour AM/PM (`"8:00 AM"`).
  * When `use24Hour == true`: Formats as 24-hour with padded hour and minute (`"08:00"` or `"17:30"`).
* Update `formatToMMDDYYYY`:
  ```kotlin
  fun LocalDate.formatCustom(useEuropean: Boolean = Date.displayConfig.useEuropeanDate): String {
      val monthStr = month.number.toString().padStart(2, '0')
      val dayStr = day.toString().padStart(2, '0')
      val yearStr = year.toString()
      return if (useEuropean) "$dayStr/$monthStr/$yearStr" else "$monthStr/$dayStr/$yearStr"
  }
  ```
* In `AppModule.kt`: Ensure `Date.displayConfig` is kept in sync with `preferenceRepository.getResolvedDateTimeFormat()`.

### 5.5 Settings UI & Visual Diff Comparison (`androidApp`)

#### Screen Generalization
* Navigation route remains `Route.ParserSettings` (or aliased as `Route.DateTimeSettings`).
* Screen Title: **"Date & Time"**
* Subtext in `SettingsScreen.kt`: **"Configure time and date formats, keywords, and display"**

#### UI Controls
1. **Time Format**: Material 3 Single-Choice Segmented Button Row:
   * `[ Follow System | 12-Hour | 24-Hour ]`
   * Subtitle shows the resolved clock style (e.g. *"Currently: 24-Hour (from device)"* when System is selected).
2. **Date Format**: Material 3 Single-Choice Segmented Button Row:
   * `[ Follow System | MM/DD/YYYY | DD/MM/YYYY ]`
   * Subtitle shows the resolved date order (e.g. *"Currently: Day / Month (from device)"*).
3. **Keyword Hours Section**:
   * Morning, Afternoon, Evening, Night items with time chip and picker dialog.
   * If a value differs from baseline, a subtle badge appears.
4. **Comparison & Diff Card**:
   * Evaluates fixed sample phrases using **Baseline Config** vs **Current Config**:
     * `"Gym tomorrow morning"`
     * `"Doctor 04/05 at 8"`
     * `"Meeting every Tuesday at 17:00"`
   * Side-by-side or stacked diff:
     * **Previous:** `Apr 5, 2026, 8:00 AM`
     * **Current:** `May 4, 2026, 08:00` (highlighted in primary container container / bold badge if changed)
5. **Live Sandbox Card**:
   * Allows arbitrary text typing, displaying both the parsed tag ranges and the formatted timestamp output adhering to current display settings.
6. **Top App Bar**:
   * Reset to Defaults icon/menu action.

---

## 6. What's Left to the Builder
* Choosing the exact transition animation for diff badges (e.g. `AnimatedContent`).
* Formatting strings in `strings.xml` for internationalization readiness.
