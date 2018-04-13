package com.ybg.app.cameraview.activity

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast

import com.ybg.app.cameraview.CameraView
import com.ybg.app.cameraview.R
import com.ybg.app.cameraview.listener.ClickListener
import com.ybg.app.cameraview.listener.ErrorListener
import com.ybg.app.cameraview.listener.JCameraListener
import com.ybg.app.cameraview.util.DeviceUtil
import com.ybg.app.cameraview.util.FileUtil

import java.io.File

class CameraActivity : AppCompatActivity() {
    private var mCameraView: CameraView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_camera)
        mCameraView = findViewById<View>(R.id.cameraView) as CameraView
        //设置视频保存路径
        mCameraView!!.setSaveVideoPath(Environment.getExternalStorageDirectory().path + File.separator + "CameraView")
        mCameraView!!.setFeatures(CameraView.BUTTON_STATE_BOTH)
        mCameraView!!.setTip("轻触拍照，长按摄像")
        mCameraView!!.setMediaQuality(CameraView.MEDIA_QUALITY_MIDDLE)
        mCameraView!!.setErrorLisenter(object : ErrorListener {
            override fun onError() {
                //错误监听
                Log.i("CameraActivity", "camera error")
                val intent = Intent()
                setResult(103, intent)
                finish()
            }

            override fun audioPermissionError() {
                Toast.makeText(this@CameraActivity, "给点录音权限可以?", Toast.LENGTH_SHORT).show()
            }
        })
        //CameraView监听
        mCameraView!!.setJCameraLisenter(object : JCameraListener {
            override fun captureSuccess(bitmap: Bitmap) {
                //获取图片bitmap
                //                Log.i("CameraView", "bitmap = " + bitmap.getWidth());
                val path = FileUtil.saveBitmap("CameraView", bitmap)
                val intent = Intent()
                intent.putExtra("path", path)
                setResult(101, intent)
                finish()
            }

            override fun recordSuccess(url: String, firstFrame: Bitmap) {
                //获取视频路径
                val path = FileUtil.saveBitmap("CameraView", firstFrame)
                Log.i("CJT", "url = $url, Bitmap = $path")
                val intent = Intent()
                intent.putExtra("path", url)
                intent.putExtra("thumbnail", path)
                setResult(102, intent)
                finish()
            }
        })

        mCameraView!!.setLeftClickListener(object : ClickListener {
            override fun onClick() {
                this@CameraActivity.finish()
            }
        })
        mCameraView!!.setRightClickListener(object : ClickListener {
            override fun onClick() {
                Toast.makeText(this@CameraActivity, "Right", Toast.LENGTH_SHORT).show()
            }
        })

        Log.i("CameraActivity", DeviceUtil.deviceModel)
    }

    override fun onStart() {
        super.onStart()
        //全屏显示
        if (Build.VERSION.SDK_INT >= 19) {
            val decorView = window.decorView
            decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        } else {
            val decorView = window.decorView
            val option = View.SYSTEM_UI_FLAG_FULLSCREEN
            decorView.systemUiVisibility = option
        }
    }

    override fun onResume() {
        super.onResume()
        mCameraView!!.onResume()
    }

    override fun onPause() {
        super.onPause()
        mCameraView!!.onPause()
    }
}
