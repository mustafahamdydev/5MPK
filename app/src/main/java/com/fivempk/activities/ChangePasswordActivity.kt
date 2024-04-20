package com.fivempk.activities

import android.app.AlertDialog
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.fivempk.databinding.ActivityChangePasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordActivity : AppCompatActivity() {
    private var binding : ActivityChangePasswordBinding? = null
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        auth = FirebaseAuth.getInstance()

        binding!!.ResetBtn.setOnClickListener{
            val textEmail = binding!!.editTextEmail.text.toString()
            resetPass(textEmail)
        }



    }
    private fun resetPass(email : String ){
        auth.sendPasswordResetEmail(email).addOnSuccessListener {
            val message = "Please check your email for the reset link"
            AlertDialog.Builder(this)
                .setTitle("Form submitted")
                .setMessage(message)
                .setPositiveButton("Okay"){ _,_ ->
                    binding!!.editTextEmail.text = null
                    finish()
                }
                .show()
        }.addOnFailureListener {
            val message = "Something went wrong Try again later"
            AlertDialog.Builder(this)
                .setTitle("Failed")
                .setMessage(message)
                .setPositiveButton("Okay"){ _,_ ->
                    binding!!.editTextEmail.text = null
                    finish()
                }
                .show()
        }



    }
}