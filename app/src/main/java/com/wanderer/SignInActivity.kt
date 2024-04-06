package com.wanderer

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.wanderer.databinding.ActivitySignInBinding


class SignInActivity : AppCompatActivity() {

    private var binding: ActivitySignInBinding? = null
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding?.root)
        firebaseAuth = FirebaseAuth.getInstance()
       

        binding?.addButton?.setOnClickListener{
            // Move the initialization of animation options here
            val options = ActivityOptions.makeCustomAnimation(
                this@SignInActivity,
                R.anim.slide_in_right,
                R.anim.stay
            )
            val intent = Intent(this@SignInActivity, SignUpActivity::class.java)
            startActivity(intent, options.toBundle())
        }

        binding?.cirLoginButton?.setOnClickListener{
            val email = binding!!.editTextEmail.text.toString()
            val pass = binding!!.editTextPassword.text.toString()
            if (email.isNotEmpty() && pass.isNotEmpty()){
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { it
                    if (it.isSuccessful) {
                        val intents = Intent(this@SignInActivity, MapsActivity::class.java)
                        startActivity(intents)
                    } else {
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please Fill out All Fields", Toast.LENGTH_SHORT).show()
            }
        }
    }






    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}