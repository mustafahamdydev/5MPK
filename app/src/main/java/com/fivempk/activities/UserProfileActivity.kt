package com.fivempk.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fivempk.R
import com.fivempk.databinding.ActivitySignUpBinding
import com.fivempk.databinding.ActivityUserProfileBinding

class UserProfileActivity : AppCompatActivity() {
    private var binding: ActivityUserProfileBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding?.root)

    }
}