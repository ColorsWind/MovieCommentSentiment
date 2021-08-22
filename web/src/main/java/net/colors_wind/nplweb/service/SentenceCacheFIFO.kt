package net.colors_wind.nplweb.service

import net.colors_wind.nplweb.data.SentenceData
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReferenceArray

const val SIZE = 16

class SentenceCacheFIFO {
    /**
     * 句子缓存数组
     */
    private val cache = AtomicReferenceArray(
        Array(SIZE) { SentenceData("null", -1.0) }
    )

    /**
     * 最后一次插入的下表
     */
    private val lastInsert = AtomicInteger(-1)

    operator fun get(sentence: String): SentenceData? {
        //println(cache)
        repeat(SIZE) { index ->
            val sentenceData = cache[index]
            if (sentence == sentenceData.sentence) return@get sentenceData
        }
        return null
    }

    fun insert(sentenceData: SentenceData) {
        cache[lastInsert.addAndGet(1) % SIZE] = sentenceData
    }
}

class SentenceCacheSCR {
    private val cache = AtomicReferenceArray(
        Array(SIZE) { SentenceSCR(SentenceData("", -1.0))
    })
    data class SentenceSCR(val sentenceData: SentenceData, val visited: AtomicBoolean = AtomicBoolean(false))

    /**
     * 最后一次插入的下表
     */
    private val lastInsert = AtomicInteger(-1)

    operator fun get(sentence: String): SentenceData? {
        repeat(SIZE) { index ->
            val sentenceSCR = cache[index]
            if (sentenceSCR.sentenceData.sentence == sentence) {
                sentenceSCR.visited.set(true)
                return@get sentenceSCR.sentenceData
            }
        }
        return null
    }

    fun insert(sentenceData: SentenceData) {
        var localLastInsert = lastInsert.get()
        while(true) {
            localLastInsert = (localLastInsert + 1) % SIZE
            val sentenceSCR = cache[localLastInsert]
            if (!sentenceSCR.visited.getAndSet(false)) {
                cache[localLastInsert] = SentenceSCR(sentenceData)
                lastInsert.set(localLastInsert)
                return
            }
        }
    }
}