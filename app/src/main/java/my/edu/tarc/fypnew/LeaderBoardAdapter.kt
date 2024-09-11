package my.edu.tarc.fypnew

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.fypnew.databinding.LeaderBoardListBinding

class LeaderBoardAdapter(private var volunteers: List<Activity.Volunteer>) :
    RecyclerView.Adapter<LeaderBoardAdapter.LeaderBoardViewHolder>() {

    enum class SortCriterion {
        VOLUNTEER_HOURS,
        COMPLETED_ACTIVITIES
    }

    private var sortCriterion: SortCriterion = SortCriterion.VOLUNTEER_HOURS

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderBoardViewHolder {
        val binding = LeaderBoardListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LeaderBoardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LeaderBoardViewHolder, position: Int) {
        holder.bind(volunteers[position], position + 1)
    }

    override fun getItemCount(): Int = volunteers.size

    fun setSortCriterion(criterion: SortCriterion) {
        sortCriterion = criterion
        sortVolunteers()
        notifyDataSetChanged()
    }

    fun updateVolunteers(newVolunteers: List<Activity.Volunteer>) {
        volunteers = newVolunteers
        sortVolunteers()
        notifyDataSetChanged()
    }

    private fun sortVolunteers() {
        volunteers = when (sortCriterion) {
            SortCriterion.VOLUNTEER_HOURS -> volunteers.sortedByDescending {
                Log.d("LeaderBoardAdapter", "Sorting by hours: ${it.volunteerHours}")
                it.volunteerHours
            }
            SortCriterion.COMPLETED_ACTIVITIES -> volunteers.sortedByDescending {
                Log.d("LeaderBoardAdapter", "Sorting by activities: ${it.completedActivityList.size}")
                it.completedActivityList.size
            }
        }
    }

    inner class LeaderBoardViewHolder(private val binding: LeaderBoardListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(volunteer: Activity.Volunteer, rank: Int) {
            Log.d("LeaderBoardAdapter", "Binding volunteer: ${volunteer.name} with value: ${when (sortCriterion) {
                SortCriterion.VOLUNTEER_HOURS -> volunteer.volunteerHours
                SortCriterion.COMPLETED_ACTIVITIES -> volunteer.getCompletedActivityCount()
            }}")

            binding.textViewRanking.text = rank.toString()
            binding.textViewLeadName.text = volunteer.name

            val value = when (sortCriterion) {
                SortCriterion.VOLUNTEER_HOURS -> volunteer.volunteerHours.toString()
                SortCriterion.COMPLETED_ACTIVITIES -> volunteer.completedActivityList.size.toString()
            }
            binding.textViewLeadValue.text = value
            Log.d("LeaderBoardAdapter", "Binding volunteer: ${volunteer.name} with value: $value")
        }
    }
}