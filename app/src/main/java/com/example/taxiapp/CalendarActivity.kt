package com.example.taxiapp

import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.widget.CalendarView
import androidx.appcompat.app.AppCompatActivity
import com.example.taxiapp.databinding.ActivityCalendarBinding
import java.util.*

class CalendarActivity : AppCompatActivity() {
    // Define the variable of CalendarView type
    // and TextView type;
    private var calendar: CalendarView? = null

    private val myCalendar: Calendar = Calendar.getInstance()

    private lateinit var binding: ActivityCalendarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        calendar = binding.calendar

        // Add Listener in calendar

        calendar!!.setOnDateChangeListener { view, year, month, dayOfMonth ->
            // In this Listener have one method
            // and in this method we will
            // get the value of DAYS, MONTH, YEARS

            myCalendar[Calendar.YEAR] = year
            myCalendar[Calendar.MONTH] = month
            myCalendar[Calendar.DATE] = dayOfMonth

            val epochTimeStart = myCalendar.timeInMillis
            //This will go through the users calendars and from the calculated time range of the proposed event
            // and work out if it conflicts with any existing events

            val builder: Uri.Builder = CalendarContract.CONTENT_URI.buildUpon()
                .appendPath("time")

            ContentUris.appendId(builder, epochTimeStart)

            val intent = Intent(Intent.ACTION_VIEW)
                .setData(builder.build())

            startActivity(intent)

        }

    }

}

