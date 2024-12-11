package ap.panini.procrastaint.ui.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import java.time.DayOfWeek
import java.time.LocalDate
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
        var currentMonth by remember { mutableStateOf(YearMonth.now()) }
        var isYearView by remember { mutableStateOf(false) }
        val today = LocalDate.now()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Toggle between Month View and Year View
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { isYearView = false }) {
                    Text("Month View")
                }
                Button(onClick = { isYearView = true }) {
                    Text("Year View")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isYearView) {
                YearView(currentMonth = currentMonth) { selectedMonth ->
                    currentMonth = selectedMonth
                    isYearView = false
                }
            } else {
                MonthView(currentMonth = currentMonth) { updatedMonth ->
                    currentMonth = updatedMonth
                }
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun YearView(currentMonth: YearMonth, onMonthSelected: (YearMonth) -> Unit) {
        val currentYear = currentMonth.year
        val months = (1..12).map { YearMonth.of(currentYear, it) } // List of YearMonth for all months of the year

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Display current year at the top
            Text(
                text = currentYear.toString(),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Grid displaying all 12 months
            LazyVerticalGrid(
                columns = GridCells.Fixed(3), // 3 months per row
                modifier = Modifier.fillMaxSize(),
                content = {
                    items(months.size) { index ->
                        val month = months[index]
                        Card(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth()
                                .aspectRatio(1f),
                            onClick = { onMonthSelected(month) },
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = month.month.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun MonthView(currentMonth: YearMonth, onMonthChanged: (YearMonth) -> Unit) {
        var currentMonth by remember { mutableStateOf(YearMonth.now()) }
        val today = LocalDate.now()
        var searchDateText by remember { mutableStateOf("") }
        var highlightedDate by remember { mutableStateOf<LocalDate?>(null) }

        val dateFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy", Locale.getDefault())

        val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value % 7 // Adjust to 0 (Sunday) to 6 (Saturday)
        val daysInMonth = currentMonth.lengthOfMonth()


        // Combine empty slots and days into one list
        val calendarCells = List(firstDayOfWeek) { null } +
                (1..daysInMonth).map { currentMonth.atDay(it) }


        val daysOfWeek = DayOfWeek.values().map {
            it.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        }


        Column(
                modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragEnd = { /* No-op */ },
                onHorizontalDrag = { change, dragAmount ->
                    change.consume()
                    if (dragAmount > 0) {
                        // Swipe right to move to the previous month
                        currentMonth = currentMonth.minusMonths(1)
                    } else if (dragAmount < 0) {
                        // Swipe left to move to the next month
                        currentMonth = currentMonth.plusMonths(1)
                    }
                }
            )
        },
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Search Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = searchDateText,
                    onValueChange = { searchDateText = it },
                    placeholder = { Text("Enter date (MM-DD-YYYY)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    try {
                        val parsedDate = LocalDate.parse(searchDateText, dateFormatter)
                        currentMonth = YearMonth.of(parsedDate.year, parsedDate.month)
                        highlightedDate = parsedDate
                    } catch (e: Exception) {
                        // Handle invalid date format
                    }
                }) {
                    Text("Search")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Text("<", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                        text = "${
                        currentMonth.month.getDisplayName(
                                TextStyle.FULL,
                                Locale.getDefault()
                        )
            } ${currentMonth.year}",
            style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
               )
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Text(">", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }


        Spacer(modifier = Modifier.height(16.dp))


        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
        ) {
            daysOfWeek.forEach { day ->
                    Text(
                            text = day,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                    )
            }
        }


        Spacer(modifier = Modifier.height(8.dp))

        // Calendar Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxHeight(),
            content = {
                items(calendarCells.size) { index ->
                    val date = calendarCells[index]
                    val isToday = date == today

                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (date != null) {
                            Card(
                                modifier = Modifier.fillMaxSize(),
                                colors = CardDefaults.cardColors(
                                    if (isToday) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = date.dayOfMonth.toString(),
                                        fontSize = 16.sp,
                                        color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }
        )
        }
    }
}
