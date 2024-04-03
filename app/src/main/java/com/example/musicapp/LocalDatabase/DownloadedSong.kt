    package com.example.musicapp.LocalDatabase

    import androidx.room.Entity
    import androidx.room.PrimaryKey



    @Entity(tableName = "songs")
    data class DownloadedSong(
        @PrimaryKey(autoGenerate = true)
        val id: Long = 0,
        val songId: String, // Assuming your song ID is stored as a string
        val title: String,
        val subtitle: String,
        val url: String,
        val coverUrl: String,
        val credits: Int,
        val audioFileName: String
    )


