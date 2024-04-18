package com.fivempk.activities

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fivempk.R
import com.fivempk.databinding.ActivityFaqactivityBinding
import com.fivempk.databinding.ActivityForgotPassBinding

class FAQActivity : AppCompatActivity() {
    private var binding : ActivityFaqactivityBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFaqactivityBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        var isButtonChecked = false // Initially, the button is unchecked
        binding!!.q1.setOnClickListener {
            isButtonChecked = !isButtonChecked // Toggle the state
            if (isButtonChecked) {
                // Code to run when q1 is checked
                binding!!.answer1.visibility = View.VISIBLE // For example, make answer1 visible
            } else {
                binding!!.q1.isChecked = false
                // Code to run when q1 is unchecked
                binding!!.answer1.visibility = View.GONE // For example, make answer1 invisible
            }
        }
        binding!!.q2.setOnClickListener {
            isButtonChecked = !isButtonChecked // Toggle the state
            if (isButtonChecked) {
                // Code to run when q1 is checked
                binding!!.answer2.visibility = View.VISIBLE // For example, make answer1 visible
            } else {
                binding!!.q2.isChecked = false
                // Code to run when q1 is unchecked
                binding!!.answer2.visibility = View.GONE // For example, make answer1 invisible
            }
        }
        binding!!.q3.setOnClickListener {
            isButtonChecked = !isButtonChecked // Toggle the state
            if (isButtonChecked) {
                // Code to run when q1 is checked
                binding!!.answer3.visibility = View.VISIBLE // For example, make answer1 visible
            } else {
                binding!!.q3.isChecked = false
                // Code to run when q1 is unchecked
                binding!!.answer3.visibility = View.GONE // For example, make answer1 invisible
            }
        }
        binding!!.q4.setOnClickListener {
            isButtonChecked = !isButtonChecked // Toggle the state
            if (isButtonChecked) {
                // Code to run when q1 is checked
                binding!!.answer4.visibility = View.VISIBLE // For example, make answer1 visible
            } else {
                binding!!.q4.isChecked = false
                // Code to run when q1 is unchecked
                binding!!.answer4.visibility = View.GONE // For example, make answer1 invisible
            }
        }
        binding!!.q5.setOnClickListener {
            isButtonChecked = !isButtonChecked // Toggle the state
            if (isButtonChecked) {
                // Code to run when q1 is checked
                binding!!.answer5.visibility = View.VISIBLE // For example, make answer1 visible
            } else {
                binding!!.q5.isChecked = false
                // Code to run when q1 is unchecked
                binding!!.answer5.visibility = View.GONE // For example, make answer1 invisible
            }
        }


    }
}