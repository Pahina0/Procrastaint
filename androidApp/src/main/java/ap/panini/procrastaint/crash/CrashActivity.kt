package ap.panini.procrastaint.crash

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ap.panini.procrastaint.ui.MainActivity
import ap.panini.procrastaint.ui.theme.ProcrastaintTheme
import java.net.URLEncoder

class CrashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        val exception = intent.getStringExtra(GlobalExceptionHandler.INTENT_EXCEPTION)!!

        setContent {
            ProcrastaintTheme {
                Scaffold {
                    Box(modifier = Modifier.padding(it)) {
                        CrashScreen(exception)
                    }
                }
            }
        }
    }

    @Composable
    fun CrashScreen(exception: String, modifier: Modifier = Modifier) {

        Column(
            modifier
                .padding(10.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                Icons.Outlined.BugReport,
                contentDescription = "Bug",
                modifier = Modifier.size(80.dp)
            )

            Text(
                "Uh oh.\nSomething unexpected happened!",
                style = MaterialTheme.typography.headlineLarge,
            )

            CrashActions(exception)

            Card(modifier = Modifier.fillMaxWidth()) {
                Text(exception, modifier = Modifier.padding(10.dp))
            }

            CrashActions(exception)
        }
    }

    @Composable
    private fun CrashActions(exception: String) {
        val clipboardManager = LocalClipboardManager.current
        val uriHandler = LocalUriHandler.current

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = {
                clipboardManager.setText(buildAnnotatedString { append(exception) })
                uriHandler.openUri(
                    "https://github.com/Pahina0/Procrastaint/issues/new?title=Android+uncaught+exception&body=${
                        URLEncoder.encode(
                            exception,
                            java.nio.charset.StandardCharsets.UTF_8.toString()
                        )
                    }"
                )
            }) {
                Icon(
                    Icons.Outlined.BugReport,
                    contentDescription = "Report",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Report")
            }

            Button(onClick = {
                val intent = Intent(applicationContext, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }

                startActivity(intent)
            }) {
                Icon(
                    Icons.Outlined.Replay,
                    contentDescription = "Restart",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Restart")
            }
        }
    }

    @Preview
    @Composable
    private fun CrashScreenPreview() {
        CrashScreen(
            """
            
                                                                                                            at org.koin.core.scope.Scope.resolveFromContext(Scope.kt:315)
                                                                                                    at org.koin.core.scope.Scope.stackParametersCall(Scope.kt:285)
                                                                                                    at org.koin.core.scope.Scope.resolveInstance(Scope.kt:263)
                                                                                                    at org.koin.core.scope.Scope.resolve(Scope.kt:236)
                                                                                                    at org.koin.core.scope.Scope.get(Scope.kt:218)
                                                                                                    at org.koin.core.scope.Scope.getOrNull(Scope.kt:178)
                                                                                                    at org.koin.androidx.workmanager.factory.KoinWorkerFactory.createWorker(KoinWorkerFactory.kt:47)
                                                                                                    at androidx.work.DelegatingWorkerFactory.createWorker(DelegatingWorkerFactory.kt:49)
                                                                                                    at androidx.work.WorkerFactory.createWorkerWithDefaultFallback(WorkerFactory.kt:95)
                                                                                                    at androidx.work.impl.WorkerWrapper.runWorker(WorkerWrapper.kt:234)
                                                                                                    at androidx.work.impl.WorkerWrapper.access${'$'}runWorker(WorkerWrapper.kt:67)
                                                                                                    at androidx.work.impl.WorkerWrapper${'$'}launch${'$'}1${'$'}resolution${'$'}1.invokeSuspend(WorkerWrapper.kt:98)
                                                                                                    at androidx.work.impl.WorkerWrapper${'$'}launch${'$'}1${'$'}resolution${'$'}1.invoke(Unknown Source:8)
                                                                                                    at androidx.work.impl.WorkerWrapper${'$'}launch${'$'}1${'$'}resolution${'$'}1.invoke(Unknown Source:4)
                                                                                                    at kotlinx.coroutines.intrinsics.UndispatchedKt.startUndispatchedOrReturn(Undispatched.kt:43)
                                                                                                    at kotlinx.coroutines.BuildersKt__Builders_commonKt.withContext(Builders.common.kt:166)
                                                                                                    at kotlinx.coroutines.BuildersKt.withContext(Unknown Source:1)
                                                                                                    at androidx.work.impl.WorkerWrapper${'$'}launch${'$'}1.invokeSuspend(WorkerWrapper.kt:98)
                                                                                                    at androidx.work.impl.WorkerWrapper${'$'}launch${'$'}1.invoke(Unknown Source:8)
                                                                                                    at androidx.work.impl.WorkerWrapper${'$'}launch${'$'}1.invoke(Unknown Source:4)
                                                                                                    at androidx.work.ListenableFutureKt${'$'}launchFuture${'$'}1${'$'}2.invokeSuspend(ListenableFuture.kt:42)
                                                                                                    at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
                                                                                                    at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:100)
                                                                                                    at androidx.work.impl.utils.SerialExecutorImpl${'$'}Task.run(SerialExecutorImpl.java:96)
                                                                                                    at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1156)
                                                                                                    at java.util.concurrent.ThreadPoolExecutor${'$'}Worker.run(ThreadPoolExecutor.java:651)
                                                                                                    at java.lang.Thread.run(Thread.java:1119)
                                                                                                Caused by: org.koin.core.error.NoDefinitionFoundException: No definition found for type 'androidx.work.WorkerParameters'. Check your Modules configuration and add missing type and/or qualifier!
                                                                                                    at org.koin.core.scope.Scope.resolveFromContext(Scope.kt:546)
                                                                                                    at org.koin.core.scope.Scope.stackParametersCall(Scope.kt:277)
                                                                                                    at org.koin.core.scope.Scope.resolveInstance(Scope.kt:263)
                                                                                                    at org.koin.core.scope.Scope.resolve(Scope.kt:236)
                                                                                                    at org.koin.core.scope.Scope.get(Scope.kt:218)
                                                                                                    at ap.panini.procrastaint.di.AndroidModuleKt.androidModule${'$'}lambda${'$'}7${'$'}lambda${'$'}6(AndroidModule.kt:56)
                                                                                                    at ap.panini.procrastaint.di.AndroidModuleKt.${'$'}r8${'$'}lambda${'$'}p29QtuJfVEM-fayQk-cWueq4P5o(Unknown Source:0)
                                                                                                    at ap.panini.procrastaint.di.AndroidModuleKt${'$'}${'$'}ExternalSyntheticLambda7.invoke(D8${'$'}${'$'}SyntheticClass:0)
                                                                                                    at org.koin.core.instance.InstanceFactory.create(InstanceFactory.kt:51)
                                                                                                    at org.koin.core.instance.FactoryInstanceFactory.get(FactoryInstanceFactory.kt:38) 
                                                                                                    at org.koin.core.registry.InstanceRegistry.resolveInstance${'$'}koin_core(InstanceRegistry.kt:113) 
                                                                                                    at org.koin.core.scope.Scope.resolveFromRegistry(Scope.kt:325) 
                                                                                                    at org.koin.core.scope.Scope.resolveFromContext(Scope.kt:315) 
                                                                                                    at org.koin.core.scope.Scope.stackParametersCall(Scope.kt:285) 
                                                                                                    at org.koin.core.scope.Scope.resolveInstance(Scope.kt:263) 
                                                                                                    at org.koin.core.scope.Scope.resolve(Scope.kt:236) 
                                                                                                    at org.koin.core.scope.Scope.get(Scope.kt:218) 
                                                                                                    at org.koin.core.scope.Scope.getOrNull(Scope.kt:178) 
                                                                                                    at org.koin.androidx.workmanager.factory.KoinWorkerFactory.createWorker(KoinWorkerFactory.kt:47) 
                                                                                                    at androidx.work.DelegatingWorkerFactory.createWorker(DelegatingWorkerFactory.kt:49) 
                                                                                                    at androidx.work.WorkerFactory.createWorkerWithDefaultFallback(WorkerFactory.kt:95) 
                                                                                                    at androidx.work.impl.WorkerWrapper.runWorker(WorkerWrapper.kt:234) 
                                                                                                    at androidx.work.impl.WorkerWrapper.access${'$'}runWorker(WorkerWrapper.kt:67) 
                                                                                                    at androidx.work.impl.WorkerWrapper${'$'}launch${'$'}1${'$'}resolution${'$'}1.invokeSuspend(WorkerWrapper.kt:98) 
                                                                                                    at androidx.work.impl.WorkerWrapper${'$'}launch${'$'}1${'$'}resolution${'$'}1.invoke(Unknown Source:8) 
                                                                                                    at androidx.work.impl.WorkerWrapper${'$'}launch${'$'}1${'$'}resolution${'$'}1.invoke(Unknown Source:4) 
                                                                                                    at kotlinx.coroutines.intrinsics.UndispatchedKt.startUndispatchedOrReturn(Undispatched.kt:43) 
                                                                                                    at kotlinx.coroutines.BuildersKt__Builders_commonKt.withContext(Builders.common.kt:166) 
                                                                                                    at kotlinx.coroutines.BuildersKt.withContext(Unknown Source:1) 
                                                                                                    at androidx.work.impl.WorkerWrapper${'$'}launch${'$'}1.invokeSuspend(WorkerWrapper.kt:98) 
                                                                                                    at androidx.work.impl.WorkerWrapper${'$'}launch${'$'}1.invoke(Unknown Source:8) 
                                                                                                    at androidx.work.impl.WorkerWrapper${'$'}launch${'$'}1.invoke(Unknown Source:4) 
                                                                                                    at androidx.work.ListenableFutureKt${'$'}launchFuture${'$'}1${'$'}2.invokeSuspend(ListenableFuture.kt:42) 
                                                                                                    at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33) 
                                                                                                    at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:100) 
                                                                                                    at androidx.work.impl.utils.SerialExecutorImpl${'$'}Task.run(SerialExecutorImpl.java:96) 
                                                                                                    at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1156) 
                                                                                                    at java.util.concurrent.ThreadPoolExecutor${'$'}Worker.run(ThreadPoolExecutor.java:651) 
                                                                                                    at java.lang.Thread.run(Thread.java:1119) 
 
            
            """.trimIndent()
        )
    }
}
