package my.edu.tarc.fypnew

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties


data class Activity(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val date: String = "",
    val organizerId: String = "",
    val tags: List<String> = listOf(),
    val style:String="",
    val startTime:String="",
    val endTime:String = "",
    val location: String = "",
    val personNeed :String="",
    val state: String="",
    val status: String ="",
    val listOfVolunteers: List<String> = listOf(),
    val fileUrls: List<FileUrl> = listOf()
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: emptyList(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: emptyList(),
        parcel.createTypedArrayList(FileUrl.CREATOR) ?: emptyList()

    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeString(date)
        parcel.writeString(organizerId)
        parcel.writeString(style)
        parcel.writeString(startTime)
        parcel.writeString(endTime)
        parcel.writeString(personNeed)
        parcel.writeStringList(tags)
        parcel.writeString(location)
        parcel.writeString(state)
        parcel.writeString(status)
        parcel.writeStringList(listOfVolunteers)
        parcel.writeTypedList(fileUrls)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Activity> {
        override fun createFromParcel(parcel: Parcel): Activity {
            return Activity(parcel)
        }

        override fun newArray(size: Int): Array<Activity?> {
            return arrayOfNulls(size)
        }
    }

    data class FileUrl(
        val url: String = "",
        val volunteerId: String = "",
        val volunteerName: String = "",
        val volunteerEmail: String = ""
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: ""
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(url)
            parcel.writeString(volunteerId)
            parcel.writeString(volunteerName)
            parcel.writeString(volunteerEmail)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<FileUrl> {
            override fun createFromParcel(parcel: Parcel): FileUrl {
                return FileUrl(parcel)
            }

            override fun newArray(size: Int): Array<FileUrl?> {
                return arrayOfNulls(size)
            }
        }
    }

    @IgnoreExtraProperties
    data class Volunteer(
        val name: String = "",
        val email: String = "",
        val contact: String = "",
        val completedActivityList: List<String> = listOf(),
        val volunteerHours: Long = 0
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.createStringArrayList() ?: listOf(),
            parcel.readLong()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(name)
            parcel.writeString(email)
            parcel.writeString(contact)
            parcel.writeStringList(completedActivityList)
            parcel.writeLong(volunteerHours)
        }

        override fun describeContents(): Int {
            return 0
        }

        fun getCompletedActivityCount(): Int {
            return completedActivityList.size
        }

        companion object CREATOR : Parcelable.Creator<Volunteer> {
            override fun createFromParcel(parcel: Parcel): Volunteer {
                return Volunteer(parcel)
            }

            override fun newArray(size: Int): Array<Volunteer?> {
                return arrayOfNulls(size)
            }
        }
    }

    data class FriendRequest(
        val requestId: String = "",
        val receiverId: String = "",
        val senderId: String = "",
        var senderName: String = "", // You might want to store sender details
        val status: String = "pending" // Default status is pending
    ): Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: ""
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(requestId)
            parcel.writeString(receiverId)
            parcel.writeString(senderId)
            parcel.writeString(senderName)
            parcel.writeString(status)
        }

        override fun describeContents(): Int {
            return 0
        }


        companion object CREATOR : Parcelable.Creator<Volunteer> {
            override fun createFromParcel(parcel: Parcel): Volunteer {
                return Volunteer(parcel)
            }

            override fun newArray(size: Int): Array<Volunteer?> {
                return arrayOfNulls(size)
            }
        }
    }


}