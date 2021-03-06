package com.ybg.app.cameraview.state

import android.content.Context
import android.view.Surface
import android.view.SurfaceHolder

import com.ybg.app.cameraview.CameraInterface
import com.ybg.app.cameraview.view.CameraView

class CameraMachine(val context: Context, val view: CameraView, cameraOpenOverCallback: CameraInterface.CameraOpenOverCallback) : State {
    var state: State? = null
    //    private CameraInterface.CameraOpenOverCallback cameraOpenOverCallback;

    //获取空闲状态
    internal val previewState: State       //浏览状态(空闲)
    //获取浏览图片状态
    internal val borrowPictureState: State //浏览图片
    //获取浏览视频状态
    internal val borrowVideoState: State   //浏览视频

    init {
        previewState = PreviewState(this)
        borrowPictureState = BorrowPictureState(this)
        borrowVideoState = BorrowVideoState(this)
        //默认设置为空闲状态
        this.state = previewState
    }//        this.cameraOpenOverCallback = cameraOpenOverCallback;

    override fun start(holder: SurfaceHolder, screenProp: Float) {
        state!!.start(holder, screenProp)
    }

    override fun stop() {
        state!!.stop()
    }

    override fun focus(x: Float, y: Float, callback: CameraInterface.FocusCallback) {
        state!!.focus(x, y, callback)
    }

    override fun swtich(holder: SurfaceHolder, screenProp: Float) {
        state!!.swtich(holder, screenProp)
    }

    override fun restart() {
        state!!.restart()
    }

    override fun capture() {
        state!!.capture()
    }

    override fun record(surface: Surface, screenProp: Float) {
        state!!.record(surface, screenProp)
    }

    override fun stopRecord(isShort: Boolean, time: Long) {
        state!!.stopRecord(isShort, time)
    }

    override fun cancle(holder: SurfaceHolder, screenProp: Float) {
        state!!.cancle(holder, screenProp)
    }

    override fun confirm() {
        state!!.confirm()
    }


    override fun zoom(zoom: Float, type: Int) {
        state!!.zoom(zoom, type)
    }

    override fun flash(mode: String) {
        state!!.flash(mode)
    }
}
