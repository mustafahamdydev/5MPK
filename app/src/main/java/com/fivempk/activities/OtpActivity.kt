package com.fivempk.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.fivempk.R
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.fivempk.databinding.ActivityOtpBinding
import java.util.concurrent.TimeUnit

class OtpActivity : AppCompatActivity() {

    private lateinit var email:String
    private lateinit var pass : String
    private lateinit var name : String
    private var binding: ActivityOtpBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var otp :String
    private lateinit var resendOtp:PhoneAuthProvider.ForceResendingToken
    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var phoneNumber: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        auth = FirebaseAuth.getInstance()

        otp=intent.getStringExtra("OTP").toString()
        resendToken=intent.getParcelableExtra("resendToken")!!
        phoneNumber=intent.getStringExtra("phoneNumber")!!

        addTextChangeListener()
        resendOTPVisibility()
        binding!!.resendOTP.setOnClickListener{
            resendVerification()
            resendOTPVisibility()
        }

        binding!!.doneBtn.setOnClickListener{

            val typedOTP= (binding!!.inOtp1.text.toString()
                    +binding!!.inOtp2.text.toString()
                    +binding!!.inOtp3.text.toString()
                    +binding!!.inOtp4.text.toString()
                    +binding!!.inOtp5.text.toString()
                    +binding!!.inOtp6.text.toString())

            Toast.makeText(this, "$typedOTP", Toast.LENGTH_SHORT).show()
            
            if (typedOTP.isNotEmpty()){
                if (typedOTP.length==6){
                    val credential = PhoneAuthProvider.getCredential(otp,typedOTP)
                    signInWithPhoneAuthCredential(credential)
                }else{
                    Toast.makeText(this, "Incorrect OTP", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, "Please Enter OTP", Toast.LENGTH_SHORT).show()
            }
        }


    }


    // Function to sign in with the phone authentication credential
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    // Phone number verification successful, complete registration
                    // Get user input from the beginning
                    val intent = Intent(this@OtpActivity, MapsActivity::class.java)
                    startActivity(intent)
                    val email = email
                    val password = pass
                    val phoneNumber = phoneNumber

                    // Update user profile with phone number
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                        if (profileTask.isSuccessful) {
                            // Profile updated successfully, now update email and password
                            user.updateEmail(email).addOnCompleteListener { emailTask ->
                                if (emailTask.isSuccessful) {
                                    // Email updated successfully, now update password
                                    user.updatePassword(password).addOnCompleteListener { passwordTask ->
                                        if (passwordTask.isSuccessful) {
                                            // Email and password updated successfully
                                            // Proceed with your registration process here
                                        } else {
                                            // Handle password update failure
                                        }
                                    }
                                } else {
                                    // Handle email update failure
                                }
                            }
                        } else {
                            // Handle profile update failure
                        }
                    }
                } else {
                    // Handle sign-in failure
                }
            }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d(PhoneAuthentication.TAG, "onVerificationCompleted:$credential")

        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w(PhoneAuthentication.TAG, "onVerificationFailed", e)

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                Log.w(PhoneAuthentication.TAG, "FirebaseAuthInvalidCredentials", e)
            } else if (e is FirebaseTooManyRequestsException) {
                Log.w(PhoneAuthentication.TAG, "FirebaseTooManyRequests", e)
                // The SMS quota for the project has been exceeded
            } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                // reCAPTCHA verification attempted with null Activity
                Log.w(PhoneAuthentication.TAG, "FirebaseAuthMissingActivityForRecaptcha", e)
            }

            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            otp=verificationId
            resendToken=token

        }
    }
    private fun resendOTPVisibility(){
        binding!!.inOtp1.setText("")
        binding!!.inOtp2.setText("")
        binding!!.inOtp3.setText("")
        binding!!.inOtp4.setText("")
        binding!!.inOtp5.setText("")
        binding!!.inOtp6.setText("")

        binding!!.resendOTP.setTextColor( getResources().getColor(R.color.black))
        binding!!.resendOTP.isEnabled = false

        Handler(Looper.myLooper()!!).postDelayed(Runnable {
            binding!!.resendOTP.setTextColor( getResources().getColor(R.color.blue_150))
            binding!!.resendOTP.isEnabled = true
        },60000)

    }
    private fun resendVerification(){
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(callbacks)
            .setForceResendingToken(resendToken) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun addTextChangeListener(){
        binding!!.inOtp1.addTextChangedListener(EditTextWatcher(binding!!.inOtp1))
        binding!!.inOtp2.addTextChangedListener(EditTextWatcher(binding!!.inOtp2))
        binding!!.inOtp3.addTextChangedListener(EditTextWatcher(binding!!.inOtp3))
        binding!!.inOtp4.addTextChangedListener(EditTextWatcher(binding!!.inOtp4))
        binding!!.inOtp5.addTextChangedListener(EditTextWatcher(binding!!.inOtp5))
        binding!!.inOtp6.addTextChangedListener(EditTextWatcher(binding!!.inOtp6))
    }

    inner class EditTextWatcher( private val view: View):TextWatcher{
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // we will do nothing
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            ////asdasdadasd
        }

        override fun afterTextChanged(s: Editable?) {
            val text = s.toString()
            when (view.id){
                R.id.in_otp1 -> if (text.length == 1) binding!!.inOtp2.requestFocus()
                R.id.in_otp2 -> if (text.length == 1) binding!!.inOtp3.requestFocus() else if (text.isEmpty()) binding!!.inOtp1.requestFocus()
                R.id.in_otp3 -> if (text.length == 1) binding!!.inOtp4.requestFocus() else if (text.isEmpty()) binding!!.inOtp2.requestFocus()
                R.id.in_otp4 -> if (text.length == 1) binding!!.inOtp5.requestFocus() else if (text.isEmpty()) binding!!.inOtp3.requestFocus()
                R.id.in_otp5 -> if (text.length == 1) binding!!.inOtp6.requestFocus() else if (text.isEmpty()) binding!!.inOtp4.requestFocus()
                R.id.in_otp6 -> if (text.isEmpty()) binding!!.inOtp5.requestFocus()
            }
        }

    }
}