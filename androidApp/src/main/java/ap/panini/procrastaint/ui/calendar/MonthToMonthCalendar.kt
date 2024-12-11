package ap.panini.procrastaint.ui.calendar

import android.graphics.Paint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ap.panini.procrastaint.ui.theme.onPrimaryContainerLight
import ap.panini.procrastaint.ui.theme.onSurfaceVariantLight
import ap.panini.procrastaint.ui.theme.scrimLight
import ap.panini.procrastaint.ui.theme.secondaryContainerLight
import kotlinx.coroutines.launch

class MonthToMonthCalendar : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val calendarInputList by remember {
                mutableStateOf(createCalendarList())
            }
            var clickedCalendarElem by remember {
                mutableStateOf<CalendarInput?>(null)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(secondaryContainerLight) ,
                contentAlignment = Alignment.TopCenter
            ) {
                Calendar(
                    calendarInput = calendarInputList,
                    onDayClick =  { day->
                        clickedCalendarElem = calendarInputList.first {
                            it.day == day
                        }
                    },
                    month = "December", //apply whole year logic here needed
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                        .aspectRatio(1.3f)
                )

                //Show DayView when a day is clicked
                clickedCalendarElem?.let { calendarInput ->
                    DayView(
                        day = calendarInput.day,
                        toDos = calendarInput.toDos,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(MaterialTheme.colorScheme.background)
                            .padding(16.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .align(Alignment.Center)
                ) {
                    clickedCalendarElem?.toDos?.forEach {
                        Text(
                            if (it.contains("Day")) it else "- $it",
                            color = scrimLight,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = if (it.contains("Day")) 25.sp else 18.sp
                        )
                    }
                }
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

    var canvasSize by remember {
        mutableStateOf(Size.Zero)
    }
    var clickAnimationOffset by remember {
        mutableStateOf(Offset.Zero)
    }

    var animationRadius by remember {
        mutableStateOf(0f)
    }

    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = month,
            fontWeight = FontWeight.SemiBold,
            color = scrimLight,
            fontSize = 40.sp
        )

        Canvas(
            modifier = Modifier
                .size(400.dp)
                .pointerInput(true){
                    detectTapGestures (
                        //calculate the relative amount of the user click
                        onTap = { offset ->
                            val column = (offset.x / canvasSize.width * CALENDAR_COLUMNS.toInt())
                            val row = (offset.x / canvasSize.height * CALENDAR_ROWS.toInt())
                            val day = column + (row * CALENDAR_COLUMNS) + 1
                            if (day <= calendarInput.size){
                                onDayClick(day.toInt())
                                clickAnimationOffset = offset
                                scope.launch {
                                    animate(0f, 225f, animationSpec = tween(300)){value, _ ->
                                        animationRadius = value
                                    }
                                }
                            }
                        }
                    )
                }
        ){
            val canvasHeight = size.height
            val canvasWidth = size.width
            canvasSize = Size(canvasWidth, canvasHeight)
            val ySteps = canvasHeight/ CALENDAR_ROWS
            val xSteps = canvasWidth/ CALENDAR_COLUMNS

            val column = clickAnimationOffset.x / canvasSize.width * CALENDAR_COLUMNS.toInt()
            val row = clickAnimationOffset.x / canvasSize.height * CALENDAR_ROWS.toInt()

            //path exactly the grid item
            val path = Path().apply{
                moveTo(column * xSteps, row * ySteps)
                lineTo((column + 1) * xSteps, row * ySteps)
                lineTo((column + 1) * xSteps, (row + 1) * ySteps)
                lineTo(column * xSteps, (row + 1) * ySteps)
                close()
            }

            clipPath(path){
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(
                            onPrimaryContainerLight.copy(0.8f),
                            onPrimaryContainerLight.copy(0.2f)
                        ),
                        center = clickAnimationOffset,
                        radius = animationRadius + 0.1f
                    ),
                    radius = animationRadius + 0.1f,
                    center = clickAnimationOffset
                )
            }

            drawRoundRect(
                onPrimaryContainerLight,
                cornerRadius = CornerRadius(25f,25f),
                style = Stroke(
                    width = strokeWidth
                )
            )

            //draw the grid lines
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
            val textHeight = 12.dp.toPx()
            for(i in calendarInput.indices){
                val column = i % CALENDAR_COLUMNS
                val row = i / CALENDAR_COLUMNS
                val textPositionX = xSteps * column + xSteps / 2
                val textPositionY = ySteps * row + textHeight + strokeWidth

                // Draw the day number
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        "${calendarInput[i].day}",
                        textPositionX,
                        textPositionY,
                        Paint().apply {
                            textSize = textHeight
                            color = onSurfaceVariantLight.toArgb()
                            isFakeBoldText = true
                            textAlign = Paint.Align.CENTER
                        }
                    )
                }

                // Draw events below the day number
                calendarInput[i].toDos.forEachIndexed { index, todo ->
                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            todo,
                            textPositionX,
                            textPositionY + (index + 1) * textHeight,
                            Paint().apply {
                                textSize = textHeight * 0.75f
                                color = scrimLight.toArgb()
                                textAlign = Paint.Align.CENTER
                            }
                        )
                    }
                }
            }
            // Draw animation (circle when clicked)
            if (animationRadius > 0f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(onPrimaryContainerLight.copy(0.8f), onPrimaryContainerLight.copy(0.2f)),
                        center = clickAnimationOffset,
                        radius = animationRadius
                    ),
                    radius = animationRadius,
                    center = clickAnimationOffset
                )
            }
        }
    }
}

data class CalendarInput(
    val day: Int,
    val toDos: List<String> = emptyList()
)