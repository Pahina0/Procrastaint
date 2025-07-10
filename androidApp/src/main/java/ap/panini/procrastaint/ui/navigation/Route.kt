package ap.panini.procrastaint.ui.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

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

fun List<KClass<out Route>>.isEntryIn(navDestination: NavDestination?): Boolean {
    return any { navDestination?.hasRoute(it) == true }
}
