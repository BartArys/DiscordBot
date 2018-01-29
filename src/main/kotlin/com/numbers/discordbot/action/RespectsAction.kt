package com.numbers.discordbot.action

import com.numbers.discordbot.extensions.then
import com.numbers.discordbot.guard.Argument
import com.numbers.discordbot.guard.Guard
import com.numbers.discordbot.guard.Guards
import com.numbers.discordbot.guard2.ArgumentType
import com.numbers.discordbot.guard2.CommandArguments
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.impl.obj.ReactionEmoji
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.util.RequestBuilder
import java.awt.Image
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URL
import java.nio.file.Paths
import javax.imageio.ImageIO


class RespectsAction {

    @Guards("""
        generates a custom respect message
    """,
            Guard("$ f|respect {person}?", Argument(ArgumentType.USER_MENTION, "the person to pays respect to")),
            Guard("\$f {person}?", Argument(ArgumentType.USER_MENTION, "the person to pays respect to"))
    )
    fun payRespect(event: MessageReceivedEvent, args: CommandArguments){

        val user : IUser = args["person"] ?: event.author

        val image = ImageIO.read(Paths.get("src/main/resources/respect.jpg").toFile())


        val icon = ImageIO.read(URL(user.avatarURL.replace("webp","png")))

        val skewX = 0.2
        val x = 0.0

        val at = AffineTransform.getTranslateInstance(x, 0.0)
        at.shear(skewX, 0.0)
        val op = AffineTransformOp(at,
                RenderingHints(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC))
        var skew = op.filter(icon, null)

        val transform = AffineTransform.getRotateInstance(Math.toRadians(-5.0))
        val op2 = AffineTransformOp(transform,
                RenderingHints(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC))


        skew = op2.filter(skew, null)

        val scaled = skew.getScaledInstance(50,75, Image.SCALE_SMOOTH)

        val graphics = image.createGraphics()

        graphics.drawImage(scaled, 260, 65, null)

        graphics.dispose()

        val os = ByteArrayOutputStream()
        ImageIO.write(image, "jpg", os)
        val input = ByteArrayInputStream(os.toByteArray())

        var message : IMessage? = null

        RequestBuilder(event.client).shouldBufferRequests(true).doAction {
            message = event.message.channel.sendFile("pay respects", input, "respect.png")
            true
        }.then {
           message!!.addReaction(ReactionEmoji.of("\uD83C\uDDEB"))
        }.build()

    }

}