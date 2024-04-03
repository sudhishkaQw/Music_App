package com.example.musicapp.model

import android.util.Log

data class UserModel(
    val username : String,
    val profileImage: String,
    val creditScore:Int,
    val uid:String,
    val purchasedSongs: List<String> = emptyList(),
    val favoriteSongs :List<String> = emptyList()

) {
    constructor() : this("", "",500,"",) {
        Log.d("UserModel", "Empty constructor called")
    }
    init {
        Log.d("UserModel", "UserModel instance created - Name: $username, profileUrl: $profileImage,creditScore -CreditScore:$creditScore")
    }
}

