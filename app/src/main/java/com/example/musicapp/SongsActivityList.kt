package com.example.musicapp

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.musicapp.adapter.SongListAdapter
import com.example.musicapp.databinding.ActivitySongsListBinding
import com.example.musicapp.model.CategoryModel

class SongsActivityList : AppCompatActivity() {
    companion object {
        var category: CategoryModel = CategoryModel()
    }

    lateinit var binding: ActivitySongsListBinding
    lateinit var songListAdapter: SongListAdapter
    private lateinit var customPlayer: CustomPlayer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySongsListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.loader.visibility=View.VISIBLE

        // Set the category name and cover image
        binding.nameText.text = category.name
        Glide.with(binding.cover).load(category.coverUrl)
            .apply(RequestOptions().transform(RoundedCorners(32)))
            .into(binding.cover)

        // Initialize CustomPlayer
        customPlayer = CustomPlayer(this)
        binding.backSong.setOnClickListener {
            startActivity(Intent(this@SongsActivityList,MainActivity::class.java))
        }
        // Setup the songs list
        setupSongsList()
    }

    private fun setupSongsList() {

        // Create and set up the adapter
        songListAdapter = SongListAdapter(category.songs)

        // Set the layout manager and adapter for the RecyclerView
        binding.songListRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.songListRecyclerView.adapter = songListAdapter
        binding.loader.visibility=View.GONE

    }



    override fun onDestroy() {
        super.onDestroy()
        customPlayer.pause()
        customPlayer.release() // Release the CustomPlayer instance
    }
}
//    override fun onDestroy() {
//        super.onDestroy()
//        customPlayer.release() // Release the CustomPlayer instance
//    }
