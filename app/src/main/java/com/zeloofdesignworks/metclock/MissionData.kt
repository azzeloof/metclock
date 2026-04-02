package com.zeloofdesignworks.metclock

import java.time.Instant

data class MissionMilestone(
    val name: String,
    val timestamp: Instant
)

data class Mission(
    val id: String,
    val name: String,
    val patchResId: Int,
    val liftoffTime: Instant,
    val milestones: List<MissionMilestone>
)

object MissionRepository {

    private fun calcTime(base: Instant, days: Long, hours: Long, minutes: Long): Instant {
        return base.plusSeconds((days * 24 * 60 * 60) + (hours * 60 * 60) + (minutes * 60))
    }

    private val artemisLiftoff = Instant.parse("2026-04-01T22:35:00Z")

    val availableMissions = listOf(
        Mission(
            id = "artemis_2",
            name = "Artemis II",
            patchResId = R.drawable.artemis_ii,
            liftoffTime = artemisLiftoff,
            milestones = listOf(
                MissionMilestone("ICPS PRM", calcTime(artemisLiftoff, 0, 0, 50)),
                MissionMilestone("ARB TIG", calcTime(artemisLiftoff, 0, 1, 48)),
                MissionMilestone("Orion/ICPS Spring Sep", calcTime(artemisLiftoff, 0, 3, 24)),
                MissionMilestone("Orion USS", calcTime(artemisLiftoff, 0, 4, 52)),
                MissionMilestone("ICPS Disposal Burn", calcTime(artemisLiftoff, 0, 5, 2)),
                MissionMilestone("SPL Deploys", calcTime(artemisLiftoff, 0, 5, 27)),
                MissionMilestone("Orion PRB", calcTime(artemisLiftoff, 0, 13, 45)),
                MissionMilestone("Translunar Injection (TLI)", calcTime(artemisLiftoff, 1, 1, 37)),
                MissionMilestone("OTC-1 Burn", calcTime(artemisLiftoff, 2, 0, 7)),
                MissionMilestone("OTC-2 Burn", calcTime(artemisLiftoff, 3, 0, 12)),
                MissionMilestone("Lunar Sphere of Influence Entry", calcTime(artemisLiftoff, 4, 6, 59)),
                MissionMilestone("Lunar Close Approach", calcTime(artemisLiftoff, 5, 1, 23)),
                MissionMilestone("Max Earth Distance", calcTime(artemisLiftoff, 5, 1, 26)),
                MissionMilestone("Lunar Sphere of Influence Exit", calcTime(artemisLiftoff, 5, 19, 47)),
                MissionMilestone("RTC-1 Burn", calcTime(artemisLiftoff, 6, 4, 23)),
                MissionMilestone("RTC-2 Burn", calcTime(artemisLiftoff, 8, 4, 33)),
                MissionMilestone("RTC-3 Burn", calcTime(artemisLiftoff, 8, 20, 33)),
                MissionMilestone("CM/SM Separation", calcTime(artemisLiftoff, 9, 1, 13)),
                MissionMilestone("Entry Interface", calcTime(artemisLiftoff, 9, 1, 33)),
                MissionMilestone("Splashdown", calcTime(artemisLiftoff, 9, 1, 46))
            )
        ),
        Mission(
            id = "europa_clipper",
            name = "Europa Clipper",
            patchResId = android.R.drawable.btn_star_big_on,
            liftoffTime = Instant.parse("2024-10-14T16:06:00Z"),
            milestones = listOf(
                MissionMilestone("Mars Gravity Assist", Instant.parse("2025-02-28T00:00:00Z")),
                MissionMilestone("Earth Gravity Assist", Instant.parse("2026-12-01T00:00:00Z")),
                MissionMilestone("Jupiter Arrival", Instant.parse("2030-04-11T00:00:00Z"))
            )
        )
    )

    fun getMissionById(id: String): Mission? {
        return availableMissions.find { it.id == id }
    }
}