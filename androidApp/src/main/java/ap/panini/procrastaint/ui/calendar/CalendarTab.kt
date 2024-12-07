package ap.panini.procrastaint.ui.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import java.time.LocalDate
import androidx.compose.ui.unit.sp
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

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

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    override fun Content() {
        ScrollableCalendar()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun ScrollableCalendar() {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())
        val daysToShow = 365 // Display a year's worth of dates

        LazyColumn(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(daysToShow) { offset ->
                val date = today.plusDays(offset.toLong())
                CalendarItem(date.format(formatter))
            }
        }
    }

    @Composable
    fun CalendarItem(formattedDate: String) {
        Row(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = formattedDate,
                modifier = androidx.compose.ui.Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }

}
