package com.example.taxiapp


import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.CompoundButton
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.example.taxiapp.databinding.ActivitySettingsBinding


class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)

        val myEdit = sharedPreferences.edit()

        setContentView(R.layout.activity_settings)

        binding = ActivitySettingsBinding.inflate(layoutInflater)

        setContentView(binding.root)


            binding.editTextRate.setText(sharedPreferences.getString("rate:", ""))


// needs to initialised
       // var dEditText: Double

        var switchUnit = findViewById<Switch>(R.id.switchUnit)


        switchUnit.isChecked = sharedPreferences.getString("unit:", "") == "metric"


        switchUnit.setOnCheckedChangeListener { _: CompoundButton?, isMetric:Boolean ->

         var dEditText = try {

                binding.editTextRate.text.toString().toDouble()

            } catch(e:NumberFormatException) {

                Log.wtf(TAG, e.toString())

                binding.editTextRate.setText("0.00")

             0.00

            }

            if (isMetric) {

                binding.textViewPricePerUnit.text = "Rate per Kilometre"

                dEditText *= 0.6213712

                binding.editTextRate.setText("%.2f".format(dEditText))

                myEdit.putString("unit:", "metric")

            } else {

                binding.textViewPricePerUnit.text = "Rate per Mile"

                dEditText *= 1.609344

                binding.editTextRate.setText("%.2f".format(dEditText))

                myEdit.putString("unit:", "imperial")

            }

        }

        binding.buttonSave.setOnClickListener {

// Storing the key and its value as the data fetched from edittext
            myEdit.putString("rate:", binding.editTextRate.text.toString())

            myEdit.commit()

        }
    }

}




