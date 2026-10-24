package ap.panini.procrastaint.util

import android.content.Context
import android.text.format.DateFormat
import org.koin.mp.KoinPlatform.getKoin

actual class SystemDateTimeProvider(
    private val context: Context?
) {
    actual constructor() : this(runCatching { getKoin().get<Context>() }.getOrNull())

    actual fun is24HourFormat(): Boolean {
        return context?.let { DateFormat.is24HourFormat(it) } ?: false
    }

    actual fun isEuropeanDateFormat(): Boolean {
        return context?.let { ctx ->
            val order = DateFormat.getDateFormatOrder(ctx)
            val dayIndex = order.indexOf('d')
            val monthIndex = order.indexOf('M')
            if (dayIndex != -1 && monthIndex != -1) {
                dayIndex < monthIndex
            } else {
                false
            }
        } ?: false
    }
}
