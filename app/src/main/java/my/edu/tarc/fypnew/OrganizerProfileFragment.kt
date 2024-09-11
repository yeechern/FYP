package my.edu.tarc.fypnew

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.fypnew.databinding.FragmentOrganizerProfileBinding
import my.edu.tarc.fypnew.databinding.FragmentVolunteerProfileBinding


class OrganizerProfileFragment : Fragment() {
    private lateinit var binding: FragmentOrganizerProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var userId: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        userId=arguments?.getString("userId")

        if (userId == null) {
            Toast.makeText(context, "No user ID provided", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_organizerProfileFragment_to_titleFragment)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentOrganizerProfileBinding.inflate(layoutInflater)
        val view = binding.root


        userId?.let{
            fetchOrganizerData(it)
        }

        binding.buttonOrganizerLogout.setOnClickListener {
            auth.signOut()
            findNavController().navigate(R.id.action_organizerProfileFragment_to_titleFragment)
        }

        binding.buttonAddActivity.setOnClickListener {
            findNavController().navigate(R.id.action_organizerProfileFragment_to_addActivityFragment)
        }

        binding.textViewActivitiesList.setOnClickListener {
            findNavController().navigate(R.id.action_organizerProfileFragment_to_onGoActivity)
        }

        return view
    }

    private fun fetchOrganizerData(userId: String){

        firestore.collection("organizers").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val email = document.getString("email")
                    val name = document.getString("name")
                    val contact = document.getString("contact")
                    val state = document.getString("state")
                    val postedActivities = document.get("postedActivityList") as? List<String> ?: emptyList()
                    val finishedActivities = document.get("finishedActivityList") as? List<String> ?: emptyList()
                    val postedActivityCount = postedActivities.size
                    val finishedActivityCount = finishedActivities.size

                    binding.textViewOrganizerProfileName.text = name
                    binding.textViewOrganizerProfileEmail.text = email
                    binding.textViewOrganizerProfileState.text = state
                    binding.textViewActivityPosted.text = postedActivityCount.toString()
                    binding.textViewOrganizerActivityFinish.text = finishedActivityCount.toString()


                    buildActivitiesText(postedActivities)



                } else {
                    Toast.makeText(context, "No such organizer found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to get volunteer data: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.w("VolunteerProfileFragment", "Error getting document", e)
            }
    }

    private fun buildActivitiesText(activityIds: List<String>) {
        if (activityIds.isEmpty()) {
            binding.textViewActivitiesList.text = "No activities created"
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
                    binding.textViewActivitiesList.text = "No ongoing activities found"
                    return@addOnSuccessListener
                }

                val activitiesText = StringBuilder()
                ongoingActivities.forEachIndexed { index, document ->
                    val style = document.getString("style") ?: "No style"
                    val name = document.getString("name") ?: "No name"
                    val date = document.getString("date") ?: "No date"
                    activitiesText.append("${index + 1}. $style - $name - $date\n")
                }

                binding.textViewActivitiesList.text = activitiesText.toString().trim()
            }
            .addOnFailureListener { e ->
                Log.e("OrganizerProfileFragment", "Error fetching activities", e)
                Toast.makeText(context, "Failed to fetch activities: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }






}


