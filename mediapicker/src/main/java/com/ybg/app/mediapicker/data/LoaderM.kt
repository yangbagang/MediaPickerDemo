package com.ybg.app.mediapicker.data

import com.ybg.app.mediapicker.entity.Folder

import java.util.ArrayList

open class LoaderM {

    fun getParent(path: String): String {
        val sp = path.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return sp[sp.size - 2]
    }

    fun hasDir(folders: ArrayList<Folder>, dirName: String): Int {
        for (i in folders.indices) {
            val folder = folders[i]
            if (folder.name == dirName) {
                return i
            }
        }
        return -1
    }

}
