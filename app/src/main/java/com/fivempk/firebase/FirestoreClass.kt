package com.fivempk.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.fivempk.activities.MapsActivity
import com.fivempk.activities.SignInActivity
import com.fivempk.activities.SignUpActivity
import com.fivempk.activities.UserProfileActivity
import com.fivempk.models.User
import com.fivempk.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FireBaseClass {
    private val database = Firebase.database.reference

    fun registerUser(activity: SignUpActivity, userInfo: User){
        database.child(Constants.USERS)
            .child(getCurrentUserId())
            .setValue(userInfo)
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }
    }
    fun registerGoogleUser(activity: SignInActivity, userInfo: User){
        database.child(Constants.USERS)
            .child(getCurrentUserId())
            .setValue(userInfo)
            .addOnSuccessListener {
                activity.signInSuccess(userInfo)
            }
    }
    fun signInUser(activity: Activity){
        database.child(Constants.USERS)
            .child(getCurrentUserId())
            .get()
            .addOnSuccessListener {dataSnapshot->
                val loggedInUser = dataSnapshot.getValue(User::class.java)
                when (activity){
                    is SignInActivity ->{
                        activity.signInSuccess  (loggedInUser!!)
                    }
                    is MapsActivity ->{
                        if (loggedInUser != null) {
                            activity.updateNavigationUserDetails(loggedInUser)
                        }
                    }
                    is UserProfileActivity ->{
                        if (loggedInUser != null){
                            activity.fillUserInfo(loggedInUser)
                        }
                    }
                }

            }.addOnFailureListener{
                Log.e("firebase", "Error getting data", it)
            }
    }
    fun deleteUser(activity: UserProfileActivity){
        database.child(Constants.USERS)
            .child(getCurrentUserId())
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(activity, "Data deleted successfully", Toast.LENGTH_SHORT).show()
            }
    }
    private fun getCurrentUserId():String{
        return FirebaseAuth.getInstance().currentUser!!.uid
    }
}