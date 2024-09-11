package my.edu.tarc.fypnew

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.fypnew.databinding.FragmentCheckAppliedFraagmentBinding


class CheckAppliedFraagment : Fragment() {
    private lateinit var binding: FragmentCheckAppliedFraagmentBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var checkAppliedAdapter: checkAppliedAdapter
    private val volunteers = arrayListOf<Activity.Volunteer>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCheckAppliedFraagmentBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkAppliedAdapter = checkAppliedAdapter(volunteers)
        binding.recycleViewVolunList.layoutManager = LinearLayoutManager(context)
        binding.recycleViewVolunList.adapter = checkAppliedAdapter

        val activityId = arguments?.getString("activityId") ?: return
        fetchAppliedVolunteers(activityId)
    }

    private fun fetchAppliedVolunteers(activityId: String) {
        firestore.collection("activities").document(activityId).get()
            .addOnSuccessListener { document ->
                val listOfVolunteers = document.get("listOfVolunteers") as? List<String> ?: return@addOnSuccessListener
                val newVolunteers = arrayListOf<Activity.Volunteer>()
                listOfVolunteers.forEach { volunteerId ->
                    fetchVolunteerDetails(volunteerId) { volunteer ->
                        newVolunteers.add(volunteer)
                        checkAppliedAdapter.updateVolunteers(newVolunteers)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error fetching volunteers: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchVolunteerDetails(volunteerId: String, callback: (Activity.Volunteer) -> Unit) {
        firestore.collection("volunteers").document(volunteerId).get()
            .addOnSuccessListener { document ->
                val volunteer = document.toObject(Activity.Volunteer::class.java) ?: Activity.Volunteer()
                callback(volunteer)
            }
            .addOnFailureListener {
                callback(Activity.Volunteer())
            }
    }


}