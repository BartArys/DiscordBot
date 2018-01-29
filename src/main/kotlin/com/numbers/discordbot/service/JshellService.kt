package com.numbers.discordbot.service

import kotlinx.io.PrintWriter
import java.io.File
import java.nio.file.Paths
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class JshellService {

    private val path = "C:\\Program Files\\Java\\jdk-9.0.1\\bin\\jshell.exe"

    fun execute(javacode : String) : String{

        val file = File.createTempFile("command",".tmp")

        file.writeText(javacode + "\n/exit")

        val process = ProcessBuilder(path, file.absolutePath).redirectErrorStream(true).directory(Paths.get("C:\\Users\\Numbe\\ideaProjects\\DiscordBot\\target\\lib").toFile()).start()
        Executors.newSingleThreadScheduledExecutor().schedule({ process.destroyForcibly() }, 5, TimeUnit.SECONDS)

        val inputStream = process.inputStream

        val output = PrintWriter(process.outputStream)
        output.write(javacode + "\n")
        output.flush()
        output.write("/exit\n")
        output.flush()

        val builder = StringBuilder()
        val bufferSize = 4000
        val buffer =  ByteArray(bufferSize, { _ -> 0 })
        while (process.isAlive) {
            val no = inputStream.available()
            if (no > 0) {
                val n = inputStream.read(buffer, 0, Math.min(no, buffer.size))
                builder.append(String(buffer, 0, n))
            }
        }
        output.close()
        file.delete()

        return  builder.toString()
    }
}