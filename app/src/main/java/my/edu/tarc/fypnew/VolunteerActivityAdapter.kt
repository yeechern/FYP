package my.edu.tarc.fypnew

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.fypnew.databinding.PhysicalActivityListBinding

class VolunteerActivityAdapter (
    private var activities: ArrayList<Activity>,
    private val userFriends: List<String>,
    private val isVolunteer: Boolean,
    private val currentUserId: String,
    private val onActivityClick:(Activity)->Unit
):RecyclerView.Adapter<VolunteerActivityAdapter.ActivityViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val binding = PhysicalActivityListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ActivityViewHolder(binding,onActivityClick)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activity = activities[position]
        holder.bind(activity, userFriends, isVolunteer, currentUserId)
    }

    override fun getItemCount(): Int = activities.size

    fun updateActivities(newActivities: ArrayList<Activity>) {
        activities = newActivities
        notifyDataSetChanged()
    }

    inner class ActivityViewHolder(
        private val binding: PhysicalActivityListBinding,
        private val onActivityClick: (Activity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            activity: Activity,
            userFriends: List<String>,
            isVolunteer: Boolean,
            currentUserId: String
        ) {
            binding.textViewPTitle.text = activity.name
            binding.textViewPTag1.text = activity.tags.getOrNull(0) ?: ""
            binding.textViewPTag2.text = activity.tags.getOrNull(1) ?: ""
            binding.textViewPTag3.text = activity.tags.getOrNull(2) ?: ""

            val volunteersNeeded = activity.personNeed.toIntOrNull() ?: 0
            val volunteersApplied = activity.listOfVolunteers.size

            val isFull = volunteersApplied >= volunteersNeeded
            val isApplied = activity.listOfVolunteers.contains(currentUserId)

            binding.textViewStillNeed.text = volunteersApplied.toString()
            binding.textViewTotalNeed.text = volunteersNeeded.toString()

            val cardColor = when {
                isFull -> ContextCompat.getColor(itemView.context, R.color.lightRed) // or any color for "full"
                isApplied -> ContextCompat.getColor(itemView.context, R.color.darkCyan) // or any color for "applied"
                else -> ContextCompat.getColor(itemView.context, R.color.cyan) // default color
            }
            binding.cardViewPhysical.setCardBackgroundColor(cardColor)

            Log.d("VolunteerActivityAdapter", "User isVolunteer: $isVolunteer") // Debug log
            if (isVolunteer) {
                val friendsApplied = activity.listOfVolunteers.count { userFriends.contains(it) }
                binding.textViewNumFriend.text = friendsApplied.toString()
                binding.textViewNumFriend.visibility = View.VISIBLE
                binding.textView37.visibility = View.VISIBLE
            } else {
                binding.textViewNumFriend.visibility = View.INVISIBLE
                binding.textView37.visibility = View.INVISIBLE
            }


            itemView.setOnClickListener {
                onActivityClick(activity)

            }

        }
    }


}