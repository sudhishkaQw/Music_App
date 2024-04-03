package com.example.musicapp.model

import androidx.lifecycle.ViewModel

class MyViewModel (private val downloadedSongRepository: SongRepository) : ViewModel() {
//
//    private val _downloadedSongs = MutableLiveData<List<DownloadedSong>>()
//    val downloadedSongs: LiveData<List<DownloadedSong>> get() = _downloadedSongs
//
//    fun getDownloadedSongsById(songId: String) {
//        viewModelScope.launch {
//            val downloadedSongs = downloadedSongRepository.getDownloadedSongs()
//            _downloadedSongs.value = downloadedSongs
//        }
//    }
}
