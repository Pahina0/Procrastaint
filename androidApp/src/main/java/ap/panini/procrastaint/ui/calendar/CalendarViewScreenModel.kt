package ap.panini.procrastaint.ui.calendar

import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScopeInstance.align
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.bundle.Bundle
import ap.panini.procrastaint.data.repositories.AppRepository
import ap.panini.procrastaint.data.repositories.PreferenceRepository
import ap.panini.procrastaint.ui.theme.secondaryContainerDarkHighContrast
import cafe.adriel.voyager.core.model.StateScreenModel
import androidx. compose. foundation. layout. fillMaxSize
import androidx. compose. foundation. layout. fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import ap.panini.procrastaint.ui.theme.inverseSurfaceDarkHighContrast

class CalendarViewScreenModel(
    private const val CALENDAR_ROWS: Int = 5,
    private const val CALENDAR_COLUMNS: Int = 7,

    val db: AppRepository,
    private val pref: PreferenceRepository
) : StateScreenModel<CalendarViewScreenModel.State>(State()) {

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContent{
            val calendarInputList by remember{
                mutableStateOf(createCalendarList())
            }

            val clickCalendarElem by remember{
                mutableStateOf<CalendarInput?>(null)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(secondaryContainerDarkHighContrast)
                contentAlignment = Alignment.TopCenter

            ){
                Calendar(
                    calendarInput = calendarInputList,
                    onDayClick = {
                        clickCalendarElem = calendarInputList.first { it.day == day}
                    },
                    month = "December"
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                        aspectratio(1.3f)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .align(LineHeightStyle.Alignment.Center)

                ){
                    clickedCalendarElem?.toDos?.forEach {
                        Text(
                            if(it.contains("Day")) it else "- $it",
                            color = inverseSurfaceDarkHighContrast,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = if(it.contains("Day")) 25.sp else 18.sp
                        )
                    }
                }
            }
        }
    }

    private fun createCalendarList(): List<CalendarInput>{
        val calendarInputs = mutableListOf<CalendarInput>()
        for (i in 1..31){
            calendarInputs.add(
                CalendarInput(
                    i,
                    toDos = listOf(
                        "Day: $i:",
                        "2 p.m. Test Case 1",
                        "4 p.m. Test case 2"
                    )
                )
            )
        }
        return calendarInputs
    }

    data class CalendarInput(
        val day: Int,
        val toDos:List<String> = emptyList()
    )
}