package com.example.musicapp.model

import android.util.Log
import java.net.URL

data class CategoryModel(
    val name : String,
    val coverUrl: String,
    var songs:List<String>

) {
    constructor(): this("", "", listOf()) {
        Log.d("CategoryModel", "Empty constructor called")
    }

    init {
        Log.d("CategoryModel", "CategoryModel instance created - Name: $name, Cover URL: $coverUrl")
    }
}
