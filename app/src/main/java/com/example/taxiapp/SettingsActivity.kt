package com.example.taxiapp


import android.os.Bundle
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


        if (sharedPreferences.getString("rate:", "") != "") {

            binding.editTextRate.setText(sharedPreferences.getString("rate:", ""))

        }


        val switchUnit = findViewById<Switch>(R.id.switchUnit)


        switchUnit.isChecked = sharedPreferences.getString("unit:", "") == "metric"


        switchUnit.setOnCheckedChangeListener { _: CompoundButton?, isMetric: Boolean ->


            var dEditText = binding.editTextRate.text.toString().toDouble()


            if (isMetric) {

                binding.textViewPricePerUnit.text = resources.getString(R.string.kmRateTitle)

                dEditText *= 0.6213712

                binding.editTextRate.setText(
                    resources.getString(
                        R.string.parameter,
                        "%.2f".format(dEditText)
                    )
                )

                myEdit.putString("unit:", "metric")

            } else {


                binding.textViewPricePerUnit.text = resources.getString(R.string.miRateTitle)

                dEditText *= 1.609344

                binding.editTextRate.setText(
                    resources.getString(
                        R.string.parameter,
                        "%.2f".format(dEditText)
                    )
                )

                myEdit.putString("unit:", "imperial")

            }

        }

        binding.buttonSave.setOnClickListener {

// Storing the key and its value as the data fetched from edittext
            myEdit.putString("rate:", binding.editTextRate.text.toString())

            myEdit.apply()

        }
    }

}




