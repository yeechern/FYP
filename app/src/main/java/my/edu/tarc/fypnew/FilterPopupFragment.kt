package my.edu.tarc.fypnew

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import my.edu.tarc.fypnew.databinding.FragmentFilterPopupBinding
import my.edu.tarc.fypnew.databinding.FragmentTagPopupBinding


class FilterPopupFragment : DialogFragment() {
    private lateinit var binding: FragmentFilterPopupBinding
    private var listener: FilterPopupListener? = null

    interface FilterPopupListener{
        fun onFilterSelected(selectedTags: List<String>)
    }

    fun setFilterPopupListener(listener: FilterPopupListener){
        this.listener = listener
    }





    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentFilterPopupBinding.inflate(layoutInflater)
        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFClearTag.setOnClickListener {
            binding.toggleButtonFVideo.isChecked = false
            binding.toggleButtonFAnimalShelter.isChecked = false
            binding.toggleButtonFCaring.isChecked = false
            binding.toggleButtonFCleanup.isChecked = false
            binding.toggleButtonFCommunityService.isChecked = false
            binding.toggleButtonFDesign.isChecked = false
            binding.toggleButtonFDigital.isChecked = false
            binding.toggleButtonFEnvironment.isChecked = false
            binding.toggleButtonFNurseHouse.isChecked = false
            binding.toggleButtonFOutdoor.isChecked = false
            binding.toggleButtonFPlanting.isChecked = false
            binding.toggleButtonFSchool.isChecked = false
            binding.toggleButtonFOther.isChecked = false

            listener?.onFilterSelected(emptyList())
        }

        binding.buttonFConfirmTag.setOnClickListener {
            val selectedTags = mutableListOf<String>()
            if (binding.toggleButtonFVideo.isChecked) selectedTags.add(getString(R.string.typeVideoClip))
            if (binding.toggleButtonFAnimalShelter.isChecked) selectedTags.add(getString(R.string.typeAnimalShelter))
            if (binding.toggleButtonFCaring.isChecked) selectedTags.add(getString(R.string.typeCaring))
            if (binding.toggleButtonFCleanup.isChecked) selectedTags.add(getString(R.string.typeCleanup))
            if (binding.toggleButtonFCommunityService.isChecked) selectedTags.add(getString(R.string.typeCommunityService))
            if (binding.toggleButtonFDesign.isChecked) selectedTags.add(getString(R.string.typeDesign))
            if (binding.toggleButtonFDigital.isChecked) selectedTags.add(getString(R.string.typeDigital))
            if (binding.toggleButtonFEnvironment.isChecked) selectedTags.add(getString(R.string.typeEnvironment))
            if (binding.toggleButtonFNurseHouse.isChecked) selectedTags.add(getString(R.string.typeNurseHouse))
            if (binding.toggleButtonFOutdoor.isChecked) selectedTags.add(getString(R.string.typeOutdoor))
            if (binding.toggleButtonFPlanting.isChecked) selectedTags.add(getString(R.string.typePlanting))
            if (binding.toggleButtonFSchool.isChecked) selectedTags.add(getString(R.string.typeSchool))
            if (binding.toggleButtonFOther.isChecked) selectedTags.add(getString(R.string.typeOther))

            listener?.onFilterSelected(selectedTags)
            dismiss()
        }
    }




}