package com.example.musicapp

import android.content.Context
import android.media.MediaPlayer

class MusicPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    fun playSong(songUrl: String) {
        mediaPlayer?.apply {
            reset()
            setDataSource(songUrl)
            prepare()
            start()
        }
    }

    fun stopPlayback() {
        mediaPlayer?.stop()
    }
}
