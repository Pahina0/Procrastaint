package ap.panini.procrastaint.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.runtime.Composable
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import ap.panini.procrastaint.ui.calendar.CalendarScreen
import ap.panini.procrastaint.ui.library.LibraryScreen
import ap.panini.procrastaint.ui.onboarding.OnBoardingScreen
import ap.panini.procrastaint.ui.settings.SettingsScreen
import ap.panini.procrastaint.ui.settings.groups.AboutLibrariesScreen
import ap.panini.procrastaint.ui.settings.sync.SyncScreen
import ap.panini.procrastaint.ui.tag.TagScreen
import ap.panini.procrastaint.ui.upcoming.UpcomingScreen
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Composable
fun NavGraph(navController: NavHostController, startDestination: Route) {
    NavHost(
        navController = navController, startDestination = startDestination,
        enterTransition = {
            fadeIn(
                animationSpec = tween(
                    300, easing = LinearEasing
                )
            ) + slideIntoContainer(
                animationSpec = tween(300, easing = EaseIn),
                towards = AnimatedContentTransitionScope.SlideDirection.Start
            )
        },
        exitTransition = {
            fadeOut(
                animationSpec = tween(
                    300, easing = LinearEasing
                )
            ) + slideOutOfContainer(
                animationSpec = tween(300, easing = EaseOut),
                towards = AnimatedContentTransitionScope.SlideDirection.End
            )
        }) {
        composable<Route.OnBoarding> { OnBoardingScreen() }

        composable<Route.Calendar> { CalendarScreen() }

        composable<Route.Upcoming> { UpcomingScreen() }

        composable<Route.Library> {
            LibraryScreen(
                onNavigateToTag = { navController.navigate(it) }
            )
        }

        composable<Route.Tag> {
            TagScreen(
                args = it.toRoute(),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Route.Settings> {
            SettingsScreen(
                onNavigateToAboutLibraries = { navController.navigate(Route.AboutLibraries) },
                onNavigateToSync = { navController.navigate(Route.Sync) }
            )
        }

        composable<Route.AboutLibraries> {
            AboutLibrariesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Route.Sync> {
            SyncScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

fun List<KClass<out Route>>.isEntryIn(navDestination: NavDestination?): Boolean {
    return any { navDestination?.hasRoute(it) == true }
}

sealed interface Route {

    @Serializable
    object OnBoarding : Route

    @Serializable
    object Calendar : Route

    @Serializable
    object Upcoming : Route

    @Serializable
    object Library : Route

    @Serializable
    object Settings : Route

    @Serializable
    class Tag(val tagId: Long) : Route

    @Serializable
    object AboutLibraries : Route

    @Serializable
    object Sync : Route
}
