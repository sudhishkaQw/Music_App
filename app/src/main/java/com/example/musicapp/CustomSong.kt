package com.example.musicapp

import android.media.MediaPlayer
import android.util.Log
import com.example.musicapp.model.SongsModel

object CustomSong {

    private var mediaPlayer: MediaPlayer = MediaPlayer()
    private var currentSongUrl :String?=null
    private var onPreparedCallback: (() -> Unit)? = null
    //private var currentSong:SongsModel?=null
     var currentSongTitle:String?=null
     var currentSongSubtitle:String?=null
     var currentSongCoverUrl:String?=null
    private var currentSongId: String? = null
    private var allSongs: ArrayList<SongsModel>? = null
    private var currentSong: SongsModel? = null
    private var currentSongIndex: Int? = 0

    fun getMediaDuration(): Int? {
        return mediaPlayer.duration
    }
    fun getCurrentSongIndex(): Int {
        return currentSongIndex?:0
    }

    fun playActivitySong(songsModel: SongsModel, onPrepared: () -> Unit) {
        currentSongId = songsModel.id
        currentSongUrl = songsModel.url
        currentSongTitle=songsModel.title
        currentSongSubtitle=songsModel.subtitle
        currentSongCoverUrl=songsModel.coverUrl
        onPreparedCallback = onPrepared
        if(mediaPlayer.isPlaying)
        {
            mediaPlayer.pause()
            mediaPlayer.reset()
            mediaPlayer.apply {
                reset()
                setDataSource(currentSongUrl)
                setOnPreparedListener { mp ->
                    mp.start()
                    onMediaPrepared()
                }
                setOnErrorListener { mp, what, extra ->
                    // Handle error here (optional)
                    return@setOnErrorListener false
                }
                prepareAsync()
            }
        }
        else
        {
            mediaPlayer.apply {
                reset()
                setDataSource(currentSongUrl)
                setOnPreparedListener { mp ->
                    Log.d("MediaPlayer", "Prepared listener called")
                    mp.start()
                    onMediaPrepared()
                }
                setOnErrorListener { mp, what, extra ->
                    return@setOnErrorListener false
                }
                prepareAsync()
            }
        }
    }
    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }


    fun play(songsModel: SongsModel) {
        if (songsModel.id == currentSongId && mediaPlayer.isPlaying) {
            // If it is the same song and the media player is already playing, just return
            return
        }
        currentSongId = songsModel.id
        currentSongUrl = songsModel.url
        currentSongTitle=songsModel.title
        currentSongSubtitle=songsModel.subtitle
        currentSongCoverUrl=songsModel.coverUrl

            if(mediaPlayer.isPlaying)
            {
                mediaPlayer.pause()
                mediaPlayer.reset()
                mediaPlayer.apply {
                    reset()
                    setDataSource(currentSongUrl)
                    setOnPreparedListener { mp ->
                        mp.start()
                    }
                    setOnErrorListener { mp, what, extra ->
                        // Handle error here (optional)
                        return@setOnErrorListener false
                    }
                    prepareAsync()
                }
            }
            else
            {
                mediaPlayer.apply {
                    reset()
                    setDataSource(currentSongUrl)
                    setOnPreparedListener { mp ->
                        Log.d("MediaPlayer", "Prepared listener called")
                        mp.start()
                    }
                    setOnErrorListener { mp, what, extra ->
                        return@setOnErrorListener false
                    }
                    prepareAsync()
                }
            }
        }
    fun getCurrentSong(): SongsModel? {

        return currentSong
    }

    fun pause() {
        try {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
            }
        } catch (e: IllegalStateException) {
            // Handle IllegalStateException gracefully
            e.printStackTrace()
        }
    }

        fun isPlaying(): Boolean {
            return mediaPlayer.isPlaying
        }

         fun stopMediaPlayer() {
            mediaPlayer.let {
                if (it.isPlaying) {
                    mediaPlayer.seekTo(0)
                    it.pause()
                }
            }
        }
    fun reset()
    {
        mediaPlayer.reset()
    }
    fun release()
    {
        mediaPlayer.release()
    }

    fun getMediaPlayer(): MediaPlayer {
        return mediaPlayer
    }

    fun resume()
    {
        mediaPlayer.start()
    }
    fun getResume()
    {
        return mediaPlayer.start()
    }
    private fun onMediaPrepared() {
        onPreparedCallback?.invoke() // Invoke callback function when media player is prepared
        onPreparedCallback = null // Reset callback after invoking
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer.currentPosition
    }
    fun getCurrentSongId(): String? {
        return currentSongId
    }


}
