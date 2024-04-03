package com.example.musicapp.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.musicapp.ActivityPlayer
import com.example.musicapp.CustomPlayer
import com.example.musicapp.CustomSong
import com.example.musicapp.R
import com.example.musicapp.databinding.SongListItemRecyclerBinding
import com.example.musicapp.model.SongsModel

class OfflinePlaylistAdapter(private var context: Context,private var songs: MutableList<SongsModel>) :
    RecyclerView.Adapter<OfflinePlaylistAdapter.ViewHolder>()  {
    private var currentPlayingPosition: Int? = null
    inner class ViewHolder(private val binding: SongListItemRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(song: SongsModel, songs: MutableList<SongsModel>, position: Int) {
            with(binding) {
                songTextView.text = song.title
                songSubtitleView.text = song.subtitle
                Glide.with(songCoverImageView)
                    .load(song.coverUrl)
                    .apply(RequestOptions().transform(RoundedCorners(32)))
                    .into(songCoverImageView)

                if (position == currentPlayingPosition) {
                    if (CustomSong.isPlaying()) {
                        binding.playPauseButton.setImageResource(R.drawable.baseline_pause_circle_24)
                    } else {
                        binding.playPauseButton.setImageResource(R.drawable.baseline_play_circle_24)
                    }
                } else {
                    // Song is not currently playing, set play icon
                    binding.playPauseButton.setImageResource(R.drawable.baseline_play_circle_24)
                }
                // Play or pause the song when the play/pause button is clicked
                binding.playPauseButton.setOnClickListener {
                    if (currentPlayingPosition == position) {
                        // Clicked on the same song, toggle play/pause
                        if (CustomSong.isPlaying()) {
                            CustomSong.pause()
                            binding.playPauseButton.setImageResource(R.drawable.baseline_play_circle_24)
                        } else {
                            CustomSong.play(song)
                            binding.playPauseButton.setImageResource(R.drawable.baseline_pause_circle_24)
                        }
                    } else {
                        // Clicked on a different song, stop current playback and start new one
                        currentPlayingPosition?.let { oldPosition ->
                            notifyItemChanged(oldPosition)
                        }
                        currentPlayingPosition = position
                        CustomSong.play(song)
                        binding.playPauseButton.setImageResource(R.drawable.baseline_pause_circle_24)
                    }
                }

                // In OfflinePlaylistAdapter class, inside ViewHolder's root click listener
                root.setOnClickListener {
                    val intent = Intent(it.context, ActivityPlayer::class.java).apply {
                        // Pass the list of songs as a parcelable ArrayList
                        putExtra("From","Offline")
                        putExtra("song",song)
                        putExtra("songId",song.id)
                        putExtra("currentPlaybackPosition", CustomSong.getCurrentPosition())
                        putParcelableArrayListExtra("offline_Song_list", ArrayList(songs))
                        putExtra("index", position) // Pass the index of the clicked song
                    }
                    it.context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SongListItemRecyclerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
    //    fun updateDownloadedSongs(downloadedSongs: List<SongsModel>) {
//        this.downloadedSongs.clear()
//        this.downloadedSongs.addAll(downloadedSongs)
//        notifyDataSetChanged()
//    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(songs[position],songs,position )
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    fun updateSongsList(updatedSongs: List<SongsModel>) {
        songs.clear()
        songs.addAll(updatedSongs)
        notifyDataSetChanged()
    }
}
//    private fun isSongAvailableOffline(song: SongsModel): Boolean {
//        // Check if the song is available offline by checking if it exists in the local cache
//        // You need to implement this logic based on how you cache and store the songs locally
//        // Return true if the song is available offline, false otherwise
//        // Example: Check if the audio file exists in the device's local storage
//        val audioFile = File(context.cacheDir, song.audioFileName)
//        return audioFile.exists()
//    }


