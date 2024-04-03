package com.example.musicapp
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.musicapp.adapter.SongListAdapter
import com.example.musicapp.databinding.ActivityPlayer2Binding
import com.example.musicapp.model.SongsModel
import com.example.musicapp.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.Timer
import java.util.TimerTask


class ActivityPlayer : AppCompatActivity(), Runnable {
    private lateinit var binding: ActivityPlayer2Binding
    private var currentSongIndexx: Int = 0
    private var songList: ArrayList<String>? = null
    private var handler: Handler? = null
    private var isSongPlaying: Boolean = false
    private var from: String? = null
    private  var isFavorite: Boolean = false
    private lateinit var userFetcher: UserFetcher
    private lateinit  var mediaPlayer: MediaPlayer
    private lateinit var userModel: UserModel
    private var purchanseSong: ArrayList<SongsModel>? = null
    private var offlineSongs:ArrayList<SongsModel>?=null
    private lateinit var currentSong: SongsModel
    private var countDown : CountDownTimer? = null
    private var isPaused: Boolean = false
    var remainingTime: Long = 0
    private  var  currentPlaybackPosition:Int=0
    private var isLimitedSong:Boolean=false
    private lateinit var song:SongsModel
    private var songId:String?=null


    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayer2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        val intent = intent
        userModel = UserModel()
        handler = Handler(Looper.getMainLooper())
        from = intent.getStringExtra("From")
        userFetcher = UserFetcher()
        currentSong= SongsModel()
        mediaPlayer=MediaPlayer()
        binding.loader.visibility=View.GONE
        currentSongIndexx = intent.getIntExtra("index", 0)
        songList = intent.getStringArrayListExtra("songsList")

        if (from.equals("formPurchaseSong")) {
            purchanseSong = intent?.getParcelableArrayListExtra("songsList")
            currentSongIndexx = intent.getIntExtra("index", 0)
            songId = intent.getStringExtra("songId") ?: ""
            currentPlaybackPosition=intent.getIntExtra("currentPlaybackPosition", 0)
                playPurchaseSong(currentSongIndexx)

        }
        else if(from.equals("Offline")) {
            offlineSongs=intent.getParcelableArrayListExtra<SongsModel>("offline_Song_list")
            currentSongIndexx= intent.getIntExtra("index", 0)
            currentPlaybackPosition=intent.getIntExtra("currentPlaybackPosition", 0)
            songId = intent.getStringExtra("songId") ?: ""

                playOfflineSong(currentSongIndexx)


        }
        else if(from == "formSectionSong") {
            songList = intent.getStringArrayListExtra("songsList1")
            currentSongIndexx = intent.getIntExtra("index", 0)
            currentSong = intent.getParcelableExtra("song") ?: SongsModel()
            songId = intent.getStringExtra("songId") ?: ""
            currentPlaybackPosition = intent.getIntExtra("currentPlaybackPosition",0)
            playSong(currentSongIndexx)
        }
        else if (from.equals("MainActivity")) {
            val songTitle = intent.getStringExtra("songTitle")
            val songSubtitle = intent.getStringExtra("songSubtitle")
            val songCoverUrl = intent.getStringExtra("songCoverUrl")
            val playBackPosition=intent.getIntExtra("currentPlaybackPositionMain", CustomSong.getCurrentPosition())
            val isSongPurchased = intent.getBooleanExtra("isSongPurchased", false)

            if (playBackPosition > 0) {
                CustomSong.getMediaPlayer().seekTo(playBackPosition)
            }
            if (CustomSong.isPlaying()) {
                binding.play.setBackgroundResource(R.drawable.baseline_pause_24)
                startUpdatingSeekBar()
            } else {
                binding.play.setBackgroundResource(R.drawable.baseline_play_arrow_24)
            }

            binding.play.setBackgroundResource(R.drawable.baseline_pause_24)
            binding.songTitleTextview.text = songTitle
            binding.songSubtitleTextview.text = songSubtitle
            Glide.with(binding.songCover).load(songCoverUrl)
                .circleCrop()
                .apply(RequestOptions().transform(RoundedCorners(32)))
                .into(binding.songCover)
            if (isSongPurchased) {
                binding.buy.visibility=View.GONE
            }
            else
            {   var totalDuration=CustomSong.getCurrentPosition()
                remainingTime= ((40000-totalDuration).toLong())
                stopCountDowun(remainingTime,1000)
                Toast.makeText(this, "Song not purchased", Toast.LENGTH_SHORT).show()
                binding.seekBar.setOnTouchListener { v, event ->
                    return@setOnTouchListener true
                }
            }
        }


        else {
            songList = intent.getStringArrayListExtra("songsList")
            currentSongIndexx=intent.getIntExtra("index", 0)
            song = intent.getParcelableExtra("song") ?: SongsModel()
            songId = intent.getStringExtra("songId") ?: ""
            currentPlaybackPosition=intent.getIntExtra("currentPlaybackPosition", 0)
            if(songList!=null)
            {
                playSong(currentSongIndexx)
            }
        }
        binding.backPlayer.setOnClickListener {
            startActivity(Intent(this@ActivityPlayer,MainActivity::class.java))
            finish()
        }
        binding.previousButton.setOnClickListener {
            if (songList != null) {
                if (currentSongIndexx != 0) {
                    CustomSong.pause()
                    currentSongIndexx--
                    playSong(currentSongIndexx)
                }
            }


            else if (purchanseSong != null) {
                if (currentSongIndexx != 0) {
                    CustomSong.pause()
                    currentSongIndexx--
                    playPurchaseSong(currentSongIndexx)
                }
            }
            else
            {
                if(offlineSongs!=null)
                {
                    if(currentSongIndexx!=0)
                    {
                        CustomSong.pause()
                        currentSongIndexx--
                        playOfflineSong(currentSongIndexx)
                    }
                }
            }

        }

        binding.nextButton.setOnClickListener {
            if (songList != null) {
                if (currentSongIndexx < songList!!.size - 1) {
                    // Pause MediaPlayer
                    CustomSong.pause()
                    // Increment currentSongIndex
                    currentSongIndexx++
                    // Reset MediaPlayer
                    CustomSong.reset()
                    // Play next song
                    playSong(currentSongIndexx)

                }
            } else if (purchanseSong != null) {
                if (currentSongIndexx < purchanseSong!!.size - 1) {
                    // Pause MediaPlayer
                    CustomSong.pause()

                    // Increment currentSongIndex
                    currentSongIndexx++

                    // Reset MediaPlayer
                    CustomSong.reset()

                    // Play next song
                    playPurchaseSong(currentSongIndexx)
                }
            }
            else
            {
                if(offlineSongs!=null)
                {
                    if(currentSongIndexx<offlineSongs!!.size-1)
                    {
                        CustomSong.pause()
                        currentSongIndexx++
                        playOfflineSong(currentSongIndexx)
                    }
                }
            }

        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val mediaPlayer = CustomSong.getMediaPlayer()
                    mediaPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // No action needed
            }
        })

        binding.play.setOnClickListener {
            isSongPlaying = CustomSong.getMediaPlayer().isPlaying
            if (isSongPlaying) {
                if(isLimitedSong)
                {
                    countDown?.cancel()
                }
                binding.play.setBackgroundResource(R.drawable.baseline_play_arrow_24)
                binding.seekBar.removeCallbacks(createUpdateSeekBarRunnable())
                CustomSong.pause()
                showGif(false)
            }

            else {
                if(isLimitedSong)
                {
                    stopCountDowun(30000,remainingTime)
                }
                binding.play.setBackgroundResource(R.drawable.baseline_pause_24)
                CustomSong.resume()
                //    showGif(true)
                createUpdateSeekBarRunnable().run()
            }

        }

        binding.buy.setOnClickListener {
            purchaseCurrentSong()
        }
        binding.download.setOnClickListener {
            checkIfSongIsPurchased()
        }


    }
    override fun onResume() {
        super.onResume()
        GlobalScope.launch(Dispatchers.Main) {
            fetchPurchasedSongs()
        }
    }


    // Other methods...

    private suspend fun fetchPurchasedSongs() {
        userFetcher.fetchPurchasedSongsForCurrentUser(
            onSuccess = { purchasedSongs ->
                // Check if the current song ID is in the list of purchased song IDs
                val isSongPurchased = purchasedSongs.any { it.first.id == currentSong.id }

                // Update UI based on whether the song is purchased
                if (isSongPurchased) {
                    binding.buy.visibility=View.GONE // Hide buy button if the song is purchased
                    if (isSongDownloaded(currentSong.id!!)) {
                        hideDownloadButton() // Hide download button if the song is downloaded
                    } else {
                        showDownloadButton() // Show download button if the song is purchased but not downloaded
                    }
                } else {
                    showBuyButton() // Show buy button if the song is not purchased
                    hideDownloadButton()
                    // Hide download button if the song is not purchased
                }

                // Show alert if the song is already purchased
                if (isSongPurchased) {
                    binding.buy.visibility=View.GONE
                    showAlreadyPurchasedAlert()
                }
            },
            onFailure = { exception ->
                // Handle failure to fetch purchased songs
                Log.e(TAG, "Failed to fetch purchased songs", exception)
                Toast.makeText(this, "Failed to fetch purchased songs: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun stopMediaPlayer() {
        binding.play.setBackgroundResource(R.drawable.baseline_play_arrow_24)
        CustomSong.let {
            if (it.isPlaying()) {
                it.stopMediaPlayer()
            }
        }
        handler?.removeCallbacksAndMessages(null)
    }


    private fun showAlreadyPurchasedAlert() {
        AlertDialog.Builder(this)
            .setTitle("Already Purchased")
            .setMessage("You have already purchased this song.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun isSongDownloaded(songId: String): Boolean {
        // Get the directory where downloaded songs are stored
        val storageDir = getExternalFilesDir(null)?.absolutePath + "/Downloads"

        // Create a File object for the song file
        val songFile = File("$storageDir/$songId.mp3")

        // Check if the song file exists
        return songFile.exists()
    }
    private fun hideBuyButton() {
        binding.buy.visibility = View.GONE
    }

    private fun hideDownloadButton() {
        binding.download.visibility = View.GONE
    }

    private fun checkSongPurchaseAndDownload(song: SongsModel) {
        song.id?.let {
            userFetcher.isSongPurchased(
                it,
                onSuccess = { isPurchased ->
                    if (isPurchased) {
                        checkDownload(song)
                    } else {
                        // Song is not purchased, show a message
                        showToast("To download this song, please purchase it first.")
                    }
                },
                onFailure = { exception ->
                    // Handle failure to check song purchase status
                    showToast("Error checking song purchase status: ${exception.message}")
                }
            )
        }
    }


    private fun checkDownload(song: SongsModel) {
        Toast.makeText(this, "Download Successful", Toast.LENGTH_SHORT).show()
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request write permission if not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_WRITE_EXTERNAL_STORAGE
            )
        } else {
            // Permission granted, start download
            downloadSong(userModel.uid)

            // Check for downloaded songs after the download operation
            checkDownloadedSongs()
        }
    }

    private fun showPurchaseDialog() {
        // Show pop-up dialog indicating that download is only available after purchase
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Download Unavailable")
        builder.setMessage("This song is only available for download after purchase.")
        builder.setPositiveButton("OK") { dialog, which ->
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun PurchaseDialog() {

        if (!isFinishing && window.decorView.windowToken != null) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Full Song Unavailable")
            builder.setMessage("This song needs to be purchased to play in full.")
            builder.setPositiveButton("OK") { dialog, which ->
            }
            val dialog = builder.create()
            dialog.show()
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

    private fun playSongForLimitedTime(song: SongsModel) {
        isLimitedSong=true
        binding.seekBar.setOnTouchListener { v, event ->
            return@setOnTouchListener true
        }
        CustomSong.playActivitySong(song) {
            val totalLength = CustomSong.getMediaDuration()
            binding.endChronometer.text = totalLength?.let { formatTime(it) }
            isSongPlaying = true
            binding.play.setBackgroundResource(R.drawable.baseline_pause_24)
            createUpdateSeekBarRunnable().run()
            stopCountDowun(30000,1000)
            showGif(false)

        }
    }

    private fun stopCountDowun(i: Long, i1: Long) {
        countDown = object : CountDownTimer(i, i1) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished

            }

            override fun onFinish() {
                showPurchaseMsgAlert()
                stopMediaPlayer()
            }
        }
        countDown?.start()

    }
    private fun addCurrentSongToFavorites() {
        val userId = userModel.uid
        val songId = currentSong.id
        if (userId != null && songId != null) {
            userFetcher.addSongToFavorites(userId, songId,
                onSuccess = {
                    // Handle success, if needed
                    // For example, show a toast indicating success
                    showToast("Song added to favorites")
                },
                onFailure = { exception ->
                    // Handle failure
                    // For example, show a toast with the error message
                    showToast("Failed to add song to favorites: ${exception.message}")
                }
            )
        }
    }


    private fun downloadSong(userId: String) {
        // Replace the following with actual code to download the song
        val songUrl = "https://example.com/song.mp3"
        val fileName = "song_$userId.mp3" // Append userId to the filename
        val storageDir = File(getExternalFilesDir(null), "Downloads")
        storageDir.mkdirs()

        val file = File(storageDir, fileName)
        if (!file.exists()) {
            val url = URL(songUrl)
            val connection = url.openConnection()
            connection.connect()
            val input: InputStream = connection.getInputStream()
            val output = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
            }
            output.close()
            input.close()
            Toast.makeText(this, "Song downloaded", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Song already downloaded", Toast.LENGTH_SHORT).show()
        }
    }
    private fun checkDownloadedSongs() {
        val storageDir = File(getExternalFilesDir(null), "Downloads")
        if (storageDir.exists()) {
            val fileList = storageDir.listFiles()
            if (fileList.isNotEmpty()) {
                // Files found, proceed with your logic
                openOfflineActivity(fileList.toList())
            } else {
                // No files found in the directory
                Toast.makeText(this, "No downloaded songs found", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Directory does not exist
            Toast.makeText(this, "Download directory does not exist", Toast.LENGTH_SHORT).show()
        }
    }
    private fun openOfflineActivity(downloadedSongs: List<File>) {
        // Pass the list of downloaded songs to OfflineActivity
        val intent = Intent(this, OfflinePlaylist::class.java)
        intent.putExtra("downloadedSongs", ArrayList(downloadedSongs))
        startActivity(intent)
    }

    // Function to remove the song from favorites
    private fun removeFromFavorites() {
        currentSong.id?.let { songId ->
            userFetcher?.removeSongFromFavorites(songId,
                onSuccess = {
                    showToast("Song removed from favorites")
                    // Update the drawable resource to unfilled when song is removed from favorites
                    //        binding.favSong.setImageResource(R.drawable.baseline_favorite_border_24)
                    // Update the isFavorite flag
                    isFavorite = false
                },
                onFailure = { exception ->
                    showToast("Failed to remove song from favorites: ${exception.message}")
                })
        }
    }

    private fun playPurchaseSong(currentSongIndex: Int) {
        val song = purchanseSong?.get(currentSongIndex)
        if (CustomSong.isPlaying() && currentPlaybackPosition != 0 && currentSongIndexx == CustomSong.getCurrentSongIndex()) {
            // Song is already playing, and there is a last playback position
            binding.songTitleTextview.text = song?.title
            binding.songSubtitleTextview.text = song?.subtitle
            Glide.with(binding.songCover).load(song?.coverUrl)
                .circleCrop()
                .into(binding.songCover)
            Glide.with(binding.songGifImageView).load(R.drawable.media_playing)
                .circleCrop()
                .into(binding.songGifImageView)
            startUpdatingSeekBar()
            binding.buy.visibility=View.INVISIBLE
            CustomSong.seekTo(currentPlaybackPosition) // Seek to the last playback position
            CustomSong.resume() // Resume playback from the last position
            binding.play.setBackgroundResource(R.drawable.baseline_pause_24)
        }
        else {
            // Song is not playing or is different from the selected song
            binding.songTitleTextview.text = song?.title
            binding.songSubtitleTextview.text = song?.subtitle
            Glide.with(binding.songCover).load(song?.coverUrl)
                .circleCrop()
                .into(binding.songCover)
            Glide.with(binding.songGifImageView).load(R.drawable.media_playing)
                .circleCrop()
                .into(binding.songGifImageView)
            if (song != null) {
                CustomSong.playActivitySong(song) {
                    val totalLength = CustomSong.getMediaDuration()
                    binding.endChronometer.text = totalLength?.let { formatTime(it) }
                    isSongPlaying = true
                    binding.play.setBackgroundResource(R.drawable.baseline_pause_24)
                    createUpdateSeekBarRunnable().run()
                }
            }
        }
    }
    private fun purchaseCurrentSong() {
        // Ensure that the current song index and song list are valid
        if (currentSongIndexx == -1 || songList == null || currentSongIndexx >= songList!!.size) {
            binding.buy.visibility=View.GONE
            return
        }

        // Retrieve the current song from the appropriate source
        val song = if (from == "formPurchaseSong") {
            purchanseSong?.get(currentSongIndexx)
        } else {
            FirebaseFirestore.getInstance().collection("songs")
                .document(songList!![currentSongIndexx]).get()
                .addOnSuccessListener { document ->
                    val song = document.toObject(SongsModel::class.java)
                    song?.let {
                        currentSong = it
                        // Check if the song is already purchased
                        isSongPurchased(
                            song.id!!,
                            onSuccess = { isPurchased ->
                                if (isPurchased) {
                                   binding.buy.visibility=View.GONE
                                    showAlreadyPurchasedAlert()
                                } else {
                                    // Song is not purchased, proceed with purchasing
                                    userFetcher.purchaseSong(
                                        currentSong,
                                        onSuccess = {
                                            showToast("Song purchased successfully")
                                            // Hide the buy button after successful purchase
                                            binding.buy.visibility=View.INVISIBLE
                                        },
                                        onFailure = { exception ->
                                            showToast("Failed to purchase song: ${exception.message}")
                                        }
                                    )
                                }
                            },
                            onFailure = { exception ->
                                showToast("Failed to check song purchase status: ${exception.message}")
                            }
                        )
                    }
                }
                .addOnFailureListener { exception ->
                    showToast("Failed to retrieve song: ${exception.message}")
                }
            // Return null as the song is retrieved asynchronously
            null
        }

        // If the song is already retrieved synchronously, proceed with purchasing
        song?.id?.let { songId ->
            isSongPurchased(
                songId,
                onSuccess = { isPurchased ->
                    if (isPurchased) {
                        binding.buy.visibility=View.GONE
                        showAlreadyPurchasedAlert()

                    } else {
                        // Song is not purchased, proceed with purchasing
                        userFetcher.purchaseSong(
                            song,
                            onSuccess = {
                                showToast("Song purchased successfully")
                                // Hide the buy button after successful purchase
                                binding.buy.visibility=View.INVISIBLE
                            },
                            onFailure = { exception ->
                                showToast("Failed to purchase song: ${exception.message}")
                            }
                        )
                    }
                },
                onFailure = { exception ->
                    showToast("Failed to check song purchase status: ${exception.message}")
                }
            )
        }
    }



    private fun checkIfSongIsPurchased() {
        // Ensure currentSong is properly initialized
        val currentSong = SongsModel() // Initialize with appropriate values

        // Check if currentSong's ID is not null
        currentSong.id?.let { songId ->
            // Call isSongPurchased with currentSong object
            isSongPurchased(currentSong.id,
                onSuccess = { isPurchased ->
                    if (isPurchased) {
                        // Song is purchased, allow download and play
                        enableDownloadButton()
                    } else {
                        // Song is not purchased, show pop-up dialog
                        showPurchaseDialog()
                    }
                },
                onFailure = { exception ->
                    // Handle failure to check song purchase status
                    Log.e(TAG, "Error checking song purchase status", exception)
                    // Display an error message
                    showToast("Failed to check song purchase status")
                }
            )
        }
    }
    private fun disableBuyButton() {
        binding.buy.visibility = View.GONE
    }

    private fun showBuyButton() {
        binding.buy.visibility = View.VISIBLE
    }

    private fun showDownloadButton() {
        binding.buy.visibility = View.GONE
        binding.download.visibility = View.VISIBLE
    }


    private fun enableDownloadButton() {
        // Enable the download button
        binding.download.isEnabled = true
        // Optionally, you can update the appearance of the download button
        // For example, change the alpha value to make it fully visible
        binding.download.alpha = 1.0f
    }

    private fun updateProfileCredit() {
        val userFetcher = UserFetcher()

        // Fetch the remaining credit score and update the profile activity
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val remainingCredit = userFetcher.fetchRemainingCredit()
                // Pass the remaining credit value to ProfileActivity
                val intent = Intent(this@ActivityPlayer, ProfileActivity::class.java)
                intent.putExtra("remainingCredit", remainingCredit)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating profile credit", e)
                // Handle error, for example, display a toast message
                showToast("Error updating profile credit: ${e.message}")
            }
        }
    }
    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this@ActivityPlayer, message, Toast.LENGTH_SHORT).show()
        }
    }
    private fun showGif(show:Boolean) {
        // Show the GIF ImageView
        if(show)
            binding.songGifImageView.visibility = View.VISIBLE
        else{
            binding.songGifImageView.visibility=View.GONE
        }
    }
    private fun playOfflineSong(currentSongIndex: Int) {
        if (CustomSong.isPlaying() && currentPlaybackPosition != 0 && currentSongIndexx == CustomSong.getCurrentSongIndex()) {
            // Song is already playing and the same song is selected again
            binding.songTitleTextview.text = song?.title
            binding.songSubtitleTextview.text = song?.subtitle
            Glide.with(binding.songCover).load(song?.coverUrl)
                .circleCrop()
                .into(binding.songCover)
            Glide.with(binding.songGifImageView).load(R.drawable.media_playing)
                .circleCrop()
                .into(binding.songGifImageView)
            CustomSong.resume()
            startUpdatingSeekBar()
            binding.play.setBackgroundResource(R.drawable.baseline_pause_24)
        } else {
            // Song is either not playing or a different song is selected
            binding.songTitleTextview.text = song?.title
            binding.songSubtitleTextview.text = song?.subtitle
            Glide.with(binding.songCover).load(song?.coverUrl)
                .circleCrop()
                .into(binding.songCover)
            Glide.with(binding.songGifImageView).load(R.drawable.media_playing)
                .circleCrop()
                .into(binding.songGifImageView)
            if (song != null) {
                CustomSong.playActivitySong(song) {
                    val totalLength = CustomSong.getMediaDuration()
                    binding.endChronometer.text = totalLength?.let { formatTime(it) }
                    isSongPlaying = true
                    binding.play.setBackgroundResource(R.drawable.baseline_pause_24)
                    startUpdatingSeekBar()
                    //    showGif(true)
                }
            }
        }
    }

    private fun playSong(currentSongIndex: Int ) {
        if (CustomSong.isPlaying() && currentSong.id == CustomSong.getCurrentSongId()) {

            binding.play.setBackgroundResource(R.drawable.baseline_pause_24)
            binding.songTitleTextview.text = CustomSong.currentSongTitle
            binding.songSubtitleTextview.text = CustomSong.currentSongSubtitle
            Glide.with(binding.songCover).load(CustomSong.currentSongCoverUrl)
                .circleCrop()
                .into(binding.songCover)
            Glide.with(binding.songGifImageView).load(R.drawable.media_playing)
                .circleCrop()
                .into(binding.songGifImageView)

            val totalLength = CustomSong.getMediaDuration()
            binding.endChronometer.text = totalLength?.let { formatTime(it) }
            val song = CustomSong.getCurrentSong()
            song?.let {
                isSongPurchased(it.id!!,
                    onSuccess = { isPurchased ->
                        if (isPurchased) {
                            binding.buy.visibility = View.GONE
                                CustomSong.resume()
                                startUpdatingSeekBar()
                        }
                        else {
                            showPurchaseMsgAlert()
                            playSongForLimitedTime(it)
                        }

                    },
                    onFailure = { exception ->
                        // Handle failure to check song purchase status
                        Log.e(TAG, "Error checking song purchase status", exception)
                        showToast("Failed to check song purchase status")
                    }
                )
            }
        }
        else {
            CustomSong.stopMediaPlayer()
            binding.loader.visibility = View.VISIBLE
            FirebaseFirestore.getInstance().collection("songs")
                .document(songList!![currentSongIndex]).get()
                .addOnSuccessListener { documentSnapshot ->
                    binding.loader.visibility = View.GONE

                    // Check if the document exists and contains data
                    if (documentSnapshot.exists() && documentSnapshot.data != null) {
                        val song = documentSnapshot.toObject(SongsModel::class.java)
                        if (song != null) {
                            // Update UI with song details
                            binding.songTitleTextview.text = song.title
                            binding.songSubtitleTextview.text = song.subtitle
                            Glide.with(binding.songCover).load(song.coverUrl)
                                .circleCrop()
                                .into(binding.songCover)
                            Glide.with(binding.songGifImageView).load(R.drawable.media_playing)
                                .circleCrop()
                                .into(binding.songGifImageView)

                            // Load song from URL
                            song.url?.let { url ->
                                //   showGif(true)
                                isSongPurchased(song.id!!,
                                    onSuccess = { isPurchased ->
                                        if (isPurchased) {
                                            // Song is purchased, play the full song
                                            binding.buy.visibility=View.GONE
                                            playFullSong(song)


                                        } else {
                                            // Song is not purchased, play it for a limited time
                                            showPurchaseMsgAlert()
                                            playSongForLimitedTime(song)
                                        }
                                    },
                                    onFailure = { exception ->
                                        // Handle failure to check song purchase status
                                        Log.e(TAG, "Error checking song purchase status", exception)
                                        showToast("Failed to check song purchase status")
                                    }
                                )
                                binding.buy.setOnClickListener {
                                    purchaseCurrentSong()
                                }

                            }
                            createUpdateSeekBarRunnable()
                        }
                        else {

                        }
                    } else {
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle failure to retrieve song data
                    Log.e(TAG, "Error fetching song data", exception)
                    showToast("Failed to fetch song data: ${exception.message}")
                }
        }

    }
    private fun purchaseSong(song: SongsModel) {
        // Implement your logic for purchasing the song here
        // This could involve showing a purchase dialog, processing payment, etc.
        // After the song is successfully purchased, you can play it
        userFetcher.purchaseSong(
            song,
            onSuccess = {
                showToast("Song purchased successfully")
                // Play the full song after successful purchase
                playFullSong(song)
            },
            onFailure = { exception ->
                showToast("Failed to purchase song: ${exception.message}")
            }
        )
    }

    private fun playFullSong(song: SongsModel) {
        binding.seekBar.isClickable = true
        CustomSong.playActivitySong(song) {
            binding.buy.visibility=View.GONE
            val totalLength = CustomSong.getMediaDuration()
            binding.endChronometer.text = totalLength?.let { formatTime(it) }
            isSongPlaying = true
            binding.play.setBackgroundResource(R.drawable.baseline_pause_24)
            createUpdateSeekBarRunnable().run()

        }
    }
    private fun updateSeekBarAndTime(mediaPlayer: MediaPlayer?) {
        mediaPlayer?.let { player ->
            val totalDuration = player.duration
            binding.seekBar.max = totalDuration
            val timer = Timer()
            timer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    var handler=Handler(Looper.getMainLooper())
                    // Use handler to post the code to the main thread
                    handler.post {
                        if (player.isPlaying) {
                            val currentPosition = player.currentPosition
                            binding.seekBar.progress = currentPosition
                            binding.startingChronometer.text = formatTime(currentPosition)
                            binding.endChronometer.text = formatTime(totalDuration)
                        }
                    }
                }
            }, 0, 1000)

            player.setOnCompletionListener {
                timer.cancel()
            }
        }
    }


    // Function to format time in MM:SS format
    private fun formatTime(durationInMillis: Int): String {
        val minutes = (durationInMillis / 1000) / 60
        val seconds = (durationInMillis / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun createUpdateSeekBarRunnable(): Runnable {
        return object : Runnable {
            override fun run() {
                val duration = CustomSong.getMediaDuration() ?: 0
                val currentPosition = CustomSong.getCurrentPosition() ?: 0

                // Update seek bar progress
                if (duration > 0) {
                    binding.seekBar.max = duration
                    binding.seekBar.progress = currentPosition
                }

                // Update starting timer text
                binding.startingChronometer.text = formatTime(currentPosition)

                // Schedule the next update
                handler?.postDelayed(this, 1000)
            }
        }
    }


    private fun updateUIForPlayingSong(currentPlaybackPosition: Int) :Runnable {
        return object :Runnable {
            override fun run() {
                val totalDuration = CustomSong.getMediaDuration() ?: 0
                val currentPosition=currentPlaybackPosition
                if(totalDuration>0)
                {  binding.seekBar.max=totalDuration
                    binding.seekBar.progress = currentPlaybackPosition
                }
                binding.startingChronometer.text = formatTime(currentPosition)
                handler?.postDelayed(this,1000)
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler?.removeCallbacksAndMessages(null)
    }


    override fun run() {
        val currentPosition = CustomSong.getCurrentPosition()
        binding.startingChronometer.text = formatTime(currentPosition)
        binding.seekBar.progress = mediaPlayer.currentPosition
        handler?.postDelayed(this, 1000)
        // Update every second
    }

    private fun showPurchaseMsgAlert() {

        AlertDialog.Builder(this@ActivityPlayer)
            .setTitle("You have to purchase the song")
            .setMessage("Purchase this Song for full access")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun startUpdatingSeekBar() {
        val duration = CustomSong.getMediaDuration() ?: 0
        binding.seekBar.max = duration
        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                val currentPosition = CustomSong.getCurrentPosition()
                val totalLength = CustomSong.getMediaDuration()
                binding.endChronometer.text = totalLength?.let { formatTime(it) }
                binding.seekBar.progress = currentPosition
                binding.startingChronometer.text = formatTime(currentPosition) // Implement this method to format duration
                handler.postDelayed(this, SEEK_BAR_UPDATE_INTERVAL)
            }
        }, SEEK_BAR_UPDATE_INTERVAL)
    }
    companion object {
        private const val REQUEST_WRITE_EXTERNAL_STORAGE = 1
        private const val SEEK_BAR_UPDATE_INTERVAL = 1000L // Update interval in milliseconds
    }


}