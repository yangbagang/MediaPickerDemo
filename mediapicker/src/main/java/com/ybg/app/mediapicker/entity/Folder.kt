package com.ybg.app.mediapicker.entity

import android.os.Parcel
import android.os.Parcelable

import java.util.ArrayList

class Folder : Parcelable {

    var name: String
    var count: Int = 0
    var medias = ArrayList<Media>()
        internal set

    fun addMedias(media: Media) {
        medias.add(media)
    }

    constructor(name: String) {
        this.name = name
    }


    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.name)
        dest.writeInt(this.count)
        dest.writeTypedList(this.medias)
    }


    protected constructor(`in`: Parcel) {
        this.name = `in`.readString()
        this.count = `in`.readInt()
        this.medias = `in`.createTypedArrayList(Media.CREATOR)
    }

    companion object {

        @JvmField val CREATOR: Parcelable.Creator<Folder> = object : Parcelable.Creator<Folder> {
            override fun createFromParcel(source: Parcel): Folder {
                return Folder(source)
            }

            override fun newArray(size: Int): Array<Folder?> {
                return arrayOfNulls<Folder>(size)
            }
        }
    }

}
