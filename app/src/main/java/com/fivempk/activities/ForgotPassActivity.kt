package com.fivempk.activities

import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.fivempk.R
import com.fivempk.databinding.ActivityForgotPassBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPassActivity : AppCompatActivity() {

    private var binding : ActivityForgotPassBinding? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityForgotPassBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        auth = FirebaseAuth.getInstance()

        binding!!.ResetBtn.setOnClickListener{
            val textEmail = binding!!.editTextEmail.text.toString()
            resetPass(textEmail)
        }



        binding!!.backButton.setOnClickListener{
            val options: ActivityOptions = ActivityOptions.makeCustomAnimation(
                this@ForgotPassActivity,
                R.anim.slide_in_left,
                R.anim.animate_slide_out_right
            )
            val intent = Intent(this@ForgotPassActivity , SignInActivity::class.java)
            startActivity(intent,options.toBundle())
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
            val message = "Please make sure your email is register or try again later"
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