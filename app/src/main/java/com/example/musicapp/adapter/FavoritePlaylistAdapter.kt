package com.example.musicapp.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.musicapp.ActivityPlayer
import com.example.musicapp.MyExoplayer
import com.example.musicapp.R
import com.example.musicapp.databinding.SongListItemRecyclerBinding
import com.example.musicapp.model.SongsModel
import com.google.firebase.firestore.FirebaseFirestore

class FavoritePlaylistAdapter(private var favoriteSongs: List<SongsModel>) :
    RecyclerView.Adapter<FavoritePlaylistAdapter.ViewHolder>() {

     class ViewHolder(private val binding: SongListItemRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(songId: String, songIdList: List<SongsModel>, position: Int) {
            FirebaseFirestore.getInstance().collection("songs")
                .document(songId).get()
                .addOnSuccessListener {
                    val song = it.toObject(SongsModel::class.java)
                    song?.apply {
                        binding.songTextView.text = song.title
                        binding.songSubtitleView.text = song.subtitle
                        Glide.with(binding.songCoverImageView)
                            .load(song.coverUrl)
                            .apply(RequestOptions().transform(RoundedCorners(32)))
                            .into(binding.songCoverImageView)

                        // Check if the song is marked as favorite and set the button icon accordingly


                        binding.playPauseButton.setOnClickListener {
                            if (MyExoplayer.isPlaying()) {
                                MyExoplayer.pausePlaying()
                                // Update button icon to play
                                binding.playPauseButton.setImageResource(R.drawable.baseline_play_circle_24)
                            } else {
                                MyExoplayer.startPlaying(binding.root.context, song)
                                // Update button icon to pause
                                binding.playPauseButton.setImageResource(R.drawable.baseline_pause_circle_24)
                            }
                        }

                        // Handle favorite button click


                        binding.root.setOnClickListener {
                            val intent = Intent(it.context, ActivityPlayer::class.java)
                            intent.putExtra("index", position)
                            intent.putStringArrayListExtra(
                                "songsList",
                                songIdList as ArrayList<String>
                            )
                            it.context.startActivity(intent)
                        }
                    }
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritePlaylistAdapter.ViewHolder {
        val binding = SongListItemRecyclerBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return FavoritePlaylistAdapter.ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(favoriteSongs[position].toString(),favoriteSongs,position)
    }

    override fun getItemCount(): Int {
        return  favoriteSongs.size
    }
    fun updateList(newList: List<SongsModel>) {
        favoriteSongs = newList
        notifyDataSetChanged()
    }




}