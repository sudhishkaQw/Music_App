package com.example.musicapp.adapter
import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.musicapp.ActivityPlayer
import com.example.musicapp.CustomPlayer
import com.example.musicapp.CustomSong
import com.example.musicapp.MusicPlayerManager
import com.example.musicapp.R
import com.example.musicapp.databinding.SongListItemRecyclerBinding
import com.example.musicapp.model.SongsModel
import com.google.firebase.firestore.FirebaseFirestore

class SongListAdapter(
    private val songIdList: List<String>
) : RecyclerView.Adapter<SongListAdapter.MyViewHolder>() {

    private var currentPlayingPosition: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            SongListItemRecyclerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return songIdList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindData(songIdList[position], position)
    }

    inner class MyViewHolder(private val binding: SongListItemRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindData(songId: String, position: Int) {
            FirebaseFirestore.getInstance().collection("songs")
                .document(songId).get()
                .addOnSuccessListener { document ->
                    val song = document.toObject(SongsModel::class.java)
                    song?.apply {
                        binding.songTextView.text = title
                        binding.songSubtitleView.text = subtitle
                        Glide.with(binding.songCoverImageView)
                            .load(coverUrl)
                            .apply(RequestOptions().transform(RoundedCorners(32)))
                            .into(binding.songCoverImageView)

                        if (position == currentPlayingPosition) {
                            if (CustomSong.isPlaying()) {
                                binding.playPauseButton.setImageResource(R.drawable.baseline_pause_circle_24)
                            } else {
                                binding.playPauseButton.setImageResource(R.drawable.baseline_play_circle_24)
                            }
                        } else {
                            binding.playPauseButton.setImageResource(R.drawable.baseline_play_circle_24)
                        }

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

                        binding.root.setOnClickListener {


                            val intent = Intent(it.context, ActivityPlayer::class.java).apply {
                               putExtra("index", position)
                               putStringArrayListExtra(
                                    "songsList",
                                    songIdList as ArrayList<String>
                                )
                                putExtra("song",song)
                                putExtra("songId",songId)
                                putExtra("currentPlaybackPosition", CustomSong.getCurrentPosition())

//                                putExtra("currentPlaybackPosition", CustomSong.getCurrentPosition())
                            }
                            it.context.startActivity(intent)
                        }

                    }
                }
        }

    }


}




