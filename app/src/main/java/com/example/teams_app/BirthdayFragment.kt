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
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    private val memberNames = mapOf(
        "Member1" to "Sakthi Sarvani R",
        "Member2" to "Sowndarya Meenakshi A",
        "Member3" to "Yogeetha K"
    )

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
        birthdayManager = BirthdayManager(requireContext())

        member1DateText = view.findViewById(R.id.tvMember1Date)
        member1TimeText = view.findViewById(R.id.tvMember1Time)
        member2DateText = view.findViewById(R.id.tvMember2Date)
        member2TimeText = view.findViewById(R.id.tvMember2Time)
        member3DateText = view.findViewById(R.id.tvMember3Date)
        member3TimeText = view.findViewById(R.id.tvMember3Time)

        setupMemberButton(view, "Member1", R.id.btnMember1, member1DateText, member1TimeText)
        setupMemberButton(view, "Member2", R.id.btnMember2, member2DateText, member2TimeText)
        setupMemberButton(view, "Member3", R.id.btnMember3, member3DateText, member3TimeText)

        loadSavedBirthday("Member1", member1DateText, member1TimeText)
        loadSavedBirthday("Member2", member2DateText, member2TimeText)
        loadSavedBirthday("Member3", member3DateText, member3TimeText)
    }

    private fun setupMemberButton(
        view: View,
        memberKey: String,
        buttonId: Int,
        dateText: TextView,
        timeText: TextView
    ) {
        view.findViewById<Button>(buttonId).setOnClickListener {
            showDateTimePicker(memberKey, dateText, timeText)
        }
    }

    private fun loadSavedBirthday(memberKey: String, dateText: TextView, timeText: TextView) {
        val birthday = birthdayManager.getBirthday(memberKey)
        if (birthday != null) {
            dateText.text = "Date: ${dateFormat.format(Date(birthday.date))}"
            timeText.text = "Time: ${timeFormat.format(Date(birthday.time))}"
        } else {
            dateText.text = "Date: Not Set"
            timeText.text = "Time: Not Set"
        }
    }

    private fun showDateTimePicker(
        memberKey: String,
        dateText: TextView,
        timeText: TextView
    ) {
        val calendar = Calendar.getInstance()
        birthdayManager.getBirthday(memberKey)?.let {
            calendar.timeInMillis = it.date
        }

        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                calendar.set(year, month, day)
                dateText.text = "Date: ${dateFormat.format(calendar.time)}"

                TimePickerDialog(
                    requireContext(),
                    { _, hour, minute ->
                        val timeCalendar = Calendar.getInstance()
                        timeCalendar.set(Calendar.HOUR_OF_DAY, hour)
                        timeCalendar.set(Calendar.MINUTE, minute)

                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)

                        timeText.text = "Time: ${timeFormat.format(calendar.time)}"

                        val birthday = Birthday(
                            memberKey,
                            calendar.timeInMillis,
                            timeCalendar.timeInMillis
                        )
                        birthdayManager.saveBirthday(birthday)

                        Toast.makeText(
                            requireContext(),
                            "Birthday notification set for ${memberNames[memberKey]} at ${timeFormat.format(calendar.time)}",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis() - 1000
            show()
        }
    }

    companion object {
        fun newInstance() = BirthdayFragment()
    }
}