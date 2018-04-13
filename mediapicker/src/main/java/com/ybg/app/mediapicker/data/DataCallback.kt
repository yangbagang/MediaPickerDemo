package com.ybg.app.mediapicker.data

import com.ybg.app.mediapicker.entity.Folder

import java.util.ArrayList

interface DataCallback {

    fun onData(list: ArrayList<Folder>)

}
