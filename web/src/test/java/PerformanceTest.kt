import net.colors_wind.nplweb.service.PredictService
import net.colors_wind.nplweb.service.SentenceCacheFIFO
import net.colors_wind.nplweb.service.SentenceCacheSCR
import org.junit.Test
import java.util.*
import kotlin.random.Random.Default.nextInt

class PerformanceTest {

    fun randomSentence(n: Int) : Array<String> {
        return Array(n){UUID.randomUUID().toString()}
    }

    fun testTimer(task: () -> Unit) {
        val start = System.currentTimeMillis()
        task.invoke()
        val end = System.currentTimeMillis()
        println("Run ${end - start} ms.")
    }

    fun noCache(list: List<String>) {
        println("NoCache")
        testTimer{
            list.forEach{ str ->
                PredictService.predict(str)
            }
        }
        println()
    }

    fun cacheFIFO(list: List<String>) {
        println("FIFOCache")
        val cache = SentenceCacheFIFO()
        var miss = 0
        testTimer {
            list.forEach { str ->
                cache[str]?:run {
                    val sentenceData = PredictService.predict(str)
                    cache.insert(sentenceData)
                    miss += 1
                }
            }
        }
        println("Miss $miss")
    }

    fun cacheSCR(list: List<String>) {
        println("SCRCache")
        val cache = SentenceCacheSCR()
        var miss = 0
        testTimer {
            list.forEach { str ->
                cache[str]?:run {
                    val sentenceData = PredictService.predict(str)
                    cache.insert(sentenceData)
                    miss += 1
                }
            }
        }
        println("Miss $miss")
    }


    @Test
    fun testCache1() {
        println("Cache1")
        val testArray = arrayListOf<String>()
        repeat(500) {
            val str = nextInt().toString()
            testArray.add(str)
            testArray.add(str)
        }
        PredictService.predict(testArray.subList(0, 50))
        println(testArray.size)
        noCache(testArray)
        cacheFIFO(testArray)
        cacheSCR(testArray)

    }

    //@Test
    fun testCache2() {
        println("Cache2")
        val testArray = arrayListOf<String>()
        repeat(10) {
            val sub = arrayListOf<String>()
            repeat(50) {
                val str = UUID.randomUUID().toString()
                sub.add(str)
                sub.add(str)
            }
            sub.shuffle()
            testArray.addAll(sub)
        }
        PredictService.predict(testArray.subList(0, 50))
        println(testArray.size)
        noCache(testArray)
        cacheFIFO(testArray)
        cacheSCR(testArray)
    }


    @Test
    fun testCache3() {
        println("Cache3")
        val testArray = arrayListOf<String>()
        repeat(10) {
            val sub = arrayListOf<String>()
            repeat(25) {
                val str = UUID.randomUUID().toString()
                sub.add(str)
                sub.add(str)
                sub.add(str)
                sub.add(str)
            }
            sub.shuffle()
            testArray.addAll(sub)
        }
        PredictService.predict(testArray.subList(0, 50))
        println(testArray.size)
        noCache(testArray)
        cacheFIFO(testArray)
        cacheSCR(testArray)
    }

    @Test
    fun testCache4() {
        println("NoCache")
        val testArray = arrayListOf<String>()
        repeat(1000) {
            testArray.add(UUID.randomUUID().toString())
        }
        PredictService.predict(testArray.subList(0, 50))
        println(testArray.size)
        testArray.shuffle()
        noCache(testArray)
        cacheFIFO(testArray)
        cacheSCR(testArray)
    }



}