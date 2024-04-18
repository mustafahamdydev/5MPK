package com.fivempk.activities

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.fivempk.R
import com.github.leandroborgesferreira.loadingbutton.customViews.CircularProgressButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.maps.errors.ApiException
import com.fivempk.databinding.ActivitySignInBinding
import com.fivempk.firebase.FireBaseClass
import com.fivempk.models.User


class SignInActivity : AppCompatActivity() {
    companion object {
        private const val RC_SIGN_IN = 9001
    }

    private var binding: ActivitySignInBinding? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding?.root)
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        binding!!.tvForgotPass.setOnClickListener {
            val options = ActivityOptions.makeCustomAnimation(
                this@SignInActivity,
                R.anim.slide_in_right,
                R.anim.stay
            )
            val intent = Intent(this@SignInActivity, ForgotPassActivity::class.java)
            startActivity(intent, options.toBundle())
        }

        if (currentUser != null) {
            // The user is already signed in, navigate to MapsActivity
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
            finish() // finish the current activity to prevent the user from coming back to the SignInActivity using the back button
        }
       
        binding?.signinwithgoogle?.setOnClickListener{
            signIn()
        }
        //Go to register activity with animation
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
            val btn :CircularProgressButton = findViewById(R.id.cirLoginButton)
            btn.startAnimation()
            val email = binding!!.editTextEmail.text.toString()
            val pass = binding!!.editTextPassword.text.toString()
            if (email.isNotEmpty() && pass.isNotEmpty()){
                auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { it
                    if (it.isSuccessful) {
                        btn.revertAnimation()
                        FireBaseClass().signInUser(this)
                        val intents = Intent(this@SignInActivity, MapsActivity::class.java)
                        startActivity(intents)
                        finish()
                    } else {
                        btn.revertAnimation()
                        Log.i("firebase", it.exception.toString())
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                btn.revertAnimation()
                Toast.makeText(this, "Please Fill out All Fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val googleUser = auth.currentUser
                    val name = googleUser!!.displayName!!
                    val email = googleUser.email!!
                    val userData = User(googleUser.uid,name,googleUser.photoUrl.toString(),email)
                    FireBaseClass().registerGoogleUser(this,userData)

                    Toast.makeText(this, "Signed in as ${googleUser.displayName}", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, StartActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }


    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    fun signInSuccess(loggedInUser: User) {

    }
}