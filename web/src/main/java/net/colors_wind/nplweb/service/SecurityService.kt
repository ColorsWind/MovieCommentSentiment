package net.colors_wind.nplweb.service

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReferenceArray


object SecurityService {
    val predictSentence = SecurityProvider(periodUnit = 5, limit = 4, timeUnit = 1000L)
    val predictMovie = SecurityProvider(periodUnit = 5, limit = 2, timeUnit = 1000L)
}
/**
 * @param periodUnit 一个周期的时间单位数
 * @param limit 一个周期的访问次数限制
 * @param timeUnit 一个时间单位的毫秒数
 */
class SecurityProvider(val periodUnit: Int,
                       val limit: Int,
                       val timeUnit: Long) {
    /**
     * 存储访问记录
     */
    private val history : AtomicReferenceArray<ConcurrentHashMap<String, AtomicInteger>> =
        AtomicReferenceArray(Array(periodUnit) { ConcurrentHashMap() })
    /**
     * 上一次更新访问记录的时间
     */
    @Volatile var lastUpdate = normalize()
    /**
     * 上一次更新记录时相应的记录
     */
    @Volatile var lastIndex = 0

    /**
     * 对时间进行规范化，时间仅需要精确到 [timeUnit] 毫秒.
     */
    fun normalize(timestamp: Long = System.currentTimeMillis()) = timestamp / timeUnit
    /**
     * 尝试获取一次请求的许可
     */
    fun tryAccess(host: String): Boolean {
        val currTime = normalize()
        val deltaUnit = (currTime - lastUpdate).toInt()
        if (deltaUnit < periodUnit) {
            // find exist tag
            for (index in (lastIndex + deltaUnit)..(lastIndex + periodUnit)) {
                val count = history[index % periodUnit][host] ?: continue
                return count.getAndAdd(1) < limit
            }
            // update last info
            lastUpdate = currTime
            lastIndex = (lastIndex + deltaUnit) % periodUnit
            // create new tag
            if (deltaUnit != 0) history[lastIndex].clear()
            history[lastIndex][host] = AtomicInteger(1)
            return true
        } else {
            for (index in 0 until periodUnit) history[index].clear()
            lastUpdate = normalize()
            lastIndex = 0
            return true
        }
    }
}
