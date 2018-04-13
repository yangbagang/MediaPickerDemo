package com.ybg.app.mediapicker

object PickerConfig {
    val LOG_TAG = "MediaPicker"

    /**
     * 最大图片选择次数，int类型，默认40
     */
    val MAX_SELECT_COUNT = "max_select_count"

    val DEFAULT_SELECTED_MAX_COUNT = 40

    /**
     * 最大文件大小，int类型，默认180m
     */
    val MAX_SELECT_SIZE = "max_select_size"

    val DEFAULT_SELECTED_MAX_SIZE: Long = 188743680

    /**
     * 图片选择模式，默认选视频和图片
     */
    val SELECT_MODE = "select_mode"

    /**
     * 选择结果，返回为 ArrayList&lt;String&gt; 图片路径集合
     */
    val EXTRA_RESULT = "select_result"
    /**
     * 默认选择集
     */
    val DEFAULT_SELECTED_LIST = "default_list"
    /**
     * 预览集
     */
    val PRE_RAW_LIST = "pre_raw_List"
    val RESULT_CODE = 19901026
    val RESULT_UPDATE_CODE = 1990
    val PICKER_IMAGE = 100
    val PICKER_VIDEO = 102
    val PICKER_IMAGE_VIDEO = 101
    var GridSpanCount = 3
    var GridSpace = 4
}
