package ap.panini.procrastaint.ui.calendar

import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadResult.Page
import androidx.paging.PagingState
import ap.panini.procrastaint.data.repositories.TaskRepository
import ap.panini.procrastaint.util.hour
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.ExperimentalTime

class CalendarPagingSource(
    private val from: Long,
    private val displayMode: CalendarDisplayMode
) : PagingSource<Long, CalendarPageData>(), KoinComponent {
    private val db: TaskRepository by inject()

    @OptIn(ExperimentalTime::class)
    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, CalendarPageData> {
        val from = params.key ?: from

        val (pageData, prevKey, nextKey) = when (displayMode) {
            CalendarDisplayMode.DAILY -> {
                val fromDate = kotlin.time.Instant.fromEpochMilliseconds(from)
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date
                val fromDayStart =
                    fromDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
                val toDayEnd = fromDate.plus(1, DateTimeUnit.DAY)
                    .atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()

                val prevKey = fromDate.minus(1, DateTimeUnit.DAY)
                    .atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
                val nextKey = fromDate.plus(1, DateTimeUnit.DAY)
                    .atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()

                val tasks = db.getTasks(fromDayStart, toDayEnd)

                Triple(CalendarPageData(from, tasks), prevKey, nextKey)
            }

            CalendarDisplayMode.MONTHLY -> {
                val fromDate = kotlin.time.Instant.fromEpochMilliseconds(from)
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date
                val monthStart = LocalDate(fromDate.year, fromDate.month, 1)
                val fromMonthStart =
                    monthStart.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()

                val nextMonth = monthStart.plus(1, DateTimeUnit.MONTH)
                val toMonthEnd =
                    nextMonth.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()

                val prevMonth = monthStart.minus(1, DateTimeUnit.MONTH)

                val tasks = db.getTasks(fromMonthStart, toMonthEnd)

                Triple(
                    CalendarPageData(fromMonthStart, tasks),
                    prevMonth.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
                    nextMonth.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
                )
            }

            else -> throw IllegalArgumentException("Unsupported display mode: $displayMode")
        }

        return Page(
            data = listOf(pageData),
            prevKey = prevKey,
            nextKey = nextKey,
        )
    }

    override fun getRefreshKey(state: PagingState<Long, CalendarPageData>): Long? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
