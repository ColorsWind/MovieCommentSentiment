package net.colors_wind.nplweb

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.atomic.AtomicReferenceArray


object PerformanceTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val concurrentMap = ConcurrentHashMap<String, String>()
        val random = Array(10000000){ UUID.randomUUID().toString()}
        repeat(12) {
            concurrentMap[UUID.randomUUID().toString()] = ""
        }
        val array = AtomicReferenceArray<String>(12)
        repeat(12) {
            array[it] = UUID.randomUUID().toString()
        }
        val start = System.currentTimeMillis()
        repeat(10000000) {
            val s : String? = concurrentMap[random[it]]
        }
        val end = System.currentTimeMillis()
        println("${end - start} ms")
    }
}