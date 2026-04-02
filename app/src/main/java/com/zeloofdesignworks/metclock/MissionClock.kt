package com.zeloofdesignworks.metclock
import java.time.Duration
import java.time.Instant
import kotlin.math.abs

data class MissionTimeState(
    val metString: String,
    val nextMilestoneName: String,
    val countdownString: String,
    val countdownSeconds: Long
)

class MissionClock(
    private val liftoff: Instant,
    private val milestones: List<MissionMilestone>
) {
    fun getCurrentState(now: Instant = Instant.now()): MissionTimeState {

        val metDuration = Duration.between(liftoff, now)
        val metString = "MET: ${formatDuration(metDuration)}"

        val nextMilestone = milestones
            .filter { it.timestamp.isAfter(now) }
            .minByOrNull { it.timestamp }

        val milestoneName: String
        val countdownString: String
        val countdownSeconds: Long

        if (nextMilestone != null) {
            milestoneName = nextMilestone.name
            val countdownDuration = Duration.between(now, nextMilestone.timestamp)
            countdownString = "-${formatDuration(countdownDuration)}"
            countdownSeconds = countdownDuration.seconds
        } else {
            milestoneName = "Mission Complete"
            countdownString = "00d 00h 00m 00s"
            countdownSeconds = -1L
        }

        return MissionTimeState(metString, milestoneName, countdownString, countdownSeconds)
    }

    private fun formatDuration(duration: Duration): String {
        val totalSeconds = abs(duration.seconds)
        val days = totalSeconds / 86400
        val hours = (totalSeconds % 86400) / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        val sign = if (duration.seconds < 0) "-" else ""

        return String.format("%s%02dd %02dh %02dm %02ds", sign, days, hours, minutes, seconds)
    }
}