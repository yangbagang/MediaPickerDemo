package com.ybg.app.mediapicker.adapter

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

class SpacingDecoration(private val spanCount: Int, private val space: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
        //不是第一个的格子都设一个左边和底部的间距
        outRect.left = space
        outRect.bottom = space
        val position = parent.getChildLayoutPosition(view)
        if (position % spanCount == 0) {
            outRect.left = 0
        }
    }

}
