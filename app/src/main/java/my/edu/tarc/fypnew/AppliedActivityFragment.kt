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
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.fypnew.databinding.FragmentAppliedActivityBinding
import my.edu.tarc.fypnew.databinding.FragmentOnGoActivityBinding


class AppliedActivityFragment : Fragment() {
    private lateinit var binding: FragmentAppliedActivityBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var appliedActivityAdapter: AppliedActivityAdapter
    private lateinit var auth: FirebaseAuth
    private var userId: String? = null
    private var appliedActivityList: List<String> = emptyList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid

        appliedActivityList = arguments?.getStringArrayList("appliedActivityList") ?: emptyList()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAppliedActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerViewApplied.layoutManager = LinearLayoutManager(context)

        appliedActivityAdapter = AppliedActivityAdapter(arrayListOf()) { activity ->
            showActivityDetails(activity)
        }

        binding.recyclerViewApplied.adapter = appliedActivityAdapter

        fetchAppliedActivities()
    }

    private fun fetchAppliedActivities() {
        if (appliedActivityList.isEmpty()) {
            Log.d("appliedActivity", "No applied activities found")
            appliedActivityAdapter.updateActivities(arrayListOf())
            return
        }

        firestore.collection("activities")
            .whereIn(FieldPath.documentId(), appliedActivityList)
            .whereEqualTo("status", "ongoing")
            .get()
            .addOnSuccessListener { result ->
                val activities = result.mapNotNull { document ->
                    try {
                        val activityMap = document.data
                        val activity = Activity(
                            id = document.id,
                            name = activityMap["name"] as? String ?: "",
                            description = activityMap["description"] as? String ?: "",
                            date = activityMap["date"] as? String ?: "",
                            organizerId = activityMap["organizerId"] as? String ?: "",
                            tags = activityMap["tags"] as? List<String> ?: emptyList(),
                            style = activityMap["style"] as? String ?: "",
                            startTime = activityMap["startTime"] as? String ?: "",
                            endTime = activityMap["endTime"] as? String ?: "",
                            personNeed = activityMap["personNeed"] as? String ?: "",
                            location = activityMap["location"] as? String ?: "",
                            listOfVolunteers = activityMap["listOfVolunteers"] as? List<String> ?: emptyList()
                        )
                        Log.d("appliedActivity", "Fetched activity: $activity")
                        activity
                    } catch (e: Exception) {
                        Log.e("appliedActivity", "Error converting document to Activity", e)
                        null
                    }
                }
                appliedActivityAdapter.updateActivities(ArrayList(activities))
            }
            .addOnFailureListener { exception ->
                Log.e("appliedActivity", "Error fetching activities", exception)
            }
    }

    private fun showActivityDetails(activity: Activity) {
        val bundle = Bundle().apply {
            putParcelable("activity", activity)
        }

        if(activity.style=="Physical".trim()){
            findNavController().navigate(R.id.action_appliedActivityFragment_to_detailsVolunteerActivity, bundle)
        }else{
            findNavController().navigate(R.id.action_appliedActivityFragment_to_detailsVirtualActivity, bundle)
        }


    }


}