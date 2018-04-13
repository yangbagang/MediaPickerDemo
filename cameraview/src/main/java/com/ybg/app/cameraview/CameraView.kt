package com.ybg.app.cameraview

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.hardware.Camera
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.VideoView

import com.ybg.app.cameraview.listener.CaptureListener
import com.ybg.app.cameraview.listener.ClickListener
import com.ybg.app.cameraview.listener.ErrorListener
import com.ybg.app.cameraview.listener.JCameraListener
import com.ybg.app.cameraview.listener.TypeListener
import com.ybg.app.cameraview.state.CameraMachine
import com.ybg.app.cameraview.util.FileUtil
import com.ybg.app.cameraview.util.LogUtil
import com.ybg.app.cameraview.util.ScreenUtils
import com.ybg.app.cameraview.view.CameraView

import java.io.IOException

class CameraView @JvmOverloads constructor(private val mContext: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(mContext, attrs, defStyleAttr), CameraInterface.CameraOpenOverCallback, SurfaceHolder.Callback, CameraView {
    //    private static final String TAG = "CameraView";

    //Camera状态机
    private var machine: CameraMachine? = null
    private var type_flash = TYPE_FLASH_OFF


    //回调监听
    private var jCameraLisenter: JCameraListener? = null
    private var leftClickListener: ClickListener? = null
    private var rightClickListener: ClickListener? = null
    private var mVideoView: VideoView? = null
    private var mPhoto: ImageView? = null
    private var mSwitchCamera: ImageView? = null
    private var mFlashLamp: ImageView? = null
    private var mCaptureLayout: CaptureLayout? = null
    private var mFoucsView: FocusView? = null
    private var mMediaPlayer: MediaPlayer? = null

    private var layout_width: Int = 0
    private var screenProp = 0f

    private var captureBitmap: Bitmap? = null   //捕获的图片
    private var firstFrame: Bitmap? = null      //第一帧图片
    private var videoUrl: String? = null        //视频URL


    //切换摄像头按钮的参数
    private var iconSize = 0       //图标大小
    private var iconMargin = 0     //右上边距
    private var iconSrc = 0        //图标资源
    private var iconLeft = 0       //左图标
    private var iconRight = 0      //右图标
    private var duration = 0       //录制时间

    //缩放梯度
    private var zoomGradient = 0

    private var firstTouch = true
    private var firstTouchLength = 0f


    private var errorLisenter: ErrorListener? = null

    init {
        //get AttributeSet
        val a = mContext.theme.obtainStyledAttributes(attrs, R.styleable.CameraView, defStyleAttr, 0)
        iconSize = a.getDimensionPixelSize(R.styleable.CameraView_iconSize, TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 35f, resources.displayMetrics).toInt())
        iconMargin = a.getDimensionPixelSize(R.styleable.CameraView_iconMargin, TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 15f, resources.displayMetrics).toInt())
        iconSrc = a.getResourceId(R.styleable.CameraView_iconSrc, R.drawable.ic_camera)
        iconLeft = a.getResourceId(R.styleable.CameraView_iconLeft, 0)
        iconRight = a.getResourceId(R.styleable.CameraView_iconRight, 0)
        duration = a.getInteger(R.styleable.CameraView_duration_max, 10 * 1000)       //没设置默认为10s
        a.recycle()
        initData()
        initView()
    }

    private fun initData() {
        layout_width = ScreenUtils.getScreenWidth(mContext)
        //缩放梯度
        zoomGradient = (layout_width / 16f).toInt()
        LogUtil.i("zoom = $zoomGradient")
        machine = CameraMachine(context, this, this)
    }

    private fun initView() {
        setWillNotDraw(false)
        val view = LayoutInflater.from(mContext).inflate(R.layout.camera_view, this)
        mVideoView = view.findViewById<View>(R.id.video_preview) as VideoView
        mPhoto = view.findViewById<View>(R.id.image_photo) as ImageView
        mSwitchCamera = view.findViewById<View>(R.id.image_switch) as ImageView
        mSwitchCamera!!.setImageResource(iconSrc)
        mFlashLamp = view.findViewById<View>(R.id.image_flash) as ImageView
        setFlashRes()
        mFlashLamp!!.setOnClickListener {
            type_flash++
            if (type_flash > 0x023)
                type_flash = TYPE_FLASH_AUTO
            setFlashRes()
        }
        mCaptureLayout = view.findViewById<View>(R.id.capture_layout) as CaptureLayout
        mCaptureLayout!!.setDuration(duration)
        mCaptureLayout!!.setIconSrc(iconLeft, iconRight)
        mFoucsView = view.findViewById<View>(R.id.focus_view) as FocusView
        mVideoView!!.holder.addCallback(this)
        //切换摄像头
        mSwitchCamera!!.setOnClickListener { machine!!.swtich(mVideoView!!.holder, screenProp) }
        //拍照 录像
        mCaptureLayout!!.setCaptureListener(object : CaptureListener {
            override fun takePictures() {
                mSwitchCamera!!.visibility = View.INVISIBLE
                mFlashLamp!!.visibility = View.INVISIBLE
                machine!!.capture()
            }

            override fun recordStart() {
                mSwitchCamera!!.visibility = View.INVISIBLE
                mFlashLamp!!.visibility = View.INVISIBLE
                machine!!.record(mVideoView!!.holder.surface, screenProp)
            }

            override fun recordShort(time: Long) {
                mCaptureLayout!!.setTextWithAnimation("录制时间过短")
                mSwitchCamera!!.visibility = View.VISIBLE
                mFlashLamp!!.visibility = View.VISIBLE
                postDelayed({ machine!!.stopRecord(true, time) }, 1500 - time)
            }

            override fun recordEnd(time: Long) {
                machine!!.stopRecord(false, time)
            }

            override fun recordZoom(zoom: Float) {
                LogUtil.i("recordZoom")
                machine!!.zoom(zoom, CameraInterface.TYPE_RECORDER)
            }

            override fun recordError() {
                if (errorLisenter != null) {
                    errorLisenter!!.audioPermissionError()
                }
            }
        })
        //确认 取消
        mCaptureLayout!!.setTypeListener(object : TypeListener {
            override fun cancel() {
                machine!!.cancle(mVideoView!!.holder, screenProp)
            }

            override fun confirm() {
                machine!!.confirm()
            }
        })
        //退出
        //        mCaptureLayout.setReturnLisenter(new ReturnListener() {
        //            @Override
        //            public void onReturn() {
        //                if (jCameraLisenter != null) {
        //                    jCameraLisenter.quit();
        //                }
        //            }
        //        });
        mCaptureLayout!!.setLeftClickListener(object : ClickListener {
            override fun onClick() {
                if (leftClickListener != null) {
                    leftClickListener!!.onClick()
                }
            }
        })
        mCaptureLayout!!.setRightClickListener(object : ClickListener {
            override fun onClick() {
                if (rightClickListener != null) {
                    rightClickListener!!.onClick()
                }
            }
        })
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSize = mVideoView!!.measuredWidth.toFloat()
        val heightSize = mVideoView!!.measuredHeight.toFloat()
        if (screenProp == 0f) {
            screenProp = heightSize / widthSize
        }
    }

    override fun cameraHasOpened() {
        CameraInterface.instance!!.doStartPreview(mVideoView!!.holder, screenProp)
    }

    //生命周期onResume
    fun onResume() {
        LogUtil.i("CameraView onResume")
        resetState(TYPE_DEFAULT) //重置状态
        CameraInterface.instance!!.registerSensorManager(mContext)
        CameraInterface.instance!!.setSwitchView(mSwitchCamera, mFlashLamp!!)
        machine!!.start(mVideoView!!.holder, screenProp)
    }

    //生命周期onPause
    fun onPause() {
        LogUtil.i("CameraView onPause")
        stopVideo()
        resetState(TYPE_PICTURE)
        CameraInterface.instance!!.isPreview(false)
        CameraInterface.instance!!.unregisterSensorManager(mContext)
    }

    //SurfaceView生命周期
    override fun surfaceCreated(holder: SurfaceHolder) {
        LogUtil.i("CameraView SurfaceCreated")
        object : Thread() {
            override fun run() {
                CameraInterface.instance!!.doOpenCamera(this@CameraView)
            }
        }.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        LogUtil.i("CameraView SurfaceDestroyed")
        CameraInterface.instance!!.doDestroyCamera()
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (event.pointerCount == 1) {
                    //显示对焦指示器
                    setFocusViewWidthAnimation(event.x, event.y)
                }
                if (event.pointerCount == 2) {
                    Log.i("CJT", "ACTION_DOWN = " + 2)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount == 1) {
                    firstTouch = true
                }
                if (event.pointerCount == 2) {
                    //第一个点
                    val point_1_X = event.getX(0)
                    val point_1_Y = event.getY(0)
                    //第二个点
                    val point_2_X = event.getX(1)
                    val point_2_Y = event.getY(1)

                    val result = Math.sqrt(Math.pow((point_1_X - point_2_X).toDouble(), 2.0) + Math.pow((point_1_Y - point_2_Y).toDouble(), 2.0)).toFloat()

                    if (firstTouch) {
                        firstTouchLength = result
                        firstTouch = false
                    }
                    if ((result - firstTouchLength).toInt() / zoomGradient != 0) {
                        firstTouch = true
                        machine!!.zoom(result - firstTouchLength, CameraInterface.TYPE_CAPTURE)
                    }
                    //                    Log.i("CJT", "result = " + (result - firstTouchLength));
                }
            }
            MotionEvent.ACTION_UP -> firstTouch = true
        }
        return true
    }

    //对焦框指示器动画
    private fun setFocusViewWidthAnimation(x: Float, y: Float) {
        machine!!.focus(x, y, object : CameraInterface.FocusCallback {
            override fun focusSuccess() {
                mFoucsView!!.visibility =View.INVISIBLE
            }
        })
    }

    private fun updateVideoViewSize(videoWidth: Float, videoHeight: Float) {
        if (videoWidth > videoHeight) {
            val videoViewParam: FrameLayout.LayoutParams
            val height = (videoHeight / videoWidth * width).toInt()
            videoViewParam = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, height)
            videoViewParam.gravity = Gravity.CENTER
            mVideoView!!.layoutParams = videoViewParam
        }
    }

    /**************************************************
     * 对外提供的API                     *
     */

    fun setSaveVideoPath(path: String) {
        CameraInterface.instance!!.setSaveVideoPath(path)
    }


    fun setJCameraLisenter(jCameraLisenter: JCameraListener) {
        this.jCameraLisenter = jCameraLisenter
    }

    //启动Camera错误回调
    fun setErrorLisenter(errorLisenter: ErrorListener) {
        this.errorLisenter = errorLisenter
        CameraInterface.instance!!.setErrorLinsenter(errorLisenter)
    }

    //设置CaptureButton功能（拍照和录像）
    fun setFeatures(state: Int) {
        this.mCaptureLayout!!.setButtonFeatures(state)
    }

    //设置录制质量
    fun setMediaQuality(quality: Int) {
        CameraInterface.instance!!.setMediaQuality(quality)
    }

    override fun resetState(type: Int) {
        when (type) {
            TYPE_VIDEO -> {
                stopVideo()    //停止播放
                //初始化VideoView
                FileUtil.deleteFile(videoUrl!!)
                mVideoView!!.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                machine!!.start(mVideoView!!.holder, screenProp)
            }
            TYPE_PICTURE -> mPhoto!!.visibility = View.INVISIBLE
            TYPE_SHORT -> {
            }
            TYPE_DEFAULT -> mVideoView!!.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        }
        mSwitchCamera!!.visibility = View.VISIBLE
        mFlashLamp!!.visibility = View.VISIBLE
        mCaptureLayout!!.resetCaptureLayout()
    }

    override fun confirmState(type: Int) {
        when (type) {
            TYPE_VIDEO -> {
                stopVideo()    //停止播放
                mVideoView!!.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                machine!!.start(mVideoView!!.holder, screenProp)
                if (jCameraLisenter != null) {
                    jCameraLisenter!!.recordSuccess(videoUrl!!, firstFrame!!)
                }
            }
            TYPE_PICTURE -> {
                mPhoto!!.visibility = View.INVISIBLE
                if (jCameraLisenter != null) {
                    jCameraLisenter!!.captureSuccess(captureBitmap!!)
                }
            }
            TYPE_SHORT -> {
            }
            TYPE_DEFAULT -> {
            }
        }
        mCaptureLayout!!.resetCaptureLayout()
    }

    override fun showPicture(bitmap: Bitmap, isVertical: Boolean) {
        if (isVertical) {
            mPhoto!!.scaleType = ImageView.ScaleType.FIT_XY
        } else {
            mPhoto!!.scaleType = ImageView.ScaleType.FIT_CENTER
        }
        captureBitmap = bitmap
        mPhoto!!.setImageBitmap(bitmap)
        mPhoto!!.visibility = View.VISIBLE
        mCaptureLayout!!.startAlphaAnimation()
        mCaptureLayout!!.startTypeBtnAnimator()
    }

    override fun playVideo(firstFrame: Bitmap, url: String) {
        videoUrl = url
        this@CameraView.firstFrame = firstFrame
        Thread(Runnable {
            try {
                if (mMediaPlayer == null) {
                    mMediaPlayer = MediaPlayer()
                } else {
                    mMediaPlayer!!.reset()
                }
                mMediaPlayer!!.setDataSource(url)
                mMediaPlayer!!.setSurface(mVideoView!!.holder.surface)
                mMediaPlayer!!.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
                mMediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
                mMediaPlayer!!.setOnVideoSizeChangedListener { mp, width, height ->
                    updateVideoViewSize(mMediaPlayer!!.videoWidth.toFloat(), mMediaPlayer!!
                            .videoHeight.toFloat())
                }
                mMediaPlayer!!.setOnPreparedListener { mMediaPlayer!!.start() }
                mMediaPlayer!!.isLooping = true
                mMediaPlayer!!.prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }).start()
    }

    override fun stopVideo() {
        if (mMediaPlayer != null && mMediaPlayer!!.isPlaying) {
            mMediaPlayer!!.stop()
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }

    override fun setTip(tip: String) {
        mCaptureLayout!!.setTip(tip)
    }

    override fun startPreviewCallback() {
        LogUtil.i("startPreviewCallback")
        handlerFoucs((mFoucsView!!.width / 2).toFloat(), (mFoucsView!!.height / 2).toFloat())
    }

    override fun handlerFoucs(p0: Float, p1: Float): Boolean {
        var x = p0
        var y = p1
        if (y > mCaptureLayout!!.top) {
            return false
        }
        mFoucsView!!.visibility = View.VISIBLE
        if (x < mFoucsView!!.width / 2) {
            x = (mFoucsView!!.width / 2).toFloat()
        }
        if (x > layout_width - mFoucsView!!.width / 2) {
            x = (layout_width - mFoucsView!!.width / 2).toFloat()
        }
        if (y < mFoucsView!!.width / 2) {
            y = (mFoucsView!!.width / 2).toFloat()
        }
        if (y > mCaptureLayout!!.top - mFoucsView!!.width / 2) {
            y = (mCaptureLayout!!.top - mFoucsView!!.width / 2).toFloat()
        }
        mFoucsView!!.x = x - mFoucsView!!.width / 2
        mFoucsView!!.y = y - mFoucsView!!.height / 2
        val scaleX = ObjectAnimator.ofFloat(mFoucsView, "scaleX", 1f, 0.6f)
        val scaleY = ObjectAnimator.ofFloat(mFoucsView, "scaleY", 1f, 0.6f)
        val alpha = ObjectAnimator.ofFloat(mFoucsView, "alpha", 1f, 0.4f, 1f, 0.4f, 1f, 0.4f, 1f)
        val animSet = AnimatorSet()
        animSet.play(scaleX).with(scaleY).before(alpha)
        animSet.duration = 400
        animSet.start()
        return true
    }

    fun setLeftClickListener(clickListener: ClickListener) {
        this.leftClickListener = clickListener
    }

    fun setRightClickListener(clickListener: ClickListener) {
        this.rightClickListener = clickListener
    }

    private fun setFlashRes() {
        when (type_flash) {
            TYPE_FLASH_AUTO -> {
                mFlashLamp!!.setImageResource(R.drawable.ic_flash_auto)
                machine!!.flash(Camera.Parameters.FLASH_MODE_AUTO)
            }
            TYPE_FLASH_ON -> {
                mFlashLamp!!.setImageResource(R.drawable.ic_flash_on)
                machine!!.flash(Camera.Parameters.FLASH_MODE_ON)
            }
            TYPE_FLASH_OFF -> {
                mFlashLamp!!.setImageResource(R.drawable.ic_flash_off)
                machine!!.flash(Camera.Parameters.FLASH_MODE_OFF)
            }
        }
    }

    companion object {

        //闪关灯状态
        private val TYPE_FLASH_AUTO = 0x021
        private val TYPE_FLASH_ON = 0x022
        private val TYPE_FLASH_OFF = 0x023

        //拍照浏览时候的类型
        const val TYPE_PICTURE = 0x001
        const val TYPE_VIDEO = 0x002
        const val TYPE_SHORT = 0x003
        const val TYPE_DEFAULT = 0x004

        //录制视频比特率
        const val MEDIA_QUALITY_HIGH = 20 * 100000
        const val MEDIA_QUALITY_MIDDLE = 16 * 100000
        const val MEDIA_QUALITY_LOW = 12 * 100000
        const val MEDIA_QUALITY_POOR = 8 * 100000
        const val MEDIA_QUALITY_FUNNY = 4 * 100000
        const val MEDIA_QUALITY_DESPAIR = 2 * 100000
        const val MEDIA_QUALITY_SORRY = 1 * 80000

        const val BUTTON_STATE_ONLY_CAPTURE = 0x101      //只能拍照
        const val BUTTON_STATE_ONLY_RECORDER = 0x102     //只能录像
        const val BUTTON_STATE_BOTH = 0x103              //两者都可以
    }
}
