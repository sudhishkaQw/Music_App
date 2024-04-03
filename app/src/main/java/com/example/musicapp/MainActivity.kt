package com.example.musicapp



import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.musicapp.adapter.CategoryAdapter
import com.example.musicapp.adapter.SectionListSongAdapter
import com.example.musicapp.databinding.ActivityMainBinding
import com.example.musicapp.model.CategoryModel
import com.example.musicapp.model.SongsModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var categoryAdapter: CategoryAdapter
    private var isSongPlaying:Boolean=false
    private var isLimitedSong: Boolean = false
    private var isPurchaseAlertShown: Boolean = false
    private val userFetcher = UserFetcher()
    private var isSongPurchase:Boolean=true

    //lateinit var customPlayer: CustomPlayer
    @UnstableApi override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.loader.visibility = View.VISIBLE
        setContentView(binding.root)
        setTheme(R.style.Theme_MusicApp)
        getCategories()
        setupSections("section_1",binding.section1MainLayout,binding.section1Title,binding.section1RecyclerView)
        setupSections("section_2",binding.section2MainLayout,binding.section2Title,binding.section2RecyclerView)
        setupSections("section_3",binding.section3MainLayout,binding.section3Title,binding.section3RecyclerView)
        setupMostlyPlayed("mostly_played",binding.mostPlayedMainLayout,binding.mostPlayedTitle,binding.mostPlayedrecyclerView)
        binding.loader.visibility = View.GONE
        binding.playerButton.setOnClickListener {

            if (CustomSong.isPlaying()) {
                CustomSong.pause()
                binding.playerButton.setImageResource(R.drawable.baseline_play_circle_24)
            } else {
                CustomSong.resume()
                binding.playerButton.setImageResource(R.drawable.baseline_pause_circle_24)
            }
        }
        binding.option.setOnClickListener {
            showPopupMenu()
        }


    }

    override fun onResume() {
        super.onResume()
        showPlayerView()

    }
    fun showPlayerView() {
        isSongPlaying = CustomSong.isPlaying()
        if (isSongPlaying) {
            binding.songsubTitleNP.text = CustomSong.currentSongSubtitle
            binding.songTitleNP.text = CustomSong.currentSongTitle
            Glide.with(this@MainActivity)
                .load(CustomSong.currentSongCoverUrl)
                .apply(RequestOptions().transform(RoundedCorners(32)))
                .into(binding.NPsongCoverImageView)
            binding.playerButton.setImageResource(R.drawable.baseline_pause_circle_24)
            binding.NPplayerView.visibility = View.VISIBLE

            // Call the function to fetch purchased songs
            GlobalScope.launch(Dispatchers.Main) {
                userFetcher.fetchPurchasedSongsForCurrentUser(
                    onSuccess = { purchasedSongs ->
                        val currentSongId = CustomSong.getCurrentSongId()
                        isSongPurchase = purchasedSongs.any { it.first.id == currentSongId }
                        if (!isSongPurchase) {
                            Handler(Looper.getMainLooper()).postDelayed({
                                Log.d(TAG, "About to pause song")
                                CustomSong.pause()
                                binding.playerButton.setImageResource(R.drawable.baseline_play_circle_24)
                                Log.d(TAG, "Song paused")

                                binding.playerButton.isClickable = false
                                showPurchaseAlert()
                            }, 30000)

                        }
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Error checking song purchase status", exception)
                    }
                )
            }
        } else {
            binding.NPplayerView.visibility = View.GONE
        }
        binding.NPplayerView.setOnClickListener {
            val intent = Intent(this, ActivityPlayer::class.java).apply {
                putExtra("From", "MainActivity")
                putExtra("songId",CustomSong.getCurrentSongId())
                putExtra("songTitle", CustomSong.currentSongTitle)
                putExtra("songSubtitle", CustomSong.currentSongSubtitle)
                putExtra("songCoverUrl", CustomSong.currentSongCoverUrl)
                putExtra("currentPlaybackPositionMain", CustomSong.getCurrentPosition())
                putExtra("isSongPurchased", isSongPurchase)
            }
            startActivity(intent)
        }
    }


    private fun isSongPurchased(songId: String, onSuccess: (Boolean) -> Unit, onFailure: (Exception) -> Unit) {
        // Check if the song is purchased for the current user
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userDocRef = FirebaseFirestore.getInstance().collection("users").document(userId)
            userDocRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val purchasedSongs = documentSnapshot.get("purchasedSongs") as? List<String> ?: emptyList()
                        val isPurchased = purchasedSongs.contains(songId)
                        onSuccess(isPurchased)
                    } else {
                        onFailure(IllegalStateException("User document does not exist"))
                    }
                }
                .addOnFailureListener { exception ->
                    onFailure(exception)
                }
        } else {
            onFailure(IllegalStateException("User not authenticated"))
        }
    }
    private fun showPurchaseAlert() {
        // Implement your purchase alert dialog here
        if (!isPurchaseAlertShown) {
            AlertDialog.Builder(this)
                .setTitle("Purchase Alert")
                .setMessage("This song needs to be purchased to play in full.")
                .setNegativeButton("Cancel") { dialog, which ->
                    // Handle cancel action here, if needed
                }
                .setCancelable(false)
                .show()
            isPurchaseAlertShown = true
        }
    }


    private fun showPopupMenu() {
        val popupMenu = PopupMenu(this,binding.option)
        val inflator = popupMenu.menuInflater
        inflator.inflate(R.menu.menu_option,popupMenu.menu)
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.logout -> {
                    logout()
                    true
                }
                R.id.userProfile ->
                {
                    startActivity(Intent(this@MainActivity,ProfileActivity::class.java))
                }
                R.id.purchasedSongs->
                {
                    startActivity(Intent(this@MainActivity,PurchasedSongs::class.java))
                }
                R.id.offlinePlaylist->
                {
                    startActivity(Intent(this@MainActivity,OfflinePlaylist::class.java))
                }
//                R.id.fav->
//                {
//                    startActivity(Intent(this@MainActivity,FavoritePlaylist::class.java))
//                }
            }
            false
        }

    }

    fun logout(){

        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this,LoginActivity::class.java))
        finish()
    }


    //categories
    fun getCategories(){
        FirebaseFirestore.getInstance().collection("category")
            .get().addOnSuccessListener {

                val categoryList = it.toObjects(CategoryModel::class.java)
                setupCategoryRecyclerView(categoryList)
            }
    }

    fun setupCategoryRecyclerView(categoryList : List<CategoryModel>){
        categoryAdapter = CategoryAdapter(categoryList)
        binding.categoriesRecyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        binding.categoriesRecyclerView.adapter = categoryAdapter
    }
    //Sections
    fun setupSections(id:String,mainLayout:RelativeLayout,titleView:TextView,recyclerView: RecyclerView )
    {       binding.loader.visibility=View.VISIBLE
        FirebaseFirestore.getInstance().collection("sections")
            .document(id)
            .get().addOnSuccessListener {
                binding.loader.visibility=View.GONE
                val section = it.toObject(CategoryModel::class.java)
                section?.apply {
                    mainLayout.visibility= View.VISIBLE
                    titleView.text=name
                    recyclerView.layoutManager=LinearLayoutManager(this@MainActivity,LinearLayoutManager.HORIZONTAL,false)
                    recyclerView.adapter=SectionListSongAdapter(songs)
                    mainLayout.setOnClickListener {
                        SongsActivityList.category=section
                        startActivity(Intent(this@MainActivity,SongsActivityList::class.java))
                    }
                }
            }
    }

    fun setupMostlyPlayed(id:String,mainLayout:RelativeLayout,titleView:TextView,recyclerView: RecyclerView )
    {
        FirebaseFirestore.getInstance().collection("sections")
            .document(id)
            .get().addOnSuccessListener {
                FirebaseFirestore.getInstance().collection("songs")
                    .orderBy("count",Query.Direction.DESCENDING)
                    .limit(5)
                    .get()
                    .addOnSuccessListener {songListSnapshot->
                        val songsModelList= songListSnapshot.toObjects<SongsModel>()
                        val songIdList =songsModelList.map {
                            it.id
                        }.toList()
                        val section = it.toObject(CategoryModel::class.java)
                        section?.apply {
                            section.songs= songIdList as List<String>
                            mainLayout.visibility= View.VISIBLE
                            titleView.text=name
                            recyclerView.layoutManager=LinearLayoutManager(this@MainActivity,LinearLayoutManager.HORIZONTAL,false)
                            recyclerView.adapter=SectionListSongAdapter(songs)
                            mainLayout.setOnClickListener {
                                SongsActivityList.category=section
                                startActivity(Intent(this@MainActivity,SongsActivityList::class.java))
                            }
                        }
                    }

            }
    }



}