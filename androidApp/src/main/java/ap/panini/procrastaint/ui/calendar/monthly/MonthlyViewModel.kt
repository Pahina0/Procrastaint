package ap.panini.procrastaint.ui.calendar.monthly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import ap.panini.procrastaint.ui.calendar.CalendarDisplayMode
import ap.panini.procrastaint.ui.calendar.CalendarPagingSource
import ap.panini.procrastaint.util.Date

class MonthlyViewModel : ViewModel() {
    private val today = Date.getTodayStart()

    val dateState = Pager(
        PagingConfig(
            initialLoadSize = 10,
            enablePlaceholders = false,
            pageSize = 2,
        )
    ) {
        CalendarPagingSource(today, CalendarDisplayMode.MONTHLY)
    }.flow
        .cachedIn(viewModelScope)
}