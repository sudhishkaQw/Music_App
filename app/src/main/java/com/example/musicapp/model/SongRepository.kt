package com.example.musicapp.model

import com.example.musicapp.LocalDatabase.DownloadedSongDao

class SongRepository(private val downloadedSongDao: DownloadedSongDao) {

//    suspend fun getDownloadedSongs(): List<DownloadedSong> {
//        return downloadedSongDao.getAllDownloadedSongs()
//    }
//    suspend fun insert(downloadedSong: DownloadedSong) {
//        downloadedSongDao.insert(downloadedSong)
//    }
}
