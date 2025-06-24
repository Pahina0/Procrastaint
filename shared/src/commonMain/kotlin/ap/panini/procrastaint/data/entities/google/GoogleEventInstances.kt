package ap.panini.procrastaint.data.entities.google

import kotlinx.serialization.Serializable

@Serializable
internal data class GoogleEventInstances(
    val items: List<GoogleEvent>
)
