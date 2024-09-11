package my.edu.tarc.fypnew

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.fypnew.databinding.FragmentSuitablePopUpBinding



class suitablePopUpFragment : DialogFragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: FragmentSuitablePopUpBinding
    private lateinit var physicalAdapter: VolunteerActivityAdapter
    private lateinit var virtualAdapter: VirtualActivityAdapter
    private var userId: String? = null
    private var physicalActivities = arrayListOf<Activity>()
    private var virtualActivities = arrayListOf<Activity>()
    private var userFriends = arrayListOf<String>()
    private var isVolunteer: Boolean = true


    companion object {
        private const val ARG_STATE = "state"
        private const val ARG_TAGS = "tags"

        fun newInstance(state: String?, tags: List<String>): suitablePopUpFragment {
            val fragment = suitablePopUpFragment()
            val args = Bundle()
            args.putString(ARG_STATE, state)
            args.putStringArrayList(ARG_TAGS, ArrayList(tags))
            fragment.arguments = args
            return fragment
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSuitablePopUpBinding.inflate(inflater, container, false)
        val view = binding.root

        userId = auth.currentUser?.uid

        physicalAdapter = VolunteerActivityAdapter(physicalActivities, userFriends, isVolunteer, userId.toString()) { activity ->
            showActivityDetails(activity)
        }

        virtualAdapter = VirtualActivityAdapter(virtualActivities, userId.toString()) { activity ->
            showActivityDetails(activity)
        }

        binding.recyclerViewPhysicalActivities.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewPhysicalActivities.adapter = physicalAdapter

        binding.recyclerViewVirtualActivities.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewVirtualActivities.adapter = virtualAdapter


        binding.buttonPhysical.setOnClickListener {
            showPhysicalActivities()
        }

        binding.buttonVirtual.setOnClickListener {
            showVirtualActivities()
        }

        val state = arguments?.getString(ARG_STATE)
        val tags = arguments?.getStringArrayList(ARG_TAGS) ?: emptyList<String>()

        verifyUserRole {
            fetchUserFriends {
                fetchSuitableActivities(state, tags)
            }
        }


        return view
    }


    //maybe no need
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

    private fun fetchSuitableActivities(state: String?, tags: List<String>) {
        Log.d("SuitablePopUpFragment", "Fetching activities for state: $state, tags: $tags")

        firestore.collection("activities")
            .whereEqualTo("status", "ongoing")
            .get()
            .addOnSuccessListener { result ->
                physicalActivities.clear()
                virtualActivities.clear()
                val stateActivities = mutableListOf<Activity>()
                val tagAndStateMatchedActivities = mutableListOf<Activity>()
                Log.d("SuitablePopUpFragment", "Fetched ${result.size()} activities")

                result.mapNotNull { document ->
                    try {
                        val activityMap = document.data
                        val date = (activityMap["date"] as? com.google.firebase.Timestamp)?.toDate()?.toString() ?: ""
                        val activity = Activity(
                            id = document.id,
                            name = activityMap["name"] as? String ?: "",
                            description = activityMap["description"] as? String ?: "",
                            date = date,
                            organizerId = activityMap["organizerId"] as? String ?: "",
                            tags = activityMap["tags"] as? List<String> ?: emptyList(),
                            style = activityMap["style"] as? String ?: "",
                            startTime = activityMap["startTime"] as? String ?: "",
                            endTime = activityMap["endTime"] as? String ?: "",
                            personNeed = activityMap["personNeed"] as? String ?: "",
                            location = activityMap["location"] as? String ?: "",
                            state = activityMap["state"] as? String ?: "",
                            status = activityMap["status"] as? String ?: "",
                            listOfVolunteers = activityMap["listOfVolunteers"] as? List<String> ?: emptyList()
                        )
                        Log.d("SuitablePopUpFragment", "Activity fetched: ${activity.name}, Tags: ${activity.tags}, Style: ${activity.style}")

                        if (activity.style.equals("physical", ignoreCase = true)) {
                            if (activity.state == state && tags.any { it.trim() in activity.tags }) {
                                tagAndStateMatchedActivities.add(activity)
                            } else if (activity.state == state) {
                                stateActivities.add(activity)
                            }
                            Log.d("SuitablePopUpFragment", "Added physical activity: ${activity.name}")
                        } else if (activity.style.equals("virtual", ignoreCase = true) && tags.any { it.trim() in activity.tags }) {
                            virtualActivities.add(activity)
                            Log.d("SuitablePopUpFragment", "Added virtual activity: ${activity.name}")
                        } else {
                            Log.d("SuitablePopUpFragment", "Added unknown activity: ${activity.name}")
                        }
                    } catch (e: Exception) {
                        Log.e("SuitablePopUpFragment", "Error converting document to Activity", e)
                        null
                    }
                }

                physicalActivities.addAll(tagAndStateMatchedActivities)
                if (physicalActivities.isEmpty()) {
                    physicalActivities.addAll(stateActivities)
                }

                if (physicalActivities.isEmpty() && virtualActivities.isEmpty()) {
                    Toast.makeText(context, "No suitable activities found", Toast.LENGTH_SHORT).show()
                }

                Log.d("SuitablePopUpFragment", "Physical Activities: ${physicalActivities.size}, Virtual Activities: ${virtualActivities.size}")
                physicalAdapter.notifyDataSetChanged()
                virtualAdapter.notifyDataSetChanged()
                showPhysicalActivities() // Show physical activities by default
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error fetching activities: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("SuitablePopUpFragment", "Error fetching activities", e)
            }
    }

    private fun showPhysicalActivities() {
        binding.recyclerViewPhysicalActivities.visibility = View.VISIBLE
        binding.recyclerViewVirtualActivities.visibility = View.INVISIBLE
    }

    private fun showVirtualActivities() {
        binding.recyclerViewPhysicalActivities.visibility = View.INVISIBLE
        binding.recyclerViewVirtualActivities.visibility = View.VISIBLE
    }

    private fun showActivityDetails(activity: Activity) {
        val bundle = Bundle().apply {
            putParcelable("activity", activity)
            putStringArrayList("friends", ArrayList(userFriends.filter { activity.listOfVolunteers.contains(it) }))
        }

        if(activity.style=="Physical".trim()){
            findNavController().navigate(R.id.detailsVolunteerActivity, bundle)
        }else{
            findNavController().navigate(R.id.detailsVirtualActivity, bundle)
        }


    }


}