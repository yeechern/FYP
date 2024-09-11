package my.edu.tarc.fypnew

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.fypnew.databinding.FragmentFriendListBinding
import my.edu.tarc.fypnew.databinding.VolunteerListBinding

class FriendListAdapter (private var volunteers: List<Activity.Volunteer>) :
    RecyclerView.Adapter<FriendListAdapter.friendListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): friendListViewHolder {
        val binding = VolunteerListBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return friendListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: friendListViewHolder, position: Int) {
        val volunteer = volunteers[position]
        holder.bind(volunteer)
    }

    override fun getItemCount(): Int = volunteers.size

    fun updateFriends(newVolunteers: List<Activity.Volunteer>) {
        volunteers = newVolunteers
        notifyDataSetChanged()
    }

    inner class friendListViewHolder(
        private val binding: VolunteerListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(volunteer: Activity.Volunteer) {
            binding.textViewVolunName.text = volunteer.name
            binding.textViewVolunEmail.text = volunteer.email
            binding.textViewVolunContact.text = volunteer.contact
            binding.textViewVolunComplete.text = volunteer.getCompletedActivityCount().toString()
        }
    }
}