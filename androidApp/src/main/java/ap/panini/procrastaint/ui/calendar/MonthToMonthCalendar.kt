package ap.panini.procrastaint.ui.calendar

import android.graphics.Paint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ap.panini.procrastaint.ui.theme.onPrimaryContainerLight
import ap.panini.procrastaint.ui.theme.onPrimaryLight
import ap.panini.procrastaint.ui.theme.onSecondaryLight
import ap.panini.procrastaint.ui.theme.secondaryContainerLight

class MonthToMonthCalendar : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val calendarInputList by remember {
                mutableStateOf(createCalendarList())
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(secondaryContainerLight) ,
                contentAlignment = Alignment.TopCenter
            ) {
                Calendar(
                    calendarInput = calendarInputList,
                    onDayClick =  {

                    },
                    month = "December", //apply whole year logic here needed
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                        .aspectRatio(1.3f)
                )
            }
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
    calendarInput: List<CalendarInput>,
    onDayClick: (Int) -> Unit,
    strokeWidth: Float = 15f,
    month: String //add switch logic later to go to difference months
) {

    var canvasSize by remember{
        mutableStateOf(Offset.Zero)
    }

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

            //draw the grids
            for(i in 1 until CALENDAR_ROWS){
                drawLine(
                    color = onPrimaryContainerLight,
                    start = Offset(0f,ySteps*i),
                    end = Offset(canvasWidth, ySteps*i),
                    strokeWidth = strokeWidth
                )
            }
            for(i in 1 until CALENDAR_COLUMNS){
                drawLine(
                    color = onPrimaryContainerLight,
                    start = Offset(xSteps*i,0f),
                    end = Offset(xSteps*i, canvasHeight),
                    strokeWidth = strokeWidth
                )
            }
            //draw calendar dates
            val textHeight = 17.dp.toPx()
            for(i in calendarInput.indices){
                val textPositionX = xSteps * (i% CALENDAR_ROWS) + strokeWidth
                val textPositionY = (i / CALENDAR_COLUMNS) * ySteps + textHeight + strokeWidth/2
                drawContext.canvas.nativeCanvas.apply{
                    drawText(
                        "${i+1}",
                        textPositionX,
                        textPositionY,
                        Paint().apply{
                            textSize = textHeight
                            color = onSecondaryLight.toArgb()
                            isFakeBoldText = true

                        }
                    )
                }
            }
        }
    }
}

data class CalendarInput(
    val day: Int,
    val toDos: List<String> = emptyList()
)