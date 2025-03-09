package com.example.teams_app

data class MemberInfo(
    var id: String = "",
    var name: String = "",
    var role: String = "",
    var bio: String = "",
    var description: String = "", // Added to match what's used in DataMigrationUtil
    var email: String = "",
    var phone: String = "",
    var imageUrl: String = "",
    var imageResId: Int = 0 // Added for local resource images
)