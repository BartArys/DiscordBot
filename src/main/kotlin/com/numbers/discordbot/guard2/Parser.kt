package com.numbers.discordbot.guard2

import com.google.inject.Injector
import com.numbers.discordbot.guard.Guard
import com.numbers.discordbot.service.PrefixService
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.util.concurrent.atomic.AtomicInteger

class Parser private constructor(guard: Guard, private val injector: Injector){

    private val indexCounter = AtomicInteger()

    private var arguments = guard.params.map { it.type }
    private val argumentCounter = AtomicInteger()

    private var argumentIndex inline get() = argumentCounter.get()
        inline set(value) = argumentCounter.set(value)

    private var index inline get() = indexCounter.get()
        inline set(value) = indexCounter.set(value)

    private val token : Char inline get() = chars[index]

    private val hasMore : Boolean inline get() = index <= max

    private val chars = guard.format.trim().toCharArray()
    private val max = guard.format.length - 1

    val builder = FilterBuilder()

    private val specialChars = arrayOf('{', '}', '|', '?', '$')

    private fun Char.isSpecial() = specialChars.contains(this)

    fun parse() : com.numbers.discordbot.guard2.Filter<MessageReceivedEvent>{
        while (hasMore){
            skipWhileSpace()
            if (hasMore){
                parseNextToken()
            }
        }
        return builder.build()
    }

    private fun next(){
        index++
    }

    private fun parsePrefix() : PrefixItem{
        skipWhileSpecial()

        if(hasMore && !token.isWhitespace()){
            val suffix = collectUntilSpecial()
            return  PrefixSuffixItem(injector.getInstance(PrefixService::class.java), suffix)
        }

        return PrefixItem(injector.getInstance(PrefixService::class.java))
    }

    private fun parseNextToken(){
        when(token){
            '|' -> {
                val index = builder.nextIndex-1
                val item = if(isArgumentContext()){
                    parseArgument()
                }else{
                    parseWord()
                }
                val items = builder.remove(index).toMutableList()
                items.add(item)

                builder.insert(index, OrFilterItem(items))
            }
            '{' -> builder.insert(builder.nextIndex, parseArgument())
            '?' -> {
                val index = builder.nextIndex-1
                builder.remove(index).map { OptionalFilterItem(it) }.forEach { builder.insert(index, it) }
                next()
            }
            '}' -> next()
            '$' -> builder.insert(builder.nextIndex, parsePrefix())
            else -> builder.insert(builder.nextIndex, parseWord())
        }
    }

    private fun isArgumentContext() : Boolean{

        if(chars[index-1] == '}' && chars[index+1] == '{') return true

        for (i in index..max){
            when(chars[i]){
                '{' -> return false
                '}' -> return true
            }
        }

        return false

    }

    private fun parseWord() : com.numbers.discordbot.guard2.FilterItem{
        skipWhileSpecial()
        skipWhileSpace()
        val word = collectUntilSpecial()
        return if(hasMore && token == '{'){
            val argument = parseArgument()
            next() //skip '}'
            val suffix = collectUntilSpecial()
            PaddedArgumentItem(argument.type, argument.key, prefix = word, suffix = suffix)
        }else{
            WordSequenceItem(word)
        }
    }

    private fun collectUntilSpecial(collectWhiteSpace : Boolean = false) : String{
        val sb = StringBuilder()

        while (hasMore && (!token.isWhitespace() || collectWhiteSpace) && !token.isSpecial()){
            sb.append(token)
            next()
        }

        return sb.toString()
    }

    private fun parseArgument() : ArgumentItem{
        skipWhileSpace()
        skipWhileSpecial()
        val word = collectUntilSpecial(collectWhiteSpace = true)
        val argument = arguments[argumentIndex]

        argumentIndex++

        return ArgumentItem(argument, word)
    }

    private fun skipWhileSpace(){
        while (hasMore && token.isWhitespace()) next()
    }

    private fun skipWhileSpecial(){
        while (hasMore && token.isSpecial()) next()
    }

    companion object {
        fun parse(guard: Guard, injector: Injector) : Filter<MessageReceivedEvent>{
            return Parser(guard, injector)
                    .parse()
        }
    }
}