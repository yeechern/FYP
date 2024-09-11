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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.fypnew.databinding.FragmentDetailsVolunteerActivityBinding
import my.edu.tarc.fypnew.databinding.FragmentVolunteerActivityBinding
import java.text.SimpleDateFormat
import java.util.Locale


class DetailsVolunteerActivity : Fragment() {
    private lateinit var binding: FragmentDetailsVolunteerActivityBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth:FirebaseAuth
    private lateinit var userFriends: ArrayList<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailsVolunteerActivityBinding.inflate(inflater, container, false)
        val view= binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity =arguments?.getParcelable<Activity>("activity")
        userFriends = arguments?.getStringArrayList("friends") ?: arrayListOf()
        activity?.let{
            val currentUser = auth.currentUser
            val currentUserId = currentUser?.uid

            if (currentUserId == activity.organizerId){
                binding.buttonOrganizerComplete.visibility = View.VISIBLE
                binding.buttonVolunteerApply.visibility=View.GONE
                binding.buttonCheckApplied.visibility =View.VISIBLE
                binding.buttonDetailsCancel.visibility = View.GONE
                binding.textViewAppliedFriends.visibility =View.GONE
                binding.textView43.visibility = View.GONE

                binding.buttonOrganizerComplete.setOnClickListener {
                    markAsDone(activity.id)
                }

                binding.buttonCheckApplied.setOnClickListener {
                    checkAppliedVolunteer(activity.id)
                }

            } else{
                currentUserId?.let { userId ->
                    firestore.collection("volunteers").document(userId).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                binding.buttonOrganizerComplete.visibility = View.GONE
                                binding.buttonVolunteerApply.visibility = View.VISIBLE
                                binding.textViewAppliedFriends.visibility =View.VISIBLE
                                binding.textView43.visibility = View.VISIBLE
                                binding.buttonCheckApplied.visibility =View.GONE
                                binding.buttonDetailsCancel.visibility = View.GONE
                                binding.buttonDetailsCancel.isEnabled = false

                                checkApplication(activity.id,userId)

                            } else {
                                binding.buttonOrganizerComplete.visibility = View.GONE
                                binding.buttonVolunteerApply.visibility = View.GONE
                                binding.textViewAppliedFriends.visibility =View.GONE
                                binding.textView43.visibility = View.GONE
                                binding.buttonCheckApplied.visibility =View.GONE
                                binding.buttonDetailsCancel.visibility = View.GONE
                            }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(context, "Error checking volunteer data: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }

            val organizer = it.organizerId
            binding.textViewDetailsTitle.text = it.name
            binding.textViewDetailsDate.text = it.date
            binding.textViewDetailsLocation.text = it.location
            binding.textViewDetailsDescribe.text=it.description
            binding.textViewDetailsStartTime.text =it.startTime
            binding.textViewDetailsEndTime.text = it.endTime
            binding.textViewDetailsStillNeed.text =it.personNeed
            binding.textViewDetailsState.text = it.state
            binding.textViewDetailsApplied.text =it.listOfVolunteers.size.toString()
            binding.textViewDetailsTag1.text = activity.tags.getOrNull(0) ?: ""
            binding.textViewDetailsTag2.text = activity.tags.getOrNull(1) ?: ""
            binding.textViewDetailsTag3.text = activity.tags.getOrNull(2) ?: ""

            fetchOrganizerData(organizer)
            buildFriendsText(userFriends)

        }
    }

    private fun fetchOrganizerData(organizerId:String){
        firestore.collection("organizers").document(organizerId).get()
            .addOnSuccessListener { document ->
                if(document.exists()){
                    val orgaName = document.getString("name")?: "Unknown"
                    val email = document.getString("email") ?: "Unknown"

                    binding.textViewDetailsOrgaName.text =orgaName
                    binding.textViewDetailsOrgaEmail.text = email
                }else {
                    Toast.makeText(context, "Organizer not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error fetching organizer data: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun markAsDone(activityId: String) {
        // Step 1: Change the status of the activity to "done"
        firestore.collection("activities").document(activityId)
            .update("status", "done")
            .addOnSuccessListener {
                // Step 2: Retrieve the organizer ID and volunteers from the activity
                firestore.collection("activities").document(activityId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            val organizerId = document.getString("organizerId")
                            val volunteers = document.get("listOfVolunteers") as? List<String> ?: emptyList()

                            if (organizerId != null) {
                                // Step 3: Update the organizer's completed activity list
                                firestore.collection("organizers").document(organizerId)
                                    .update("finishedActivityList", FieldValue.arrayUnion(activityId))
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Activity marked as done", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Error adding activity to organizer's completed list: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(context, "Organizer ID not found", Toast.LENGTH_SHORT).show()
                            }

                            // Step 4: Update each volunteer's completed activity list and volunteer hours
                            for (volunteerId in volunteers) {
                                updateVolunteerData(volunteerId, activityId)
                            }
                        } else {
                            Toast.makeText(context, "Activity document not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error retrieving activity: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error marking activity as done: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkApplication(activityId: String, userId: String) {
        firestore.collection("activities").document(activityId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val listOfVolunteers = document.get("listOfVolunteers") as? List<String>
                    val personNeed = document.getString("personNeed")?.toIntOrNull() ?: 0
                    if (listOfVolunteers != null && listOfVolunteers.contains(userId)) {
                        binding.buttonVolunteerApply.text = getString(R.string.applied)
                        binding.buttonVolunteerApply.isEnabled = false
                        binding.buttonDetailsCancel.isEnabled = true
                        binding.buttonDetailsCancel.visibility = View.VISIBLE


                        binding.buttonDetailsCancel.setOnClickListener {
                            cancelApplication(activityId, userId)
                        }
                    }else if (listOfVolunteers != null && listOfVolunteers.size >= personNeed) {
                        binding.buttonVolunteerApply.text = getString(R.string.full)
                        binding.buttonVolunteerApply.isEnabled = false
                    } else {
                        binding.buttonVolunteerApply.setOnClickListener {
                            applyForActivity(activityId, userId)
                        }
                    }
                } else {
                    Toast.makeText(context, "Activity not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error checking activity: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun applyForActivity(activityId: String, userId: String) {
        firestore.collection("activities").document(activityId)
            .update("listOfVolunteers", FieldValue.arrayUnion(userId))
            .addOnSuccessListener {
                firestore.collection("volunteers").document(userId)
                    .update("appliedActivityList", FieldValue.arrayUnion(activityId))
                    .addOnSuccessListener {
                        binding.buttonVolunteerApply.text = getString(R.string.applied)
                        binding.buttonVolunteerApply.isEnabled = false
                        binding.buttonDetailsCancel.isEnabled = true
                        binding.buttonDetailsCancel.visibility = View.VISIBLE
                        Toast.makeText(context, "Applied for activity", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error updating volunteer's applied activity list: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error applying for activity: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cancelApplication(activityId: String, userId: String) {
        firestore.collection("activities").document(activityId)
            .update("listOfVolunteers", FieldValue.arrayRemove(userId))
            .addOnSuccessListener {
                firestore.collection("volunteers").document(userId)
                    .update("appliedActivityList", FieldValue.arrayRemove(activityId))
                    .addOnSuccessListener {
                        binding.buttonVolunteerApply.text = getString(R.string.apply)
                        binding.buttonVolunteerApply.isEnabled = true
                        binding.buttonDetailsCancel.isEnabled = false
                        binding.buttonDetailsCancel.visibility = View.GONE

                        binding.buttonVolunteerApply.setOnClickListener {
                            applyForActivity(activityId, userId)
                        }

                        Toast.makeText(context, "Application canceled", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error updating volunteer's applied activity list: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error removing user from activity: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun buildFriendsText(userFriends: List<String>) {
        if (userFriends.isEmpty()) {
            binding.textViewAppliedFriends.text = "No friends applied"
            return
        }

        firestore.collection("volunteers")
            .whereIn(FieldPath.documentId(), userFriends)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    binding.textViewAppliedFriends.text = "No friends found"
                    return@addOnSuccessListener
                }

                val friendsText = StringBuilder()
                documents.forEachIndexed { index, document ->
                    val name = document.getString("name") ?: "No name"
                    friendsText.append("${index + 1}. $name ")
                }

                binding.textViewAppliedFriends.text = friendsText.toString().trim()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to fetch friends: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }




    private fun checkAppliedVolunteer(activityId: String) {
        val bundle = Bundle().apply {
            putString("activityId", activityId) // Pass the activity ID
        }
        findNavController().navigate(R.id.action_detailsVolunteerActivity_to_checkAppliedFraagment, bundle)
    }

    private fun updateVolunteerData(volunteerId: String, activityId: String) {
        firestore.collection("volunteers").document(volunteerId)
            .update("completedActivityList", FieldValue.arrayUnion(activityId))
            .addOnSuccessListener {
                Log.d("LeaderBoardFragment", "Volunteer $volunteerId's completed activities updated successfully")
                calculateTotalVolunteerHours(volunteerId)
            }
            .addOnFailureListener { e ->
                Log.e("LeaderBoardFragment", "Error updating volunteer's completed activities", e)
            }
    }

    private fun calculateTotalVolunteerHours(volunteerId: String) {
        firestore.collection("volunteers").document(volunteerId).get()
            .addOnSuccessListener { document ->
                val completedActivityIds = document.get("completedActivityList") as? List<String> ?: return@addOnSuccessListener

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
                                    Log.e("LeaderBoardFragment", "Error parsing time", e)
                                }
                            }
                        }

                        firestore.collection("volunteers").document(volunteerId)
                            .update("volunteerHours", totalHours)
                            .addOnSuccessListener {
                                Log.d("LeaderBoardFragment", "Volunteer hours updated successfully")
                            }
                            .addOnFailureListener { e ->
                                Log.e("LeaderBoardFragment", "Error updating volunteer hours", e)
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("LeaderBoardFragment", "Error fetching completed activities", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("LeaderBoardFragment", "Error fetching volunteer data", e)
            }
    }






}