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
import my.edu.tarc.fypnew.databinding.FragmentOnGoActivityBinding
import my.edu.tarc.fypnew.databinding.FragmentVolunteerActivityBinding


class onGoActivity : Fragment() {
    private lateinit var binding: FragmentOnGoActivityBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var onGoActivityAdapter: onGoActivityAdapter
    private lateinit var auth: FirebaseAuth
    private var userId: String? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOnGoActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.RecyclerViewOnGo.layoutManager = LinearLayoutManager(context)

        onGoActivityAdapter = onGoActivityAdapter(arrayListOf()) { activity ->
            showActivityDetails(activity)
        }

        binding.RecyclerViewOnGo.adapter = onGoActivityAdapter

        fetchPostedActivities()
    }

    private fun fetchPostedActivities() {
        userId?.let { uid ->
            firestore.collection("activities")
                .whereIn("organizerId", listOf(uid))
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
                            Log.d("onGoActivity", "Fetched activity: $activity")
                            activity
                        } catch (e: Exception) {
                            Log.e("onGoActivity", "Error converting document to Activity", e)
                            null
                        }
                    }
                    onGoActivityAdapter.updateActivities(ArrayList(activities))
                }
                .addOnFailureListener { exception ->
                    Log.e("onGoActivity", "Error fetching activities", exception)
                }
        }
    }

    private fun showActivityDetails(activity: Activity) {
        val bundle = Bundle().apply {
            putParcelable("activity", activity)
        }

        if(activity.style=="Physical".trim()){
            findNavController().navigate(R.id.action_onGoActivity_to_detailsVolunteerActivity, bundle)
        }else{
            findNavController().navigate(R.id.action_onGoActivity_to_detailsVirtualActivity, bundle)
        }

    }


}