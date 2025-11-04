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

        val fromDate = kotlin.time.Instant.fromEpochMilliseconds(from)
            .toLocalDateTime(TimeZone.currentSystemDefault()).date

        val (fromRange, toRange, prevKey, nextKey, pageTime) = when (displayMode) {
            CalendarDisplayMode.DAILY -> {
                val fromDayStart =
                    fromDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
                val toDayEnd = fromDate.plus(1, DateTimeUnit.DAY)
                    .atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()

                val prevKey = fromDate.minus(1, DateTimeUnit.DAY)
                    .atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
                val nextKey = fromDate.plus(1, DateTimeUnit.DAY)
                    .atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()

                CalendarPageLoadData(fromDayStart, toDayEnd, prevKey, nextKey, from)
            }

            CalendarDisplayMode.MONTHLY -> {
                val monthStart = LocalDate(fromDate.year, fromDate.month, 1)
                val fromMonthStart =
                    monthStart.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()

                val nextMonth = monthStart.plus(1, DateTimeUnit.MONTH)
                val toMonthEnd =
                    nextMonth.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()

                val prevMonth = monthStart.minus(1, DateTimeUnit.MONTH)

                CalendarPageLoadData(
                    fromMonthStart,
                    toMonthEnd,
                    prevMonth.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
                    nextMonth.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
                    fromMonthStart
                )
            }

            else -> throw IllegalArgumentException("Unsupported display mode: $displayMode")
        }

        val tasksFlow = db.getTasks(fromRange, toRange)
        val tasksByDayFlow = tasksFlow.map { tasks ->
            tasks.groupBy {
                kotlin.time.Instant.fromEpochMilliseconds(it.currentEventTime)
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date
            }
        }

        return Page(
            data = listOf(CalendarPageData(pageTime, tasksByDayFlow)),
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

data class CalendarPageLoadData(
    val fromRange: Long,
    val toRange: Long,
    val prevKey: Long,
    val nextKey: Long,
    val pageTime: Long
)
