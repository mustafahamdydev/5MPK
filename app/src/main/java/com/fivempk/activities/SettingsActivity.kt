package com.fivempk.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.fivempk.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private var binding : ActivitySettingsBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding!!.privacyBtn.setOnClickListener{
            val intent = Intent(this,PrivacyActivity::class.java)
            startActivity(intent)

        }
        binding!!.profileBtn.setOnClickListener{
            val intent = Intent(this,UserProfileActivity::class.java)
            startActivity(intent)

        }
        binding!!.changePassBtn.setOnClickListener{
            val intent = Intent(this,ChangePasswordActivity::class.java)
            startActivity(intent)

        }
        binding!!.aboutUsBtn.setOnClickListener{
            val intent = Intent(this,AboutUsActivity::class.java)
            startActivity(intent)

        }


    }
}