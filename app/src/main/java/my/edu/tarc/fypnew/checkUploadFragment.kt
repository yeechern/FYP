package my.edu.tarc.fypnew

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import my.edu.tarc.fypnew.databinding.FragmentCheckUploadBinding
import my.edu.tarc.fypnew.databinding.FragmentVirtualActivityBinding



class checkUploadFragment : Fragment() {
    private lateinit var binding: FragmentCheckUploadBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var checkUploadAdapter: checkUploadAdapter
    private val fileUploads = arrayListOf<Activity.FileUrl>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentCheckUploadBinding.inflate(inflater, container, false)
        val view = binding.root

        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkUploadAdapter = checkUploadAdapter(fileUploads) { fileUrl ->
            downloadFile(fileUrl)
        }
        binding.recycleViewUploadedFile.layoutManager = LinearLayoutManager(context)
        binding.recycleViewUploadedFile.adapter = checkUploadAdapter

        val activityId = arguments?.getString("activityId") ?: return
        fetchUploadedFiles(activityId)
    }

    private fun fetchUploadedFiles(activityId: String) {
        firestore.collection("fileUpload")
            .whereEqualTo("activityId", activityId)
            .get()
            .addOnSuccessListener { result ->
                val newFileUploads = arrayListOf<Activity.FileUrl>()
                result.forEach { document ->
                    val url = document.getString("fileUrl") ?: return@forEach
                    val volunteerId = document.getString("volunteerId") ?: return@forEach

                    fetchVolunteerDetails(volunteerId) { volunteerName, volunteerEmail ->
                        val fileUrl = Activity.FileUrl(
                            url = url,
                            volunteerId = volunteerId,
                            volunteerName = volunteerName,
                            volunteerEmail = volunteerEmail
                        )
                        newFileUploads.add(fileUrl)
                        checkUploadAdapter.updateFileUploads(newFileUploads)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error fetching files: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchVolunteerDetails(volunteerId: String, callback: (String, String) -> Unit) {
        firestore.collection("volunteers").document(volunteerId).get()
            .addOnSuccessListener { document ->
                val volunteerName = document.getString("name") ?: "Unknown"
                val volunteerEmail = document.getString("email") ?: "Unknown"
                callback(volunteerName, volunteerEmail)
            }
            .addOnFailureListener {
                callback("Unknown", "Unknown")
            }
    }

    private fun downloadFile(fileUrl: String) {
        val request = DownloadManager.Request(Uri.parse(fileUrl))
            .setTitle("Downloading File")
            .setDescription("Please wait...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "downloadedfile")

        val downloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
        downloadManager?.enqueue(request)
    }


}