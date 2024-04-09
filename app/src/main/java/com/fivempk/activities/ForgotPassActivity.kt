package com.fivempk.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fivempk.R
import com.fivempk.databinding.ActivityForgotPassBinding

class ForgotPassActivity : AppCompatActivity() {

    private var binding : ActivityForgotPassBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityForgotPassBinding.inflate(layoutInflater)
        setContentView(binding?.root)

    }
}