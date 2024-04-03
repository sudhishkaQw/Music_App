package com.example.musicapp.model

import android.os.Parcel
import android.os.Parcelable

data class SongsModel(
    val id: String?,
    val title: String?,
    val subtitle: String?,
    val url: String?,
    val coverUrl: String?,
    val credits:Int,
    var downloaded: Boolean,
    var isFavorite:Boolean,
    val audioFileName: String?,
    var isPlaying:Boolean
) : Parcelable
{
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    constructor():this("","","","","",10,false,false,"",false)


    companion object CREATOR : Parcelable.Creator<SongsModel> {
        override fun createFromParcel(parcel: Parcel): SongsModel {
            return SongsModel(parcel)
        }

        override fun newArray(size: Int): Array<SongsModel?> {
            return arrayOfNulls(size)
        }
    }

    override fun describeContents(): Int {
        return 0;
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(title)
        dest.writeString(subtitle)
        dest.writeString(url)
        dest.writeString(coverUrl)
        dest.writeInt(credits)
        dest.writeByte(if (downloaded) 1 else 0)
        dest.writeByte(if (isFavorite) 1 else 0) // Write isFavorite field
        dest.writeString(audioFileName)
        dest.writeByte(if (isPlaying) 1 else 0)
    }

}
