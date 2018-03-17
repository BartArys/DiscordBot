package com.numbers.discordbot.commands

import com.numbers.discordbot.dsl.*
import com.numbers.discordbot.dsl.gui.builder.Emote
import com.numbers.discordbot.dsl.gui.builder.toSelectList
import com.numbers.discordbot.dsl.gui2.ScreenBuilder
import com.numbers.discordbot.dsl.gui2.controls
import com.numbers.discordbot.dsl.gui2.detach
import com.numbers.discordbot.dsl.gui2.split
import com.numbers.discordbot.extensions.await
import com.numbers.discordbot.extensions.random
import com.numbers.discordbot.service.EightBallService
import com.numbers.discordbot.service.InspirationService
import com.numbers.discordbot.service.KtShellService
import com.numbers.discordbot.service.WikiSearchService
import com.numbers.discordbot.service.discordservices.ReactionService
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
import kotlin.math.max

@CommandsSupplier
fun funCommands() = commands {
    command("£ 8ball {question}"){
        arguments(words("question"))

        execute {
            val service = services<EightBallService>()
            val response = service.shake(args["question"]!!).await().body()!!

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

    command("£ claim reaction {reaction} {content}"){
        arguments(word("reaction"), words("content"))

        execute {
            val service = services<ReactionService>()
            val reactions = service.getReactionsByKey(args["reaction"]!!)
            if(reactions.isEmpty()){
                message.deleteLater()
                service.createReaction(author, guild!!, args["reaction"]!!, args["content"]!!)
                respond {
                    description = "reaction set to ${args<String>("content")}"
                    autoDelete = true
                }
            }else{
                message.delete()
                respondError {
                    description = "reaction already claimed"
                    autoDelete = true
                }
            }
        }

        info {
            description = "claims an unclaimed reaction"
            name = "claim reaction"
        }
    }

    command("£ declaim reaction {reaction}"){
        arguments(words("reaction"))

        execute {
            val service = services<ReactionService>()
            val reaction = service.getReactionsByKey(args["reaction"]!!).firstOrNull()
            if(reaction == null){
                message.deleteLater()
                respondError {
                    description = "reaction is currently not claimed"
                    autoDelete = true
                }
            }else{
                println(reaction)
                service.deleteReaction(reaction)
                message.delete()
                respond {
                    description = "reaction has been declaimed"
                    autoDelete = true
                }
            }
        }

        info {
            description = "removes an existing reaction"
            name = "declaim reaction"
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

    command(":{reaction}:"){
        arguments(words("reaction"))

        execute {
            val service = services<ReactionService>()
            val reaction = service.getReactionsByKey(args["reaction"]!!).firstOrNull() ?: return@execute

            respond {
                if(UrlValidator.getInstance().isValid(reaction.content)){
                    image = reaction.content
                }else{
                    description = reaction.content
                }
            }
        }

        info {
            description = "displays claimed reaction"
            name = "reaction"
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
            }.await().addReaction(ReactionEmoji.of("\uD83C\uDDEB"))
            message.delete()
        }
    }

    command("£ wiki {words}"){
        arguments(words("words"))

        execute {
            val result = services<WikiSearchService>().searchFor(search = args("words")!!).await().body()!!
            respondScreen(block = result.toSelectList())
        }
    }

    command("Kompile {code}"){
        arguments(words("code"))

        execute {
            val response = services<KtShellService>().executeForContext(this, args["code"]!!)
            respond(response.toString())
        }
    }

    command("you're supposed to {words}")
    command("you're not supposed to {words}"){
        arguments(words("words"))

        execute {
            val answer = listOf(
                    "oh",
                    "> trying to blame a program :thinking:",
                    "I'm sorry, I *guess* I must've read the source code wrong then",
                    "I have 3% code coverage, does this surprise you in any way?",
                    "your feedback is appreciated, let me just write that do- \nOh no, it just go already garbage collected...\n *what a shame*...",
                    "Executing this code is like throwing bowling pins at a bowling ball. You're technically reaching the desired goal \nbut not really"
            ).random()

            respond {
                description = answer
            }
        }
    }

    simpleCommand("quote"){
        val response = services<InspirationService>().generateQuote().await().body()!!.string()
        respond {
            image = response
        }
    }

    simpleCommand("embed quote"){
        fun showEmbed(quotes : MutableList<String>, displayIndex : Int = 0): ScreenBuilder.() -> Unit = {
            fun addQuote() = quotes.add(services<InspirationService>().generateQuote().execute().body()!!.string())
            var index = displayIndex
            if(quotes.isEmpty()) { addQuote() }

            var displayQuote = false

            onRefresh {
                image = quotes[index]
                description = if(displayQuote){
                    quotes[index]
                }else null
            }

            controls {
                forEmote(Emote.prev) {
                    if(index != 0){
                        index -= 1
                        it.refresh()
                    }
                }

                forEmote(Emote.next) {
                    if(index + 1 >= quotes.size){
                        addQuote()
                    }
                    index += 1
                    it.refresh()
                }

                forEmote(Emote.eject) {
                    it.detach()
                    displayQuote = true
                    it.refresh()
                    quotes.removeAt(index)
                    it.split(showEmbed(quotes, max(index - 1, 0)))
                }
            }
        }
        respondScreen(block = showEmbed(mutableListOf()))
    }
}