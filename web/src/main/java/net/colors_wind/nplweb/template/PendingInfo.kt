package net.colors_wind.nplweb.template

import kotlin.math.min

data class PendingInfo(val total: Int, val position: Int) {
    val message : String
    val refresh : Int

    init {
        if (position == 0) {
            message = "分析任务进行中, 页面将会自动刷新..."
            refresh = 1
        } else {
            message = "分析任务排队中, 当前位置 $position / $total, 页面将会自动刷新..."
            refresh = min(position * 3, 15)
        }
    }
}
