package com.ybg.app.mediapicker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.widget.Toast

import com.ybg.app.mediapicker.entity.Media

import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date

class TakePhotoActivity : Activity() {
    private var NuriForFile: Uri? = null
    private var mTmpFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        try {
            mTmpFile = createImageFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//如果大于等于7.0使用FileProvider
            NuriForFile = FileProvider.getUriForFile(this, this.packageName + ".share", mTmpFile!!)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, NuriForFile)
            startActivityForResult(intent, 100)
        } else {
            if (mTmpFile != null && mTmpFile!!.exists()) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTmpFile))
                startActivityForResult(intent, 101)
            } else {
                Toast.makeText(this, "take error", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        val medias = ArrayList<Media>()
        if (requestCode == 100 || requestCode == 101 && resultCode == Activity.RESULT_OK) {
            if (mTmpFile!!.length() > 0) {
                val media = Media(mTmpFile!!.path, mTmpFile!!.name, 0, 1, mTmpFile!!.length(), 0, "")
                medias.add(media)
                val intent = Intent()
                intent.putParcelableArrayListExtra(PickerConfig.EXTRA_RESULT, medias)
                setResult(PickerConfig.RESULT_CODE, intent)
            }
            finish()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir      /* directory */
        )
    }
}
