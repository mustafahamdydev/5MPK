package com.fivempk.activities

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.fivempk.R
import com.fivempk.databinding.ActivityForgotPassBinding

class ForgotPassActivity : AppCompatActivity() {

    private var binding : ActivityForgotPassBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityForgotPassBinding.inflate(layoutInflater)
        setContentView(binding?.root)

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
}