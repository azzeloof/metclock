package com.zeloofdesignworks.metclock

import kotlinx.coroutines.*
import android.widget.TextView
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.widget.Switch
import android.content.Context
import android.widget.ImageView
import androidx.core.content.edit

class MainActivity : AppCompatActivity() {

    private var selectedMissionId: String? = null
    private lateinit var btnStartTracker: Button
    private lateinit var timelineAdapter: TimelineAdapter
    private lateinit var missionAdapter: MissionAdapter
    private lateinit var textLiveMet: TextView
    private lateinit var textLiveMilestoneName: TextView
    private lateinit var textLiveCountdown: TextView
    private var uiTickingJob: Job? = null
    private val uiScope = CoroutineScope(Dispatchers.Main + Job())

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted && selectedMissionId != null) {
            startMissionService(selectedMissionId!!)
        } else {
            Toast.makeText(this, "Permission required for lockscreen tracking.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnStartTracker = findViewById(R.id.btnStartTracker)
        val timelineRecycler = findViewById<RecyclerView>(R.id.timelineRecycler)
        val missionRecycler = findViewById<RecyclerView>(R.id.missionRecycler)

        // Setup Timeline Recycler
        timelineAdapter = TimelineAdapter(emptyList())
        timelineRecycler.layoutManager = LinearLayoutManager(this)
        timelineRecycler.adapter = timelineAdapter

        // Setup Mission Recycler
        missionAdapter = MissionAdapter(MissionRepository.availableMissions) { clickedMission ->
            selectedMissionId = clickedMission.id
            btnStartTracker.isEnabled = true
            timelineAdapter.updateData(clickedMission.milestones)
            startLiveDashboard(clickedMission)
        }
        missionRecycler.layoutManager = LinearLayoutManager(this)
        missionRecycler.adapter = missionAdapter

        // Handle the Start Button
        btnStartTracker.setOnClickListener {
            checkPermissionsAndStart()
        }

        val btnSettings = findViewById<ImageView>(R.id.btnSettings)
        btnSettings.setOnClickListener {
            showSettingsDialog()
        }

        textLiveMet = findViewById(R.id.textLiveMet)
        textLiveMilestoneName = findViewById(R.id.textLiveMilestoneName)
        textLiveCountdown = findViewById(R.id.textLiveCountdown)
    }

    private fun checkPermissionsAndStart() {
        if (selectedMissionId == null) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                startMissionService(selectedMissionId!!)
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            startMissionService(selectedMissionId!!)
        }
    }

    private fun startMissionService(missionId: String) {
        val serviceIntent = Intent(this, MissionClockService::class.java).apply {
            putExtra("EXTRA_MISSION_ID", missionId)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        Toast.makeText(this, "Tracking Initiated.", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun showSettingsDialog() {
        val bottomSheet = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_settings, null)

        val switchAlerts = view.findViewById<Switch>(R.id.switchAlerts)
        val prefs = getSharedPreferences("MissionPrefs", Context.MODE_PRIVATE)

        // Set the switch to whatever the user previously saved
        switchAlerts.isChecked = prefs.getBoolean("alerts_enabled", false)

        // Save the new state whenever the user flips the switch
        switchAlerts.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit { putBoolean("alerts_enabled", isChecked) }
        }

        bottomSheet.setContentView(view)
        bottomSheet.show()
    }

    private fun startLiveDashboard(mission: Mission) {
        // Cancel the old loop if the user clicks a different mission
        uiTickingJob?.cancel()

        // Create a new clock engine for this specific mission
        val localClock = MissionClock(mission.liftoffTime, mission.milestones)

        uiTickingJob = uiScope.launch {
            while (isActive) {
                val state = localClock.getCurrentState()
                textLiveMet.text = state.metString.replace("MET: ", "") // Clean up the label
                textLiveMilestoneName.text = "Next: ${state.nextMilestoneName}"
                textLiveCountdown.text = "T${state.countdownString}"
                delay(1000)
            }
        }
    }

    // Clean up the coroutine if the app is closed
    override fun onDestroy() {
        super.onDestroy()
        uiScope.cancel()
    }
}