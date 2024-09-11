package my.edu.tarc.fypnew

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.fypnew.databinding.FragmentLeaderBoardBinding


class LeaderBoardFragment : Fragment() {
    private lateinit var binding: FragmentLeaderBoardBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var leaderBoardAdapter: LeaderBoardAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLeaderBoardBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = auth.currentUser?.uid ?: return

        leaderBoardAdapter = LeaderBoardAdapter(emptyList())
        binding.recyclerViewLeaderBoard.adapter = leaderBoardAdapter
        binding.recyclerViewLeaderBoard.layoutManager = LinearLayoutManager(context)


        binding.buttonLeadHours.isEnabled = false  // Assuming VOLUNTEER_HOURS is the default criterion
        binding.buttonLeadComplete.isEnabled = true
        leaderBoardAdapter.setSortCriterion(LeaderBoardAdapter.SortCriterion.VOLUNTEER_HOURS)

        binding.buttonLeadHours.setOnClickListener {
            binding.buttonLeadHours.isEnabled = false
            binding.buttonLeadComplete.isEnabled = true
            leaderBoardAdapter.setSortCriterion(LeaderBoardAdapter.SortCriterion.VOLUNTEER_HOURS)
        }

        binding.buttonLeadComplete.setOnClickListener {
            binding.buttonLeadHours.isEnabled = true
            binding.buttonLeadComplete.isEnabled = false
            leaderBoardAdapter.setSortCriterion(LeaderBoardAdapter.SortCriterion.COMPLETED_ACTIVITIES)
        }

        fetchFriends(userId)
    }

    private fun fetchFriends(userId: String) {
        firestore.collection("volunteers").document(userId).get()
            .addOnSuccessListener { document ->
                val listOfFriends = document.get("friendList") as? List<String> ?: return@addOnSuccessListener
                val newFriends = arrayListOf<Activity.Volunteer>()
                fetchFriendDetails(userId) { user ->
                    newFriends.add(user)

                    var fetchedCount = 0
                    listOfFriends.forEach { volunteerId ->
                        fetchFriendDetails(volunteerId) { volunteer ->
                            newFriends.add(volunteer)
                            fetchedCount++
                            if (fetchedCount == listOfFriends.size) {
                                leaderBoardAdapter.updateVolunteers(newFriends)
                            }
                        }
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
                Log.d("LeaderBoardFragment", "Fetched document: ${document.data}")
                val friend = document.toObject(Activity.Volunteer::class.java) ?: Activity.Volunteer()
                Log.d("LeaderBoardFragment", "Fetched volunteer: ${friend.name} with hours: ${friend.volunteerHours}")
                callback(friend)
            }
            .addOnFailureListener {
                Log.e("LeaderBoardFragment", "Error fetching volunteer details: ${it.message}")
                callback(Activity.Volunteer())
            }
    }
}




