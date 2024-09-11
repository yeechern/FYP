package my.edu.tarc.fypnew

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.fypnew.databinding.FragmentRegisterBinding

class registerFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding


    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.buttonConfirmRegister.setOnClickListener {
            registerUser()
        }
        return view
    }

    private fun registerUser() {
        val name = binding.editTextTextName.text.toString().trim()
        val birthday = binding.editTextBirthday.text.toString().trim()
        val contact = binding.editTextContact.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()
        val confirmPassword = binding.editTextConfirmPassword.text.toString().trim()
        val stateNumber = binding.spinnerState.selectedItemPosition
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

        val position = when {
            binding.radioButtonVolunteer.isChecked -> "Volunteer"
            binding.radioButtonOrganizer.isChecked -> "Organizer"
            else -> ""
        }

        // Validation checks
        if (!validateInput(name, birthday, contact, email, password, confirmPassword, state, position)) {
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        if (position == "Volunteer") {
                            registerVolunteer(userId, name, birthday, contact, email, state,password)
                        } else if (position == "Organizer") {
                            registerOrganizer(userId, name, birthday, contact, email, state,password)
                        }
                    } else {
                        Log.w("registerFragment", "User ID is null")
                    }
                } else {
                    handleRegistrationError(task.exception)
                }
            }
    }

    private fun validateInput(
        name: String,
        birthday: String,
        contact: String,
        email: String,
        password: String,
        confirmPassword: String,
        state: String,
        position: String
    ): Boolean {
        when {
            name.isEmpty() -> {
                showToast("Key in your Name.")
                return false
            }
            birthday.isEmpty() -> {
                showToast("Key in your Birthday.")
                return false
            }
            contact.isEmpty() -> {
                showToast("Key in your Contact Number")
                return false
            }
            contact.length < 10 || contact.length > 11 -> {
                showToast("Fill in the correct contact.")
                return false
            }
            email.isEmpty() -> {
                showToast("Email is required.")
                return false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showToast("Email format is incorrect.")
                return false
            }
            password.isEmpty() -> {
                showToast("Password is required.")
                return false
            }
            password.length < 6 -> {
                showToast("Make password more than 6 words.")
                return false
            }
            confirmPassword.isEmpty() -> {
                showToast("Please confirm your password.")
                return false
            }
            password != confirmPassword -> {
                binding.editTextConfirmPassword.text.clear()
                binding.editTextPassword.text.clear()
                showToast("Passwords do not match. Please try again.")
                return false
            }
            state.isEmpty() -> {
                showToast("State is required.")
                return false
            }
            position.isEmpty() -> {
                showToast("Please select a position (Volunteer or Organizer).")
                return false
            }
        }
        return true
    }

    private fun registerVolunteer(userId: String, name: String, birthday: String, contact: String, email: String, state: String,password: String) {
        val volunteer = hashMapOf(
            "id" to userId,
            "name" to name,
            "birthday" to birthday,
            "contact" to contact,
            "email" to email,
            "state" to state,
            "password" to password,
            "volunteerHours" to 0,
            "appliedActivityList" to emptyList<String>(),
            "completedActivityList" to emptyList<String>(),
            "friendList" to emptyList<String>()
        )

        firestore.collection("volunteers").document(userId)
            .set(volunteer)
            .addOnSuccessListener {
                showToast("Registration successful")
                findNavController().navigate(R.id.action_registerFragment_to_titleFragment)
            }
            .addOnFailureListener { e ->
                Log.w("registerFragment", "Error adding document", e)
                showToast("Registration failed: ${e.message}")
            }
    }

    private fun registerOrganizer(userId: String, name: String, birthday: String, contact: String, email: String, state: String,password: String) {
        val organizer = hashMapOf(
            "id" to userId,
            "name" to name,
            "birthday" to birthday,
            "contact" to contact,
            "email" to email,
            "state" to state,
            "password" to password,
            "postedActivityList" to emptyList<String>(),
            "finishedActivityList" to emptyList<String>()
        )

        firestore.collection("organizers").document(userId)
            .set(organizer)
            .addOnSuccessListener {
                showToast("Registration successful")
                findNavController().navigate(R.id.action_registerFragment_to_titleFragment)
            }
            .addOnFailureListener { e ->
                Log.w("registerFragment", "Error adding document", e)
                showToast("Registration failed: ${e.message}")
            }
    }

    private fun handleRegistrationError(exception: Exception?) {
        if (exception is FirebaseAuthUserCollisionException) {
            showToast("This email is already registered.")
        } else {
            showToast("Registration failed: ${exception?.message}")
            Log.e("registerFragment", "Registration failed", exception)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}