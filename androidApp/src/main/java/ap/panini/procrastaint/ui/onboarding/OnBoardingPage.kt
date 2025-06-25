package ap.panini.procrastaint.ui.onboarding

import androidx.compose.runtime.Composable
import ap.panini.procrastaint.ui.onboarding.pages.DemoPage
import ap.panini.procrastaint.ui.onboarding.pages.SetupPage
import ap.panini.procrastaint.ui.onboarding.pages.TryPage

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

    Try(
        "Try",
        "Got it",
        { TryPage() }
    ),

    Setup(
        "Setup",
        "Complete",
        { SetupPage() }
    )
}
