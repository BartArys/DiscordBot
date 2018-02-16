package com.numbers.discordbot.commands

import com.numbers.discordbot.dsl.*
import com.numbers.discordbot.extensions.await
import com.numbers.discordbot.extensions.random
import com.numbers.discordbot.service.EightBallService
import com.numbers.discordbot.service.TagService
import org.apache.commons.validator.routines.UrlValidator
import sx.blah.discord.handle.impl.obj.ReactionEmoji
import sx.blah.discord.handle.obj.IUser
import java.awt.Image
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URL
import java.nio.file.Paths
import javax.imageio.ImageIO

@CommandsSupplier
fun funCommands() = commands {
    command("£ 8ball {question}"){
        arguments(words("question"))

        execute {
            val service = services<EightBallService>()
            val response = service.shake(args["question"]!!).await().body()!!

            message.deleteLater()
            respond {
                description = response.answer
                autoDelete = true
            }
        }

        info {
            description = "answers your questions"
            name = "8ball"
        }
    }

    command("£ claim tag {tag} {content}"){
        arguments(word("tag"), words("content"))

        execute {
            val service = services<TagService>()
            val tag = service.get(args["tag"]!!)
            if(tag == null){
                message.deleteLater()
                service.set(args["tag"]!!, args["content"]!!)
                respond {
                    description = "tag set to ${args<String>("content")}"
                    autoDelete = true
                }
            }else{
                message.delete()
                respondError {
                    description = "tag already claimed"
                    autoDelete = true
                }
            }
        }

        info {
            description = "claims an unclaimed tag"
            name = "claim tag"
        }
    }

    command("£ declaim tag {tag}"){
        arguments(words("tag"))

        execute {
            val service = services<TagService>()
            if(service.get(args["tag"]!!) == null){
                message.deleteLater()
                respondError {
                    description = "tag is currently not claimed"
                    autoDelete = true
                }
            }else{
                message.delete()
                respond {
                    description = "tag has been declaimed"
                    autoDelete = true
                }
            }
        }

        info {
            description = "removes an existing tag"
            name = "declaim tag"
        }
    }

    command("jeb")
    command("£pc")
    command("please clap"){
        execute { respond { description = ":clap: :clap: :clap:" } }

        info {
            description = "it won't increase your polls"
            name = "please clap"
        }
    }

    command(":{tag}:"){
        arguments(words("tag"))

        execute {
            val service = services<TagService>()
            val tag = service.get(args["tag"]!!)

            respond {
                if(UrlValidator.getInstance().isValid(tag)){
                    image = tag
                }else{
                    description = tag
                }
            }
        }

        info {
            description = "displays claimed tag"
            name = "tag"
        }
    }

    command("£f {user}?")
    command("£ f|respect {user}?"){

        arguments(userMention("user"))

        execute {
            val user : IUser = args("user") ?: event.author

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

            respond {
                description = "pay respects"

                file {
                    name = "respect.png"
                    file = input
                }
            }.addReaction(ReactionEmoji.of("\uD83C\uDDEB"))
            message.delete()
        }
    }

    simpleCommand("*teleports behind you*"){
        respond("Nothing personal kid")
    }

    simpleCommand("you know i'm something of a scientist myself"){
        respond {
            image = "https://i.imgur.com/AwkvuC6.jpg"
        }
    }

    command(literal("nani?!")){

        val urls = listOf("https://i.redd.it/6dwlf4rnmajz.jpg", "http://i0.kym-cdn.com/entries/icons/medium/000/017/640/giphy.gif", "http://i0.kym-cdn.com/photos/images/original/001/046/872/1e2.jpg")

        execute {
            respond{
                image = urls.random()
            }
        }
    }

    simpleCommand("omae wa mou shindeiru"){
        val urls = listOf("https://i.redd.it/6dwlf4rnmajz.jpg")

        respond(urls.random())
    }

}