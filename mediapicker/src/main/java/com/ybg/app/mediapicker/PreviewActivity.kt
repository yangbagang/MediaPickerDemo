package com.ybg.app.mediapicker

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import com.ybg.app.mediapicker.entity.Media
import com.ybg.app.mediapicker.view.PreviewFragment

import java.util.ArrayList

class PreviewActivity : AppCompatActivity(), View.OnClickListener, ViewPager.OnPageChangeListener {

    private lateinit var done: Button
    private lateinit var check_layout: LinearLayout
    private lateinit var check_image: ImageView
    private lateinit var viewpager: ViewPager
    private lateinit var bar_title: TextView
    private lateinit var preRawList: ArrayList<Media>
    private lateinit var selects: ArrayList<Media>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.preview_main)
        findViewById<View>(R.id.btn_back).setOnClickListener(this)
        check_image = findViewById<View>(R.id.check_image) as ImageView
        check_layout = findViewById<View>(R.id.check_layout) as LinearLayout
        check_layout.setOnClickListener(this)
        bar_title = findViewById<View>(R.id.bar_title) as TextView
        done = findViewById<View>(R.id.done) as Button
        done.setOnClickListener(this)
        viewpager = findViewById<View>(R.id.viewpager) as ViewPager
        preRawList = intent.getParcelableArrayListExtra(PickerConfig.PRE_RAW_LIST)
        selects = ArrayList()
        selects.addAll(preRawList)
        setView(preRawList)
    }

    internal fun setView(default_list: ArrayList<Media>) {
        setDoneView(default_list.size)
        bar_title.text = 1.toString() + "/" + preRawList.size
        val fragmentArrayList = ArrayList<Fragment>()
        for (media in default_list) {
            fragmentArrayList.add(PreviewFragment.newInstance(media, ""))
        }
        val adapterFragment = AdapterFragment(supportFragmentManager, fragmentArrayList)
        viewpager.adapter = adapterFragment
        viewpager.addOnPageChangeListener(this)
    }

    internal fun setDoneView(num1: Int) {
        done.text = getString(R.string.done) + "(" + num1 + "/" + intent.getIntExtra(PickerConfig.MAX_SELECT_COUNT, PickerConfig.DEFAULT_SELECTED_MAX_COUNT) + ")"
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btn_back) {
            done(selects, PickerConfig.RESULT_UPDATE_CODE)
        } else if (id == R.id.done) {
            done(selects, PickerConfig.RESULT_CODE)
        } else if (id == R.id.check_layout) {
            val media = preRawList[viewpager.currentItem]
            val select = isSelect(media, selects)
            if (select < 0) {
                check_image.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.btn_selected))
                selects.add(media)
            } else {
                check_image.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.btn_unselected))
                selects.removeAt(select)
            }
            setDoneView(selects.size)
        }
    }

    /**
     * @param media
     * @return 大于等于0 就是表示以选择，返回的是在selectMedias中的下标
     */
    fun isSelect(media: Media, list: ArrayList<Media>): Int {
        var `is` = -1
        if (list.size <= 0) {
            return `is`
        }
        for (i in list.indices) {
            val m = list[i]
            if (m.path == media.path) {
                `is` = i
                break
            }
        }
        return `is`
    }

    fun done(list: ArrayList<Media>, code: Int) {
        val intent = Intent()
        intent.putParcelableArrayListExtra(PickerConfig.EXTRA_RESULT, list)
        setResult(code, intent)
        finish()
    }

    override fun onBackPressed() {
        done(selects, PickerConfig.RESULT_UPDATE_CODE)
        super.onBackPressed()
    }

    inner class AdapterFragment(fm: FragmentManager, private val mFragments: List<Fragment>) : FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return mFragments[position]
        }

        override fun getCount(): Int {
            return mFragments.size
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        bar_title.text = (position + 1).toString() + "/" + preRawList.size
        check_image.setImageDrawable(if (isSelect(preRawList[position], selects) < 0) ContextCompat.getDrawable(this, R.drawable.btn_unselected) else ContextCompat.getDrawable(this, R.drawable.btn_selected))
    }

    override fun onPageScrollStateChanged(state: Int) {}
}
