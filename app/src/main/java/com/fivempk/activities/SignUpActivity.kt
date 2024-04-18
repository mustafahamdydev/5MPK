package com.fivempk.activities

import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.fivempk.R
import com.fivempk.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.fivempk.databinding.ActivitySignUpBinding
import com.fivempk.firebase.FireBaseClass
import com.github.leandroborgesferreira.loadingbutton.customViews.CircularProgressButton
import com.google.firebase.auth.FirebaseUser

class SignUpActivity : AppCompatActivity() {

    private var exists : Boolean  = false
    private var binding: ActivitySignUpBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding?.root)
        emailFocusListener()
        passwordFocusListener()
        phoneFocusListener()
        nameFocusListener()
        conPasswordFocusListener()

        val options: ActivityOptions = ActivityOptions.makeCustomAnimation(
            this@SignUpActivity,
            R.anim.slide_in_left,
            R.anim.animate_slide_out_right
        )
        val intent = Intent(this@SignUpActivity, SignInActivity::class.java)

        auth = FirebaseAuth.getInstance()

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
                val btn : CircularProgressButton = findViewById(R.id.cirRegisterButton)
                btn.startAnimation()
                if (pass == confirmPass){
                    auth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener {
                        if (it.isSuccessful){
                            submitForm(numb)
                            val firebaseUser:FirebaseUser= it.result!!.user!!
                            val registeredEmail = firebaseUser.email!!
                            val user = User(firebaseUser.uid,name,"",registeredEmail,numb)
                            FireBaseClass().registerUser(this,user)

                        }else{
                            submitForm("")
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }else{
                    submitForm("")
                    Toast.makeText(this, "Invalid Inputs", Toast.LENGTH_SHORT).show()
                }
            }else{
                submitForm("")
                Toast.makeText(this, "Please Fill out All Fields", Toast.LENGTH_SHORT).show()
            }

        }


        binding?.backButton?.setOnClickListener {
            startActivity(intent, options.toBundle())
        }
    }

    fun userRegisteredSuccess(){
        auth.signOut()
        val btn : CircularProgressButton = findViewById(R.id.cirRegisterButton)
        btn.revertAnimation()
    }

    private fun submitForm(numb:String)
    {
        binding!!.textInputName.helperText = validName()
        binding!!.textInputEmail.helperText = validEmail()
        binding!!.textInputMobile.helperText = validPhone()
        binding!!.textInputPassword.helperText = validPassword()
        binding!!.textInputConfPassword.helperText = validConPassword()

        val validName = binding!!.textInputName.helperText == null
        val validEmail = binding!!.textInputEmail.helperText == null
        val validPassword = binding!!.textInputPassword.helperText == null
        val validConPassword = binding!!.textInputConfPassword.helperText == null
        val validPhone = binding!!.textInputMobile.helperText == null

        if (validName && validEmail && validPhone && validPassword && validConPassword )
            resetForm(numb)
        else
            invalidForm()
    }

    private fun invalidForm()
    {
        var message = ""
        if(binding!!.textInputName.helperText != null)
            message += "\n\nName: " + binding!!.textInputName.helperText
        if(binding!!.textInputEmail.helperText != null)
            message += "\n\nEmail: " + binding!!.textInputEmail.helperText
        if(binding!!.textInputPassword.helperText != null)
            message += "\n\nPassword: " + binding!!.textInputPassword.helperText
        if(binding!!.textInputConfPassword.helperText != null)
            message += "\n\nConfirm Password: " + binding!!.textInputConfPassword.helperText
        if(binding!!.textInputMobile.helperText != null)
            message += "\n\nPhone: " + binding!!.textInputMobile.helperText

        AlertDialog.Builder(this)
            .setTitle("Invalid Form")
            .setMessage(message)
            .setPositiveButton("Okay"){ _,_ ->
                // do nothing
            }
            .show()
    }

    private fun resetForm(numb: String)
    {
        val message = "Just Verifiy Phone number And We are ready to start"
        AlertDialog.Builder(this)
            .setTitle("Registration Successful")
            .setMessage(message)
            .setPositiveButton("Okay"){ _,_ ->
                binding!!.editTextEmail.text = null
                binding!!.editTextPassword.text = null
                binding!!.editTextMobile.text = null


                binding!!.textInputEmail.helperText = getString(R.string.empty_text)
                binding!!.textInputPassword.helperText = getString(R.string.empty_text)
                binding!!.textInputMobile.helperText = getString(R.string.empty_text)
                binding!!.textInputName.helperText = getString(R.string.empty_text)
                binding!!.textInputConfPassword.helperText = getString(R.string.empty_text)
                binding!!.textInputMobile.helperText = getString(R.string.empty_text)

                val intent = Intent(this,PhoneAuthentication::class.java)
                intent.putExtra("phoneNumber",numb)
                startActivity(intent)

            }
            .show()
    }

    ////////////////////////////////////////////////////////////////////////////
    //Register  From Validation
    ///////////////////////////////////////////////////////////////////////////

    private fun nameFocusListener(){
        binding!!.editTextName.setOnFocusChangeListener { _ , hasFocus ->
            if (!hasFocus){
                binding!!.textInputName.helperText = validName()
            }
        }
    }
    private fun validName():String?{
        val nameText = binding!!.editTextName.text.toString()
        if (nameText.isEmpty()){
            return "required"
        }
        if (nameText.length < 4){
            return "Name must be more than 4 letters"
        }
        return null
    }
    private fun emailFocusListener(){
        binding!!.editTextEmail.setOnFocusChangeListener { _ , hasFocus ->
            if (!hasFocus){
                binding!!.textInputEmail.helperText = validEmail()
            }
        }
    }
    private fun validEmail():String?{
        val emailText = binding!!.editTextEmail.text.toString()
        if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()){
            return "Invalid Email Address"
        }
        return null
    }
    private fun phoneFocusListener(){
        binding!!.editTextMobile.setOnFocusChangeListener { _ , hasFocus ->
            if (!hasFocus){
                binding!!.textInputMobile.helperText = validPhone()
            }
        }
    }
    private fun validPhone():String?{
        val phoneText = binding!!.editTextMobile.text.toString()
        if (phoneText.length != 11){
            return "Invalid Phone Number"
        }
        return null
    }
    private fun passwordFocusListener(){
        binding!!.editTextPassword.setOnFocusChangeListener { _ , hasFocus ->
            if (!hasFocus){
                binding!!.textInputPassword.helperText = validPassword()
            }
        }
    }
    private fun validPassword():String?{
        val passwordText = binding!!.editTextPassword.text.toString()
        if (passwordText.length < 8){
            return "Minimum 8 Character Password"
        }
        if (!passwordText.matches(".*[A-Z].*".toRegex())){
            return "Must Contain at least 1 Upper-case Character"
        }
        if (!passwordText.matches(".*[a-z].*".toRegex())){
            return "Must Contain at least 1 Lower-case Character"
        }
        if (!passwordText.matches(".*[ @#$%/^&].*".toRegex())){
            return "Must Contain at least 1 Special Character{[,@,#,$,&,*,/,^}"
        }
        if (!passwordText.matches(".*[0-9].*".toRegex())){
            return "Must Contain at least 1 Number"
        }
        return null
    }
    private fun conPasswordFocusListener(){
        binding!!.editTextConfPassword.setOnFocusChangeListener { _ , hasFocus ->
            if (!hasFocus){
                binding!!.textInputConfPassword.helperText = validConPassword()
            }
        }
    }
    private fun validConPassword():String?{
        val passwordText = binding!!.editTextPassword.text.toString()
        val conPasswordText = binding!!.editTextConfPassword.text.toString()
        if (passwordText.isEmpty() || conPasswordText.isEmpty()){
            return "Please Enter Password"
        }
        if (passwordText != conPasswordText ){
            return "Password Doesn't Match"
        }
        return null
    }


    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}