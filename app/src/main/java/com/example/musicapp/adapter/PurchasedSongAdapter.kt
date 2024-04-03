package com.example.musicapp.adapter
import android.content.Intent
import android.util.Log
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

class PurchasedSongAdapter(private var purchasedSongsWithCover: List<Pair<SongsModel, String>>) :
    RecyclerView.Adapter<PurchasedSongAdapter.ViewHolder>() {
    private var currentPlayingPosition: Int? = null
    inner class ViewHolder(private val binding: SongListItemRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            songWithCover: Pair<SongsModel, String>,
            purchasedSongsWithCover: List<Pair<SongsModel, String>>,
            position: Int
        ) {
            val (song, coverUrl) = songWithCover
            with(binding) {
                songTextView.text = song.title
                songSubtitleView.text = song.subtitle
                Glide.with(songCoverImageView)
                    .load(coverUrl)
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
                root.setOnClickListener {
                    val intent = Intent(it.context, ActivityPlayer::class.java)
                    intent.putExtra("From", "formPurchaseSong")
                    intent.putExtra("index", position)
                    intent.putExtra("song",song)
                    intent.putExtra("songId",song.id)
                        intent.putExtra("currentPlaybackPosition", CustomSong.getCurrentPosition())
                    val songsList = ArrayList<SongsModel>()
                    purchasedSongsWithCover.forEach { pair ->
                        songsList.add(pair.first)
                    }
                    intent.putParcelableArrayListExtra("songsList", songsList)
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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(purchasedSongsWithCover[position], purchasedSongsWithCover, position)
    }

    override fun getItemCount(): Int {
        return purchasedSongsWithCover.size
    }

    fun updatePurchasedSongs(newPurchasedSongsWithCover: List<Pair<SongsModel, String?>>) {
        purchasedSongsWithCover = newPurchasedSongsWithCover as List<Pair<SongsModel, String>>
        notifyDataSetChanged()
    }
}
