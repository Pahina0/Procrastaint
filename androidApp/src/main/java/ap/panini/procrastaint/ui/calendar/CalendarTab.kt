package ap.panini.procrastaint.ui.calendar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

class CalendarTab : Tab {

    override val options: TabOptions
        @Composable
        get() {
            val title = "Calendar"
            val icon = rememberVectorPainter(Icons.Outlined.CalendarMonth)

            return remember {
                TabOptions(
                    index = 0u,
                    title = title,
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        //Text(text = "Calendar")
        CalendarScreen(
            calendarInput = listOf(
                CalendarInput(day = 20, toDos = listOf("test1","test2")),
                CalendarInput(day = 25, toDos = listOf("test3","test4"))
            ),
            onDayClick = { day ->
                println("Day clicked: $day") // logic update needed
            },
            month = "December"
        )
    }
}
