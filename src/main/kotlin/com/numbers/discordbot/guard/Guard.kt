package com.numbers.discordbot.guard

import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions

@Retention()
@Target(AnnotationTarget.FUNCTION)
annotation class Guard (
        val format : String,
        vararg val params : Argument = [],
        val description: String = ""
)

@Retention()
@Target()
annotation class Argument(
        val type: com.numbers.discordbot.guard2.ArgumentType,
        val description: String = ""
)

@Retention()
@Target(AnnotationTarget.FUNCTION)
annotation class Guards(val description: String = "",vararg val guards : Guard)

/*
class GuardParser{

    private val index : AtomicInteger = AtomicInteger()
    private val argCounter : AtomicInteger = AtomicInteger()

    companion object {
        fun parse(guard: Guard) : () -> GuardBase {
            return GuardParser().parse(guard)
        }
    }

    fun parse(guard : Guard) : () -> GuardBase{

        return {
            val toParse = guard.format.trim().toCharArray()

            val tokens = LinkedList<GuardBaseBuilderItem>()
            while (toParse.size > index.get()){
                skipWhiteSpace(toParse)
                val token : GuardBaseBuilderItem =when(toParse[index.get()]){
                    '|' -> {
                        index.incrementAndGet()
                        any(tokens.removeLast(), parseNext(guard, toParse[index.get()], toParse))
                    }
                    '?' -> {
                        index.incrementAndGet()
                        tokens.removeLast().asOptional()
                    }
                    else -> {
                        parseNext(guard, toParse[index.get()], toParse)
                    }
                }

                tokens.add(token)
            }

            val ret = tokens.reduce(operation = { acc : GuardBase, guardBaseBuilderItem : GuardBaseBuilderItem ->  acc + guardBaseBuilderItem })
            ret
        }

    }

    private fun parseNext(guard : Guard, char : Char, charArray: CharArray) : GuardBaseBuilderItem{
        return when(char){
            '{' -> {
                consumeArgument(charArray, guard.params[argCounter.get()].type)
            }
            else -> {
                consumeWord(charArray)
            }
        }
    }

    private fun skipWhiteSpace(charArray: CharArray) {

        while (charArray.size > index.get() && charArray[index.get()] == ' '){
            index.getAndIncrement()
        }

    }

    private fun consumeWord(charArray: CharArray) : GuardBaseBuilderItem {
        val builder = StringBuilder()

        while (charArray.size > index.get() && charArray[index.get()] != ' ' && charArray[index.get()] != '|' && charArray[index.get()] != '?' && charArray[index.get()] != '{'){
            builder.append(charArray[index.getAndIncrement()])
        }

        val word = builder.toString()

        if(word == "$") {
            return prefix { ";b" }
        }

        return sequence(builder.toString())
    }

    private fun consumeArgument(charArray: CharArray, type: ArgumentType) : GuardBaseBuilderItem{
        index.incrementAndGet()

        val builder = StringBuilder()
        while (charArray.size > index.get() && charArray[index.get()] != '}'){
            builder.append(charArray[index.getAndIncrement()])
        }

        argCounter.incrementAndGet()
        index.incrementAndGet()

        return arg(builder.toString(), type)
    }
}
*/

val KClass<*>.guards : List<Pair<KFunction<*>, List<Guard>>> get() =  functions
        .map { it to ( it.findAnnotation<Guard>()?.let { listOf(it) } ?: it.findAnnotation<Guards>()?.guards?.toList().orEmpty() ) }
        .filter { it.second.isNotEmpty() }