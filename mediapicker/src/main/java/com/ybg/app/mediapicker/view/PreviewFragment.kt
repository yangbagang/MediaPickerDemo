package com.ybg.app.mediapicker.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.bumptech.glide.Glide
import com.ybg.app.mediapicker.R
import com.ybg.app.mediapicker.entity.Media

import uk.co.senab.photoview.PhotoView
import uk.co.senab.photoview.PhotoViewAttacher

class PreviewFragment : Fragment() {
    private var mPhotoView: PhotoView? = null
    private var mPlayView: ImageView? = null
    private var mAttacher: PhotoViewAttacher? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        retainInstance = true
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.preview_fragment_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val media = arguments!!.getParcelable<Media>("media")
        mPlayView = view.findViewById<View>(R.id.play_view) as ImageView
        mPhotoView = view.findViewById<View>(R.id.photoview) as PhotoView
        mAttacher = PhotoViewAttacher(mPhotoView!!)
        mAttacher!!.setRotatable(true)
        mAttacher!!.setToRightAngle(true)

        setPlayView(media)
        Glide.with(activity!!)
                .load(media!!.path)
                .into(mPhotoView!!)
    }

    internal fun setPlayView(media: Media?) {
        if (media!!.mediaType == 3) {
            mPlayView?.visibility = View.VISIBLE
            mPlayView?.setOnClickListener {
                val uri = Uri.parse(media.path)
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(uri, "video/*")
                startActivity(intent)
            }
        }
    }

    override fun onDestroyView() {
        mAttacher!!.cleanup()
        super.onDestroyView()
    }

    companion object {

        fun newInstance(media: Media, label: String): PreviewFragment {
            val f = PreviewFragment()
            val b = Bundle()
            b.putParcelable("media", media)
            f.arguments = b
            return f
        }
    }
}
