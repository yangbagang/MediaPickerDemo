package com.ybg.app.mediapicker

import android.Manifest
import android.content.Intent
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.ListPopupWindow
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import com.bumptech.glide.Glide
import com.ybg.app.mediapicker.adapter.FolderAdapter
import com.ybg.app.mediapicker.adapter.MediaGridAdapter
import com.ybg.app.mediapicker.adapter.SpacingDecoration
import com.ybg.app.mediapicker.data.DataCallback
import com.ybg.app.mediapicker.data.ImageLoader
import com.ybg.app.mediapicker.data.MediaLoader
import com.ybg.app.mediapicker.data.VideoLoader
import com.ybg.app.mediapicker.entity.Folder
import com.ybg.app.mediapicker.entity.Media
import com.ybg.app.mediapicker.utils.ScreenUtils

import java.util.ArrayList

import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

class PickerActivity : AppCompatActivity(), DataCallback, View.OnClickListener {

    private var argsIntent: Intent? = null
    private var recyclerView: RecyclerView? = null
    private var done: Button? = null
    private var category_btn: Button? = null
    private var preview: Button? = null
    private var gridAdapter: MediaGridAdapter? = null
    private var mFolderPopupWindow: ListPopupWindow? = null
    private var mFolderAdapter: FolderAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        argsIntent = intent
        setContentView(R.layout.main)
        recyclerView = findViewById<View>(R.id.recycler_view) as RecyclerView
        findViewById<View>(R.id.btn_back).setOnClickListener(this)
        setTitleBar()
        done = findViewById<View>(R.id.done) as Button
        category_btn = findViewById<View>(R.id.category_btn) as Button
        preview = findViewById<View>(R.id.preview) as Button
        done?.setOnClickListener(this)
        category_btn?.setOnClickListener(this)
        preview?.setOnClickListener(this)
        //get view end
        createAdapter()
        createFolderAdapter()
        getMediaData()
    }


    fun setTitleBar() {
        val type = argsIntent?.getIntExtra(PickerConfig.SELECT_MODE, PickerConfig.PICKER_IMAGE_VIDEO)
        if (type == PickerConfig.PICKER_IMAGE_VIDEO) {
            (findViewById<View>(R.id.bar_title) as TextView).text = getString(R.string.select_title)
        } else if (type == PickerConfig.PICKER_IMAGE) {
            (findViewById<View>(R.id.bar_title) as TextView).text = getString(R.string.select_image_title)
        } else if (type == PickerConfig.PICKER_VIDEO) {
            (findViewById<View>(R.id.bar_title) as TextView).text = getString(R.string.select_video_title)
        }
    }

    internal fun createAdapter() {
        //创建默认的线性LayoutManager
        val mLayoutManager = GridLayoutManager(this, PickerConfig.GridSpanCount)
        recyclerView?.layoutManager = mLayoutManager
        recyclerView?.addItemDecoration(SpacingDecoration(PickerConfig.GridSpanCount, PickerConfig.GridSpace))
        //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        recyclerView?.setHasFixedSize(true)
        //创建并设置Adapter
        val medias = ArrayList<Media>()
        val select = argsIntent?.getParcelableArrayListExtra<Media>(PickerConfig.DEFAULT_SELECTED_LIST)
        val maxSelect = argsIntent?.getIntExtra(PickerConfig.MAX_SELECT_COUNT, PickerConfig.DEFAULT_SELECTED_MAX_COUNT)
        val maxSize = argsIntent?.getLongExtra(PickerConfig.MAX_SELECT_SIZE, PickerConfig.DEFAULT_SELECTED_MAX_SIZE)
        gridAdapter = MediaGridAdapter(medias, this, select, maxSelect ?: PickerConfig.DEFAULT_SELECTED_MAX_COUNT, maxSize ?: PickerConfig.DEFAULT_SELECTED_MAX_SIZE)
        recyclerView?.adapter = gridAdapter
    }

    internal fun createFolderAdapter() {
        val folders = ArrayList<Folder>()
        mFolderAdapter = FolderAdapter(folders, this)
        mFolderPopupWindow = ListPopupWindow(this)
        mFolderPopupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        mFolderPopupWindow?.setAdapter(mFolderAdapter)
        mFolderPopupWindow?.height = (ScreenUtils.getScreenHeight(this) * 0.6).toInt()
        mFolderPopupWindow?.anchorView = findViewById(R.id.footer)
        mFolderPopupWindow?.isModal = true
        mFolderPopupWindow?.setOnItemClickListener { parent, view, position, id ->
            mFolderAdapter!!.setSelectIndex(position)
            category_btn?.text = mFolderAdapter!!.getItem(position).name
            gridAdapter?.updateAdapter(mFolderAdapter!!.selectMedias)
            mFolderPopupWindow?.dismiss()
        }
    }

    @AfterPermissionGranted(119)
    private fun getMediaData() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            val type = argsIntent?.getIntExtra(PickerConfig.SELECT_MODE, PickerConfig
                    .PICKER_IMAGE_VIDEO)
            if (type == PickerConfig.PICKER_IMAGE_VIDEO) {
                loaderManager.initLoader<Cursor>(type, null, MediaLoader(this, this))
            } else if (type == PickerConfig.PICKER_IMAGE) {
                loaderManager.initLoader<Cursor>(type, null, ImageLoader(this, this))
            } else if (type == PickerConfig.PICKER_VIDEO) {
                loaderManager.initLoader<Cursor>(type, null, VideoLoader(this, this))
            }
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.READ_EXTERNAL_STORAGE), 119, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    override fun onData(list: ArrayList<Folder>) {
        setView(list)
        category_btn?.text = list[0].name
        mFolderAdapter!!.updateAdapter(list)
    }

    internal fun setView(list: ArrayList<Folder>) {
        gridAdapter?.updateAdapter(list[0].medias)
        setButtonText()
        gridAdapter?.setOnItemClickListener(object : MediaGridAdapter.OnRecyclerViewItemClickListener {
            override fun onItemClick(view: View, data: Media, selectMedias: ArrayList<Media>) {
                setButtonText()
            }

            override fun openCameraWin() {
                println("准备启动拍照或拍视频窗口")
                val intent = Intent()
                setResult(PickerConfig.RESULT_CAMERA_EXTRA, intent)
                finish()
            }
        })
    }

    internal fun setButtonText() {
        val max = argsIntent?.getIntExtra(PickerConfig.MAX_SELECT_COUNT, PickerConfig
                .DEFAULT_SELECTED_MAX_COUNT)
        done?.text = getString(R.string.done) + "(" + gridAdapter?.selectMedias?.size + "/" + max + ")"
        preview?.text = getString(R.string.preview) + "(" + gridAdapter?.selectMedias?.size + ")"
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btn_back) {
            finish()
        } else if (id == R.id.category_btn) {
            if (mFolderPopupWindow != null && mFolderPopupWindow!!.isShowing) {
                mFolderPopupWindow?.dismiss()
            } else {
                mFolderPopupWindow?.show()
            }
        } else if (id == R.id.done) {
            done(gridAdapter!!.selectMedias)
        } else if (id == R.id.preview) {
            if (gridAdapter!!.selectMedias.size <= 0) {
                Toast.makeText(this, getString(R.string.select_null), Toast.LENGTH_SHORT).show()
                return
            }
            val intent = Intent(this, PreviewActivity::class.java)
            intent.putExtra(PickerConfig.MAX_SELECT_COUNT, argsIntent!!.getIntExtra(PickerConfig
                    .MAX_SELECT_COUNT, PickerConfig.DEFAULT_SELECTED_MAX_COUNT))
            intent.putExtra(PickerConfig.PRE_RAW_LIST, gridAdapter!!.selectMedias)
            this.startActivityForResult(intent, 200)
        }
    }

    fun done(selects: ArrayList<Media>) {
        val intent = Intent()
        intent.putParcelableArrayListExtra(PickerConfig.EXTRA_RESULT, selects)
        setResult(PickerConfig.RESULT_CODE, intent)
        finish()
    }

    override fun onDestroy() {
        Glide.get(this).clearMemory()
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200) {
            val selects = data.getParcelableArrayListExtra<Media>(PickerConfig.EXTRA_RESULT)
            if (resultCode == PickerConfig.RESULT_UPDATE_CODE) {
                gridAdapter?.updateSelectAdapter(selects)
                setButtonText()
            } else if (resultCode == PickerConfig.RESULT_CODE) {
                done(selects)
            }
        }
    }
}
