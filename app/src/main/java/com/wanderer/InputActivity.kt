package com.wanderer

import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.maps.model.LatLng
import com.wanderer.databinding.ActivityInputBinding

class InputActivity : AppCompatActivity() {

    private var binding : ActivityInputBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.btnRoute?.setOnClickListener {

            //Checking the inputs first
            if(validateInputs()){ //if the inputs are okay:
                //convert user inputs to double
                val startLat : Double = binding?.etStartLat?.text.toString().toDouble()
                val startLon : Double = binding?.etStartLon?.text.toString().toDouble()
                val endLat : Double = binding?.etEndLat?.text.toString().toDouble()
                val endLon : Double = binding?.etEndLon?.text.toString().toDouble()

                PyBackend.startPoint = LatLng(startLat,startLon)
                PyBackend.endPoint = LatLng(endLat,endLon)

                val output = PyBackend.getRoute(this@InputActivity, startLat, startLon, endLat, endLon)

                if (output == null){
                    Toast.makeText(this@InputActivity, "output = null", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this@InputActivity, "Got Route Successfully", Toast.LENGTH_SHORT).show()
                    //Sets the TextView to the output of the python code
                    binding?.tvOutput?.text = PyBackend.multiRouteCoordinatesList.toString()
                    //Makes the TextView scrollable
                    binding?.tvOutput?.movementMethod = ScrollingMovementMethod()
                }
            }else{ //if inputs are not okay display this Toast to the user
                Toast.makeText(this@InputActivity, "Please fill the inputs", Toast.LENGTH_SHORT).show()
            }
        }

        binding?.btnViewRoute?.setOnClickListener {
            val intent = Intent(this@InputActivity, ResultActivity::class.java)
            startActivity(intent)
        }
    }

    //Function that checks the validity of the user inputs.
    private fun validateInputs (): Boolean{
        var isValid = true
        if(binding?.etStartLat?.text.toString().isEmpty() ||
            binding?.etStartLat?.text.toString().toDoubleOrNull() == null){
            isValid = false
        }else if (binding?.etStartLon?.text.toString().isEmpty() ||
            binding?.etStartLon?.text.toString().toDoubleOrNull() == null){
            isValid = false
        }else if (binding?.etEndLat?.text.toString().isEmpty() ||
            binding?.etEndLat?.text.toString().toDoubleOrNull() == null){
            isValid = false
        }else if (binding?.etEndLon?.text.toString().isEmpty() ||
            binding?.etEndLon?.text.toString().toDoubleOrNull() == null){
            isValid = false
        }
        return isValid
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}