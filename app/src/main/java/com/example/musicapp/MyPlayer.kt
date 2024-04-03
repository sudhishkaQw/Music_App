package com.example.musicapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.example.musicapp.databinding.ActivityMyPlayerBinding

class MyPlayer : AppCompatActivity() {
    private lateinit var binding: ActivityMyPlayerBinding
    private lateinit var exoPlayer:ExoPlayer
   override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMyPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

       exoPlayer =ExoPlayer.Builder(this).build()
       binding.playerView.player=exoPlayer

        ExoPlayer2.getCurrentSong()?.apply {
            binding.songTitleTextview.text = title
            binding.songSubtitleTextview.text  = subtitle
            Glide.with(binding.songCover).load(coverUrl)
                .circleCrop()
                .into(binding.songCover)
            Glide.with(binding.songGifImageView).load(R.drawable.media_playing)
                .circleCrop()
                .into(binding.songGifImageView)

//            binding.playerView.showController()
//            exoPlayer.addListener(playerListener)
        }
    }

    var playerListener = object: Player.Listener{
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            showGif(isPlaying)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.removeListener(playerListener)
    }
    fun showGif(show:Boolean)
    {
        if(show)
            binding.songGifImageView.visibility= View.VISIBLE
        else{
            binding.songGifImageView.visibility=View.INVISIBLE
        }
    }
} 