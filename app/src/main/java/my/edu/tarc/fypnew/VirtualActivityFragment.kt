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
import my.edu.tarc.fypnew.databinding.FragmentVirtualActivityBinding
import my.edu.tarc.fypnew.databinding.FragmentVolunteerActivityBinding


class VirtualActivityFragment : Fragment(), FilterPopupFragment.FilterPopupListener {
    private lateinit var binding: FragmentVirtualActivityBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var virtualActivityAdapter: VirtualActivityAdapter
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
        binding = FragmentVirtualActivityBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = auth.currentUser?.uid ?: return

        binding.recycleViewVActivity.layoutManager = LinearLayoutManager(context)


        binding.buttonVFilter.setOnClickListener {
            val filterPopupFragment = FilterPopupFragment()
            filterPopupFragment.setFilterPopupListener(this)
            filterPopupFragment.show(parentFragmentManager, "FilterPopupFragment")
        }

        virtualActivityAdapter = VirtualActivityAdapter(arrayListOf(), userId) { activity ->
            showActivityDetails(activity)
        }
        binding.recycleViewVActivity.adapter = virtualActivityAdapter

        fetchVirtualActivities()
    }

    override fun onFilterSelected(selectedTags: List<String>) {
        this.selectedTags = selectedTags
        fetchVirtualActivities()
    }

    private fun showActivityDetails(activity: Activity) {
        val bundle = Bundle().apply {
            putParcelable("activity", activity)
        }
        findNavController().navigate(R.id.action_virtualActivityFragment_to_detailsVirtualActivity, bundle)
    }

    private fun fetchVirtualActivities() {
        var query = firestore.collection("activities")
            .whereEqualTo("status","ongoing")
            .whereEqualTo("style","Virtual")
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
                virtualActivityAdapter.updateActivities(ArrayList(activities))
            }
            .addOnFailureListener { exception ->
                Log.e("VolunteerActivityFragment", "Error fetching activities", exception)
            }
    }



}