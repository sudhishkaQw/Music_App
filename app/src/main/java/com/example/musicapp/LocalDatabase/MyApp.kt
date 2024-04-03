package com.example.musicapp.LocalDatabase

import android.app.Application
import androidx.room.Room

class MyApp : Application() {
    companion object {
        lateinit var database: MyDatabase
    }

    override fun onCreate() {
        super.onCreate()
        database = MyDatabase.getDatabase(this)
    }
}
