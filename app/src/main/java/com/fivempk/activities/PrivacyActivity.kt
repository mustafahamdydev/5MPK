package com.fivempk.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import com.fivempk.R
import com.fivempk.databinding.ActivityPrivacyBinding
import com.google.maps.android.Context

class PrivacyActivity : AppCompatActivity() {

    private var binding : ActivityPrivacyBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPrivacyBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val textView = binding!!.tvPrivacy
        val text = readTextFile(this, R.raw.privacy_policy)
        textView.text=text

    }
    private fun readTextFile(context: PrivacyActivity, resourceId: Int): String {
        val inputStream = resources.openRawResource(resourceId)
        return inputStream.bufferedReader().use { it.readText()}
    }
}