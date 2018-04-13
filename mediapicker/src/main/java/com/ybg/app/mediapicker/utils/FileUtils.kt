package com.ybg.app.mediapicker.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.text.TextUtils

import java.io.File
import java.io.IOException
import java.text.DecimalFormat
import java.util.Locale

import android.os.Environment.MEDIA_MOUNTED

object FileUtils {

    fun getSizeByUnit(size: Double): String {

        if (size == 0.0) {
            return "0K"
        }
        if (size >= MB) {
            val sizeInM = size / MB
            return String.format(Locale.getDefault(), "%.1f", sizeInM) + "M"
        }
        val sizeInK = size / 1024
        return String.format(Locale.getDefault(), "%.1f", sizeInK) + "K"
    }

    private const val JPEG_FILE_PREFIX = "IMG_"
    private const val JPEG_FILE_SUFFIX = ".jpg"

    @Throws(IOException::class)
    fun createTmpFile(context: Context): File {
        var dir: File?
        if (TextUtils.equals(Environment.getExternalStorageState(), Environment.MEDIA_MOUNTED)) {
            dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            if (!dir!!.exists()) {
                dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/Camera")
                if (!dir!!.exists()) {
                    dir = getCacheDirectory(context, true)
                }
            }
        } else {
            dir = getCacheDirectory(context, true)
        }
        return File.createTempFile(JPEG_FILE_PREFIX, JPEG_FILE_SUFFIX, dir)
    }


    private val EXTERNAL_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE"

    /**
     * Returns application cache directory. Cache directory will be created on SD card
     * *("/Android/data/[app_package_name]/cache")* (if card is mounted and app has appropriate permission) or
     * on device's file system depending incoming parameters.
     *
     * @param context        Application context
     * @param preferExternal Whether prefer external location for cache
     * @return Cache [directory][File].<br></br>
     * **NOTE:** Can be null in some unpredictable cases (if SD card is unmounted and
     * [Context.getCacheDir()][Context.getCacheDir] returns null).
     */
    @JvmOverloads
    fun getCacheDirectory(context: Context, preferExternal: Boolean = true): File {
        var appCacheDir: File? = null
        var externalStorageState: String
        try {
            externalStorageState = Environment.getExternalStorageState()
        } catch (e: NullPointerException) { // (sh)it happens (Issue #660)
            externalStorageState = ""
        } catch (e: IncompatibleClassChangeError) { // (sh)it happens too (Issue #989)
            externalStorageState = ""
        }

        if (preferExternal && MEDIA_MOUNTED == externalStorageState && hasExternalStoragePermission(context)) {
            appCacheDir = getExternalCacheDir(context)
        }
        if (appCacheDir == null) {
            appCacheDir = context.cacheDir
        }
        if (appCacheDir == null) {
            val cacheDirPath = "/data/data/" + context.packageName + "/cache/"
            appCacheDir = File(cacheDirPath)
        }
        return appCacheDir
    }

    /**
     * Returns individual application cache directory (for only image caching from ImageLoader). Cache directory will be
     * created on SD card *("/Android/data/[app_package_name]/cache/uil-images")* if card is mounted and app has
     * appropriate permission. Else - Android defines cache directory on device's file system.
     *
     * @param context  Application context
     * @param cacheDir Cache directory path (e.g.: "AppCacheDir", "AppDir/cache/images")
     * @return Cache [directory][File]
     */
    fun getIndividualCacheDirectory(context: Context, cacheDir: String): File {
        val appCacheDir = getCacheDirectory(context)
        var individualCacheDir = File(appCacheDir, cacheDir)
        if (!individualCacheDir.exists()) {
            if (!individualCacheDir.mkdir()) {
                individualCacheDir = appCacheDir
            }
        }
        return individualCacheDir
    }

    private fun getExternalCacheDir(context: Context): File? {
        val dataDir = File(File(Environment.getExternalStorageDirectory(), "Android"), "data")
        val appCacheDir = File(File(dataDir, context.packageName), "cache")
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                return null
            }
            try {
                File(appCacheDir, ".nomedia").createNewFile()
            } catch (e: IOException) {
            }

        }
        return appCacheDir
    }

    private val MB = (1024 * 1024).toLong()

    private fun hasExternalStoragePermission(context: Context): Boolean {
        val perm = context.checkCallingOrSelfPermission(EXTERNAL_STORAGE_PERMISSION)
        return perm == PackageManager.PERMISSION_GRANTED
    }

    fun fileSize(size: Long): String {
        if (size <= 0) return "0"
        val units = arrayOf("B", "kB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }

}
