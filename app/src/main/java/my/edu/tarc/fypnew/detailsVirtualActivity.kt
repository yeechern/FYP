package my.edu.tarc.fypnew

import android.content.Intent
import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import my.edu.tarc.fypnew.databinding.FragmentDetailsVirtualActivityBinding
import my.edu.tarc.fypnew.databinding.FragmentDetailsVolunteerActivityBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID


class detailsVirtualActivity : Fragment() {
    private lateinit var binding: FragmentDetailsVirtualActivityBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var selectedFileUri: Uri? = null
    private lateinit var storage: FirebaseStorage

    companion object {
        private const val PICK_FILE_REQUEST_CODE = 1000
    }


    private val fileAccessLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedFileUri = uri
                Log.d(TAG, "File selected: ${uri.path}")
                binding.textViewVDetailsUploaded.text = "File Selected"
                Toast.makeText(context, "File selected: ${uri.path}", Toast.LENGTH_SHORT).show()
            } ?: run {
                Log.d(TAG, "No file selected")
                Toast.makeText(context, "No file selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailsVirtualActivityBinding.inflate(inflater, container, false)
        val view= binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val activity =arguments?.getParcelable<Activity>("activity")
        activity?.let{
            val currentUser = auth.currentUser
            val currentUserId = currentUser?.uid

            if (currentUserId == it.organizerId){
                binding.buttonVMarkDone.visibility = View.VISIBLE
                binding.buttonVCheckUpload.visibility = View.VISIBLE
                binding.buttonVCheckApplied.visibility = View.VISIBLE
                binding.buttonVApply.visibility = View.GONE
                binding.buttonVDetailsCancel.visibility = View.GONE
                binding.buttonVDetailsSubmit.visibility = View.GONE
                binding.buttonVSelectFile.visibility = View.GONE
                binding.textView32.visibility = View.INVISIBLE

                binding.buttonVMarkDone.setOnClickListener {
                    markAsDone(activity.id)
                }

                binding.buttonVCheckUpload.setOnClickListener {
                    checkUploadedFile(activity.id)
                }

                binding.buttonVCheckApplied.setOnClickListener {
                    checkAppliedVolunteer(activity.id)
                }



            } else{
                currentUserId?.let { userId ->
                    firestore.collection("volunteers").document(userId).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                binding.buttonVMarkDone.visibility = View.GONE
                                binding.buttonVCheckUpload.visibility = View.GONE
                                binding.buttonVDetailsCancel.visibility = View.GONE
                                binding.buttonVDetailsCancel.isEnabled = false
                                binding.buttonVDetailsSubmit.visibility = View.GONE
                                binding.buttonVSelectFile.visibility = View.GONE
                                binding.buttonVApply.visibility = View.VISIBLE
                                binding.buttonVCheckApplied.visibility = View.GONE
                                binding.textView32.visibility = View.INVISIBLE

                                checkApplication(activity.id,userId)

                            } else {
                                binding.buttonVMarkDone.visibility = View.GONE
                                binding.buttonVCheckUpload.visibility = View.GONE
                                binding.buttonVDetailsCancel.visibility = View.GONE
                                binding.buttonVDetailsSubmit.visibility = View.GONE
                                binding.buttonVSelectFile.visibility = View.GONE
                                binding.buttonVApply.visibility = View.GONE
                                binding.buttonVCheckApplied.visibility = View.GONE
                                binding.textView32.visibility = View.INVISIBLE
                            }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(context, "Error checking volunteer data: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }

            val organizer = it.organizerId
            binding.textViewVDetailsTitle.text = it.name
            binding.textViewVDetailsDue.text = it.date
            binding.textViewVDetailsDescrib.text = it.description
            binding.textViewVDetailsTag1.text = activity.tags.getOrNull(0) ?: ""
            binding.textViewVDetailsTag2.text = activity.tags.getOrNull(1) ?: ""
            binding.textViewVDetailsTag3.text = activity.tags.getOrNull(2) ?: ""

            binding.buttonVSelectFile.setOnClickListener {
                selectFile()
            }

            binding.buttonVDetailsSubmit.setOnClickListener {
                selectedFileUri?.let { uri ->
                    uploadFileFromUri(uri, activity.id)
                } ?: run {
                    Toast.makeText(context, "No file selected", Toast.LENGTH_SHORT).show()
                }
            }

            fetchOrganizerData(organizer)
        }


    }



    private fun fetchOrganizerData(organizerId:String){
        firestore.collection("organizers").document(organizerId).get()
            .addOnSuccessListener { document ->
                if(document.exists()){
                    val orgaName = document.getString("name")?: "Unknown"
                    val email = document.getString("email") ?: "Unknown"

                    binding.textViewVDetailsOrganizer.text =orgaName
                    binding.textViewVDetailsOrgaEmail.text = email
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


    private fun checkApplication(activityId: String, userId: String) {
        firestore.collection("activities").document(activityId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val listOfVolunteers = document.get("listOfVolunteers") as? List<String>
                    if (listOfVolunteers != null && listOfVolunteers.contains(userId)) {
                        binding.buttonVApply.text = getString(R.string.applied)
                        binding.buttonVApply.isEnabled = false
                        binding.buttonVDetailsCancel.isEnabled = true
                        binding.buttonVDetailsSubmit.visibility = View.VISIBLE
                        binding.buttonVSelectFile.visibility = View.VISIBLE
                        binding.buttonVDetailsCancel.visibility = View.VISIBLE
                        binding.textView32.visibility = View.VISIBLE

                        binding.buttonVDetailsCancel.setOnClickListener {
                            cancelApplication(activityId, userId)
                        }

                    } else {
                        binding.buttonVApply.setOnClickListener {
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
                        binding.buttonVApply.text = getString(R.string.applied)
                        binding.buttonVApply.isEnabled = false
                        binding.buttonVDetailsCancel.isEnabled = true
                        binding.buttonVDetailsSubmit.visibility = View.VISIBLE
                        binding.buttonVSelectFile.visibility = View.VISIBLE
                        binding.buttonVDetailsCancel.visibility = View.VISIBLE
                        binding.textView32.visibility = View.VISIBLE
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
                        binding.buttonVApply.text = getString(R.string.apply)
                        binding.buttonVApply.isEnabled = true
                        binding.buttonVDetailsCancel.isEnabled = false
                        binding.buttonVDetailsSubmit.visibility = View.GONE
                        binding.buttonVSelectFile.visibility = View.GONE
                        binding.buttonVDetailsCancel.visibility = View.GONE

                        binding.buttonVApply.setOnClickListener {
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


    // upload file

    private fun selectFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        fileAccessLauncher.launch(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedFileUri = uri
                Log.d(TAG, "File selected: ${uri.path}")
                Toast.makeText(context, "File selected: ${uri.path}", Toast.LENGTH_SHORT).show()
            } ?: run {
                Log.d(TAG, "No file selected")
                Toast.makeText(context, "No file selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadFileFromUri(uri: Uri, activityId: String) {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        inputStream?.readBytes()?.let { byteArray ->
            uploadFile(byteArray, activityId)
        } ?: run {
            Toast.makeText(context, "Error reading file", Toast.LENGTH_SHORT).show()
        }
    }


    private fun uploadFile(fileBytes: ByteArray, activityId: String) {
        val storageRef = storage.reference
        val fileRef = storageRef.child("uploads/${UUID.randomUUID()}.pdf")

        fileRef.putBytes(fileBytes)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    val fileUrl = uri.toString()
                    saveFileUrlToFirestore(fileUrl, activityId)
                }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error retrieving file URL: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error uploading file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveFileUrlToFirestore(fileUrl: String, activityId: String) {
        val currentUser = auth.currentUser
        val currentUserId = currentUser?.uid ?: return
        val fileUpload = hashMapOf(
            "activityId" to activityId,
            "volunteerId" to currentUserId,
            "fileUrl" to fileUrl
        )
        firestore.collection("fileUpload").add(fileUpload)
            .addOnSuccessListener {
                firestore.collection("activities").document(activityId)
                    .update("fileUrls", FieldValue.arrayUnion(fileUrl))
                    .addOnSuccessListener {
                        Toast.makeText(context, "File uploaded and saved", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error saving file URL: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error uploading file record: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun checkUploadedFile(activityId: String) {
        val bundle = Bundle().apply {
            putString("activityId", activityId) // Pass the activity ID
        }
        findNavController().navigate(R.id.action_detailsVirtualActivity_to_checkUploadFragment, bundle)
    }

    private fun checkAppliedVolunteer(activityId: String) {
        val bundle = Bundle().apply {
            putString("activityId", activityId) // Pass the activity ID
        }
        findNavController().navigate(R.id.action_detailsVirtualActivity_to_checkAppliedFraagment, bundle)
    }






}