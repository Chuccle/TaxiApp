package com.example.taxiapp


import android.content.SharedPreferences
import android.os.Bundle
import android.widget.CompoundButton
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.example.taxiapp.databinding.ActivitySettingsBinding


class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences: SharedPreferences =
            getSharedPreferences("MySharedPref", MODE_PRIVATE)

        val myEdit = sharedPreferences.edit()

        setContentView(R.layout.activity_settings)

        binding = ActivitySettingsBinding.inflate(layoutInflater)

        setContentView(binding.root)


        // we assign the value to what exists in the shared preferences otherwise it will be 0.00
        if (sharedPreferences.getString("rate:", "") != "") {

            binding.editTextRate.setText(sharedPreferences.getString("rate:", ""))

        } else {

            binding.editTextRate.setText(resources.getString(R.string._0_00))

        }


        val switchUnit = findViewById<Switch>(R.id.switchUnit)


        // we assign the position of the switch based on the value of unit in shared preferences, we also assign the value to the textview
        // we also input the value into the shared preferences editor so that it can be saved without a switch event
        if (sharedPreferences.getString("unit:", "") == "metric") {

            switchUnit.isChecked = true

            binding.textViewPricePerUnit.text = resources.getString(R.string.kmRateTitle)

            myEdit.putString("unit:", "metric")

        } else {

            switchUnit.isChecked = false

            binding.textViewPricePerUnit.text = resources.getString(R.string.miRateTitle)

            myEdit.putString("unit:", "imperial")

        }


        switchUnit.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->


            // We parse the value of the edit text and assigning it to a variable
            // We need the try-catch because if the value of our edittext is empty we will get an error
            var dEditText = try {

                binding.editTextRate.text.toString().toDouble()

            } catch (e: Exception) {
                0.00
            }


            // We perform the conversion of dEditText based on when the unit is swapped from either metric or imperial
            // We also set the Shared Preferences editor to the corresponding unit
            if (isChecked) {

                binding.textViewPricePerUnit.text = resources.getString(R.string.kmRateTitle)
                //Mathematical operation to convert miles to km


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
                //Mathematical operation to convert km to miles
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
            //commit the changes
            myEdit.apply()

        }
    }


    // We trap the user inside this activity until we have a rate and unit
    override fun onBackPressed() {

        val sharedPreferences: SharedPreferences =
            getSharedPreferences("MySharedPref", MODE_PRIVATE)

        if (sharedPreferences.getString("rate:", "") != "" && sharedPreferences.getString(
                "unit:",
                ""
            ) != ""
        ) {

            super.onBackPressed()

        }


    }

}




