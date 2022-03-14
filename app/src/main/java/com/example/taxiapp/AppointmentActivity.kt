package com.example.taxiapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.taxiapp.databinding.ActivityAppointmentBinding
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.gson.Gson
import java.util.*


data class DistanceMatrixRoot(

    val destination_addresses: List<String>,

    val origin_addresses: List<String>,

    val rows: List<Row>,

    val status: String,

    )

data class Row(

    val elements: List<Element>,

    )

data class Element(

    val distance: Distance,

    val duration: Duration,

    val status: String,

    )

data class Distance(

    val text: String,

    val value: Int,

    )

data class Duration(

    val text: String,

    val value: Int,

    )


class AppointmentActivity : AppCompatActivity() {

    var clientname: String? = null

    var pickupID: String? = null

    var pickup: String? = null

    var destinationID: String? = null

    var destination: String? = null

    private lateinit var binding: ActivityAppointmentBinding

    private lateinit var fusedLocationClient: FusedLocationProviderClient


    private val myCalendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Places.initialize(applicationContext, "AIzaSyCgqQfyspQQ-cq-enAKgCQPmVkSb1bcLjM")

        // Create a new PlacesClient instance
        Places.createClient(this)

        autocompleteLogic(R.id.autocomplete_fragment1)

        autocompleteLogic(R.id.autocomplete_fragment2)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // val filter =
        //  InputFilter { source, start, end, dest, dstart, dend ->
        //      for (i in start until end) {
        //
        //
        //           when (source[i]) {
        //              '/'->  null
        //              '0'->  null
        //              '1'->  null
        //              '2'->  null
        //              '3'->  null
        //              '4'->  null
        //              '5'->  null
        //              '6'->  null
        //              '7'->  null
        //              '8'->  null
        //              '9'->  null
        //             else -> { // Note the block
        //                  return@InputFilter ""
        //         }
        //     }
        //
        //      if (i == 1) {
        //        return@InputFilter '/'.toString()
        //      }
        //
        //    }
        //     null
        //   }
        //  binding.editTextDate.filters = arrayOf(filter)

        val dateStart =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth -> // TODO Auto-generated method stub
                binding.textViewDateField.text =
                    "" + dayOfMonth + "/" + (monthOfYear + 1) + "/" + year

                myCalendar[Calendar.YEAR] = year
                myCalendar[Calendar.MONTH] = monthOfYear
                myCalendar[Calendar.DATE] = dayOfMonth

            }



        binding.btnDatePicker.setOnClickListener {

            DatePickerDialog(
                // pass the Context
                this,
                // listener to perform task
                // when time is picked
                dateStart, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DATE)
            ).show()


        }


        var timeStart =
            OnTimeSetListener { view, hourOfDay, minute ->

                binding.textViewTimeField.text =
                    "" + hourOfDay + ":" + minute

                myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                myCalendar.set(Calendar.MINUTE, minute)


            }


        binding.btnTimePicker.setOnClickListener {


            TimePickerDialog(
                // pass the Context
                this,
                // listener to perform task
                // when time is picked
                timeStart,
                // default hour when the time picker
                // dialog is opened
                myCalendar.get(Calendar.HOUR_OF_DAY),
                // default minute when the time picker
                // dialog is opened
                myCalendar.get(Calendar.MINUTE),
                // 24 hours time picker is
                // true
                true
            )

                .show()
        }

    }


    // listener which is triggered when the
    // time is picked from the time picker dialog


    //  private fun setDate() {
    //      showDialog(999)
    // }


    // override fun onCreateDialog(id: Int): Dialog? {
    //     var calendar = Calendar.getInstance()
    //      val year = calendar.get(Calendar.YEAR)
    //      val month = calendar.get(Calendar.MONTH)
    //   val day = calendar.get(Calendar.DAY_OF_MONTH)

    //    return DatePickerDialog(this,
    //        { view, year, monthOfYear, dayOfMonth ->
    //           binding.textView.text = "" + dayOfMonth + "/" + (monthOfYear + 1) + "/" + year
    //       }, year, month, day)

    // }


    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun requestNewLocationData(): String {

        var epochTimeStart = myCalendar.timeInMillis

        Log.i("Time", epochTimeStart.toString())
        Log.i("Time", myCalendar.toString())

        val sharedPreferences: SharedPreferences =
            getSharedPreferences("MySharedPref", MODE_PRIVATE)

        val retrieveUnit = sharedPreferences.getString("unit:", "")

        val retrieveRate = sharedPreferences.getString("rate:", "")


        if (isLocationEnabled(this)) {

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        // use your location object
                        // get latitude , longitude and other info from this
                        binding.textView2Test.text =
                            location.latitude.toString() + "," + location.longitude.toString()

                        val queue = Volley.newRequestQueue(this)

                        val url =
                            "https://maps.googleapis.com/maps/api/distancematrix/json?units=${retrieveUnit.toString()}&origins=${location.latitude.toString() + "," + location.longitude.toString()}|place_id:$pickupID&destinations=place_id:$pickupID|place_id:$destinationID&key=AIzaSyCgqQfyspQQ-cq-enAKgCQPmVkSb1bcLjM"

                        val stringRequest = StringRequest(
                            Request.Method.GET, url,
                            { response ->

                                this.title = "Results"

                                val gson = Gson()

                                try {

                                    val mDistanceMatrix =
                                        gson.fromJson(response, DistanceMatrixRoot::class.java)


                                    binding.textView2Test.text =
                                        "currentlocation to pickup distance: " + mDistanceMatrix.rows[0].elements[0].distance.text + "\n" + "currentlocation to pickup distance: " + mDistanceMatrix.rows[0].elements[0].duration.text + "\n" + "pickup to destination distance: " + mDistanceMatrix.rows[1].elements[1].distance.text + "\n" + "pickup to destination duration: " + mDistanceMatrix.rows[1].elements[1].duration.text

                                    /*  useful row 0 element 0    currentlocation to pickup distance
                                    useful   row 0 element 0    currentlocation to pickup duration

                                    useful  row 1 element 1  pickup to destination distance
                                    useful  row 1 element 1  pickup to destination duration

                                    useless row 1 element 0  currentlocation to des distance
                                    useless row 1 element 0  currentlocation to pickup duration

                                    useful row 0 element 1    pickup to pickup distance
                                    useful row 0 element 1    pickup to pickup duration */

                                    // total distance uses return text of distance splits it at the space delimiter and takes the first element and parses it into a double

                                    val totalDistance =
                                        mDistanceMatrix.rows[0].elements[0].distance.text.split(" ")[0].toDouble() + mDistanceMatrix.rows[1].elements[1].distance.text.split(
                                            " "
                                        )[0].toDouble()

                                    // totalduration is the sum of the duration between currentlocation to pickup and pickup to destination. Value property is in seconds.

                                    val totalDurationMilliseconds =
                                        (mDistanceMatrix.rows[0].elements[0].duration.value + mDistanceMatrix.rows[1].elements[1].duration.value) * 1000

                                    Log.i(TAG, totalDistance.toString())
                                    Log.i(TAG, totalDurationMilliseconds.toString())


                                    val totalCost = "%.2f".format(
                                        totalDistance * retrieveRate.toString().toDouble()
                                    )


                                    Log.i(TAG, totalCost)

                                    //TODO
                                    // convert API distance to KM and Metric depending on settings - DONE
                                    // parse start time - DONE
                                    // Calculate event end time by processing pickup to destination duration - parse into timefromepoch millis - DONE + FIXED
                                    // Calculate event reminder length by processing currentlocation to pickup - parse into timefromepoch millis
                                    // Ensure that the calender does not overlap with any other events
                                    // Create a dialog box to inform the user that the location is off and ask them to turn it on - DONE
                                    // Validation on each text field


                                    val intent = Intent(Intent.ACTION_INSERT)
                                        .setData(CalendarContract.Events.CONTENT_URI)
                                        .putExtra(
                                            CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                                            epochTimeStart
                                        )
                                        .putExtra(
                                            CalendarContract.EXTRA_EVENT_END_TIME,
                                            epochTimeStart + totalDurationMilliseconds
                                        )
                                        .putExtra(
                                            CalendarContract.Events.TITLE,
                                            "Appointment for $clientname"
                                        )

                                        //why are these reversed???
                                        .putExtra(
                                            CalendarContract.Events.DESCRIPTION,
                                            pickup
                                        )
                                        .putExtra(
                                            CalendarContract.Events.EVENT_LOCATION,
                                            "Estimated fare for the job: £$totalCost + Duration of job: $totalDurationMilliseconds ms"
                                        )


                                        .putExtra(CalendarContract.Events.AVAILABILITY,
                                            CalendarContract.Events.AVAILABILITY_BUSY)
                                        .putExtra(Intent.EXTRA_EMAIL,
                                            "rowan@example.com,trevor@example.com")
                                    startActivity(intent)


                                } catch (e: Throwable) {

                                    Log.e(TAG, e.toString())

                                    binding.textView2Test.text = e.toString()

                                }

                            },

                            { this.title = "That didn't work!" })

                        queue.add(stringRequest)

                    } else {

                        this.title = "An error has occurred"

                    }


                }

        } else {

            binding.textView2Test.text =
                "Please turn on location"

        }

        return "swag"
    }


    private fun autocompleteLogic(autocompleteID: Int) {
        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(autocompleteID)
                    as AutocompleteSupportFragment

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME))

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {

                if (autocompleteID == R.id.autocomplete_fragment1) {

                    pickupID = place.id
                    pickup = place.name

                    Log.i(TAG, "Place1: ${pickup}, ${place.id}")

                } else {

                    destinationID = place.id
                    destination = place.name

                    Log.i(TAG, "Place2: ${destination}, ${place.id}")

                }

            }

            override fun onError(status: Status) {

                Log.i(TAG, "An error occurred: $status")
            }

        })

    }

    fun submitBtnOnClick(view: View) {


        clientname = binding.editTextTextPersonName.text.toString()


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_DENIED


        ) {


            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION),
                1)

        }


        requestNewLocationData()


    }

    private fun isLocationEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is a new method provided in API 28
            val lm = context.getSystemService(LOCATION_SERVICE) as LocationManager

            lm.isLocationEnabled

        } else {
            // This was deprecated in API 28
            val mode: Int =
                Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE,

                    Settings.Secure.LOCATION_MODE_OFF)

            mode != Settings.Secure.LOCATION_MODE_OFF

        }

    }
}


