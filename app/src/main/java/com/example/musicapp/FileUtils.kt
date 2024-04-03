package com.example.musicapp

import android.content.Context
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader

object FileUtils {
fun saveSongToFile(context: Context, songData: ByteArray, fileName: String) {
    val fileOutputStream: FileOutputStream
    try {
        fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
        fileOutputStream.write(songData)
        fileOutputStream.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun readSongFromFile(context: Context, fileName: String): ByteArray? {
    return try {
        val fileInputStream: FileInputStream = context.openFileInput(fileName)
        val inputStreamReader = InputStreamReader(fileInputStream)
        val bufferedReader = BufferedReader(inputStreamReader)
        val stringBuilder = StringBuilder()
        var text: String? = null
        while ({ text = bufferedReader.readLine(); text }() != null) {
            stringBuilder.append(text)
        }
        fileInputStream.readBytes()
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}
}