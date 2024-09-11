package my.edu.tarc.fypnew

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

import my.edu.tarc.fypnew.databinding.VirtualActivityListBinding

class VirtualActivityAdapter (
    private var virtualActivities: ArrayList<Activity>,
    private val currentUserId: String,
    private val onActivityClick:(Activity)->Unit
): RecyclerView.Adapter<VirtualActivityAdapter.ActivityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VirtualActivityAdapter.ActivityViewHolder {
        val binding = VirtualActivityListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ActivityViewHolder(binding,onActivityClick)
    }

    override fun onBindViewHolder(holder: VirtualActivityAdapter.ActivityViewHolder, position: Int) {
        val activity = virtualActivities[position]
        holder.bind(activity, currentUserId)
    }

    override fun getItemCount(): Int = virtualActivities.size

    fun updateActivities(newActivities: ArrayList<Activity>) {
        virtualActivities = newActivities
        notifyDataSetChanged()
    }

    inner class ActivityViewHolder(
        private val binding: VirtualActivityListBinding,
        private val onActivityClick: (Activity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(activity: Activity, currentUserId: String) {
            binding.textViewVTitle.text = activity.name
            binding.textViewVTag1.text = activity.tags.getOrNull(0) ?: ""
            binding.textViewVTag2.text = activity.tags.getOrNull(1) ?: ""
            binding.textViewVTag3.text = activity.tags.getOrNull(2) ?: ""

            val isApplied = activity.listOfVolunteers.contains(currentUserId)

            val cardColor = when {
                isApplied -> ContextCompat.getColor(itemView.context, R.color.darkYellow)
                else -> ContextCompat.getColor(itemView.context, R.color.yellow)
            }
            binding.cardViewVirtual.setCardBackgroundColor(cardColor)

            binding.textViewVDueDate.text = activity.date



            itemView.setOnClickListener {
                onActivityClick(activity)
            }

        }
    }

}
