package com.example.musicapp.model

import androidx.lifecycle.ViewModelProvider
import com.example.musicapp.LocalDatabase.DownloadedSongDao

class MyViewModelFactory(private val dataSource: DownloadedSongDao) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(MyViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return MyViewModel(dataSource) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
}
