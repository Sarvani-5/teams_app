package com.example.teams_app

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MemberDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member_detail)

        // Get data from intent
        val name = intent.getStringExtra("MEMBER_NAME") ?: ""
        val role = intent.getStringExtra("MEMBER_ROLE") ?: ""
        val email = intent.getStringExtra("MEMBER_EMAIL") ?: ""
        val phone = intent.getStringExtra("MEMBER_PHONE") ?: ""
        val description = intent.getStringExtra("MEMBER_DESCRIPTION") ?: ""
        val imageResId = intent.getIntExtra("MEMBER_IMAGE", 0)

        // Set up views
        findViewById<TextView>(R.id.toolbarTitle).text = name
        findViewById<TextView>(R.id.memberName).text = name
        findViewById<TextView>(R.id.memberRole).text = role
        findViewById<TextView>(R.id.memberDescription).text = description
        findViewById<TextView>(R.id.memberEmail).text = email
        findViewById<TextView>(R.id.memberPhone).text = phone
        findViewById<ImageView>(R.id.memberImage).setImageResource(imageResId)
    }
}