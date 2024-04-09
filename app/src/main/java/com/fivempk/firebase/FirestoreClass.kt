package com.fivempk.firebase

import android.app.Activity
import android.util.Log
import com.fivempk.activities.MapsActivity
import com.fivempk.activities.SignInActivity
import com.fivempk.activities.SignUpActivity
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
                }

            }.addOnFailureListener{
                Log.e("firebase", "Error getting data", it)
            }
    }
    private fun getCurrentUserId():String{
        return FirebaseAuth.getInstance().currentUser!!.uid
    }
}