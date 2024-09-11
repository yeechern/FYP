package my.edu.tarc.fypnew

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.fypnew.databinding.FragmentAddActivityBinding
import my.edu.tarc.fypnew.databinding.FragmentVolunteerActivityBinding


class VolunteerActivityFragment : Fragment(), FilterPopupFragment.FilterPopupListener{
    private lateinit var binding: FragmentVolunteerActivityBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var volunteerActivityAdapter: VolunteerActivityAdapter
    private val userFriends = arrayListOf<String>()
    private var isVolunteer: Boolean = true
    private var selectedTags = listOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVolunteerActivityBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = auth.currentUser?.uid ?: return

        binding.recycleViewPActivity.layoutManager = LinearLayoutManager(context)

        binding.buttonFilter.setOnClickListener {
            val filterPopupFragment = FilterPopupFragment()
            filterPopupFragment.setFilterPopupListener(this)
            filterPopupFragment.show(parentFragmentManager, "FilterPopupFragment")
        }

        verifyUserRole {
            fetchUserFriends {
                volunteerActivityAdapter = VolunteerActivityAdapter(arrayListOf(), userFriends, isVolunteer, userId) { activity ->
                    showActivityDetails(activity)
                }
                binding.recycleViewPActivity.adapter = volunteerActivityAdapter
                fetchPhysicalActivities()
            }
        }
    }

    override fun onFilterSelected(selectedTags: List<String>) {
        this.selectedTags = selectedTags
        fetchPhysicalActivities()
    }

    private fun verifyUserRole(callback: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("volunteers").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    isVolunteer = true
                } else {
                    isVolunteer = false
                }
                callback()
            }
            .addOnFailureListener { exception ->
                Log.e("VolunteerActivityFragment", "Error verifying user role", exception)
                isVolunteer = false
                callback()
            }
    }

    private fun fetchUserFriends(callback: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("volunteers").document(userId).get()
            .addOnSuccessListener { document ->
                val friends = document.get("friendList") as? List<String> ?: emptyList()
                userFriends.clear()
                userFriends.addAll(friends)
                callback()
            }
            .addOnFailureListener { exception ->
                Log.e("VolunteerActivityFragment", "Error fetching user friends", exception)
                callback()
            }
    }

    private fun fetchPhysicalActivities() {
        var query = firestore.collection("activities")
            .whereEqualTo("status","ongoing")
            .whereEqualTo("style","Physical")
        if (selectedTags.isNotEmpty()) {
            query = query.whereArrayContainsAny("tags", selectedTags)
        }
        query.get()
            .addOnSuccessListener { result ->
                val activities = result.mapNotNull { document ->
                    try {
                        val activityMap = document.data
                        val activity = Activity(
                            id = document.id,
                            name = activityMap["name"] as? String ?: "",
                            description = activityMap["description"] as? String ?: "",
                            date = activityMap["date"] as? String?: "",
                            organizerId = activityMap["organizerId"] as? String ?: "",
                            tags = activityMap["tags"] as? List<String> ?: emptyList(),
                            style = activityMap["style"] as? String ?: "",
                            startTime = activityMap["startTime"] as? String ?: "",
                            endTime = activityMap["endTime"] as? String ?: "",
                            personNeed = activityMap["personNeed"] as? String ?: "",
                            location = activityMap["location"] as? String ?: "",
                            state = activityMap["state"] as? String?: "",
                            status = activityMap["status"] as? String?: "",
                            listOfVolunteers = activityMap["listOfVolunteers"] as? List<String> ?: emptyList()
                        )
                        Log.d("VolunteerActivityFragment", "Fetched activity: $activity")
                        activity
                    } catch (e: Exception) {
                        Log.e("VolunteerActivityFragment", "Error converting document to Activity", e)
                        null
                    }
                }
                // Update the adapter with new activities
                volunteerActivityAdapter.updateActivities(ArrayList(activities))
            }
            .addOnFailureListener { exception ->
                Log.e("VolunteerActivityFragment", "Error fetching activities", exception)
            }
    }



    private fun showActivityDetails(activity: Activity) {
        val bundle = Bundle().apply {
            putParcelable("activity", activity)
            putStringArrayList("friends", ArrayList(userFriends.filter { activity.listOfVolunteers.contains(it) }))
        }
        findNavController().navigate(R.id.action_volunteerActivityFragment_to_detailsVolunteerActivity, bundle)
    }
}