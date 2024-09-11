package my.edu.tarc.fypnew

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.fypnew.databinding.UploadedFileListBinding

class checkUploadAdapter  (
    private var fileUploads: List<Activity.FileUrl>,
    private val onDownloadClick: (String) -> Unit
): RecyclerView.Adapter<checkUploadAdapter.FileUploadViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):FileUploadViewHolder {
        val binding = UploadedFileListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileUploadViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileUploadViewHolder, position: Int) {
        val fileUpload = fileUploads[position]
        holder.bind(fileUpload)
    }

    override fun getItemCount(): Int = fileUploads.size

    fun updateFileUploads(newFileUploads: ArrayList<Activity.FileUrl>) {
        fileUploads = newFileUploads
        notifyDataSetChanged()
    }

    inner class FileUploadViewHolder(
        private val binding: UploadedFileListBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(fileUpload:Activity.FileUrl) {
            binding.textViewUploadName.text = fileUpload.volunteerName
            binding.textViewUploadEmail.text = fileUpload.volunteerEmail

            binding.buttonUploadDownload.setOnClickListener {
                onDownloadClick(fileUpload.url)
            }
        }
    }

}