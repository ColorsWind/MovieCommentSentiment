import net.colors_wind.nplweb.service.SecurityProvider
import net.colors_wind.nplweb.service.SecurityService
import org.junit.Test

class SecurityServiceTest {

    /**
     * 单一时间点测试
     */
    @Test
    fun staticTest() {
        val host = "test"
        repeat(2) {
            assert(SecurityService.predictMovie.tryAccess(host))
        }
        assert(!SecurityService.predictMovie.tryAccess(host))
    }

    /**
     * 短时间密集访问测试
     */
    @Test
    fun delayTest() {
        val host = "test"
        val service = SecurityProvider(periodUnit = 5, limit = 2, timeUnit = 100L)
        repeat(7) {
            assert(service.tryAccess(host))
            assert(service.tryAccess(host))
            Thread.sleep(50L)
            assert(!service.tryAccess(host))
            Thread.sleep(550L)
            assert(service.tryAccess(host))
        }
    }

    /**
     * 长时间低频访问测试
     */
    @Test
    fun cleanTest() {
        val host = "test"
        val service = SecurityProvider(periodUnit = 3, limit = 1, timeUnit = 100L)
        repeat(3) {
            assert(service.tryAccess(host))
            assert(!service.tryAccess(host))
            Thread.sleep(500L)
            assert(service.tryAccess(host))
        }
    }
}