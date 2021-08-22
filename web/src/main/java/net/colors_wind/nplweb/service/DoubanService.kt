package net.colors_wind.nplweb.service

import net.colors_wind.nplweb.data.MovieData
import net.colors_wind.nplweb.data.SentenceData
import net.colors_wind.nplweb.service.DoubanService.avg
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.UnsupportedOperationException
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

const val QUEUE_SIZE = 1000
object DoubanService {
    private const val END_MOVIE = "%%END_MOVIE"
    /** 评估结果 **/
    val movieResults = ConcurrentHashMap<Int, MovieData>()

    /** 将已评估的电影信息写入磁盘**/
    fun dump(file: File) {
        println("Dump to $file .")
        val writer = FileWriter(file)
        val data = movieResults.values.filterNot { it.isExpired() }
        writer.appendLine(data.size.toString())
        data.forEach { it.dump(writer) }
        writer.flush()
        writer.close()
    }
    /** 从磁盘加载文件 **/
    fun import(file: File, clear : Boolean = false) {
        if (clear) movieResults.clear()
        val reader = BufferedReader(FileReader(file))
        val num = reader.readLine()?.toInt() ?: 0
        for (i in 0 until num) {
            val movieData = MovieData(reader)
            movieResults[movieData.id] = movieData
        }
        reader.close()
        println("Import ${movieResults.values.map { it.name }}")
    }

    /** 获取电影评估结果，若不命中缓存则进行安全检查并提交任务到队列中 **/
    operator fun get(id: Int, host : String? = null): MovieData? {
        return movieResults[id] ?: let {
            return if (host == null || SecurityService.predictMovie.tryAccess(host)) {
                movieResults[id] = MovieData.PLACEHOLDER
                addTask(id)
                MovieData.PLACEHOLDER
            } else null
        }
    }



    fun add(movieData: MovieData) {
        movieResults[movieData.id] = movieData
    }

    fun getTaskTotal() = queue.size
    fun getTaskNum(id: Int) = queue.indexOf(id) + 1

    fun getComment(document: Document): List<String> {
        throw UnsupportedOperationException()
    }

    fun getDocument(id: Int): Document {
        throw UnsupportedOperationException()
    }

    fun getTitle(document: Document): String {
        throw UnsupportedOperationException()
    }

    fun addTask(id: Int) {
        queue.offer(id)
        println("Add Task $id, Queue: ${queue.size}")
    }

    private val queue = ArrayBlockingQueue<Int>(QUEUE_SIZE)

    /**
     * 电影处理线程
     */
    class CrawlerThread : Runnable {
        override fun run() {
            while (!Thread.interrupted()) try {
                val id = queue.take()
                println("Processing Task $id, Queue: ${queue.size}")
                //result[id] = MovieData.PLACEHOLDER
                val title = try {
                    val (title, comments) = getMovie(id)
                    val sentences = PredictService.predict(comments)
                    movieResults[id] = MovieData(id, title, sentences.avg(), sentences)
                    title
                }  catch (e : Exception) {
                    movieResults[id] = MovieData(id, "错误", "NaN", emptyList(), exception = e)
                    e.toString()
                }
                val sleep = Random.nextLong(800, 1500)
                println("处理电影 $title, 休眠 $sleep ms.")
                Thread.sleep(sleep)
            } catch (ex: InterruptedException) {}
        }

        /**
         * 爬取电影短评
         */
        fun getMovie(id: Int) : Pair<String, List<String>> {
            val document : Document = try {
                getDocument(id)
            } catch (e: HttpStatusException) {
                if (e.statusCode == 404) return "找不到电影" to emptyList()
                else throw e
            }
            val title = getTitle(document)
            val comments = getComment(document)
            return title to comments
        }

    }



    private fun List<SentenceData>.avg(): String {
        val sum = this.sumByDouble { it.possibility }
        val avg = sum / size
        return String.format("%.1f", avg * 10)
    }

    fun randomId(): String {
        val index = Random.nextInt(movieResults.size)
        val iterator = movieResults.keys()
        repeat(index) {
            iterator.nextElement()
        }
        return iterator.nextElement().toString()
    }

}


