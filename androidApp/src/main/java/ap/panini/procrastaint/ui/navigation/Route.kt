package ap.panini.procrastaint.ui.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

sealed interface Route {
    @Serializable
    data object OnBoarding : Route

    @Serializable
    data object Calendar : Route

    @Serializable
    data object Upcoming : Route

    @Serializable
    data object Library : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data class Tag(val tagId: Long) : Route

    @Serializable
    data object AboutLibraries : Route

    @Serializable
    data object Sync : Route
}

fun List<KClass<out Route>>.isEntryIn(navDestination: NavDestination?): Boolean {
    return any { navDestination?.hasRoute(it) == true }
}
