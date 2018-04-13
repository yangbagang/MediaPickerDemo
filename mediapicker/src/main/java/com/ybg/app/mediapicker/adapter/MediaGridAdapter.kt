package com.ybg.app.mediapicker.adapter

import android.content.Context
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

import com.bumptech.glide.Glide
import com.ybg.app.mediapicker.PickerConfig
import com.ybg.app.mediapicker.R
import com.ybg.app.mediapicker.entity.Media
import com.ybg.app.mediapicker.utils.FileUtils
import com.ybg.app.mediapicker.utils.ScreenUtils

import java.util.ArrayList

class MediaGridAdapter(private var medias: ArrayList<Media>, internal var context: Context, select: ArrayList<Media>?,
                       max: Int, private var maxSize: Long) : RecyclerView.Adapter<MediaGridAdapter.MyViewHolder>() {

    var selectMedias = ArrayList<Media>()
        internal set
    private var maxSelect: Long = 0
    private var mediaType = 0

    private val TYPE_CAMERA = 0
    private val TYPE_MEDIA = 1

    internal val itemWidth: Int
        get() = ScreenUtils.getScreenWidth(context) / PickerConfig.GridSpanCount - PickerConfig.GridSpanCount

    private var mOnItemClickListener: OnRecyclerViewItemClickListener? = null

    init {
        if (select != null) {
            this.selectMedias = select
        }
        this.maxSelect = max.toLong()
    }


    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var media_image: ImageView? = view.findViewById<ImageView?>(R.id.media_image)
        var check_image: ImageView? = view.findViewById<ImageView?>(R.id.check_image)
        var mask_view: View? = view.findViewById<View?>(R.id.mask_view)
        var textView_size: TextView? = view.findViewById<TextView?>(R.id.textView_size)
        var video_info: RelativeLayout? = view.findViewById<RelativeLayout?>(R.id.video_info)
        var fl_camera: FrameLayout? = view.findViewById<FrameLayout?>(R.id.fl_camera)

        init {
            itemView.layoutParams = AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemWidth) //让图片是个正方形
        }
    }

    override fun getItemViewType(position: Int): Int = if (position == 0) TYPE_CAMERA else TYPE_MEDIA

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MyViewHolder {
        val view: View
        if (viewType == TYPE_MEDIA) {
            view = LayoutInflater.from(viewGroup.context).inflate(R.layout.media_view_item,
                    viewGroup, false)
        } else {
            view = LayoutInflater.from(viewGroup.context).inflate(R.layout.camera_view_item,
                    viewGroup, false)
        }
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (position == 0) {
            holder.fl_camera?.setOnClickListener {
                println("准备开启拍摄")
                mOnItemClickListener?.openCameraWin()
            }
            return
        }
        val media = medias[position]
        val mediaUri = Uri.parse("file://" + media.path)

        Glide.with(context)
                .load(mediaUri)
                .into(holder.media_image)

        if (media.mediaType == 3) {
            holder.video_info?.visibility = View.VISIBLE
            holder.textView_size?.text = FileUtils.getSizeByUnit(media.size.toDouble())
        } else {
            holder.video_info?.visibility = View.INVISIBLE
        }

        val isSelect = isSelect(media)
        holder.mask_view?.visibility = if (isSelect >= 0) View.VISIBLE else View.INVISIBLE
        holder.check_image?.setImageDrawable(if (isSelect >= 0) ContextCompat.getDrawable
        (context, R.drawable.btn_selected) else ContextCompat.getDrawable(context, R.drawable.btn_unselected))


        holder.media_image?.setOnClickListener { v ->
            val isSelect = isSelect(media)
            if (selectMedias.size >= maxSelect && isSelect < 0) {
                Toast.makeText(context, context.getString(R.string.msg_amount_limit), Toast.LENGTH_SHORT).show()
            } else {
                if (media.size > maxSize) {
                    Toast.makeText(context, context.getString(R.string.msg_size_limit) + FileUtils.fileSize(maxSize), Toast.LENGTH_LONG).show()
                } else {
                    if (mediaType == 0) {
                        mediaType = media.mediaType
                        println("首次添加，设定类型")
                    }
                    if (mediaType != media.mediaType && isSelect < 0) {
                        Toast.makeText(context, context.getString(R.string.media_type_has_selected),
                                Toast.LENGTH_SHORT).show()
                        println("选择了不同类型文件")
                    } else if (mediaType == 3 && isSelect < 0 && selectMedias.isNotEmpty()){
                        Toast.makeText(context, context.getString(R.string.video_type_has_selected),
                                Toast.LENGTH_SHORT).show()
                        println("视频文件只能选择一个")
                    } else {
                        println("选择了同一类型文件或取消选择")
                        holder.mask_view?.visibility = if (isSelect >= 0) View.INVISIBLE else View.VISIBLE
                        holder.check_image?.setImageDrawable(if (isSelect >= 0) ContextCompat.getDrawable(context, R.drawable.btn_unselected) else ContextCompat.getDrawable(context, R.drawable.btn_selected))
                        setSelectMedias(media)
                        mOnItemClickListener?.onItemClick(v, media, selectMedias)
                    }
                }
            }
        }
    }


    private fun setSelectMedias(media: Media) {
        val index = isSelect(media)
        if (index == -1) {
            selectMedias.add(media)
        } else {
            selectMedias.removeAt(index)
            if (selectMedias.isEmpty()) {
                mediaType = 0
                println("移除己选定类型")
            }
        }
    }

    /**
     * @param media
     * @return 大于等于0 就是表示以选择，返回的是在selectMedias中的下标
     */
    private fun isSelect(media: Media): Int {
        var `is` = -1
        if (selectMedias.size <= 0) {
            return `is`
        }
        for (i in selectMedias.indices) {
            val m = selectMedias[i]
            if (m.path == media.path) {
                `is` = i
                break
            }
        }
        return `is`
    }

    fun updateSelectAdapter(select: ArrayList<Media>?) {
        if (select != null) {
            this.selectMedias = select
        }
        notifyDataSetChanged()
    }

    fun updateAdapter(list: ArrayList<Media>) {
        this.medias = list
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: OnRecyclerViewItemClickListener) {
        this.mOnItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return medias.size + 1
    }

    interface OnRecyclerViewItemClickListener {
        fun onItemClick(view: View, data: Media, selectMedias: ArrayList<Media>)
        fun openCameraWin()
    }

}
