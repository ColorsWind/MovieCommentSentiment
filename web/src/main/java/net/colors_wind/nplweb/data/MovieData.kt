package net.colors_wind.nplweb.data

import java.io.BufferedReader
import java.lang.Appendable
import java.text.SimpleDateFormat
import java.util.*

data class MovieData(
    val id: Int,
    val name: String,
    val score: String,
    val sentenceData: List<SentenceData>,
    val createDate: Long = System.currentTimeMillis(),
    val exception: Exception? = null
) {

    fun toMessage(): List<String> {
        val message = arrayListOf<String>()
        message.add("<h2>$name</h2> <h6>电影评分: $score</h6>")
        sentenceData.forEach { message.add(it.toMessage()) }
        return message
    }

    fun exceptionMessage() : String? = exception?.message



    fun dump(appendable: Appendable) {
        val dataFormat = initializeDataFormat()
        appendable.appendLine(id.toString())
        appendable.appendLine(name)
        appendable.appendLine(score)
        sentenceData.forEach { it.dump(appendable) }
        appendable.appendLine(END_SENTENCES)
        appendable.appendLine(dataFormat.format(Date(createDate)))
    }

    fun isExpired() : Boolean {
        return System.currentTimeMillis() - createDate > EXPIRED
    }

    fun getTitle(): List<String> {
        return listOf("$name 电影评分: $score")
    }

    companion object {
        /**
         * 占位符, 标记正在处理电影评论
         */
        val PLACEHOLDER = MovieData(-1, "-", "-", emptyList())

        const val END_SENTENCES = "%%END_SENTENCES"
        const val EXPIRED = 30 * 24 * 60 * 60 * 1000L

        fun initializeDataFormat() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        operator fun invoke(reader: BufferedReader): MovieData {
            val dataFormat = initializeDataFormat()
            val id = reader.readLine().toInt()
            val name = reader.readLine()
            val score = reader.readLine()
            val sentenceData = arrayListOf<SentenceData>()
            while (true) {
                val sentence = reader.readLine()
                if (sentence == END_SENTENCES) break
                val possibility = reader.readLine().toDouble()
                sentenceData.add(SentenceData(sentence, possibility))
            }
            val createDate =dataFormat.parse(reader.readLine()).time
            return MovieData(id, name, score, sentenceData, createDate)
        }
    }
}

