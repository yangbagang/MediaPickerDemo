package com.ybg.app.cameraview.state

import android.graphics.Bitmap
import android.view.Surface
import android.view.SurfaceHolder

import com.ybg.app.cameraview.CameraInterface
import com.ybg.app.cameraview.CameraView
import com.ybg.app.cameraview.util.LogUtil

internal class PreviewState(private val machine: CameraMachine) : State {

    override fun start(holder: SurfaceHolder, screenProp: Float) {
        CameraInterface.instance?.doStartPreview(holder, screenProp)
    }

    override fun stop() {
        CameraInterface.instance?.doStopPreview()
    }


    override fun focus(x: Float, y: Float, callback: CameraInterface.FocusCallback) {
        LogUtil.i("preview state focus")
        if (machine.view.handlerFoucs(x, y)) {
            CameraInterface.instance?.handleFocus(machine.context, x, y, callback)
        }
    }

    override fun swtich(holder: SurfaceHolder, screenProp: Float) {
        CameraInterface.instance?.switchCamera(holder, screenProp)
    }

    override fun restart() {

    }

    override fun capture() {
        CameraInterface.instance?.takePicture(object : CameraInterface.TakePictureCallback{
            override fun captureResult(bitmap: Bitmap, isVertical: Boolean) {
                machine.view.showPicture(bitmap, isVertical)
                machine.state = machine.borrowPictureState
                LogUtil.i("capture")
            }
        })
    }

    override fun record(surface: Surface, screenProp: Float) {
        CameraInterface.instance?.startRecord(surface, screenProp, object : CameraInterface.ErrorCallback{
            override fun onError() {
                println("onError")
            }
        })
    }

    override fun stopRecord(isShort: Boolean, time: Long) {
        CameraInterface.instance?.stopRecord(isShort, object : CameraInterface.StopRecordCallback {
            override fun recordResult(url: String?, firstFrame: Bitmap?) {
                if (isShort) {
                    machine.view.resetState(CameraView.TYPE_SHORT)
                } else {
                    machine.view.playVideo(firstFrame!!, url!!)
                    machine.state = machine.borrowVideoState
                }
            }
        })
    }

    override fun cancle(holder: SurfaceHolder, screenProp: Float) {
        LogUtil.i("浏览状态下,没有 cancle 事件")
    }

    override fun confirm() {
        LogUtil.i("浏览状态下,没有 confirm 事件")
    }

    override fun zoom(zoom: Float, type: Int) {
        LogUtil.i(TAG, "zoom")
        CameraInterface.instance?.setZoom(zoom, type)
    }

    override fun flash(mode: String) {
        CameraInterface.instance?.setFlashMode(mode)
    }

    companion object {
        val TAG = "PreviewState"
    }
}
