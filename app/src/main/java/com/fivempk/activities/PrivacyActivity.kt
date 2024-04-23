package com.fivempk.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.fivempk.R
import com.fivempk.databinding.ActivityPrivacyBinding

class PrivacyActivity : AppCompatActivity() {

    private var binding : ActivityPrivacyBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPrivacyBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val textView = binding!!.tvPrivacy
        val text = readTextFile(R.raw.privacy_policy)
        textView.text=text

    }
    private fun readTextFile(resourceId: Int): String {
        val inputStream = resources.openRawResource(resourceId)
        return inputStream.bufferedReader().use { it.readText()}
    }
}