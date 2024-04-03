package com.example.musicapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.adapter.FavoritePlaylistAdapter
import com.example.musicapp.model.SongsModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FavoritePlaylist : AppCompatActivity() {
    private lateinit var favoriteSongsRecyclerView: RecyclerView
    private lateinit var favoriteSongsAdapter: FavoritePlaylistAdapter
    private lateinit var userFetcher: UserFetcher
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_playlist)
        // Initialize RecyclerView and adapter
        favoriteSongsRecyclerView = findViewById(R.id.recyclerViewFavorite)
        favoriteSongsAdapter = FavoritePlaylistAdapter(emptyList())
        favoriteSongsRecyclerView.adapter = favoriteSongsAdapter
        favoriteSongsRecyclerView.layoutManager = LinearLayoutManager(this)
        userFetcher = UserFetcher()
        lifecycleScope.launch {
           fetchAndDisplayFavoriteSongs()
        }


    }



    private fun fetchAndDisplayFavoriteSongs() {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val favoriteSongs = userFetcher.fetchFavoriteSongsForCurrentUser()
                updateFavList(favoriteSongs)
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }

    private fun updateFavList(favoriteSongs: List<SongsModel>) {
        favoriteSongsAdapter.updateList(favoriteSongs)
    }
    companion object {
        private const val TAG = "FavoriteActivity"
    }



    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


}