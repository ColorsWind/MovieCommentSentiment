package net.colors_wind.nplweb

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import freemarker.cache.*
import io.ktor.features.*
import io.ktor.freemarker.*
import io.ktor.http.content.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.pipeline.*
import net.colors_wind.nplweb.data.MovieData
import net.colors_wind.nplweb.template.RespondInfo
import net.colors_wind.nplweb.service.DoubanService
import net.colors_wind.nplweb.service.PredictService
import net.colors_wind.nplweb.service.SecurityService
import net.colors_wind.nplweb.template.PendingInfo
import java.io.File
import java.util.*
import kotlin.system.exitProcess

const val port = 8081;
fun main() {
    val file = File("movie_cache.txt")
    file.takeUnless { it.exists() }?.createNewFile()
    DoubanService.import(file)
    Thread(DoubanService.CrawlerThread()).start()
    val server = embeddedServer(Netty, port = 8081) {
        install(FreeMarker) {
            templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
        }
        routing {
            static("/static") { resources("static") }
            route("/") { get { respond(this) } }
        }
    }
    server.start(wait = false)
    server.addShutdownHook {
        DoubanService.dump(file)
    }
    val scanner = Scanner(System.`in`)
    while (true) when (val line = scanner.nextLine()) {
        "stop" -> {
            DoubanService.dump(file)
            server.stop(5000, 30000)
            exitProcess(0)
        }
        else -> {
            println(line)
        }
    }
}

suspend fun respond(context: PipelineContext<Unit, ApplicationCall>) {
    when (val text = context.call.request.queryParameters["text"]) {
        "random" -> {
            context.call.respondRedirect { this.parameters["text"] = DoubanService.randomId() }
        }
        null, "" -> {
            context.call.respond(FreeMarkerContent("index.ftl", mapOf("respond" to RespondInfo()), ""))
        }
        else -> {
            text.toIntOrNull()?.let { id ->
                // 豆瓣电影id
                DoubanService[id, context.call.request.origin.remoteHost]?.let{
                    it.takeIf { it != MovieData.PLACEHOLDER /* 占位符*/ }?.let { movieData ->
                        context.call.respond(
                            FreeMarkerContent(
                                "index.ftl",
                                mapOf("respond" to RespondInfo(movieData.getTitle(), movieData.sentenceData))
                            )
                        )
                    } ?: run {
                        val pendingInfo = PendingInfo(DoubanService.getTaskTotal(), DoubanService.getTaskNum(id))
                        context.call.respond(
                            FreeMarkerContent(
                                "index.ftl",
                                mapOf("respond" to RespondInfo(listOf(pendingInfo.message)), "pending" to pendingInfo)
                            )
                        )
                    }
                } ?: run {
                    context.call.respond(
                        FreeMarkerContent(
                            "index.ftl",
                            mapOf("respond" to RespondInfo(listOf("尝试评估电影过于频繁"))), ""
                        )
                    )
                }
            } ?: run {
                if (SecurityService.predictSentence.tryAccess(context.call.request.origin.remoteHost)) {
                    val sentenceData = PredictService[text]
                    context.call.respond(
                        FreeMarkerContent(
                            "index.ftl",
                            mapOf("respond" to RespondInfo(sentenceData = listOf(sentenceData))), ""
                        )
                    )
                } else {
                    context.call.respond(
                        FreeMarkerContent(
                            "index.ftl",
                            mapOf("respond" to RespondInfo(listOf("尝试评估句子过于频繁"))), ""
                        )
                    )
                }
            }
        }
    }
}



