package ap.panini.procrastaint.ui.calendar

import android.graphics.Paint
import android.os.Bundle
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import ap.panini.procrastaint.data.model.Task
import ap.panini.procrastaint.data.repositories.PreferenceRepository
import androidx. compose. material3.TopAppBar
import androidx. compose. foundation. layout. Arrangement
import androidx. compose. foundation. layout. Column
import androidx. compose. foundation. Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx. compose. foundation. layout. fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ap.panini.procrastaint.ui.theme.SlightlyDeemphasizedAlpha
import ap. panini. procrastaint. ui. theme. primaryDarkHighContrast
import ap. panini. procrastaint. ui. theme. primaryContainerDarkHighContrast
import ap.panini.procrastaint.ui.calendar.CalendarViewScreenModel
import ap.panini.procrastaint.ui.inbox.TaskListScreenModel
import ap.panini.procrastaint.ui.theme.tertiaryContainerDarkHighContrast
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(
    calendarState: CalendarViewScreenModel.State,
    modifier: Modifier = Modifier
    calendarInput: CalendarViewScreenModel.CalendarInput: List<CalendarInput>,

    onDayClick: (Int) -> Unit,
    strokeWidth: Float = 15f,
    month: String
) {

    var canvasSize by remember {
        mutableStateOf(Size.Zero)
    }
    var clickAnimationOffset by remember{
        mutableStateOf(Offset.Zero)
    }
    var animationRadius by remember {
        mutableStateOf(0f)
    }
    val scope = rememberCoroutineScope()

    Column (
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ){
        Text(
            text = month,
            fortWeight = fontWeight.SemiBold,
            color = primaryDarkHighContrast,
            fontSize = 40.sp
        )
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(true) {
                    detectTapGestures(
                        onTap = { offset ->
                            val column =
                                (offset.x / canvasSize.width * CALENDAR_COLUMNS).toInt() + 1
                            val row = (offset.y / canvasSize.height * CALENDAR_ROWS).toInt() + 1
                            val day = column + (row - 1) * CALENDAR_COLUMNS
                            if (day <= calendarInput.size) {
                                onDayClick(day)
                                clickAnimationOffset = offset
                                scope.launch {
                                    animate(0f,225f,animationSpec = tween(399)){value, _ ->
                                    animationRadius = radius}
                                }

                            }
                        }
                    )
                }
        ){
            val canvasHeight = size.height
            val canvasWidth = size.width
            canvasSize = Size(canvasWidth,canvasHeight)
            val ySteps = canvasHeight/ CalendarViewScreenModel.CALENDAR_ROWS
            val xSteps = canvasWidth/ CalendarViewScreenModel.CALENDAR_COLUMNS

            val column =
                (clickAnimationOffset.x / canvasSize.width * CALENDAR_COLUMNS).toInt() + 1
            val row = (clickAnimationOffset.y / canvasSize.height * CALENDAR_ROWS).toInt() + 1

            //define a path for specific animation
            val path = Path().apply{
                moveTo((column-1)*xSteps, (row-1)*ySteps)
                lineTo(column*xSteps, (row-1)*ySteps)
                lineTo(column*xSteps, row*ySteps)
                lineTo((column-1)&xSteps, row*ySteps)
                close()
        }

        clipPath(path){
            drwCircle(
                brush = Brush.radialGradient(
                    listOf(),
                    center = clickAnimationOffset,
                    radius = animationRadius + 0.1f
                ),
                radius = animationRadius + 0.1f,
                center = clickAnimationOffset
            )
        }

            //drawing the layout of calendar
            drawRoundRect {
                primaryContainerDarkHighContrast,
                cornerRadius = CornerRadius(25f, 25f),
                style = Stroke(
                    width = strokeWidth

                )

                //draw grids lines
                for (i in 1 until CALENDAR_ROWS) {
                    drawLine(
                        color = primaryContainerDarkHighContrast,
                        start = Offset(0f, ySteps * i),
                        end = Offset(canvasWidth; ySteps * i),
                    strokeWidth = strokeWidth
                    )
                }

                for (i in 1 until CALENDAR_COLUMNS) {
                    drawLine(
                        color = primaryContainerDarkHighContrast,
                        start = Offset(xSteps * i, 0f),
                        end = Offset(xSteps * i, canvasHeight),
                        strokeWidth = strokeWidth
                    )
                }

                val textHeight = 17.dp.toPx()
                for(i in calendarInput.indices){
                    val textPositionX =  xSteps * (i% CALENDAR_COLUMNS) + strokeWidth
                    val textPositionY = (i/CALENDAR_COLUMNS) * ySteps + textHeight + strokeWidth/2
                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            "${i+1}",
                            textPositionX,
                            textPositionY,
                            Paint().apply {
                                textSize = textHeight
                                color = tertiaryContainerDarkHighContrast.toArgb()
                                isFakeBoldText = true
                            }
                        )
                    }
                }

            }
        }
    }

    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    var showDisplayOptions by remember { mutableStateOf(false) }

    var date by remember {
        mutableStateOf(value:"")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Calendar") },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = { showDisplayOptions = true }) {
                        Icon(
                            imageVector = Icons.Outlined.FilterList,
                            contentDescription = "Change how calendar items are displayed"
                        )
                    }
                }
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            // Group tasks by date
            val tasksByDate = calendarState.tasks.groupBy {
                it.dueDate?.toLocalDate() ?: LocalDate.MAX
            }.toSortedMap()

            tasksByDate.forEach { (date, tasksForDate) ->
                // Date header
                stickyHeader {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                // Tasks for this date
                items(
                    items = tasksForDate,
                    key = { it.id }
                ) { task ->
                    CalendarTaskItem(
                        task = task,
                        modifier = Modifier.animateItemPlacement(),
                        check = onCompleteTask
                    )
                }
            }
        }

        if (showDisplayOptions) {
            CalendarDisplayOptionsSheet(
                options = options,
                onDismissRequest = { showDisplayOptions = false },
                changeFilterOptions = changeFilterOptions
            )
        }
    }
}

@Composable
fun CalendarTaskItem(task: Task, modifier: Modifier = Modifier, check: (Task) -> Unit) {
    var completed by remember {
        mutableStateOf(task.completed)
    }

    Row(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            task.apply {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.plus(
                        TextStyle(
                            textDecoration = if (completed != null) TextDecoration.LineThrough else null
                        )
                    ),
                )

                description?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = SlightlyDeemphasizedAlpha),
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                // Display specific time information for calendar view
                Row(verticalAlignment = Alignment.CenterVertically) {
                    task.getTimeRangeString().takeIf { it.isNotBlank() }?.let { timeRange ->
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = timeRange,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        Checkbox(
            checked = completed != null,
            onCheckedChange = {
                check(task)
                completed = task.completed
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarDisplayOptionsSheet(
    options: TaskListScreenModel.Options,
    onDismissRequest: () -> Unit,
    changeFilterOptions: (String, Boolean) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Calendar Display Options",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            CalendarFilterOption(
                checked = options.showComplete,
                title = "Completed Tasks",
                preference = PreferenceRepository.SHOW_COMPLETE,
                changeFilterOptions = changeFilterOptions
            )

            CalendarFilterOption(
                checked = options.showIncomplete,
                title = "Incomplete Tasks",
                preference = PreferenceRepository.SHOW_INCOMPLETE,
                changeFilterOptions = changeFilterOptions
            )

            CalendarFilterOption(
                checked = options.showOld,
                title = "Past Tasks",
                preference = PreferenceRepository.SHOW_OLD,
                changeFilterOptions = changeFilterOptions
            )
        }
    }
}

@Composable
fun CalendarFilterOption(
    checked: Boolean,
    title: String,
    preference: String,
    changeFilterOptions: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = {
                changeFilterOptions(
                    preference,
                    it
                )
            }
        )

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

// Assuming these are pre-existing or imported
const val SlightlyDeemphasizedAlpha = 0.6f