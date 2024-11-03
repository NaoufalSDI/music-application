package com.example.musicano.Models

import android.os.Parcel
import android.os.Parcelable

data class TrackList(
    val data: List<Song>?
) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.createTypedArrayList(Song.CREATOR))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(data)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TrackList> {
        override fun createFromParcel(parcel: Parcel): TrackList {
            return TrackList(parcel)
        }

        override fun newArray(size: Int): Array<TrackList?> {
            return arrayOfNulls(size)
        }
    }
}
