package my.edu.tarc.fypnew

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.fypnew.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var userType: String = "volunteer"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        navController = findNavController(R.id.nav_host_fragment_activity_main)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.titleFragment,R.id.homeFragment, R.id.volunteerActivityFragment, R.id.virtualActivityFragment,R.id.profile
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, nd: NavDestination, _ ->
            if (nd.id == R.id.titleFragment || nd.id == R.id.registerFragment ||
                nd.id == R.id.organizerLoginFragment || nd.id == R.id.volunteerLoginFragment ||
                nd.id == R.id.addActivityFragment) {
                navView.visibility = View.GONE
            } else {
                navView.visibility = View.VISIBLE
            }
            checkUserType()
        }
    }

    private fun checkUserType() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val db = FirebaseFirestore.getInstance()

            // Check if the user is in the organizers collection
            db.collection("organizers").document(user.uid).get().addOnSuccessListener { organizerDocument ->
                if (organizerDocument.exists()) {
                    userType = "organizer"
                } else {
                    // Check if the user is in the volunteers collection
                    db.collection("volunteers").document(user.uid).get().addOnSuccessListener { volunteerDocument ->
                        if (volunteerDocument.exists()) {
                            userType = "volunteer"
                        } else {
                            // Default to volunteer if not found in either collection
                            userType = "volunteer"
                        }
                    }.addOnFailureListener {
                        // Handle failure to fetch volunteer document
                        userType = "volunteer"
                    }
                }

                // Set up navigation based on user type
                setupNavigation(userType, user.uid)
            }.addOnFailureListener {
                // Handle failure to fetch organizer document
                userType = "volunteer"
                // Set up navigation assuming volunteer by default
                setupNavigation(userType, user.uid)
            }
        } else {
            // User not authenticated, assume volunteer by default
            setupNavigation(userType, null)
        }
    }

    private fun setupNavigation(userType: String, userId: String?) {
        binding.navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    val bundle = Bundle().apply {
                        putString("userType", userType)
                    }
                    navController.navigate(R.id.homeFragment, bundle)
                    true
                }
                R.id.volunteerActivityFragment -> {
                    navController.navigate(R.id.volunteerActivityFragment)
                    true
                }
                R.id.virtualActivityFragment -> {
                    navController.navigate(R.id.virtualActivityFragment)
                    true
                }
                R.id.profile -> {
                    if (userType == "organizer") {
                        val bundle = Bundle().apply {
                            putString("userId", userId)
                        }
                        navController.navigate(R.id.organizerProfileFragment, bundle)
                    } else {
                        val bundle = Bundle().apply {
                            putString("userId", userId)
                        }
                        navController.navigate(R.id.volunteerProfileFragment, bundle)
                    }
                    true
                }
                else -> false
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}