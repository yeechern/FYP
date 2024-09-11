package my.edu.tarc.fypnew

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.fypnew.databinding.FragmentHome2Binding
import my.edu.tarc.fypnew.databinding.FragmentVolunteerProfileBinding


class HomeFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: FragmentHome2Binding
    private var userId: String? = null
    private lateinit var userType: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        userId = auth.currentUser?.uid

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentHome2Binding.inflate(layoutInflater)
        val view = binding.root

        userId?.let {
            checkUserTypeAndFetchData(it)
        } ?: run {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
        }

        binding.buttonOneClickSearch.setOnClickListener {
            userId?.let {
                fetchVolunteerDataForPopup(it)
            }
        }




        return view

    }

    private fun checkUserTypeAndFetchData(userId: String) {
        // Check if the user is an organizer
        firestore.collection("organizers").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    userType = "organizer"
                    setBackgroundBasedOnUserType()
                    fetchOrganizerData(userId)
                } else {
                    // Check if the user is a volunteer
                    firestore.collection("volunteers").document(userId).get()
                        .addOnSuccessListener { doc ->
                            if (doc.exists()) {
                                userType = "volunteer"
                                setBackgroundBasedOnUserType()
                                fetchVolunteerData(userId)
                            } else {
                                Toast.makeText(context, "User data not found", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error fetching volunteer data: ${e.message}", Toast.LENGTH_SHORT).show()
                            Log.e("HomeFragment", "Error fetching volunteer data", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error fetching organizer data: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("HomeFragment", "Error fetching organizer data", e)
            }
    }

    private fun setBackgroundBasedOnUserType() {
        val backgroundColor = when (userType) {
            "organizer" -> R.color.orgaPurple
            "volunteer" -> R.color.cyan
            else -> R.color.cyan
        }
        binding.root.setBackgroundColor(ContextCompat.getColor(requireContext(), backgroundColor))
    }

    private fun fetchOrganizerData(userId: String) {
        firestore.collection("organizers").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name")
                    binding.textViewHomeName.text = name
                    binding.buttonOneClickSearch.visibility = View.GONE
                    binding.cardViewApplied.visibility = View.GONE
                    binding.cardViewHours.visibility = View.GONE
                    binding.textView36.visibility = View.GONE
                    binding.textView38.visibility = View.GONE
                    binding.textView27.visibility = View.GONE
                } else {
                    Toast.makeText(context, "No such organizer found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error fetching organizer data: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("HomeFragment", "Error fetching organizer data", e)
            }
    }

    private fun fetchVolunteerData(userId: String) {
        firestore.collection("volunteers").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name")
                    val hours = document.getLong("volunteerHours")
                    val activityCompleted = document.get("completedActivityList")as? List<String> ?: emptyList()
                    val activityCompletedCount = activityCompleted.size


                    binding.textViewHomeName.text = name
                    binding.textViewHomeHours.text = hours.toString()
                    binding.textViewHomeCompleted.text = activityCompletedCount.toString()


                } else {
                    Toast.makeText(context, "No such volunteer found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error fetching volunteer data: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("HomeFragment", "Error fetching volunteer data", e)
            }
    }

    private fun fetchVolunteerDataForPopup(userId: String) {
        firestore.collection("volunteers").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val state = document.getString("state")
                    val tags = document.getString("tags")?.split(",") ?: emptyList()
                    showSuitablePopUp(state, tags)
                } else {
                    Toast.makeText(context, "No such volunteer found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error fetching volunteer data: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("HomeFragment", "Error fetching volunteer data", e)
            }
    }

    private fun showSuitablePopUp(state: String?, tags: List<String>) {
        val suitablePopUpFragment = suitablePopUpFragment.newInstance(state, tags)
        suitablePopUpFragment.show(childFragmentManager, "suitablePopUpFragment")
    }



}