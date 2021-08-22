package net.colors_wind.nplweb.service

import net.colors_wind.nplweb.Config
import net.colors_wind.nplweb.data.SentenceData
import java.io.*
import java.util.concurrent.LinkedBlockingDeque

const val N = 2

object PredictService {

    val cache = SentenceCacheFIFO()

    val python
        get() = Config.instance.python

    val path
        get() = File(Config.instance.network)

    /**
     * 管理 Python 实例
     */
    private val pythons = LinkedBlockingDeque<Python>().apply { repeat(N) { add(Python()) } }

    /**
     * 进行预测
     */
    fun predict(sentence: String): SentenceData {
        val python = pythons.takeLast().ensure()
        val data = python.predict(sentence)
        pythons.putLast(python)
        return data
    }

    /**
     * 进行预测
     */
    fun predict(sentence: List<String>): List<SentenceData> {
        val python = pythons.takeLast().ensure()
        val data = python.predict(sentence)
        pythons.putLast(python)
        return data
    }

    /**
     * 进行预测(检测缓存)
     */
    operator fun get(sentence: String): SentenceData = cache[sentence] ?: let {
        val sentenceData = predict(sentence)
        cache.insert(sentenceData)
        sentenceData
    }

}

class Python {
    val process: Process = Runtime.getRuntime().exec("${PredictService.python} predict.py unicode", null, PredictService.path)
    val output: OutputStream = process.outputStream
    val input: InputStream = process.inputStream
    val reader = BufferedReader(InputStreamReader(input))

    fun predict(sentence: String): SentenceData {
        val writer = BufferedWriter(OutputStreamWriter(output))
        val reader = BufferedReader(InputStreamReader(input))
        writer.write(sentence.toUnicodeString())
        writer.newLine()
        writer.flush()

        val pre = reader.readLine().toDouble()
        return SentenceData(sentence, pre)
    }

    fun predict(sentences: List<String>): List<SentenceData> {
        val writer = BufferedWriter(OutputStreamWriter(output))
        val reader = BufferedReader(InputStreamReader(input))
        val result = ArrayList<SentenceData>(sentences.size)
        val buffered = Array(20){""}
        var curr = 0
        fun pushBuffered() = repeat(curr) {
            result.add(SentenceData(buffered[it], reader.readLine().toDouble()))
        }
        sentences.forEach { str ->
            writer.write(str.toUnicodeString())
            writer.newLine()
            buffered[curr] = str
            curr += 1
            if (curr == buffered.size) {
                writer.flush()
                pushBuffered()
                curr = 0
            }
        }
        if (curr > 0) {
            writer.flush()
            pushBuffered()
        }
        return result
    }


    fun String.toUnicodeString(): String {
        val sb = StringBuffer()
        for (element in this) {
            if (element.toInt() in 0..255) {
                sb.append(element)
            } else {
                sb.append("\\u${Integer.toHexString(element.toInt())}")
            }
        }
        return sb.toString()
    }


    fun destroy() {
        input.close()
        output.close()
        process.destroy()
    }

    fun ensure(): Python {
        return if (!process.isAlive) {
            destroy()
            Python()
        } else this
    }
}