package ap.panini.procrastaint.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import ap.panini.procrastaint.ui.agenda.AgendaScreen
import ap.panini.procrastaint.ui.onboarding.OnBoardingScreen
import ap.panini.procrastaint.ui.settings.SettingsScreen
import ap.panini.procrastaint.ui.settings.groups.AboutLibrariesScreen
import ap.panini.procrastaint.ui.settings.parser.ParserSettingsScreen
import ap.panini.procrastaint.ui.settings.sync.SyncScreen
import ap.panini.procrastaint.ui.tag.TagScreen

private const val AnimationSpeed = 250

@Composable
fun NavGraph(navController: NavHostController, startDestination: Route) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(animationSpec = tween(AnimationSpeed)) },
        exitTransition = { fadeOut(animationSpec = tween(AnimationSpeed)) },
        popEnterTransition = { fadeIn(animationSpec = tween(AnimationSpeed)) },
        popExitTransition = { fadeOut(animationSpec = tween(AnimationSpeed)) }
    ) {
        composable<Route.OnBoarding> { OnBoardingScreen() }

        composable<Route.Agenda> {
            AgendaScreen(
                onNavigateToSettings = { navController.navigate(Route.Settings) },
                onNavigateToTag = { navController.navigate(it) }
            )
        }

        composable<Route.Tag>(
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(AnimationSpeed, easing = EaseIn)
                ) + fadeIn(tween(AnimationSpeed))
            },
            exitTransition = { fadeOut(tween(AnimationSpeed / 2)) },
            popEnterTransition = { fadeIn(tween(AnimationSpeed)) },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(AnimationSpeed, easing = EaseOut)
                ) + fadeOut(tween(AnimationSpeed))
            }
        ) {
            TagScreen(
                args = it.toRoute(),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Route.Settings> {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAboutLibraries = { navController.navigate(Route.AboutLibraries) },
                onNavigateToSync = { navController.navigate(Route.Sync) },
                onNavigateToParserSettings = { navController.navigate(Route.ParserSettings) }
            )
        }

        composable<Route.AboutLibraries>(
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(AnimationSpeed, easing = EaseIn)
                ) + fadeIn(tween(AnimationSpeed))
            },
            exitTransition = { fadeOut(tween(AnimationSpeed / 2)) },
            popEnterTransition = { fadeIn(tween(AnimationSpeed)) },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(AnimationSpeed, easing = EaseOut)
                ) + fadeOut(tween(AnimationSpeed))
            }
        ) {
            AboutLibrariesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Route.Sync>(
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(AnimationSpeed, easing = EaseIn)
                ) + fadeIn(tween(AnimationSpeed))
            },
            exitTransition = { fadeOut(tween(AnimationSpeed / 2)) },
            popEnterTransition = { fadeIn(tween(AnimationSpeed)) },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(AnimationSpeed, easing = EaseOut)
                ) + fadeOut(tween(AnimationSpeed))
            }
        ) {
            SyncScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Route.ParserSettings>(
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(AnimationSpeed, easing = EaseIn)
                ) + fadeIn(tween(AnimationSpeed))
            },
            exitTransition = { fadeOut(tween(AnimationSpeed / 2)) },
            popEnterTransition = { fadeIn(tween(AnimationSpeed)) },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(AnimationSpeed, easing = EaseOut)
                ) + fadeOut(tween(AnimationSpeed))
            }
        ) {
            ParserSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
