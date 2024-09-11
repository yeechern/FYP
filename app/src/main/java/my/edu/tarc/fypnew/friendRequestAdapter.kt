package my.edu.tarc.fypnew

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.fypnew.databinding.FriendRequestListBinding

class friendRequestAdapter (
    private var friendRequests: List<Activity.FriendRequest>,
    private val onAcceptClick: (Activity.FriendRequest) -> Unit,
    private val onRejectClick: (Activity.FriendRequest) -> Unit
) : RecyclerView.Adapter<friendRequestAdapter.FriendRequestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendRequestViewHolder {
        val binding = FriendRequestListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendRequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendRequestViewHolder, position: Int) {
        val friendRequest = friendRequests[position]
        holder.bind(friendRequest)
    }

    override fun getItemCount(): Int {
        return friendRequests.size
    }

    fun updateFriendRequests(newFriendRequests: List<Activity.FriendRequest>) {
        friendRequests = newFriendRequests
        notifyDataSetChanged()
    }

    inner class FriendRequestViewHolder(private val binding: FriendRequestListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(friendRequest: Activity.FriendRequest) {
            binding.textViewRequestName.text = friendRequest.senderName
            binding.buttonFriendAccept.setOnClickListener { onAcceptClick(friendRequest) }
            binding.buttonFriendReject.setOnClickListener { onRejectClick(friendRequest) }
        }
    }
}