package com.ybg.app.mediapicker.data

import android.app.LoaderManager
import android.content.Context
import android.content.CursorLoader
import android.content.Loader
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore

import com.ybg.app.mediapicker.R
import com.ybg.app.mediapicker.entity.Folder
import com.ybg.app.mediapicker.entity.Media

import java.util.ArrayList

class ImageLoader(private var mContext: Context, private var mLoader: DataCallback) : LoaderM(),
        LoaderManager.LoaderCallbacks<Cursor> {

    internal var IMAGE_PROJECTION = arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media.SIZE, MediaStore.Images.Media._ID)

    override fun onCreateLoader(picker_type: Int, bundle: Bundle?): Loader<Cursor> {
        val queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        return CursorLoader(
                mContext,
                queryUri,
                IMAGE_PROJECTION,
                null, null, // Selection args (none).
                MediaStore.Images.Media.DATE_ADDED + " DESC" // Sort order.
        )
    }

    override fun onLoadFinished(loader: Loader<Cursor>?, o: Cursor?) {
        val folders = ArrayList<Folder>()
        val allFolder = Folder(mContext.resources.getString(R.string.all_image))
        folders.add(allFolder)
        val cursor = o as Cursor
        while (cursor.moveToNext()) {

            val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
            val dateTime = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED))
            val mediaType = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE))
            val size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE))
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))

            if (size < 1) continue
            val dirName = getParent(path)
            val media = Media(path, name, dateTime, mediaType, size, id, dirName)
            allFolder.addMedias(media)

            val index = hasDir(folders, dirName)
            if (index != -1) {
                folders[index].addMedias(media)
            } else {
                val folder = Folder(dirName)
                folder.addMedias(media)
                folders.add(folder)
            }
        }
        mLoader.onData(folders)
        cursor.close()
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {

    }

}