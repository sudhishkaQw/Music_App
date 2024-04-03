package com.example.musicapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.adapter.PurchasedSongAdapter
import com.example.musicapp.databinding.ActivityPurchasedSongsBinding
import com.example.musicapp.model.PurchasedSong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PurchasedSongs : AppCompatActivity() {

    private lateinit var binding: ActivityPurchasedSongsBinding
    private lateinit var purchasedSongAdapter: PurchasedSongAdapter
    private val userFetcher = UserFetcher()
    private lateinit var customPlayer: CustomPlayer
    private lateinit var purchasedSongsList: ArrayList<PurchasedSong>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPurchasedSongsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        customPlayer=CustomPlayer(this@PurchasedSongs)
        // Initialize RecyclerView and its adapter
        purchasedSongAdapter = PurchasedSongAdapter(emptyList())
        binding.recyclerViewPurchasedSongs.apply {
            layoutManager = LinearLayoutManager(this@PurchasedSongs)
            adapter = purchasedSongAdapter
        }
        binding.backPurchased.setOnClickListener {
            startActivity(Intent(this@PurchasedSongs,MainActivity::class.java))
        }

        // Fetch and display purchased songs
        fetchPurchasedSongs()

    }

    private fun fetchPurchasedSongs() {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.loader.visibility=View.VISIBLE
            userFetcher.fetchPurchasedSongsForCurrentUser(
                onSuccess = { purchasedSongs ->
                    binding.loader.visibility=View.GONE
                    purchasedSongAdapter.updatePurchasedSongs(purchasedSongs)
                },
                onFailure = { exception ->

                }
            )
        }
    }

    companion object {
        private const val TAG = "PurchasedActivity"
    }
}
