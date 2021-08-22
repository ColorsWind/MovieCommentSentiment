import net.colors_wind.nplweb.service.PredictService
import org.junit.Test
import java.io.BufferedReader
import java.io.InputStreamReader

class PredictServiceTest {

    /**
     * 测试 Python 调用
     */
    @Test
    fun pythonTest() {
        val process =
            Runtime.getRuntime().exec("${PredictService.python} -c \"print('Hello World')\"", null, PredictService.path)
        val input = process.inputStream
        val reader = BufferedReader(InputStreamReader(input))
        val helloWorld = reader.readLine()
        input.close()
        process.waitFor()
        process.destroy()
        assert(helloWorld == "Hello World")
    }

    /**
     * 测试 Python 预测数据
     */
    @Test
    fun predictionTest() {
        val positive = PredictService.predict("非常好的电影").possibility
        println(positive)
        assert(positive > 0.8 && positive < 1.0)
        val negative = PredictService.predict("垃圾电影，浪费时间").possibility
        assert(negative > 0.0 && negative < 0.3)
        val combine = PredictService.predict(arrayListOf("垃圾", "好电影"))
        assert(combine[0].possibility < 0.5 && combine[1].possibility > 0.5)
    }

}