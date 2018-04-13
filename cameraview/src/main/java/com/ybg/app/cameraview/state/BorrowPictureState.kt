package com.ybg.app.cameraview.state

import android.view.Surface
import android.view.SurfaceHolder

import com.ybg.app.cameraview.CameraInterface
import com.ybg.app.cameraview.CameraView
import com.ybg.app.cameraview.util.LogUtil

class BorrowPictureState(private val machine: CameraMachine) : State {
    private val TAG = "BorrowPictureState"

    override fun start(holder: SurfaceHolder, screenProp: Float) {
        CameraInterface.instance?.doStartPreview(holder, screenProp)
        machine.state = machine.previewState
    }

    override fun stop() {

    }


    override fun focus(x: Float, y: Float, callback: CameraInterface.FocusCallback) {}

    override fun swtich(holder: SurfaceHolder, screenProp: Float) {

    }

    override fun restart() {

    }

    override fun capture() {

    }

    override fun record(surface: Surface, screenProp: Float) {

    }

    override fun stopRecord(isShort: Boolean, time: Long) {}

    override fun cancle(holder: SurfaceHolder, screenProp: Float) {
        CameraInterface.instance?.doStartPreview(holder, screenProp)
        machine.view.resetState(CameraView.TYPE_PICTURE)
        machine.state = machine.previewState
    }

    override fun confirm() {
        machine.view.confirmState(CameraView.TYPE_PICTURE)
        machine.state = machine.previewState
    }

    override fun zoom(zoom: Float, type: Int) {
        LogUtil.i(TAG, "zoom")
    }

    override fun flash(mode: String) {

    }

}
