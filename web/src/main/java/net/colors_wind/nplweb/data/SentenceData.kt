package net.colors_wind.nplweb.data

import java.io.BufferedReader
import java.lang.Appendable

data class SentenceData(val sentence: String, val possibility : Double) {
    fun getSentenceFixed() : String {
        return if (sentence.length > 30)
            "${sentence.substring(0, 29)}..."
        else String.format(sentence, "%20s")
    }

    fun getRating() : String {
        return when {
            possibility == Double.NaN -> "未知(NaN)"
            isPositive() -> "好评(${String.format("%.2f", possibility * 100)}%)"
            else -> "差评(${String.format("%.2f", (1 - possibility) * 100)}%)"
        }

    }

    fun isPositive() = possibility > 0.5

    fun toMessage(): String {
        return "${getRating()}: ${getSentenceFixed()}"
    }

    fun dump(appendable: Appendable) {
        appendable.appendLine(sentence)
        appendable.appendLine(possibility.toString())
    }

    companion object {
        operator fun invoke(bufferedReader: BufferedReader): SentenceData {
            val sentence = bufferedReader.readLine()
            val possibility = bufferedReader.readLine().toDouble()
            return SentenceData(sentence, possibility)
        }
    }
}