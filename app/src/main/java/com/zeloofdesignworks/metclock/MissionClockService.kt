package com.zeloofdesignworks.metclock

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.os.VibrationEffect
import android.os.Vibrator
import kotlinx.coroutines.*

class MissionClockService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var tickingJob: Job? = null

    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private var activeClock: MissionClock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()

        val missionId = intent?.getStringExtra("EXTRA_MISSION_ID") ?: return START_NOT_STICKY
        val mission = MissionRepository.getMissionById(missionId)
        if (mission == null) return START_NOT_STICKY
        activeClock = MissionClock(mission.liftoffTime, mission.milestones)
        val patchBitmap = BitmapFactory.decodeResource(resources, mission.patchResId)
        notificationBuilder = NotificationCompat.Builder(this, "MISSION_CHANNEL")
            .setContentTitle("${mission.name} Tracker")
            .setContentText("Calculating timeline...")
            .setSmallIcon(android.R.drawable.star_on)
            .setLargeIcon(patchBitmap)
            .setOngoing(true)
            .setOnlyAlertOnce(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, notificationBuilder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, notificationBuilder.build())
        }

        startTicking()

        return START_STICKY
    }

    private fun startTicking() {
        tickingJob?.cancel()

        tickingJob = serviceScope.launch {
            // Grab our save file to check if alerts are enabled
            val prefs = getSharedPreferences("MissionPrefs", Context.MODE_PRIVATE)

            while (isActive) {
                activeClock?.getCurrentState()?.let { state ->
                    val displayText = "${state.metString}\n${state.nextMilestoneName}: ${state.countdownString}"

                    notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(displayText))
                    notificationBuilder.setContentText(displayText)
                    notificationManager.notify(1, notificationBuilder.build())

                    // --- NEW ALERT LOGIC ---
                    val alertsEnabled = prefs.getBoolean("alerts_enabled", false)
                    if (alertsEnabled) {
                        when (state.countdownSeconds) {
                            300L -> triggerVibration(isFiveMinuteWarning = true)
                            0L -> triggerVibration(isFiveMinuteWarning = false)
                        }
                    }
                }
                delay(1000)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("MISSION_CHANNEL", "Mission Clock", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun triggerVibration(isFiveMinuteWarning: Boolean) {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (!vibrator.hasVibrator()) return

        // Pattern: [Wait, Vibrate, Wait, Vibrate...] in milliseconds
        val pattern = if (isFiveMinuteWarning) {
            longArrayOf(0, 300, 200, 300) // Short double-buzz for 5 mins
        } else {
            longArrayOf(0, 1000, 500, 1000, 500, 1000) // Heavy triple-buzz for T=0
        }

        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
    }
}