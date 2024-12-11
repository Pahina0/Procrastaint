package ap.panini.procrastaint.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.ui.theme.secondaryContainerLight

@Composable
fun MonthView(
    onDayClick: (CalendarInput) -> Unit
) {
    val calendarInputList = remember { createCalendarList() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(secondaryContainerLight),
        contentAlignment = Alignment.TopCenter
    ) {
        Calendar(
            calendarInput = calendarInputList,
            onDayClick = { day ->
                val clickedDay = calendarInputList.first { it.day == day }
                onDayClick(clickedDay)
            },
            month = "December",
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .aspectRatio(1.3f)
        )
    }
}

fun createCalendarList(): List<CalendarInput> {
    val calendarInputs = mutableListOf<CalendarInput>()
    for (i in 1..31) {
        calendarInputs.add(
            CalendarInput(
                i,
                toDos = listOf(
                    "Day $i:",
                    "Event 1",
                    "Event 2"
                )
            )
        )
    }
    return calendarInputs
}
