package com.wanderer

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.ktx.Firebase
import com.wanderer.databinding.ActivitySignUpBinding
import java.lang.ref.PhantomReference

class SignUpActivity : AppCompatActivity() {

    private var binding: ActivitySignUpBinding? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding?.root)

        val options: ActivityOptions = ActivityOptions.makeCustomAnimation(
            this@SignUpActivity,
            R.anim.slide_in_left,
            R.anim.animate_slide_out_right
        )
        val intent = Intent(this@SignUpActivity, SignInActivity::class.java)



        firebaseAuth = FirebaseAuth.getInstance()

        binding?.cirRegisterButton?.setOnClickListener{
            val name = binding!!.editTextName.text.toString()
            val email = binding!!.editTextEmail.text.toString()
            val numb = binding!!.editTextMobile.text.toString()
            val pass = binding!!.editTextPassword.text.toString()
            val confirmPass = binding!!.editTextConfPassword.text.toString()
            if ( name.isNotEmpty()
                && email.isNotEmpty()
                && numb.isNotEmpty()
                && pass.isNotEmpty()
                && confirmPass.isNotEmpty()){
                if (pass == confirmPass){
                    firebaseAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener {
                        if (it.isSuccessful){
                            startActivity(intent, options.toBundle())
                        }else{
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }else{
                    Toast.makeText(this, "Password is not matching", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, "Please Fill out All Fields", Toast.LENGTH_SHORT).show()
            }




        }


        binding?.backButton?.setOnClickListener {
            startActivity(intent, options.toBundle())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}