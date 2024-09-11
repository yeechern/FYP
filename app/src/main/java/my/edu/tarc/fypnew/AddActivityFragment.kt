package my.edu.tarc.fypnew

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.fypnew.databinding.FragmentAddActivityBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class AddActivityFragment : Fragment(),TagPopupFragment.TagPopupListener {
    private lateinit var binding: FragmentAddActivityBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var startTimePickerDialog: TimePickerDialog
    private lateinit var endTimePickerDialog: TimePickerDialog
    private var selectedDate: String? = null
    private var startTime:String? = null
    private var endTime:String? =null
    private var selectedTags = mutableListOf<String>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentAddActivityBinding.inflate(layoutInflater)
        val view = binding.root
        auth = FirebaseAuth.getInstance()
        initDatePicker()
        initTimePickers()

        binding.buttonChooseDate.setOnClickListener {
            openDatePicker()
        }

        binding.buttonStartTime.setOnClickListener {
            openStartTimePicker()
        }

        binding.buttonEndTime.setOnClickListener {
            openEndTimePicker()
        }

        binding.buttonPostActivity.setOnClickListener {
            postActivity()
        }

        binding.toggleButtonVirtual.setOnClickListener{
            binding.toggleButtonPhysical.isChecked = false
        }

        binding.toggleButtonPhysical.setOnClickListener{
            binding.toggleButtonVirtual.isChecked = false
        }

        binding.buttonAddTag.setOnClickListener {
            val tagPopupFragment = TagPopupFragment()
            tagPopupFragment.setTagPopupListener(this)
            tagPopupFragment.show(parentFragmentManager,"TagPopupFragment")
        }





        return view
    }


    private fun postActivity() {
        val activityName = binding.editTextActivityName.text.toString().trim()
        val location = binding.editTextTextPostalAddress.text.toString().trim()
        val description = binding.editTextDescription.text.toString().trim()
        val style: String
        val personNeed = binding.editTextNumberNeed.text.toString().trim()
        val stateNumber = binding.spinnerStateAdd.selectedItemPosition
        val state = when (stateNumber) {
            1 -> "Johor"
            2 -> "Kedah"
            3 -> "Kelantan"
            4 -> "Melaka"
            5 -> "Negeri Sembilan"
            6 -> "Pahang"
            7 -> "Penang"
            8 -> "Perak"
            9 -> "Perlis"
            10 -> "Sabah"
            11 -> "Sarawak"
            12 -> "Selangor"
            13 -> "Terengganu"
            14 -> "Kuala Lumpur"
            15 -> "Labuan"
            16 -> "Putrajaya"
            else -> ""
        }

        if (activityName.isEmpty()) {
            showToast("Please enter the activity name")
            return
        }

        if (location.isEmpty()) {
            showToast("Please enter the location")
            return
        }

        if (description.isEmpty()) {
            showToast("Please enter a description")
            return
        }

        if (selectedDate == null) {
            showToast("Please select a date")
            return
        }

        if (startTime == null) {
            showToast("Please select the start time")
            return
        }

        if (endTime == null) {
            showToast("Please select the end time")
            return
        }

        if (selectedTags.isEmpty()) {
            showToast("Please select at least one tag")
            return
        }

        if (state.isEmpty()){
            showToast("Please select state of your location")
            return
        }

        if(binding.toggleButtonPhysical.isChecked) {
            style = "Physical"
        }else if(binding.toggleButtonVirtual.isChecked){
            style = "Virtual"
        }else{
            showToast("Please select a type")
            return
        }
        val currentUserUid = auth.currentUser?.uid
        val status = "ongoing"

        val startTimeParts = startTime!!.split(":")
        val endTimeParts = endTime!!.split(":")

        val startHour = startTimeParts[0].toInt()
        val startMinute = startTimeParts[1].toInt()
        val endHour = endTimeParts[0].toInt()
        val endMinute = endTimeParts[1].toInt()

        val startCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, startMinute)
        }

        val endCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, endHour)
            set(Calendar.MINUTE, endMinute)
        }

        if (endCalendar.before(startCalendar)) {
            showToast("End time cannot be earlier than start time")
            return
        }

        // Check if user is logged in
        currentUserUid?.let { uid ->
            // Create activity data map
            val activity = hashMapOf(
                "name" to activityName,
                "location" to location,
                "description" to description,
                "style" to style,
                "date" to selectedDate,
                "startTime" to startTime,
                "endTime" to endTime,
                "tags" to selectedTags,
                "listOfVolunteers" to emptyList<String>(),
                "organizerId" to uid,
                "personNeed" to personNeed,
                "state" to state.trim(),
                "status" to status,
                "fileUrls" to emptyList<Activity.FileUrl>()
            )

            // Add activity to Firestore under the 'activities' collection
            firestore.collection("activities")
                .add(activity)
                .addOnSuccessListener { documentReference ->
                    val activityId = documentReference.id
                    val userId = auth.currentUser?.uid

                    // Update the organizer's document to include this activity ID in the 'postedActivities' list
                    firestore.collection("organizers")
                        .document(uid)
                        .update("postedActivityList", FieldValue.arrayUnion(activityId))
                        .addOnSuccessListener {
                            showToast("Activity posted successfully")
                            findNavController().navigate(R.id.action_addActivityFragment_to_organizerProfileFragment,Bundle().apply {
                                putString("userId", userId)
                            })
                            clearFields()
                        }
                        .addOnFailureListener { e ->
                            showToast("Failed to update organizer's posted activities: ${e.message}")
                        }
                }
                .addOnFailureListener { e ->
                    showToast("Failed to post activity: ${e.message}")
                }
        } ?: run {
            showToast("User not logged in")
        }
    }

    private fun clearFields() {
        binding.editTextActivityName.text.clear()
        binding.editTextTextPostalAddress.text.clear()
        binding.editTextDescription.text.clear()
        binding.buttonChooseDate.text = "Choose Date"
        binding.buttonStartTime.text = "Start Time"
        binding.buttonEndTime.text = "End Time"
        binding.toggleButtonVirtual.isChecked = false
        binding.toggleButtonPhysical.isChecked = false
        binding.textViewTag1.text = ""
        binding.textViewTag2.text = ""
        binding.textViewTag3.text = ""
        binding.editTextNumberNeed.text.clear()
        binding.spinnerStateAdd.setSelection(0)
        selectedTags.clear()
    }


    private fun initDatePicker() {
        // DatePickerDialog.OnDateSetListener
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            val formattedDate = makeDateString(day, month + 1, year)
            selectedDate = formattedDate
            binding.buttonChooseDate.text = formattedDate
        }

        // Get the current date
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Create the DatePickerDialog
        datePickerDialog = DatePickerDialog(
            requireContext(),
            AlertDialog.THEME_HOLO_LIGHT,
            dateSetListener,
            year,
            month,
            day
        )
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
    }

    private fun openDatePicker() {
        datePickerDialog.show()
    }

    private fun makeDateString(day: Int, month: Int, year: Int): String {
        return "${getMonthFormat(month)} $day, $year"
    }

    private fun getMonthFormat(month: Int): String {
        val monthNames = arrayOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")
        return monthNames[month - 1]
    }

    private fun initTimePickers(){
        val startTimeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            startTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
            binding.buttonStartTime.text = startTime
        }

        val endTimeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            endTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
            binding.buttonEndTime.text = endTime
        }

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        startTimePickerDialog = TimePickerDialog(
            requireContext(),
            AlertDialog.THEME_HOLO_LIGHT,
            startTimeSetListener,
            hour,
            minute,
            true
        )

        endTimePickerDialog = TimePickerDialog(
            requireContext(),
            AlertDialog.THEME_HOLO_LIGHT,
            endTimeSetListener,
            hour,
            minute,
            true
        )

    }

    private fun openStartTimePicker() {
        startTimePickerDialog.show()
    }

    private fun openEndTimePicker() {
        endTimePickerDialog.show()
    }

    override fun onTagSelected(selectedTags: List<String>) {
        this.selectedTags.clear()
        this.selectedTags.addAll(selectedTags)
        updateTagTextViews()
    }

    private fun updateTagTextViews() {
        val tagTextViews = listOf(binding.textViewTag1, binding.textViewTag2, binding.textViewTag3)
        for (i in tagTextViews.indices) {
            if (i < selectedTags.size) {
                tagTextViews[i].text = selectedTags[i]
            } else {
                tagTextViews[i].text = ""
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }



}