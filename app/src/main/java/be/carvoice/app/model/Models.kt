package be.carvoice.app.model

enum class LaunchAvailability { READY, NOT_INSTALLED, DISABLED, NO_LAUNCH_ACTIVITY }
data class LauncherState(
    val availability: LaunchAvailability = LaunchAvailability.NOT_INSTALLED,
    val chatGptVersion: String? = null,
    val carConnection: String = "Niet gedetecteerd",
    val audioOutputs: List<String> = emptyList(),
    val status: String? = null,
)
