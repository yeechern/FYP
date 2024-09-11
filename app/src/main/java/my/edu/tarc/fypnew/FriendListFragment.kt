package my.edu.tarc.fypnew

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.fypnew.databinding.FragmentCheckAppliedFraagmentBinding
import my.edu.tarc.fypnew.databinding.FragmentFriendListBinding


class FriendListFragment : Fragment() {
    private lateinit var binding: FragmentFriendListBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var friendListAdapter: FriendListAdapter
    private val volunteers = arrayListOf<Activity.Volunteer>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFriendListBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        friendListAdapter = FriendListAdapter(volunteers)
        binding.recycleViewFriendList.layoutManager = LinearLayoutManager(context)
        binding.recycleViewFriendList.adapter = friendListAdapter

        val userId = auth.currentUser?.uid ?: return // Get the current user's ID from FirebaseAuth
        fetchFriends(userId)

        binding.buttonAddFriend.setOnClickListener {
            val friendRequestDialog = AddFriendPopUp()
            val args = Bundle()
            args.putString("userId", userId) // Ensure userId is set here
            friendRequestDialog.arguments = args
            friendRequestDialog.show(parentFragmentManager, "FriendRequestDialogFragment")
        }

        binding.imageViewLeaderBoard.setOnClickListener{
            val args = Bundle().apply {
                putString("userId", userId)
            }
            findNavController().navigate(R.id.action_friendListFragment_to_leaderBoardFragment, args)
        }


    }

    private fun fetchFriends(userId: String) {
        firestore.collection("volunteers").document(userId).get()
            .addOnSuccessListener { document ->
                val listOfFriends = document.get("friendList") as? List<String> ?: return@addOnSuccessListener
                val newFriends = arrayListOf<Activity.Volunteer>()
                listOfFriends.forEach { volunteerId ->
                    fetchFriendDetails(volunteerId) { volunteer ->
                        newFriends.add(volunteer)
                        friendListAdapter.updateFriends(newFriends)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error fetching friends: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchFriendDetails(friendId: String, callback: (Activity.Volunteer) -> Unit) {
        firestore.collection("volunteers").document(friendId).get()
            .addOnSuccessListener { document ->
                val friend = document.toObject(Activity.Volunteer::class.java) ?: Activity.Volunteer()
                callback(friend)
            }
            .addOnFailureListener {
                callback(Activity.Volunteer())
            }
    }
}