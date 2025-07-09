package ap.panini.procrastaint.ui.calendar

import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadResult.Page
import androidx.paging.PagingState
import ap.panini.procrastaint.data.entities.TaskSingle
import ap.panini.procrastaint.data.repositories.TaskRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.hours

class CalendarPagingSource(
    private val from: Long,
) : PagingSource<Long, Pair<Long, Flow<List<TaskSingle>>>>(), KoinComponent {
    private val db: TaskRepository by inject()

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Pair<Long, Flow<List<TaskSingle>>>> {
        val from = params.key ?: from
        return Page(
            data = listOf(
                Pair(
                    from,
                    db.getTasks(from, from + 24.hours.inWholeMilliseconds)
                )
            ),
            prevKey = (from) - 24.hours.inWholeMilliseconds,
            nextKey = (from) + 24.hours.inWholeMilliseconds,
        )
    }

    override fun getRefreshKey(state: PagingState<Long, Pair<Long, Flow<List<TaskSingle>>>>): Long? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
