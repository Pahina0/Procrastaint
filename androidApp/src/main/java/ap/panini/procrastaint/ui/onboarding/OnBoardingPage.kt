package ap.panini.procrastaint.ui.onboarding

import androidx.compose.runtime.Composable
import ap.panini.procrastaint.ui.onboarding.components.SetupPage
import ap.panini.procrastaint.ui.onboarding.components.DemoPage


enum class OnBoardingPage(
    val topBarText: String,
    val completeText: String,
    val content: @Composable () -> Unit
) {
    Demo(
        "Demo",
        "Next",
        { DemoPage() }
    ),

    Setup(
        "Setup",
        "Complete",
        { SetupPage() }
    )

}