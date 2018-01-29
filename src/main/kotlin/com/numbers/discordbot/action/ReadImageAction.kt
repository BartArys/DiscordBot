package com.numbers.discordbot.action

import com.asprise.ocr.Ocr
import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import com.numbers.discordbot.guard.Guard
import com.numbers.discordbot.guard.Guards
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuffer
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path


class ReadImageAction {

    companion object {
        private val ocr : Ocr by lazy {
            Ocr.setUp()
            Ocr().also { it.startEngine("eng", Ocr.SPEED_SLOW) }
        }

        private val path: Path by lazy {
            val fs = Jimfs.newFileSystem(Configuration.unix())
            val foo = fs.getPath("/memory")
            Files.createDirectory(foo)
        }
    }

    @Guards("Attempts to read an image", Guard("$ ocr"))
    fun read(message: IMessage, channel: IChannel){
        if(message.attachments.isEmpty()) return

        val files = message.attachments
                .map {
                    URL(it.url)
                }.toTypedArray()

        val result= ocr.recognize(files, Ocr.RECOGNIZE_TYPE_ALL, Ocr.OUTPUT_FORMAT_PLAINTEXT)

        RequestBuffer.request { channel.sendMessage(EmbedBuilder().withDesc(result).build()) }
    }

}