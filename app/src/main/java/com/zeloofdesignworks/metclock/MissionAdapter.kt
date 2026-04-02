package com.zeloofdesignworks.metclock

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class MissionAdapter(
    private val missions: List<Mission>,
    private val onMissionSelected: (Mission) -> Unit
) : RecyclerView.Adapter<MissionAdapter.ViewHolder>() {

    private var selectedPosition = -1

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layout: LinearLayout = view.findViewById(R.id.missionCardLayout)
        val nameText: TextView = view.findViewById(R.id.textMissionName)
        val statusText: TextView = view.findViewById(R.id.textMissionStatus)
        val patchIcon: ImageView = view.findViewById(R.id.imgMissionPatch)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mission, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mission = missions[position]

        holder.nameText.text = mission.name
        holder.patchIcon.setImageResource(mission.patchResId)

        val hasLaunched = java.time.Instant.now().isAfter(mission.liftoffTime)
        holder.statusText.text = if (hasLaunched) "In Flight" else "Pre-Launch"

        val context = holder.itemView.context
        if (selectedPosition == position) {
            holder.layout.setBackgroundColor(ContextCompat.getColor(context, R.color.space_card_selected))
        } else {
            holder.layout.setBackgroundColor(Color.TRANSPARENT)
        }

        holder.itemView.setOnClickListener {
            val previousSelection = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previousSelection)
            notifyItemChanged(selectedPosition)

            onMissionSelected(mission)
        }
    }

    override fun getItemCount() = missions.size
}