package ap.panini.procrastaint.crash

import android.content.Context
import android.content.Intent
import android.util.Log

class GlobalExceptionHandler(
    private val context: Context

) : Thread.UncaughtExceptionHandler {

    companion object {
        const val INTENT_EXCEPTION = "exception"
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        Log.e("Uncaught Exception", "uncaughtException: ${e.stackTraceToString()}")

        val intent = Intent(context, CrashActivity::class.java).apply {
            putExtra(INTENT_EXCEPTION, e.stackTraceToString())
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }

        context.startActivity(intent)

//        (Thread.getDefaultUncaughtExceptionHandler() as Thread.UncaughtExceptionHandler)
//            .uncaughtException(t, e)
    }
}
