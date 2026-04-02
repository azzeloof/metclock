package com.zeloofdesignworks.metclock

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TimelineAdapter(private var milestones: List<MissionMilestone>) : RecyclerView.Adapter<TimelineAdapter.ViewHolder>() {

    private val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy - hh:mm a")
        .withZone(ZoneId.systemDefault())

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.textMilestoneName)
        val timeText: TextView = view.findViewById(R.id.textMilestoneTime)
        val arrowIcon: ImageView = view.findViewById(R.id.currentTimeArrow)
        val line: View = view.findViewById(R.id.timelineLine)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_timeline, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val milestone = milestones[position]
        val now = Instant.now()

        holder.nameText.text = milestone.name
        holder.timeText.text = formatter.format(milestone.timestamp)

        // Find the index of the VERY NEXT upcoming milestone
        val nextUpcomingIndex = milestones.indexOfFirst { it.timestamp.isAfter(now) }

        // If this row is the next upcoming milestone, show the arrow pointing at it
        if (position == nextUpcomingIndex) {
            holder.arrowIcon.visibility = View.VISIBLE
        } else {
            holder.arrowIcon.visibility = View.INVISIBLE
        }

        // Clean up the timeline visuals (don't draw the line above the first item or below the last)
        val layoutParams = holder.line.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.topMargin = if (position == 0) 50 else 0
        layoutParams.bottomMargin = if (position == milestones.size - 1) 50 else 0
        holder.line.requestLayout()
    }

    override fun getItemCount() = milestones.size

    fun updateData(newMilestones: List<MissionMilestone>) {
        milestones = newMilestones
        notifyDataSetChanged()
    }
}