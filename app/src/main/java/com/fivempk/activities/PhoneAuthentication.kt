package com.fivempk.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.fivempk.databinding.ActivityPhoneAuthenticationBinding
import java.util.concurrent.TimeUnit

class PhoneAuthentication : AppCompatActivity() {
    companion object {
        const val TAG = "PhoneAuthActivity"
    }
    private lateinit var number: String
    private var binding: ActivityPhoneAuthenticationBinding? = null
    private lateinit var auth: FirebaseAuth
    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPhoneAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        auth = FirebaseAuth.getInstance()

        number=intent.getStringExtra("phoneNumber")!!

        binding!!.tdPhone.setText(number.substring(1))

        binding!!.contBtn.setOnClickListener{
           number =  binding!!.tdPhone.text.trim().toString()
            if (number.isNotEmpty()){
                if (number.length == 10){
                    number="+20$number"
                    val options = PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(number) // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this) // Activity (for callback binding)
                        .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                        .build()
                    PhoneAuthProvider.verifyPhoneNumber(options)
                }else{
                    Toast.makeText(this, "Please Enter correct Number", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, "Please Enter Number", Toast.LENGTH_SHORT).show()
            }

        }

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:$credential")
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                     Log.w(TAG, "FirebaseAuthInvalidCredentials", e)
                } else if (e is FirebaseTooManyRequestsException) {
                    Log.w(TAG, "FirebaseTooManyRequests", e)
                    // The SMS quota for the project has been exceeded
                } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                    // reCAPTCHA verification attempted with null Activity
                    Log.w(TAG, "FirebaseAuthMissingActivityForRecaptcha", e)
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
                val intent = Intent(this@PhoneAuthentication, OtpActivity::class.java)
                intent.putExtra("OTP",verificationId)
                intent.putExtra("resendToken",token)
                intent.putExtra("phoneNumber",number)
                startActivity(intent)
                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token
            }
        }






    }





    override fun onDestroy() {
        super.onDestroy()
        binding=null
    }
}