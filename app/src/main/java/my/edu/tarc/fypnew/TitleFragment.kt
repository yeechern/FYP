package my.edu.tarc.fypnew

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.fypnew.databinding.FragmentRegisterBinding
import my.edu.tarc.fypnew.databinding.FragmentTitleBinding

class TitleFragment : Fragment() {
    private lateinit var binding: FragmentTitleBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentTitleBinding.inflate(layoutInflater)
        val view = binding.root


        binding.buttonRegister.setOnClickListener {
            findNavController().navigate(R.id.action_titleFragment_to_registerFragment)
        }

        binding.buttonLoginOrganizer.setOnClickListener {
            findNavController().navigate(R.id.action_titleFragment_to_organizerLoginFragment)
        }

        binding.buttonLoginVolunteer.setOnClickListener {
            findNavController().navigate(R.id.action_titleFragment_to_volunteerLoginFragment)
        }


        return view
    }
}