package com.example.musicapp.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.musicapp.ActivityPlayer
import com.example.musicapp.CustomSong
import com.example.musicapp.databinding.SectionSongListRecyclerBinding
import com.example.musicapp.model.SongsModel
import com.google.firebase.firestore.FirebaseFirestore

class SectionListSongAdapter (private val songIdList:List<String>) :
    RecyclerView.Adapter<SectionListSongAdapter.MyViewHolder>() {
    class MyViewHolder(private val binding: SectionSongListRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(songId: String, songIdList: List<String>, position: Int) {
            FirebaseFirestore.getInstance().collection("songs")
                .document(songId).get()
                .addOnSuccessListener {
                    val song = it.toObject(SongsModel::class.java)
                    song?.apply {
                        binding.sectionSongTitle.text = title
                        binding.section1SongSubtitle.text = subtitle
                        Glide.with(binding.section1CoverImage).load(coverUrl)
                            .apply(
                                RequestOptions().transform(RoundedCorners(32))
                            )
                            .into(binding.section1CoverImage)
                        binding.root.setOnClickListener {
                            val intent = Intent(it.context, ActivityPlayer::class.java)
                            intent.putExtra("From", "formSectionSong")
                            intent.putExtra("song",song)
                            intent.putExtra("songId",songId)
                            intent.putExtra("index",position)
                            intent.putExtra("currentPlaybackPosition", CustomSong.getCurrentPosition())
                            intent.putStringArrayListExtra("songsList1",songIdList as ArrayList<String>)
                            it.context.startActivity(intent)
                        }
                    }
                }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = SectionSongListRecyclerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return songIdList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindData(songIdList[position],songIdList,position)
    }
}