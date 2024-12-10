package ap.panini.procrastaint.ui.calendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ap.panini.procrastaint.ui.theme.onPrimaryContainerLight
import ap.panini.procrastaint.ui.theme.onPrimaryLight

class MonthToMonthCalendar : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

        }
    }

    //function create calendar item
    private fun createCalendarList(): List<CalendarInput> {
        val calendarInputs = mutableListOf<CalendarInput>()
        for (i in 1..31) {
            calendarInputs.add(
                CalendarInput(
                    i,
                    toDos = listOf(
                        "Day $i:",
                        "event 1",
                        "event 2"
                    )
                )
            )
        }
        return calendarInputs
    }

}

private const val CALENDAR_ROWS = 5
private const val CALENDAR_COLUMNS = 7

@Composable
fun Calendar(
    modifier: Modifier = Modifier,
    calendarInput: List<CalendarInput>
    onDayClick: (Int) -> Unit,
    strokeWidth: Float = 15f,
    month: String //add switch logic later to go to difference months
) {

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = month,
            fontWeight = FontWeight.SemiBold,
            color = onPrimaryLight,
            fontSize = 40.sp
        )

        Canvas(
            modifier = Modifier.fillMaxSize()
        ){
            val canvasHeight = size.height
            val canvasWidth = size.width
            val ySteps = canvasHeight/ CALENDAR_ROWS
            val xSteps = canvasWidth/ CALENDAR_COLUMNS

            drawRoundRect(
                onPrimaryContainerLight,
                cornerRadius = CornerRadius(25f,25f),
                style = Stroke(
                    width = strokeWidth
                )
            )
        }
    }
}

data class CalendarInput(
    val day: Int,
    val toDos: List<String> = emptyList()
)