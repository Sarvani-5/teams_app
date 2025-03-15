// TimeTableData.kt
package com.example.teams_app

object TimeTableData {
    val timeTable = mapOf(
        "Monday" to mapOf(
            "9:00" to "EEAFC",
            "9:50" to "AI",
            "10.40" to "Break",
            "11:00" to "DAD",
            "11:50" to "PSOM",
            "12.40" to "Lunch",
            "13:40" to "MAD Lab",
            "14:30" to "MAD LAB Continued...",
            "15:20" to "Project II",
            "16:10" to "Skillrack"
        ),
        "Tuesday" to mapOf(
            "9:00" to "Fuzzy",
            "9:50" to "EEAFC",
            "10.40" to "Break",
            "11:00" to "PC[theory]",
            "11:50" to "IR",
            "12.40" to "Lunch",
            "13:40" to "Project II",
            "14:30" to "Project II",
            "15:20" to "TWM",
            "16:10" to "TWM"
        ),
        "Wednesday" to mapOf(
            "9:00" to "DAD",
            "9:50" to "Fuzzy",
            "10.40" to "Break",
            "11:00" to "IR",
            "11:50" to "AI",
            "12.40" to "Lunch",
            "13:40" to "PC Lab",
            "14:30" to "PC Lab",
            "15:20" to "Project II",
            "16:10" to "Skillrack"
        ),
        "Thursday" to mapOf(
            "9:00" to "PSOM",
            "9:50" to "AI",
            "10.40" to "Break",
            "11:00" to "MAD[Theory]",
            "11:50" to "EEAFC",
            "12.40" to "Lunch",
            "13:40" to "MAD LAB",
            "14:30" to "MAD LAB",
            "15:20" to "Project II",
            "16:10" to "Peer Coaching"
        ),
        "Friday" to mapOf(
            "9:00" to "IR",
            "9:50" to "DAD",
            "10.40" to "Break",
            "11:00" to "PSOM",
            "11:50" to "Fuzzy",
            "12.40" to "Lunch",
            "13:40" to "DS LAB",
            "14:30" to "DS LAB",
            "15:20" to "Project II",
            "16:10" to "Career Guidance"
        )
    )

    val subjectFullNames = mapOf(
        "EEAFC" to "Engineering Economics and Accounting Finance",
        "AI" to "Artificial Intelligence",
        "DAD" to "Distributed Application Development",
        "PSOM" to "Privacy and Security in Online Social Media",
        "MAD" to "Mobile Application Development",
        "Fuzzy" to "Fuzzy Systems",
        "PC" to "Professional Communication",
        "IR" to "Information Retrieval",
        "DS" to "Data Science Lab",
        "TWM" to "Tutor Ward Meeting",
        "Break" to "Break Time",
        "Lunch" to "Lunch Break"
    )
}