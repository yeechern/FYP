package my.edu.tarc.fypnew

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.fypnew.databinding.PhysicalActivityListBinding

class AppliedActivityAdapter(
    private var appliedActivities: ArrayList<Activity>,
    private val onActivityClick:(Activity) ->Unit
    ): RecyclerView.Adapter<AppliedActivityAdapter.ActivityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):ActivityViewHolder {
        val binding = PhysicalActivityListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ActivityViewHolder(binding, onActivityClick)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activity = appliedActivities[position]
        holder.bind(activity)
    }

    override fun getItemCount(): Int = appliedActivities.size

    fun updateActivities(newActivities: List<Activity>) {
        appliedActivities.clear()
        appliedActivities.addAll(newActivities)
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
            if(activity.style == "Virtual"){
                binding.textViewNumFriend.visibility = View.INVISIBLE
                binding.textView37.visibility = View.INVISIBLE
                binding.cardViewPhysical.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.yellow))

            }else{
                binding.textViewNumFriend.visibility = View.VISIBLE
                binding.textView37.visibility = View.VISIBLE
                binding.cardViewPhysical.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.cyan))
            }


            itemView.setOnClickListener {
                onActivityClick(activity)
            }
        }
    }

}