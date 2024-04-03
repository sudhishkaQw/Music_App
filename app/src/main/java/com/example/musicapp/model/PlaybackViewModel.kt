package com.example.musicapp.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlaybackViewModel  : ViewModel() {
    val isPlaying: MutableLiveData<Boolean> = MutableLiveData(false)

    fun setPlaybackState(isPlaying: Boolean) {
        this.isPlaying.value = isPlaying
    }
}