package my.edu.tarc.fypnew

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.fypnew.databinding.FragmentAppliedActivityBinding
import my.edu.tarc.fypnew.databinding.FragmentCheckAppliedFraagmentBinding
import my.edu.tarc.fypnew.databinding.VolunteerListBinding

class checkAppliedAdapter(private var volunteers: List<Activity.Volunteer>) :
    RecyclerView.Adapter<checkAppliedAdapter.checkAppliedViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): checkAppliedViewHolder {
        val binding = VolunteerListBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return checkAppliedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: checkAppliedViewHolder, position: Int) {
        val volunteer = volunteers[position]
        holder.bind(volunteer)
    }

    override fun getItemCount(): Int = volunteers.size

    fun updateVolunteers(newVolunteers: List<Activity.Volunteer>) {
        volunteers = newVolunteers
        notifyDataSetChanged()
    }

    inner class checkAppliedViewHolder(
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