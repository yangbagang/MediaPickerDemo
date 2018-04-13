package com.ybg.app.mediapickerdemo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import com.ybg.app.cameraview.activity.CameraActivity
import com.ybg.app.mediapicker.PickerActivity
import com.ybg.app.mediapicker.PickerConfig
import com.ybg.app.mediapicker.entity.Media
import kotlinx.android.synthetic.main.activity_main.*
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    private val REQUEST_CAMERA = 300
    private val REQUEST_PERMISSION = 100
    private val REQUEST_MEIDA = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_browse.setOnClickListener {
            go()
        }
        btn_capture.setOnClickListener {
            openCamera()
        }
    }

    private fun openCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager
                            .PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager
                            .PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager
                            .PERMISSION_GRANTED) {
                startActivityForResult(Intent(this@MainActivity, CameraActivity::class.java), REQUEST_CAMERA)
            } else {
                //不具有获取权限，需要进行权限申请
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA), REQUEST_PERMISSION)
            }
        } else {
            startActivityForResult(Intent(this@MainActivity, CameraActivity::class.java), REQUEST_CAMERA)
        }
    }

    private var select = ArrayList<Media>()
    private fun go() {
        val intent = Intent(this@MainActivity, PickerActivity::class.java)
        intent.putExtra(PickerConfig.SELECT_MODE, PickerConfig.PICKER_IMAGE_VIDEO)//default image and video (Optional)
        val maxSize = 188743680L//long long long
        intent.putExtra(PickerConfig.MAX_SELECT_SIZE, maxSize) //default 180MB (Optional)
        intent.putExtra(PickerConfig.MAX_SELECT_COUNT, 15)  //default 40 (Optional)
        intent.putExtra(PickerConfig.DEFAULT_SELECTED_LIST, select) // (Optional)
        this@MainActivity.startActivityForResult(intent, REQUEST_MEIDA)

        //        Intent intent =new Intent(MainActivity.this, TakePhotoActivity.class); //Take a photo with a camera
        //        MainActivity.this.startActivityForResult(intent,200);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_MEIDA && resultCode == PickerConfig.RESULT_CODE) {
            select = data.getParcelableArrayListExtra<Media>(PickerConfig.EXTRA_RESULT)
            for (media in select) {
                Log.i("media", media.path)
                Log.e("media", "s:" + media.size)
            }
        } else if (requestCode == REQUEST_CAMERA) {
            if (resultCode == 101) {
                val path = data.getStringExtra("path")
                iv_demo.setImageBitmap(BitmapFactory.decodeFile(path))
                println("picture, path=$path")
            } else if (resultCode == 102) {
                val path = data.getStringExtra("path")
                println("video, path=$path")
            } else if (resultCode == 103) {
                Toast.makeText(this, "请检查相机权限~", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION) {
            var size = 0
            if (grantResults.isNotEmpty()) {
                val writeResult = grantResults[0]
                //读写内存权限
                val writeGranted = writeResult == PackageManager.PERMISSION_GRANTED//读写内存权限
                if (!writeGranted) {
                    size++
                }
                //录音权限
                val recordPermissionResult = grantResults[1]
                val recordPermissionGranted = recordPermissionResult == PackageManager.PERMISSION_GRANTED
                if (!recordPermissionGranted) {
                    size++
                }
                //相机权限
                val cameraPermissionResult = grantResults[2]
                val cameraPermissionGranted = cameraPermissionResult == PackageManager.PERMISSION_GRANTED
                if (!cameraPermissionGranted) {
                    size++
                }
                if (size == 0) {
                    startActivityForResult(Intent(this@MainActivity, CameraActivity::class.java)
                            , REQUEST_CAMERA)
                } else {
                    Toast.makeText(this, "请到设置-权限管理中开启", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
