package com.example.musicano.Models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class Song(
    @SerializedName("title") val title: String,
    @SerializedName("preview") val previewUrl: String,
    @SerializedName("album") val album: Album,
    @SerializedName("artist") val artist: Artist
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readParcelable(Album::class.java.classLoader) ?: Album("", ""),
        parcel.readParcelable(Artist::class.java.classLoader) ?: Artist("")
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(previewUrl)
        parcel.writeParcelable(album, flags)
        parcel.writeParcelable(artist, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Song> {
        override fun createFromParcel(parcel: Parcel): Song {
            return Song(parcel)
        }

        override fun newArray(size: Int): Array<Song?> {
            return arrayOfNulls(size)
        }
    }
}
