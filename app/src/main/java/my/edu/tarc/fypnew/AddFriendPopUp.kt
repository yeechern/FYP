package my.edu.tarc.fypnew

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.fypnew.databinding.FragmentAddFriendPopUpBinding
import my.edu.tarc.fypnew.databinding.FragmentSuitablePopUpBinding


class AddFriendPopUp : DialogFragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: FragmentAddFriendPopUpBinding
    private lateinit var friendRequestAdapter: friendRequestAdapter
    private val friendRequests = arrayListOf<Activity.FriendRequest>()
    private var currentUserName: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        fetchUserName(auth.currentUser?.uid ?: return) { name ->
            currentUserName = name
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddFriendPopUpBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        friendRequestAdapter = friendRequestAdapter(friendRequests, ::acceptFriendRequest, ::rejectFriendRequest)
        binding.recyclerViewFriendRequest.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewFriendRequest.adapter = friendRequestAdapter

        val userId = arguments?.getString("userId") ?: return // Get the current user's ID from arguments
        fetchFriendRequests(userId)

        binding.buttonAddNewFriend.setOnClickListener {
            val email = binding.editTextTextEmailAddress.text.toString().trim()
            if (email.isNotEmpty()) {
                sendFriendRequest(userId, email)
            } else {
                Toast.makeText(context, "Please enter an email address", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchFriendRequests(userId: String) {
        firestore.collection("friendRequest")
            .whereEqualTo("receiverId", userId)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { documents ->
                val newFriendRequests = arrayListOf<Activity.FriendRequest>()
                documents.forEach { document ->
                    val friendRequest = document.toObject(Activity.FriendRequest::class.java)
                    newFriendRequests.add(friendRequest)
                }
                friendRequests.clear()
                friendRequests.addAll(newFriendRequests)
                friendRequestAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error fetching friend requests: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendFriendRequest(userId: String, friendEmail: String) {
        firestore.collection("volunteers").whereEqualTo("email", friendEmail).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val friendId = documents.documents[0].id
                    val friendRequest = Activity.FriendRequest(
                        requestId = firestore.collection("friend_requests").document().id,
                        senderId = userId,
                        senderName = currentUserName, // Use the current user's name
                        receiverId = friendId, // Correctly set the receiverId to the friendId
                        status = "pending"
                    )

                    firestore.collection("friendRequest").document(friendRequest.requestId).set(friendRequest)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Friend request sent", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to send friend request: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to find user: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchUserName(userId: String, callback: (String) -> Unit) {
        firestore.collection("volunteers").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userName = document.getString("name") ?: "Unknown"
                    callback(userName)
                } else {
                    callback("Unknown")
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error fetching user data: ${it.message}", Toast.LENGTH_SHORT).show()
                callback("Unknown")
            }
    }

    private fun acceptFriendRequest(friendRequest: Activity.FriendRequest) {
        firestore.runTransaction { transaction ->
            val senderDocRef = firestore.collection("volunteers").document(friendRequest.senderId)
            val receiverDocRef = firestore.collection("volunteers").document(friendRequest.receiverId)

            val senderDoc = transaction.get(senderDocRef)
            val receiverDoc = transaction.get(receiverDocRef)

            val senderFriends = senderDoc.get("friendList") as? ArrayList<String> ?: arrayListOf()
            val receiverFriends = receiverDoc.get("friendList") as? ArrayList<String> ?: arrayListOf()

            senderFriends.add(friendRequest.receiverId)
            receiverFriends.add(friendRequest.senderId)

            transaction.update(senderDocRef, "friendList", senderFriends)
            transaction.update(receiverDocRef, "friendList", receiverFriends)

            transaction.update(firestore.collection("friendRequest").document(friendRequest.requestId), "status", "accepted")
        }.addOnSuccessListener {
            Toast.makeText(context, "Friend request accepted", Toast.LENGTH_SHORT).show()
            friendRequests.remove(friendRequest)
            friendRequestAdapter.notifyDataSetChanged()
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to accept friend request: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun rejectFriendRequest(friendRequest: Activity.FriendRequest) {
        firestore.collection("friendRequest").document(friendRequest.requestId)
            .update("status", "rejected")
            .addOnSuccessListener {
                Toast.makeText(context, "Friend request rejected", Toast.LENGTH_SHORT).show()
                friendRequests.remove(friendRequest)
                friendRequestAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to reject friend request: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }




}