package com.ybg.app.mediapicker

object PickerConfig {

    /**
     * 最大图片选择次数，int类型，默认40
     */
    const val MAX_SELECT_COUNT = "max_select_count"

    const val DEFAULT_SELECTED_MAX_COUNT = 40

    /**
     * 最大文件大小，int类型，默认180m
     */
    const val MAX_SELECT_SIZE = "max_select_size"

    const val DEFAULT_SELECTED_MAX_SIZE: Long = 188743680

    /**
     * 图片选择模式，默认选视频和图片
     */
    const val SELECT_MODE = "select_mode"

    /**
     * 选择结果，返回为 ArrayList&lt;String&gt; 图片路径集合
     */
    const val EXTRA_RESULT = "select_result"
    /**
     * 默认选择集
     */
    const val DEFAULT_SELECTED_LIST = "default_list"
    /**
     * 预览集
     */
    const val PRE_RAW_LIST = "pre_raw_List"
    const val RESULT_CODE = 19901026
    const val RESULT_UPDATE_CODE = 1990
    const val RESULT_CAMERA_EXTRA = 180413
    const val PICKER_IMAGE = 100
    const val PICKER_VIDEO = 102
    const val PICKER_IMAGE_VIDEO = 101
    var GridSpanCount = 3
    var GridSpace = 4
}
