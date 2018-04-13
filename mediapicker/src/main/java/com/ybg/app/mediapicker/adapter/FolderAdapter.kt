package com.ybg.app.mediapicker.adapter

import android.content.Context
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

import com.bumptech.glide.Glide
import com.ybg.app.mediapicker.R
import com.ybg.app.mediapicker.entity.Folder
import com.ybg.app.mediapicker.entity.Media

import java.util.ArrayList

class FolderAdapter(private var folders: ArrayList<Folder>, private val mContext: Context) : BaseAdapter() {
    private val mInflater: LayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var lastSelected = 0

    val selectMedias: ArrayList<Media>
        get() = folders[lastSelected].medias

    override fun getCount(): Int {
        return folders.size
    }

    override fun getItem(position: Int): Folder {
        return folders[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    fun updateAdapter(list: ArrayList<Folder>) {
        this.folders = list
        notifyDataSetChanged()
    }

    override fun getView(position: Int, v: View?, viewGroup: ViewGroup): View {
        var view = v
        val holder: ViewHolder
        if (view == null) {
            view = mInflater.inflate(R.layout.folders_view_item, viewGroup, false)
            holder = ViewHolder(view!!)
        } else {
            holder = view.tag as ViewHolder
        }

        val folder = getItem(position)
        val media: Media
        if (folder.medias.size > 0) {
            media = folder.medias[0]
            Glide.with(mContext)
                    .load(Uri.parse("file://" + media.path))
                    .into(holder.cover)
        } else {
            holder.cover.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.default_image))
        }

        holder.name.text = folder.name

        holder.size.text = "${folder.medias.size}"
        holder.indicator.visibility = if (lastSelected == position) View.VISIBLE else View.INVISIBLE
        return view
    }


    fun setSelectIndex(i: Int) {
        if (lastSelected == i) return
        lastSelected = i
        notifyDataSetChanged()
    }

    internal inner class ViewHolder(view: View) {
        var cover: ImageView = view.findViewById(R.id.cover)
        var indicator: ImageView = view.findViewById(R.id.indicator)
        var name: TextView = view.findViewById(R.id.name)
        var path: TextView = view.findViewById(R.id.path)
        var size: TextView = view.findViewById(R.id.size)

        init {
            view.tag = this
        }
    }
}
