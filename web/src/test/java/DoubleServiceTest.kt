import net.colors_wind.nplweb.data.MovieData
import net.colors_wind.nplweb.data.SentenceData
import net.colors_wind.nplweb.service.DoubanService
import org.junit.Test
import java.io.File

class DoubleServiceTest {

    /**
     * 测试文件输入输出
     */
    @Test
    fun fileTest() {
        val file = File("dump_test.txt")
        file.takeUnless { it.exists() }?.createNewFile()
        val data1 = MovieData(1, "测试电影1", "9.1",
            listOf(SentenceData("测试句子1-积极", 0.9),
                SentenceData("测试句子1-消极", 0.1)
            ))
        val data2 = MovieData(2, "测试电影2", "9.2",
            listOf(SentenceData("测试句子2-积极", 0.8),
                SentenceData("测试句子2-消极", 0.2)))
        DoubanService.add(data1)
        DoubanService.add(data2)
        DoubanService.dump(file)
        DoubanService.import(file, true)
        assert(DoubanService[1]!!.id == data1.id)
        assert(DoubanService[2]!!.name == "测试电影2")
        file.delete()
    }

    /**
     * 测试爬虫抓取短评
     */
    @Test
    fun reptileTest() {
        val document = DoubanService.getDocument(26613692) // 哥斯拉大战金刚
        val title = DoubanService.getTitle(document)
        assert(title.contains("哥斯拉大战金刚"))
    }

    /**
     * 测试队列是否能正常工作
     */
    @Test
    fun queueTest() {
        val thread = Thread(DoubanService.CrawlerThread()).apply { start() }
        DoubanService[35158160] // 我的姐姐
        DoubanService[34841067] // 你好，李焕英
        DoubanService[26935283] // 侍神令
        Thread.sleep(15 * 1000L)
        assert(DoubanService[35158160]?.name?.contains("我的姐姐") == true)
        assert(DoubanService[34841067]?.name?.contains("李焕英") == true)
        assert(DoubanService[26935283]?.name?.contains("侍神令") == true)
        thread.interrupt()
    }

}