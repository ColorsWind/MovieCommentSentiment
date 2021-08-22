package net.colors_wind.nplweb

import com.google.gson.Gson
import java.io.File
import com.google.gson.GsonBuilder
import java.io.FileReader
import java.io.FileWriter


data class Config(
    val python: String = "python3",
    val network: String = "./python/"
) {


    companion object {
        var instance = readConfig()

        fun readConfig(): Config = File("config.json").let { file ->
            file.takeIf { file.exists() }?.let {
                val reader = FileReader(file)
                val config = Gson().fromJson(reader, Config::class.java)
                reader.close()
                config
            } ?: run {
                val config = Config()
                file.createNewFile()
                val gson = GsonBuilder()
                    .setPrettyPrinting()
                    .create().toJson(config)
                val writer = FileWriter(file)
                writer.write(gson)
                writer.flush()
                writer.close()
                config
            }
        }

    }
}