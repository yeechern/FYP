package my.edu.tarc.fypnew

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.fypnew.databinding.FragmentOrganizerLoginBinding
import my.edu.tarc.fypnew.databinding.FragmentVolunteerLoginBinding


class OrganizerLoginFragment : Fragment() {
    private lateinit var binding: FragmentOrganizerLoginBinding
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
        binding = FragmentOrganizerLoginBinding.inflate(layoutInflater)
        val view = binding.root

        auth = FirebaseAuth.getInstance()
        binding.editTextLoginEmail.text.clear()
        binding.editTextLoginPassword.text.clear()

        binding.buttonOrganizerLogin.setOnClickListener {
            loginOrganizer()
        }
        return view
    }

    private fun loginOrganizer() {
        val email = binding.editTextLoginEmail.text.toString().trim()
        val password = binding.editTextLoginPassword.text.toString().trim()

        when {
            email.isEmpty() -> {
                Toast.makeText(context, "Key in your Email to login", Toast.LENGTH_SHORT).show()
                return
            }

            password.isEmpty() -> {
                Toast.makeText(context, "Key in your password to login", Toast.LENGTH_SHORT).show()
                return
            }
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        firestore.collection("organizers").document(userId).get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                                    findNavController().navigate(R.id.action_organizerLoginFragment_to_homeFragment)

                                } else {
                                    Toast.makeText(context, "No such organizer found", Toast.LENGTH_SHORT).show()
                                    Log.d("VolunteerLoginFragment", "No such volunteer document")
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Failed to check volunteer data: ${e.message}", Toast.LENGTH_SHORT).show()
                                Log.w("VolunteerLoginFragment", "Error getting document", e)
                            }
                    }
                }else {
                    val exception = task.exception
                    if (exception != null) {
                        val errorMessage = when (exception) {
                            is FirebaseAuthInvalidUserException, is FirebaseAuthInvalidCredentialsException -> {
                                "Incorrect email or password"
                            }
                            else -> {
                                "Login failed: ${exception.message}"
                            }
                        }
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        Log.w("VolunteerLoginFragment", "SignInWithEmailAndPassword: failure - $errorMessage", exception)
                    }
                }
            }
    }
}