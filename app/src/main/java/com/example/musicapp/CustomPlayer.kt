package com.example.musicapp

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.util.Log
import com.example.musicapp.model.SongsModel
import java.io.IOException

class CustomPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var playbackStateChangeListener: OnPlaybackStateChangeListener? = null
    private var currentSongUrl: String? = null
    private var currentSong : SongsModel? =null

    interface OnPlaybackStateChangeListener {
        fun onPlaybackStateChanged(isPlaying: Boolean)
        fun onLoadingStateChanged(isLoading: Boolean)
        fun onError(errorMessage: String)
    }
    init {
        mediaPlayer=MediaPlayer()
    }

    fun setPlaybackStateChangeListener(listener: OnPlaybackStateChangeListener) {
        this.playbackStateChangeListener = listener
    }
    fun getCurrentSong() : SongsModel?{
        return currentSong
    }

    fun play(url: String) {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            }
            playbackStateChangeListener?.onLoadingStateChanged(true)
            currentSongUrl = url
            mediaPlayer = MediaPlayer()
            mediaPlayer?.apply {
                setDataSource(url)
                prepareAsync()
                setOnPreparedListener {
                    startPlayback()
                }
                setOnCompletionListener {
                    playbackStateChangeListener?.onPlaybackStateChanged(false)
                }
                setOnErrorListener { _, _, _ ->
                    handleError("Error during playback")
                    false
                }
            }
        } catch (e: IOException) {
            handleError("Error playing song: ${e.message}")
        }
    }

    fun isSongPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    private fun startPlayback() {
        mediaPlayer?.start()
        playbackStateChangeListener?.onLoadingStateChanged(false)
        playbackStateChangeListener?.onPlaybackStateChanged(true)
    }

    fun pause() {
        mediaPlayer?.pause()
        playbackStateChangeListener?.onPlaybackStateChanged(false)
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        currentSongUrl = null
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }
    fun reset()
    {
        mediaPlayer?.reset()
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    private fun handleError(errorMessage: String) {
        Log.e(TAG, errorMessage)
        release()
        playbackStateChangeListener?.onLoadingStateChanged(false)
        playbackStateChangeListener?.onError(errorMessage)
    }

    companion object {
        private const val TAG = "CustomPlayer"
    }

    fun createUpdateSeekBarRunnable(): Runnable {
        return object : Runnable {
            override fun run() {
                val currentPosition = getCurrentPosition()
                val totalDuration = getDuration()

                // Update startingChronometer and endChronometer
                playbackStateChangeListener?.onLoadingStateChanged(false)
                playbackStateChangeListener?.onPlaybackStateChanged(isPlaying())

                // Schedule the next update after 1 second
                Handler().postDelayed(this, 1000)
            }
        }
    }
}