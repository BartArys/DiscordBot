@file:Suppress("unused")

package com.numbers.discordbot.dsl

import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.handle.impl.obj.Embed
import sx.blah.discord.util.EmbedBuilder
import java.awt.Color
import java.io.InputStream
import java.time.Instant

inline fun embed(apply: EmbedContainer.() -> Unit): EmbedContainer {
    val container = EmbedContainer()
    container.apply()
    return container
}

class ItemEmbedContainer<T>(var item: T) : EmbedContainer()

interface IEmbedContainer {
    var description: String?
    var image: String?
    var title: String?
    var thumbnail: String?
    var color: Color?
    var url: String?
    var timeStamp: Instant?
    var file: InputStream?
    var fileName: String?
    var autoDelete: Boolean
    var authorUrl: String?
    var authorName: String?
    var authorIcon: String?
    var footerIcon: String?
    var footerText: String?

    fun embedField(field: Embed.EmbedField) {}
}

inline fun IEmbedContainer.author(apply: EmbedAuthor.() -> Unit) {
    EmbedAuthor(this).apply()
}

inline fun IEmbedContainer.footer(apply: EmbedFooter.() -> Unit) {
    EmbedFooter(this).apply()
}

inline fun IEmbedContainer.embedField(apply: EmbedFieldContainer.() -> Unit) {
    val field = EmbedFieldContainer()
    field.apply()
}

inline fun IEmbedContainer.file(apply: EmbedFile.() -> Unit) {
    EmbedFile(this).apply()
}

open class EmbedContainer(
        override var description: String? = null,
        override var image: String? = null,
        override var title: String? = null,
        override var thumbnail: String? = null,
        override var color: Color? = null,
        override var url: String? = null,
        override var timeStamp: Instant? = null,
        override var file: InputStream? = null,
        override var fileName: String? = null,

        override var autoDelete: Boolean = false,

        private val embedFields: MutableList<Embed.EmbedField> = mutableListOf(),
        override var authorUrl: String? = null,
        override var authorName: String? = null,
        override var authorIcon: String? = null,
        override var footerIcon: String? = null,
        override var footerText: String? = null
) : IEmbedContainer {
    override fun embedField(field: Embed.EmbedField) {
        embedFields.add(field)
    }

    operator fun invoke(): EmbedObject {
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
        private val parent: IEmbedContainer
) {
    var name: String?
        get() = parent.fileName
        set(value) {
            parent.fileName = value
        }

    var file: InputStream?
        get() = parent.file
        set(value) {
            parent.file = value
        }
}

data class EmbedFieldContainer(
        var title: String? = null,
        var content: String? = null,
        var inline: Boolean = false
) {
    operator fun invoke(): Embed.EmbedField {
        return Embed.EmbedField(title!!, content ?: "_", inline)
    }
}

@Suppress("unused")
data class EmbedFooter(
        private val parent: IEmbedContainer
) {
    var icon: String?
        get() = parent.footerIcon
        set(value) {
            parent.footerIcon = value
        }

    var text: String?
        get() = parent.footerText
        set(value) {
            parent.footerText = value
        }

}

data class EmbedAuthor(
        private val parent: IEmbedContainer
) {
    var name: String?
        get() = parent.authorName
        set(value) {
            parent.authorName = value
        }

    var icon: String?
        get() = parent.authorIcon
        set(value) {
            parent.authorIcon = value
        }

    var url: String?
        get() = parent.url
        set(value) {
            parent.url = value
        }
}


