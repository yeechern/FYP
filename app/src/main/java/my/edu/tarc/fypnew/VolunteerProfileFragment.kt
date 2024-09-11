package my.edu.tarc.fypnew

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.fypnew.databinding.FragmentVolunteerLoginBinding
import my.edu.tarc.fypnew.databinding.FragmentVolunteerProfileBinding
import java.text.SimpleDateFormat
import java.util.Locale


class VolunteerProfileFragment : Fragment(),EditVolunteerProfile.EditVolunteerProfileListener {
    private lateinit var binding: FragmentVolunteerProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var userId: String? = null
    private var currentState: String? =null
    private var currentTags: List<String> = emptyList()
    private var isUpdating = false // Flag to track if an update is in progress




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        userId=arguments?.getString("userId")

        if (userId == null) {
            Toast.makeText(context, "No user ID provided", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_volunteerProfileFragment_to_titleFragment)
        }

    }

    override fun onResume() {
        super.onResume()
        fetchVolunteerData(userId!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentVolunteerProfileBinding.inflate(layoutInflater)
        val view = binding.root

        userId?.let{
            fetchVolunteerData(it)
        }

        binding.buttonViewFriends.setOnClickListener {
            findNavController().navigate(R.id.action_volunteerProfileFragment_to_friendListFragment)
        }


        binding.buttonVolunteerLogout.setOnClickListener {
            auth.signOut()
            findNavController().navigate(R.id.action_volunteerProfileFragment_to_titleFragment)
        }

        binding.imageViewSetting.setOnClickListener {
            val editFragment = EditVolunteerProfile.newInstance(currentState,currentTags)
            editFragment.setEditVolunteerProfileListener(this)
            editFragment.show(parentFragmentManager,"EditVolunteerProfile")
        }

        binding.textViewAppliedActivity.setOnClickListener{
            val appliedActivityList = binding.textViewAppliedActivity.tag as? List<String> ?: emptyList()
            Log.d("AppliedActivity", "Applied Activity List: $appliedActivityList")
            val bundle = Bundle().apply {
                putStringArrayList("appliedActivityList", ArrayList(appliedActivityList))
            }
            findNavController().navigate(R.id.action_volunteerProfileFragment_to_appliedActivityFragment, bundle)
        }

        return view

    }

    private fun fetchVolunteerData(userId: String){

        firestore.collection("volunteers").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val email = document.getString("email")
                    val name = document.getString("name")
                    val contact = document.getString("contact")
                    val state = document.getString("state")
                    val volunteerHours = document.getLong("volunteerHours")
                    val activityCompleted = document.get("completedActivityList")as? List<String> ?: emptyList()
                    val activityCompletedCount = activityCompleted.size
                    val tags = document.getString("tags")?.split(",") ?: emptyList()
                    val friendList= document.get("firendList")as? List<String> ?: emptyList()
                    val appliedActivity = document.get("appliedActivityList")as? List<String>?:emptyList()

                    binding.textViewVolunteerProfileName.text = name
                    binding.textViewVolunteerProfileEmail.text = email
                    binding.textViewVolunteerProfileState.text = state
                    binding.textViewHours.text = volunteerHours.toString()
                    binding.textViewCompletedCount.text = activityCompletedCount.toString()
                    binding.textViewVProfileTag1.text = tags.getOrNull(0) ?: ""
                    binding.textViewVProfileTag2.text = tags.getOrNull(1) ?: ""
                    binding.textViewVProfileTag3.text = tags.getOrNull(2) ?: ""

                    currentState = state
                    currentTags = tags

                    buildActivitiesText(appliedActivity)

                    binding.textViewAppliedActivity.tag = appliedActivity

                    if (!isUpdating) {
                        isUpdating = true
                        updateCompletedActivities(userId, appliedActivity)
                    }


                } else {
                    Toast.makeText(context, "No such volunteer found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to get volunteer data: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.w("VolunteerProfileFragment", "Error getting document", e)
            }
    }

    override fun onTagSelected(selectedTags: List<String>) {
        // Handle the selected tags here
        val userData = mutableMapOf<String, Any>(
            "tags" to selectedTags.joinToString(", ")
        )
        updateVolunteerData(userData)
    }

    override fun onStateSelected(selectedState: String) {
        val userData = mutableMapOf<String, Any>(
            "state" to selectedState
        )
        updateVolunteerData(userData)
    }

    private fun updateVolunteerData(userData: Map<String, Any>) {
        userId?.let {id->
            firestore.collection("volunteers").document(id).update(userData)
                .addOnSuccessListener {
                    Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    fetchVolunteerData(id) // Refresh the data to reflect the changes
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.w("VolunteerProfileFragment", "Error updating document", e)
                }
        }
    }

    private fun buildActivitiesText(activityIds: List<String>) {
        if (activityIds.isEmpty()) {
            binding.textViewAppliedActivity.text = "No activities applied"
            return
        }

        firestore.collection("activities")
            .whereIn(FieldPath.documentId(), activityIds)
            .get()
            .addOnSuccessListener { documents ->
                val ongoingActivities = documents.filter { document ->
                    document.getString("status") == "ongoing"
                }.take(3) // Take the top three ongoing activities

                if (ongoingActivities.isEmpty()) {
                    binding.textViewAppliedActivity.text = "No ongoing activities found"
                    return@addOnSuccessListener
                }

                val activitiesText = StringBuilder()
                ongoingActivities.forEachIndexed { index, document ->
                    val style = document.getString("style") ?: "No style"
                    val name = document.getString("name") ?: "No name"
                    val date = document.getString("date") ?: "No date"
                    activitiesText.append("${index + 1}. $style - $name - $date\n")
                }

                binding.textViewAppliedActivity.text = activitiesText.toString().trim()
            }
            .addOnFailureListener { e ->
                Log.e("OrganizerProfileFragment", "Error fetching activities", e)
                Toast.makeText(context, "Failed to fetch activities: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun updateCompletedActivities(volunteerId: String, appliedActivityIds: List<String>) {
        if (appliedActivityIds.isEmpty()) {
            return
        }

        firestore.collection("activities")
            .whereIn(FieldPath.documentId(), appliedActivityIds)
            .whereEqualTo("status", "done")
            .get()
            .addOnSuccessListener { documents ->
                val completedActivityIds = documents.map { it.id }
                if (completedActivityIds.isNotEmpty()) {
                    firestore.collection("volunteers").document(volunteerId)
                        .update("completedActivityList", completedActivityIds)
                        .addOnSuccessListener {
                            Log.d("VolunteerProfileFragment", "Completed activities updated successfully")
                            calculateTotalVolunteerHours(volunteerId, completedActivityIds)
                        }
                        .addOnFailureListener { e ->
                            Log.e("VolunteerProfileFragment", "Error updating completed activities", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("VolunteerProfileFragment", "Error fetching applied activities with status done", e)
            }
    }

    private fun calculateTotalVolunteerHours(volunteerId: String, completedActivityIds: List<String>) {
        firestore.collection("activities")
            .whereIn(FieldPath.documentId(), completedActivityIds)
            .get()
            .addOnSuccessListener { documents ->
                var totalHours = 0L
                val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

                for (document in documents) {
                    val startTimeString = document.getString("startTime")
                    val endTimeString = document.getString("endTime")
                    if (startTimeString != null && endTimeString != null) {
                        try {
                            val startTime = dateFormat.parse(startTimeString)
                            val endTime = dateFormat.parse(endTimeString)
                            if (startTime != null && endTime != null) {
                                val difference = endTime.time - startTime.time
                                totalHours += difference / (1000 * 60 * 60)
                            }
                        } catch (e: Exception) {
                            Log.e("VolunteerProfileFragment", "Error parsing time", e)
                        }
                    }
                }

                firestore.collection("volunteers").document(volunteerId)
                    .update("volunteerHours", totalHours)
                    .addOnSuccessListener {
                        Log.d("VolunteerProfileFragment", "Volunteer hours updated successfully")
                        fetchVolunteerData(volunteerId) // Refresh the data to reflect the changes
                    }
                    .addOnFailureListener { e ->
                        Log.e("VolunteerProfileFragment", "Error updating volunteer hours", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("VolunteerProfileFragment", "Error fetching completed activities", e)
            }
    }




}