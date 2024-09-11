package my.edu.tarc.fypnew

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.fypnew.databinding.PhysicalActivityListBinding

class onGoActivityAdapter (
    private var onGoActivities: ArrayList<Activity>,
    private val onActivityClick:(Activity)->Unit
    ):RecyclerView.Adapter<onGoActivityAdapter.ActivityViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val binding = PhysicalActivityListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ActivityViewHolder(binding, onActivityClick)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activity = onGoActivities[position]
        holder.bind(activity)
    }

    override fun getItemCount(): Int = onGoActivities.size

    fun updateActivities(newActivities: List<Activity>) {
        onGoActivities.clear()
        onGoActivities.addAll(newActivities)
        notifyDataSetChanged()
    }

    inner class ActivityViewHolder(
        private val binding: PhysicalActivityListBinding,
        private val onActivityClick: (Activity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(activity: Activity) {
            binding.textViewPTitle.text = activity.name
            binding.textViewPTag1.text = activity.tags.getOrNull(0) ?: ""
            binding.textViewPTag2.text = activity.tags.getOrNull(1) ?: ""
            binding.textViewPTag3.text = activity.tags.getOrNull(2) ?: ""

            val volunteersNeeded = activity.personNeed.toIntOrNull() ?: 0
            val volunteersApplied = activity.listOfVolunteers.size
            binding.textViewStillNeed.text = volunteersApplied.toString()
            binding.textViewTotalNeed.text = volunteersNeeded.toString()
            binding.textView37.visibility = View.INVISIBLE
            binding.textViewNumFriend.visibility = View.INVISIBLE

            if(activity.style == "Virtual"){
                binding.cardViewPhysical.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.yellow))

            }else{
                binding.cardViewPhysical.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.cyan))
            }

            itemView.setOnClickListener {
                onActivityClick(activity)
            }
        }
    }
}

