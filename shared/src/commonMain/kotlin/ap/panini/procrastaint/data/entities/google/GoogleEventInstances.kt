package ap.panini.procrastaint.data.entities.google

import kotlinx.serialization.Serializable

@Serializable
data class GoogleEventInstances(
    val items: List<GoogleEvent>
)
