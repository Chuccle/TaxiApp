package com.example.taxiapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.ContentUris
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
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

    var clientName: String = ""

    var pickupID: String? = null

    var pickup: String = ""

    var destinationID: String? = null

    var destination: String = ""

    private lateinit var binding: ActivityAppointmentBinding

    private lateinit var fusedLocationClient: FusedLocationProviderClient


    private val myCalendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Places.initialize(applicationContext, resources.getString(R.string.api_key))


        // Create a new PlacesClient instance
        Places.createClient(this)

        autocompleteLogic(R.id.autocomplete_fragment1)

        autocompleteLogic(R.id.autocomplete_fragment2)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

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


                //switch case to auto-format the time fields in a consistent way

                when {

                    hourOfDay < 10 && minute < 10 -> {

                        binding.textViewTimeField.text =
                            "0$hourOfDay:0$minute"

                    }


                    hourOfDay < 10 -> {

                        binding.textViewTimeField.text =
                            "0$hourOfDay:$minute"

                    }


                    minute < 10 -> {

                        binding.textViewTimeField.text =
                            "$hourOfDay:0$minute"

                    }

                    else -> {

                        binding.textViewTimeField.text =
                            "$hourOfDay:$minute"

                    }

                }

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

    private fun permissionCheck(): Boolean {

        //If any of the listed permissions are not granted then the function returns false

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_DENIED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_CALENDAR
            ) == PackageManager.PERMISSION_DENIED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CALENDAR
            ) == PackageManager.PERMISSION_DENIED


        ) {


            return false
        }

        return true
    }


    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun requestNewLocationData() {

        var epochTimeStart = myCalendar.timeInMillis

        Log.i("Time", epochTimeStart.toString())

        Log.i("Time", myCalendar.toString())

//shared preferences stores our preferences in local storage and retrieved here

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
                            "https://maps.googleapis.com/maps/api/distancematrix/json?units=${retrieveUnit.toString()}&origins=${location.latitude.toString() + "," + location.longitude.toString()}|place_id:$pickupID&destinations=place_id:$pickupID|place_id:$destinationID&key=${
                                resources.getString(
                                    R.string.api_key
                                )
                            }"

                        val stringRequest = StringRequest(
                            Request.Method.GET, url,
                            { response ->


                                this.title = "Results"

                                val gson = Gson()

                                try {

                                    val mDistanceMatrix =
                                        gson.fromJson(response, DistanceMatrixRoot::class.java)

                                    val totalDurationMilliseconds =
                                        (mDistanceMatrix.rows[0].elements[0].duration.value + mDistanceMatrix.rows[1].elements[1].duration.value) * 1000


                                    //This will go through the users calendars and from the calculated time range of the proposed event
                                    // and work out if it conflicts with any existing events


                                    // Projection array. Creating indices for this array instead of doing dynamic lookups improves performance.
                                    val INSTANCE_PROJECTION = arrayOf(
                                        CalendarContract.Instances.EVENT_ID,      // 0
                                        CalendarContract.Instances.BEGIN,         // 1
                                        CalendarContract.Instances.TITLE,          // 2
                                        CalendarContract.Instances.ORGANIZER,
                                    )

                                    val PROJECTION_ID_INDEX = 0
                                    val PROJECTION_BEGIN_INDEX = 1
                                    val PROJECTION_TITLE_INDEX = 2
                                    val PROJECTION_ORGANIZER_INDEX = 3


                                    val builder: Uri.Builder =
                                        CalendarContract.Instances.CONTENT_URI.buildUpon()

                                    // define time range for query
                                    ContentUris.appendId(builder, epochTimeStart)

                                    ContentUris.appendId(
                                        builder,
                                        epochTimeStart + totalDurationMilliseconds
                                    )

                                    // Submit the query
                                    val cur =
                                        contentResolver.query(
                                            builder.build(),
                                            INSTANCE_PROJECTION,
                                            null,
                                            null,
                                            null
                                        )

                                    // if event is found, then there we exit the function

                                    if (cur != null && cur.count > 0) {

                                        while (cur.moveToNext()) {

                                            val eventID = cur.getLong(PROJECTION_ID_INDEX)
                                            val beginVal = cur.getLong(PROJECTION_BEGIN_INDEX)
                                            val title = cur.getString(PROJECTION_TITLE_INDEX)
                                            val organizer =
                                                cur.getString(PROJECTION_ORGANIZER_INDEX)


                                            Log.i(
                                                "Event found",
                                                "Event ID: $eventID, Begin: $beginVal, Title: $title, Organizer: $organizer"
                                            )


                                            Toast.makeText(
                                                this,
                                                "Event ID: $title is booked for this time",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            return@StringRequest

                                        }
                                    }



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


                                    Log.i(TAG, totalDistance.toString())
                                    Log.i(TAG, totalDurationMilliseconds.toString())


                                    val totalCost = "%.2f".format(
                                        totalDistance * retrieveRate.toString().toDouble()
                                    )

                                    Log.i(TAG, totalCost)

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
                                            "Appointment for $clientName"
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


                                    val text = "An API error has occurred"
                                    val duration = Toast.LENGTH_SHORT

                                    val toast = Toast.makeText(applicationContext, text, duration)
                                    toast.show()

                                }

                            },

                            { this.title = "That didn't work!" })

                        queue.add(stringRequest)

                    } else {

                        this.title = "A location error has occurred"

                    }


                }

        } else {

            binding.textView2Test.text =
                "Please turn on location"

        }

        return
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

        clientName = binding.editTextTextPersonName.text.toString()

        if (clientName == "" || pickup == "" || destination == "") {

            val text = "Please fill in all fields"
            val duration = Toast.LENGTH_SHORT

            val toast = Toast.makeText(applicationContext, text, duration)
            toast.show()

            return

        }


        if (!permissionCheck()) {

            Toast.makeText(
                this,
                "Permissions are denied, please check your app settings",
                Toast.LENGTH_SHORT
            ).show()

            return

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


