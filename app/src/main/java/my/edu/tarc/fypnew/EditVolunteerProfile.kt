package my.edu.tarc.fypnew

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import my.edu.tarc.fypnew.databinding.FragmentEditVolunteerProfileBinding


class EditVolunteerProfile : DialogFragment() {
    private lateinit var binding: FragmentEditVolunteerProfileBinding
    private var listener: EditVolunteerProfileListener? = null
    private var currentState: String? = null
    private var currentTags: List<String> = emptyList()

    interface EditVolunteerProfileListener{
        fun onTagSelected(selectedTags: List<String>)
        fun onStateSelected(selectedState: String)
    }

    fun setEditVolunteerProfileListener(listener:EditVolunteerProfileListener){
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentState = it.getString(ARG_CURRENT_STATE)
            currentTags = it.getStringArrayList(ARG_CURRENT_TAGS) ?: emptyList()
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditVolunteerProfileBinding.inflate(layoutInflater)
        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setSpinnerSelection(currentState)
        setTagSelection(currentTags)


        binding.buttonEditClearTag.setOnClickListener {
            binding.toggleButtonVideo.isChecked = false
            binding.toggleButtonAnimalShelter.isChecked = false
            binding.toggleButtonCaring.isChecked = false
            binding.toggleButtonCleanup.isChecked = false
            binding.toggleButtonCommunityService.isChecked = false
            binding.toggleButtonDesign.isChecked = false
            binding.toggleButtonDigital.isChecked = false
            binding.toggleButtonEnvironment.isChecked = false
            binding.toggleButtonNurseHouse.isChecked = false
            binding.toggleButtonOutdoor.isChecked = false
            binding.toggleButtonPlanting.isChecked = false
            binding.toggleButtonSchool.isChecked = false
            binding.toggleButtonOther.isChecked = false

            listener?.onTagSelected(emptyList())
        }

        binding.buttonConfirmEditTag.setOnClickListener {
            val selectedTags = getSelectedTags()
            if (selectedTags.size in 1..3) {
                listener?.onTagSelected(selectedTags)
                val stateNumber = binding.spinnerEditState.selectedItemPosition
                val state = getStateFromNumber(stateNumber)
                listener?.onStateSelected(state)
                dismiss()
            } else {
                Toast.makeText(context, "Please select 1-3 Tags", Toast.LENGTH_SHORT).show()
            }
        }



    }

    private fun setTagSelection(tags: List<String>) {
        // Debugging: Print the tags list
        Log.d("EditVolunteerProfile", "Tags received: $tags")

        // Ensure the list size does not exceed 3
        val tagList = tags.take(3).map{it.trim()}

        // Define a map for tag values to their corresponding toggle buttons
        val tagToButtonMap = mapOf(
            getString(R.string.typeVideoClip) to binding.toggleButtonVideo,
            getString(R.string.typeAnimalShelter) to binding.toggleButtonAnimalShelter,
            getString(R.string.typeCaring) to binding.toggleButtonCaring,
            getString(R.string.typeCleanup) to binding.toggleButtonCleanup,
            getString(R.string.typeCommunityService) to binding.toggleButtonCommunityService,
            getString(R.string.typeDesign) to binding.toggleButtonDesign,
            getString(R.string.typeDigital) to binding.toggleButtonDigital,
            getString(R.string.typeEnvironment) to binding.toggleButtonEnvironment,
            getString(R.string.typeNurseHouse) to binding.toggleButtonNurseHouse,
            getString(R.string.typeOutdoor) to binding.toggleButtonOutdoor,
            getString(R.string.typePlanting) to binding.toggleButtonPlanting,
            getString(R.string.typeSchool) to binding.toggleButtonSchool,
            getString(R.string.typeOther) to binding.toggleButtonOther
        )

        // Uncheck all buttons first
        tagToButtonMap.values.forEach { it.isChecked = false }

        // Check the corresponding toggle buttons for the tags
        tagList.forEach { tag ->
            val button = tagToButtonMap[tag]
            if (button != null) {
                button.isChecked = true
                Log.d("EditVolunteerProfile", "Button for tag $tag is set to checked")
            } else {
                Log.d("EditVolunteerProfile", "No button found for tag: $tag")
            }
        }
    }

    private fun setSpinnerSelection(state: String?) {
        val stateIndex = when (state) {
            "Johor" -> 1
            "Kedah" -> 2
            "Kelantan" -> 3
            "Melaka" -> 4
            "Negeri Sembilan" -> 5
            "Pahang" -> 6
            "Penang" -> 7
            "Perak" -> 8
            "Perlis" -> 9
            "Sabah" -> 10
            "Sarawak" -> 11
            "Selangor" -> 12
            "Terengganu" -> 13
            "Kuala Lumpur" -> 14
            "Labuan" -> 15
            "Putrajaya" -> 16
            else -> 0 // Default position
        }
        binding.spinnerEditState.setSelection(stateIndex)
    }

    private fun getSelectedTags(): List<String> {
        val selectedTags = mutableListOf<String>()
        if (binding.toggleButtonVideo.isChecked) selectedTags.add(getString(R.string.typeVideoClip))
        if (binding.toggleButtonAnimalShelter.isChecked) selectedTags.add(getString(R.string.typeAnimalShelter))
        if (binding.toggleButtonCaring.isChecked) selectedTags.add(getString(R.string.typeCaring))
        if (binding.toggleButtonCleanup.isChecked) selectedTags.add(getString(R.string.typeCleanup))
        if (binding.toggleButtonCommunityService.isChecked) selectedTags.add(getString(R.string.typeCommunityService))
        if (binding.toggleButtonDesign.isChecked) selectedTags.add(getString(R.string.typeDesign))
        if (binding.toggleButtonDigital.isChecked) selectedTags.add(getString(R.string.typeDigital))
        if (binding.toggleButtonEnvironment.isChecked) selectedTags.add(getString(R.string.typeEnvironment))
        if (binding.toggleButtonNurseHouse.isChecked) selectedTags.add(getString(R.string.typeNurseHouse))
        if (binding.toggleButtonOutdoor.isChecked) selectedTags.add(getString(R.string.typeOutdoor))
        if (binding.toggleButtonPlanting.isChecked) selectedTags.add(getString(R.string.typePlanting))
        if (binding.toggleButtonSchool.isChecked) selectedTags.add(getString(R.string.typeSchool))
        if (binding.toggleButtonOther.isChecked) selectedTags.add(getString(R.string.typeOther))
        return selectedTags
    }

    private fun getStateFromNumber(stateNumber: Int): String {
        return when (stateNumber) {
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
    }

    companion object {
        private const val ARG_CURRENT_STATE = "currentState"
        private const val ARG_CURRENT_TAGS = "currentTags"

        fun newInstance(currentState: String?, currentTags: List<String>): EditVolunteerProfile {
            val fragment = EditVolunteerProfile()
            val args = Bundle().apply {
                putString(ARG_CURRENT_STATE, currentState)
                putStringArrayList(ARG_CURRENT_TAGS, ArrayList(currentTags))
            }
            fragment.arguments = args
            return fragment
        }
    }




}