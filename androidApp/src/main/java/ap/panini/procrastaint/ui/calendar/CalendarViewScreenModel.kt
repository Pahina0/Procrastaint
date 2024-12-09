package ap.panini.procrastaint.ui.calendar

import ap.panini.procrastaint.data.repositories.AppRepository
import ap.panini.procrastaint.data.repositories.PreferenceRepository

class CalendarViewScreenModel(

    private const val CALENDAR_ROWS: Int = 5,
    private const val CALENDAR_COLUMNS: Int = 7,

    val db: AppRepository,
    private val pref: PreferenceRepository
) : StateScreenModel<CalendarViewScreenModel.State>(State()) {

    data class CalendarInput(
        val day: Int,
        val toDos:List<String> = emptyList()
    )
}