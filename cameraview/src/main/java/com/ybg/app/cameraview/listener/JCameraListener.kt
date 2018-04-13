package com.ybg.app.cameraview.listener

import android.graphics.Bitmap

interface JCameraListener {

    fun captureSuccess(bitmap: Bitmap)

    fun recordSuccess(url: String, firstFrame: Bitmap)

}
