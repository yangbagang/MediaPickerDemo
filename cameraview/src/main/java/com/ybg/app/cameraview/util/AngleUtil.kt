package com.ybg.app.cameraview.util

object AngleUtil {
    fun getSensorAngle(x: Float, y: Float): Int {
        return if (Math.abs(x) > Math.abs(y)) {
            /**
             * 横屏倾斜角度比较大
             */
            if (x > 4) {
                /**
                 * 左边倾斜
                 */
                270
            } else if (x < -4) {
                /**
                 * 右边倾斜
                 */
                90
            } else {
                /**
                 * 倾斜角度不够大
                 */
                0
            }
        } else {
            if (y > 7) {
                /**
                 * 左边倾斜
                 */
                0
            } else if (y < -7) {
                /**
                 * 右边倾斜
                 */
                180
            } else {
                /**
                 * 倾斜角度不够大
                 */
                0
            }
        }
    }
}
