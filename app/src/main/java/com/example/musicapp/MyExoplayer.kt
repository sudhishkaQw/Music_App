package com.example.musicapp


import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicapp.model.SongsModel
import com.google.firebase.firestore.FirebaseFirestore

object MyExoplayer {

    private var exoPlayer : ExoPlayer? = null
    private var currentSong : SongsModel? = null


    fun getCurrentSong():SongsModel?
    {
        return currentSong
    }
    fun getInstance() : ExoPlayer?
    {
        return exoPlayer
    }


        fun resumePlaying() {
            exoPlayer?.play()
        }


    fun startPlaying(context: Context, song: SongsModel)
    {   if(exoPlayer==null)
        exoPlayer=ExoPlayer.Builder(context).build()

        if(currentSong!=song)
        {
            //new song start
            currentSong=song
            updateCount()
            currentSong?.url?.apply {
                val mediaItem = MediaItem.fromUri(this)
                exoPlayer?.setMediaItem(mediaItem)
                exoPlayer?.prepare()
                exoPlayer?.play()
            }
        }
    }
    fun startOrResume() {
        if (exoPlayer?.playbackState == ExoPlayer.STATE_READY) {
            exoPlayer?.playWhenReady = true
        } else {
            exoPlayer?.let {
                currentSong?.url?.apply {
                    val mediaItem = MediaItem.fromUri(this)
                    it.setMediaItem(mediaItem)
                    it.prepare()
                    it.playWhenReady = true
                }
            }
        }
    }
    fun pausePlaying() {
        exoPlayer?.pause()
    }

    fun isPlaying(): Boolean {
        return exoPlayer?.isPlaying ?: false
    }
    fun updateCount()
    {
        currentSong?.id?.let {id->
            FirebaseFirestore.getInstance().collection("songs")
                .document(id)
                .get().addOnSuccessListener {
                    var latestCount= it.getLong("count")
                    if(latestCount==null)
                    {
                        latestCount=1L
                    }
                    else
                    {
                        latestCount=latestCount+1
                    }
                    FirebaseFirestore.getInstance().collection("songs")
                        .document(id)
                        .update(mapOf("count" to latestCount))
                }
        }
    }
}
//object MyExoplayer {
//    private var exoPlayer: ExoPlayer? = null
//    private var songList: List<SongsModel>? = null
//    private var currentIndex = 0
//
//    fun initialize(exoPlayer: ExoPlayer, songList: List<SongsModel>) {
//        this.exoPlayer = exoPlayer
//        this.songList = songList
//        // Ensure to set the current index appropriately
//        currentIndex = exoPlayer.currentMediaItemIndex
//    }
//        fun getInstance(context:Context): ExoPlayer {
//        if (exoPlayer == null) {
//            exoPlayer = ExoPlayer.Builder(context).build()
//        }
//        return exoPlayer!!
//    }
//    fun getCurrentSong(): SongsModel? {
//        if (exoPlayer != null && songList != null && currentIndex < songList!!.size) {
//            return songList!![currentIndex]
//        }
//        return null
//    }
//    fun play() {
//        val currentSong = getCurrentSong()
//        if (currentSong != null && currentSong.url.isNotBlank()) {
//            val mediaItem = MediaItem.fromUri(Uri.parse(currentSong.url))
//            exoPlayer?.setMediaItem(mediaItem)
//            exoPlayer?.prepare()
//            exoPlayer?.play()
//        } else {
//            // Handle the case where the URL is null or empty
//            // For example, log an error or show a message to the user
//            Log.e("MyExoplayer", "URL is null or empty")
//            // You can also show a message to the user
//            // Toast.makeText(activity, "Failed to play the song: URL is null or empty", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//
//    fun pause() {
//        exoPlayer?.playWhenReady = false
//    }
//
//    fun stop() {
//        exoPlayer?.stop()
//    }
//
//    fun next() {
//        currentIndex = (currentIndex + 1) % songList!!.size
//        play()
//    }
//
//    fun previous() {
//        currentIndex = (currentIndex - 1 + songList!!.size) % songList!!.size
//        play()
//    }
//
//    fun release() {
//        exoPlayer?.release()
//    }
////        fun startOrResume() {
////        if (exoPlayer?.playbackState == SimpleExoPlayer.STATE_READY) {
////            exoPlayer?.playWhenReady = true
////        } else {
////            exoPlayer?.let {
////                currentSong?.url?.apply {
////                    val mediaItem = MediaItem.fromUri(this)
////                    it.setMediaItem(mediaItem)
////                    it.prepare()
////                    it.playWhenReady = true
////                }
////            }
////        }
////    }
//    fun pausePlaying() {
//        exoPlayer?.pause()
//    }
//
//    fun isPlaying(): Boolean {
//        return exoPlayer?.isPlaying ?: false
//    }
//}

//object MyExoplayer {
//
//    private var exoPlayer : ExoPlayer? = null
//    private var currentSong : SongsModel? = null
//    private lateinit var songList: List<SongsModel>
//    private var currentSongIndex: Int = -1
//
//
//    fun getCurrentSong():SongsModel?
//    {
//        return currentSong
//    }
//    fun getInstance(context: Context): ExoPlayer {
//        if (exoPlayer == null) {
//            exoPlayer = ExoPlayer.Builder(context).build()
//        }
//        return exoPlayer!!
//    }
//    fun startPlaying(context: Context, song: SongsModel)
//    {    if (exoPlayer == null) {
//        exoPlayer = ExoPlayer.Builder(context).build()
//    }
//
//        if (currentSong != song) {
//            // New song start
//            currentSong = song
//            updateCount()
//            currentSong?.url?.apply {
//                val mediaItem = MediaItem.fromUri(this)
//                exoPlayer?.setMediaItem(mediaItem)
//                exoPlayer?.prepare()
//                exoPlayer?.play()
//            }
//
//        }
//    }
//
//    fun playNext(context: Context) {
//        songList?.let {
//            currentSongIndex = (currentSongIndex + 1) % it.size
//            startPlaying(context, it[currentSongIndex])
//        }
//    }
//
//    fun playPrevious(context: Context) {
//        songList?.let {
//            currentSongIndex = (currentSongIndex - 1 + it.size) % it.size
//            startPlaying(context, it[currentSongIndex])
//        }
//    }
//
//    fun startOrResume() {
//        if (exoPlayer?.playbackState == ExoPlayer.STATE_READY) {
//            exoPlayer?.playWhenReady = true
//        } else {
//            exoPlayer?.let {
//                currentSong?.url?.apply {
//                    val mediaItem = MediaItem.fromUri(this)
//                    it.setMediaItem(mediaItem)
//                    it.prepare()
//                    it.playWhenReady = true
//                }
//            }
//        }
//    }
//    fun pausePlaying() {
//        exoPlayer?.pause()
//    }
//
//    fun isPlaying(): Boolean {
//        return exoPlayer?.isPlaying ?: false
//    }
//    fun updateCount()
//    {
//        currentSong?.id?.let {id->
//            FirebaseFirestore.getInstance().collection("songs")
//                .document(id.toString())
//                .get().addOnSuccessListener {
//                    var latestCount= it.getLong("count")
//                    if(latestCount==null)
//                    {
//                        latestCount=1L
//                    }
//                    else
//                    {
//                        latestCount=latestCount+1
//                    }
//                    FirebaseFirestore.getInstance().collection("songs")
//                        .document(id.toString())
//                        .update(mapOf("count" to latestCount))
//                }
//        }
//    }
