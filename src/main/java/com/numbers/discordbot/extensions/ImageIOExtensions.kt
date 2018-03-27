package com.numbers.discordbot.extensions

import java.awt.image.BufferedImage
import java.net.HttpURLConnection
import java.net.URL
import javax.imageio.ImageIO

 fun readAsBrowser(url: URL) : BufferedImage {
    val connection = url
            .openConnection() as HttpURLConnection
    connection.setRequestProperty(
            "User-Agent",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31")
    return ImageIO.read(connection.inputStream)
}