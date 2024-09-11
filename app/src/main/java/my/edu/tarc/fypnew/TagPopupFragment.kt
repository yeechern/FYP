package my.edu.tarc.fypnew

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.fypnew.databinding.FragmentAddActivityBinding
import my.edu.tarc.fypnew.databinding.FragmentTagPopupBinding



class TagPopupFragment : DialogFragment() {
    private lateinit var binding: FragmentTagPopupBinding
    private var listener: TagPopupListener? = null

    interface TagPopupListener{
        fun onTagSelected(selectedTags: List<String>)
    }

    fun setTagPopupListener(listener:TagPopupListener){
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentTagPopupBinding.inflate(layoutInflater)
        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonClearTag.setOnClickListener {
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

        binding.buttonConfirmTag.setOnClickListener {
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

            if (selectedTags.size in 1..3) {
                listener?.onTagSelected(selectedTags)
                dismiss()
            } else {
                Toast.makeText(context, "Please select 1-3 Tags", Toast.LENGTH_SHORT).show()
            }
        }
    }



}