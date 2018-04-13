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

class MediaLoader(private var mContext: Context, private var mLoader: DataCallback) : LoaderM(),
        LoaderManager.LoaderCallbacks<Cursor> {
    private var MEDIA_PROJECTION = arrayOf(MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.PARENT)

    override fun onCreateLoader(picker_type: Int, bundle: Bundle?): Loader<Cursor> {
        val selection = (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)

        val queryUri = MediaStore.Files.getContentUri("external")
        return CursorLoader(
                mContext,
                queryUri,
                MEDIA_PROJECTION,
                selection,
                null, // Selection args (none).
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC" // Sort order.
        )
    }

    override fun onLoadFinished(loader: Loader<Cursor>, o: Cursor) {
        val folders = ArrayList<Folder>()
        val allFolder = Folder(mContext.resources.getString(R.string.all_dir_name))
        folders.add(allFolder)
        val allVideoDir = Folder(mContext.resources.getString(R.string.video_dir_name))
        folders.add(allVideoDir)
        val cursor = o
        while (cursor.moveToNext()) {
            val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME))
            val dateTime = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED))
            val mediaType = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE))
            val size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE))
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))

            if (size < 1) continue
            val dirName = getParent(path)
            val media = Media(path, name, dateTime, mediaType, size, id, dirName)
            allFolder.addMedias(media)
            if (mediaType == 3) {
                allVideoDir.addMedias(media)
            }

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
