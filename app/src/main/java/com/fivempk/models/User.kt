package com.fivempk.models

import android.os.Parcel
import android.os.Parcelable


data class User(
    val id : String = "",
    val name:String ="",
    val image : String ="",
    val email:String = "",
    val phone : String = "",
    val fcmToken: String= ""
):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(image)
        parcel.writeString(email)
        parcel.writeString(phone)
        parcel.writeString(fcmToken)
    }

    override fun describeContents()= 0


    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }

}



