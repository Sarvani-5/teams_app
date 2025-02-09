package com.example.teams_app

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

class BirthdayFragment : Fragment() {
    private lateinit var birthdayManager: BirthdayManager

    // Date and time formatters
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    // Member TextViews for dates and times
    private lateinit var member1DateText: TextView
    private lateinit var member1TimeText: TextView
    private lateinit var member2DateText: TextView
    private lateinit var member2TimeText: TextView
    private lateinit var member3DateText: TextView
    private lateinit var member3TimeText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_birthday, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize BirthdayManager
        birthdayManager = BirthdayManager(requireContext())

        // Initialize TextViews
        member1DateText = view.findViewById(R.id.tvMember1Date)
        member1TimeText = view.findViewById(R.id.tvMember1Time)
        member2DateText = view.findViewById(R.id.tvMember2Date)
        member2TimeText = view.findViewById(R.id.tvMember2Time)
        member3DateText = view.findViewById(R.id.tvMember3Date)
        member3TimeText = view.findViewById(R.id.tvMember3Time)

        // Setup member buttons
        setupMemberButton(view, "Member 1", R.id.btnMember1, member1DateText, member1TimeText)
        setupMemberButton(view, "Member 2", R.id.btnMember2, member2DateText, member2TimeText)
        setupMemberButton(view, "Member 3", R.id.btnMember3, member3DateText, member3TimeText)

        // Load saved birthdays
        loadSavedBirthday("Member 1", member1DateText, member1TimeText)
        loadSavedBirthday("Member 2", member2DateText, member2TimeText)
        loadSavedBirthday("Member 3", member3DateText, member3TimeText)
    }

    private fun setupMemberButton(
        view: View,
        memberName: String,
        buttonId: Int,
        dateText: TextView,
        timeText: TextView
    ) {
        view.findViewById<Button>(buttonId).setOnClickListener {
            showDateTimePicker(memberName, dateText, timeText)
        }
    }

    private fun loadSavedBirthday(memberName: String, dateText: TextView, timeText: TextView) {
        val birthday = birthdayManager.getBirthday(memberName)
        if (birthday != null) {
            val date = Date(birthday.date)
            val time = Date(birthday.time)
            dateText.text = dateFormat.format(date)
            timeText.text = timeFormat.format(time)
        } else {
            dateText.text = "Not Set"
            timeText.text = "Not Set"
        }
    }

    private fun showDateTimePicker(
        memberName: String,
        dateText: TextView,
        timeText: TextView
    ) {
        val calendar = Calendar.getInstance()

        // Get currently saved birthday if it exists
        birthdayManager.getBirthday(memberName)?.let {
            calendar.timeInMillis = it.date
        }

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                calendar.set(year, month, day)
                val dateString = dateFormat.format(calendar.time)
                dateText.text = dateString

                // After date is picked, show time picker
                TimePickerDialog(
                    requireContext(),
                    { _, hour, minute ->
                        // Create a new calendar instance for time
                        val timeCalendar = Calendar.getInstance()
                        timeCalendar.set(Calendar.HOUR_OF_DAY, hour)
                        timeCalendar.set(Calendar.MINUTE, minute)

                        // Set the time on the original calendar while preserving the date
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)

                        val timeString = timeFormat.format(calendar.time)
                        timeText.text = timeString

                        // Save birthday with separate date and time
                        val birthday = Birthday(
                            memberName,
                            calendar.timeInMillis,  // Date
                            timeCalendar.timeInMillis  // Time
                        )
                        birthdayManager.saveBirthday(birthday)

                        Toast.makeText(
                            requireContext(),
                            "Birthday notification set for $memberName at $timeString",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true // Use 24-hour format
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Set min date to today
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    companion object {
        fun newInstance() = BirthdayFragment()
    }
}