package com.numbers.discordbot.dsl

import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.handle.impl.obj.Embed
import sx.blah.discord.util.EmbedBuilder
import java.awt.Color
import java.io.InputStream
import java.time.LocalDateTime

fun embed(apply: EmbedContainer.() -> Unit) : EmbedContainer{
     val container = EmbedContainer()
    container.apply()
    return container
}

class ItemEmbedContainer<T>(var item : T) : EmbedContainer()

open class EmbedContainer(
        var description : String? = null,
        var image : String? = null,
        var title : String? = null,
        var thumbnail : String? = null,
        var color: Color? = null,
        var url: String? = null,
        var timeStamp: LocalDateTime? = null,
        var file: InputStream? = null,
        var fileName: String? = null,

        var autoDelete: Boolean = false,

        internal val embedFields : MutableList<Embed.EmbedField> = mutableListOf<Embed.EmbedField>(),
        internal var authorUrl: String? = null,
        internal var authorName: String? = null,
        internal var authorIcon: String? = null,
        internal var footerIcon: String? = null,
        internal var footerText: String? = null
){
    fun author(apply: EmbedAuthor.() -> Unit){
        EmbedAuthor(this).apply()
    }

    fun footer(apply: EmbedFooter.() -> Unit){
        EmbedFooter(this).apply()
    }

    fun embedField(apply: EmbedFieldContainer.() -> Unit){
        val field = EmbedFieldContainer()
        field.apply()
        embedFields + field()
    }

    fun file(apply: EmbedFile.() -> Unit){
        EmbedFile(this).apply()
    }

    operator fun invoke() : EmbedObject{
        val builder = EmbedBuilder()
        embedFields.forEach { builder.appendField(it) }
        description?.let { builder.withDesc(it) }
        image?.let { builder.withImage(it) }
        title?.let { builder.withTitle(it) }
        thumbnail?.let { builder.withThumbnail(it) }
        color?.let { builder.withColor(it) }
        url?.let { builder.withUrl(it) }
        timeStamp?.let { builder.withTimestamp(it) }

        authorUrl?.let { builder.withAuthorUrl(it) }
        authorIcon?.let { builder.withAuthorIcon(it) }
        authorName?.let { builder.withAuthorName(it) }

        footerIcon?.let { builder.withFooterIcon(it) }
        footerText?.let { builder.withFooterText(it) }

        return builder.build()
    }
}

data class EmbedFile(
        private val parent : EmbedContainer
){
    var name: String?
        get() = parent.fileName
        set(value) { parent.fileName = value }

    var file: InputStream?
        get() = parent.file
        set(value) { parent.file = value }
}

data class EmbedFieldContainer(
        var title: String? = null,
        var content: String? = null,
        var inline: Boolean = false
){
    operator fun invoke() : Embed.EmbedField {
        return Embed.EmbedField(title!!, content!!, inline)
    }
}

data class EmbedFooter(
        private val parent : EmbedContainer
){
    var icon: String?
        get() = parent.footerIcon
        set(value) { parent.footerIcon = value }

    var text: String?
        get() = parent.footerText
        set(value) { parent.footerText = value }

}

data class EmbedAuthor(
        private val parent : EmbedContainer
){
    var name: String?
        get() = parent.authorName
        set(value) { parent.authorName = value }

    var icon: String?
        get() = parent.authorIcon
        set(value) { parent.authorIcon = value }

    var url: String?
        get() = parent.url
        set(value) { parent.url = value }
}


