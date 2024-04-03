package com.example.musicapp

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.adapter.OfflinePlaylistAdapter
import com.example.musicapp.databinding.ActivityOfflinePlaylistBinding
import com.example.musicapp.model.SongsModel
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

class OfflinePlaylist : AppCompatActivity() {

    private lateinit var binding: ActivityOfflinePlaylistBinding
    private lateinit var offlinePlaylistAdapter: OfflinePlaylistAdapter
    private lateinit var customPlayer: CustomPlayer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfflinePlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)
        customPlayer = CustomPlayer(this@OfflinePlaylist)
        setupRecyclerView()
        fetchSongsFromFirestore()
        binding.backOff.setOnClickListener {
            startActivity(Intent(this@OfflinePlaylist, MainActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        offlinePlaylistAdapter = OfflinePlaylistAdapter(this, mutableListOf())
        binding.recyclerViewOfflinePlaylist.apply {
            adapter = offlinePlaylistAdapter
            layoutManager = LinearLayoutManager(this@OfflinePlaylist)
        }
    }



    private fun fetchSongsFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        db.collection("songs")
            .get()
            .addOnSuccessListener { result ->
                val songsList = mutableListOf<SongsModel>()
                for (document in result) {
                    // Map Firestore document data to SongsModel
                    val song = document.toObject(SongsModel::class.java)
                    songsList.add(song)
                }
                // Update RecyclerView adapter with the songs list
                offlinePlaylistAdapter.updateSongsList(songsList)
            }
            .addOnFailureListener { exception ->
                // Handle errors
                // For example, show a toast message indicating failure to fetch songs
            }
    }




}
