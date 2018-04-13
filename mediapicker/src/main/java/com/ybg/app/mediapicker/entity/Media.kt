package com.ybg.app.mediapicker.entity

import android.os.Parcel
import android.os.Parcelable

class Media : Parcelable {
    var path: String
    var name: String
    var time: Long = 0
    var mediaType: Int = 0
    var size: Long = 0
    var id: Int = 0
    var parentDir: String

    constructor(path: String, name: String, time: Long, mediaType: Int, size: Long, id: Int, parentDir: String) {
        this.path = path
        this.name = name
        this.time = time
        this.mediaType = mediaType
        this.size = size
        this.id = id
        this.parentDir = parentDir
    }


    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.path)
        dest.writeString(this.name)
        dest.writeLong(this.time)
        dest.writeInt(this.mediaType)
        dest.writeLong(this.size)
        dest.writeInt(this.id)
        dest.writeString(this.parentDir)
    }

    protected constructor(`in`: Parcel) {
        this.path = `in`.readString()
        this.name = `in`.readString()
        this.time = `in`.readLong()
        this.mediaType = `in`.readInt()
        this.size = `in`.readLong()
        this.id = `in`.readInt()
        this.parentDir = `in`.readString()
    }

    companion object {

        @JvmField val CREATOR: Parcelable.Creator<Media> = object : Parcelable.Creator<Media> {
            override fun createFromParcel(source: Parcel): Media {
                return Media(source)
            }

            override fun newArray(size: Int): Array<Media?> {
                return arrayOfNulls<Media>(size)
            }
        }
    }

}
