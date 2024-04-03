package com.example.musicapp.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.musicapp.SongsActivityList
import com.example.musicapp.databinding.CateogryRecyclerRowBinding
import com.example.musicapp.model.CategoryModel

class CategoryAdapter (private val categoryList : List<CategoryModel>) :
    RecyclerView.Adapter<CategoryAdapter.MyViewHolder>() {

    class MyViewHolder(private val binding :  CateogryRecyclerRowBinding) :
        RecyclerView.ViewHolder(binding.root){
        //bind the data with views
        fun bindData(category : CategoryModel){
            binding.nameTextView.text = category.name
             Log.e("nameText", category.name)
            Glide.with(binding.coverImageView).load(category.coverUrl)
                .apply(
                    RequestOptions().transform(RoundedCorners(32))
                )
                .into(binding.coverImageView)
            Log.i("Songs",category.songs.size.toString())
            //song activity
            val context = binding.root.context
            binding.root.setOnClickListener{
                SongsActivityList.category=category
                context.startActivity(Intent(context,SongsActivityList::class.java))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = CateogryRecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindData(categoryList[position])
    }

}