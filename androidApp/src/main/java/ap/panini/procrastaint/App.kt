package ap.panini.procrastaint

import android.app.Application
import ap.panini.procrastaint.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(appModule())
        }

    }
}