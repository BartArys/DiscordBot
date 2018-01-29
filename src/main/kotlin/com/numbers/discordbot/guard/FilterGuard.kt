package com.numbers.discordbot.guard

import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.MessageTokenizer

interface GuardBase{

    var filterBuilder : FilterBuilder

    fun next(item: GuardBuilderItem) : GuardBase{
        item.apply(filterBuilder)
        return this
    }

    fun next(sequence: String) : GuardBase{
        return next(StringSequenceGuardBuilderItem(sequence))
    }

    fun next(sequence: () -> String) : GuardBase{
        return next(StringSequenceGuardBuilderItem(sequence()))
    }

    operator fun plus (item: GuardBuilderItem) : GuardBase{
        return next(item)
    }

    operator fun plus (sequence: String) : GuardBase{
        return next(sequence)
    }

}

interface GuardBuilderItem{

    fun apply(filterBuilder: FilterBuilder)

}

interface GuardBaseBuilderItem : GuardBase, GuardBuilderItem {

    override fun apply(filterBuilder: FilterBuilder) {
        this.filterBuilder.items.forEach { filterBuilder.addFilter(it) }
    }

}


fun GuardBaseBuilderItem.asOptional() :GuardBaseBuilderItem {

    val inner = this


    return object : GuardBaseBuilderItem{
        override var filterBuilder: FilterBuilder = FilterBuilder()

        init {
            inner.apply(filterBuilder)

            filterBuilder.pop().let {
                filterBuilder.addFilter(it.asOptional())
            }
        }

    }

}

class StringSequenceGuardBuilderItem(private val sequence : String) : GuardBuilderItem{

    override fun apply(filterBuilder: FilterBuilder) {
        sequence.split(" ").forEach { filterBuilder.addFilter(WordFilterItem(it)) }
    }

}

class GuardSequence(words: String) : GuardBaseBuilderItem{
    override var filterBuilder: FilterBuilder = FilterBuilder()

    init {
        next(words)
    }

}

fun sequence(words : String) : GuardSequence{
    return GuardSequence(words)
}

class PrefixGuard(private val supplier: () -> String) : GuardBaseBuilderItem{
    override var filterBuilder: FilterBuilder = FilterBuilder()

    init {
        filterBuilder.addFilter(object : FilterItem{
            override val rangeCheck: Int = 1

            override fun apply(tokens: Iterable<MessageTokenizer.Token>, args: MutableMap<String, String>): Boolean  = tokens.first().content == supplier()
        })
    }

}

fun prefix(supplier: () -> String) : GuardBaseBuilderItem{
    return PrefixGuard(supplier)
}

fun any(item1: GuardBuilderItem, item2: GuardBuilderItem) : GuardBaseBuilderItem{
    return object : GuardBaseBuilderItem {
        override var filterBuilder: FilterBuilder = FilterBuilder()

        override fun apply(filterBuilder: FilterBuilder) {
            val fb = FilterBuilder()

            item1.apply(fb)
            item2.apply(fb)

            filterBuilder.addOrFilters(fb.items)
        }

    }
}



fun FilterItem.asOptional() : FilterItem{
    return OptionalFilterItem(this)
}

class OptionalFilterItem(val filterItem: FilterItem) : FilterItem{
    override val rangeCheck: Int
        get() = filterItem.rangeCheck

    override val minRangeCheck: Int = 0

    override fun apply(tokens: Iterable<MessageTokenizer.Token>, args: MutableMap<String, String>): Boolean {
        return try{
            filterItem.apply(tokens, args)
            true
        }catch (ex : Exception){
            true
        }
    }

}

infix fun(() -> GuardBase).bindTo(handler : (MessageReceivedEvent) -> Unit) : IListener<MessageReceivedEvent> {
    return this().filterBuilder.build(IListener { handler(it) })
}

infix fun (() -> GuardBase).bindToArgs(handler : (MessageReceivedEvent, Map<String,String>) -> Unit) : IListener<MessageReceivedEvent>{
    return this().filterBuilder.build(handler)
}

fun arg(key: String, argumentType: ArgumentType = ArgumentType.WORD) : GuardBaseBuilderItem{
    return GuardArgument(key, argumentType)
}

class GuardArgument(key: String, argumentType: ArgumentType) : GuardBaseBuilderItem{
    override var filterBuilder: FilterBuilder = FilterBuilder()

    init {
        if(argumentType == ArgumentType.WORDS){
            filterBuilder.addFilter(ArgumentFilterItem(key, argumentType, 2000, 1))
        }else{
            filterBuilder.addFilter(ArgumentFilterItem(key, argumentType))
        }
    }

}
