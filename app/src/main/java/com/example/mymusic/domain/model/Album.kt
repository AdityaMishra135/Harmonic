package com.example.mymusic.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "album")
data class Album(
    val albumId : String,
    @PrimaryKey val albumName : String,
    var songs : List<Song>,
    val songCount: Int,
    val artist : String,
    val albumImage : String
)
