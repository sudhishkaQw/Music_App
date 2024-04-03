package com.example.musicapp.LocalDatabase

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

object LiveDataBus {
    private val songListLiveData: MutableLiveData<List<DownloadedSong>> = MutableLiveData()

    fun postDownloadedSongs(songs: List<DownloadedSong>) {
        songListLiveData.postValue(songs)
    }

    fun observeDownloadedSongs(owner: LifecycleOwner, observer: (List<DownloadedSong>) -> Unit) {
        songListLiveData.observe(owner, Observer { songs ->
            songs?.let {
                observer.invoke(it)
            }
        })
    }
}