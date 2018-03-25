package com.numbers.discordbot.dsl.gui2

import com.numbers.discordbot.dsl.gui.builder.Emote
import com.numbers.discordbot.dsl.gui.extensions.observable
import javafx.beans.Observable
import javafx.collections.ObservableList
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent
import sx.blah.discord.handle.impl.obj.Embed
import kotlin.math.max
import kotlin.math.min

class ListBuilder<ITEM> : Controllable<ListField<ITEM>>{
    val controls: MutableList<Pair<String, (ListField<ITEM>, event: ReactionAddEvent) -> Unit>> = mutableListOf()
    var renderer: ((Iterable<IndexedValue<ITEM>>) -> Iterable<Embed.EmbedField>)? = null
    var navigationType : NavigationType = NavigationType.pageNavigation
    var itemsOnScreen = 5

    fun properties(vararg properties : ListBuilderApplicable){
        properties.forEach { it.apply(this) }
    }

    inline fun renderIndexed(crossinline block: (index: Int, item: ITEM) -> Embed.EmbedField){
        renderer = { it.map { block(it.index, it.value) } }
    }

    inline fun renderIndexed(title: String, crossinline block: (index: Int, item: ITEM) -> String){
        renderer = {
            listOf(embedFieldBuilder {
                this.title = title
                description = it.joinToString(separator = "\n") { block(it.index, it.value) }
            })
        }
    }

    inline fun render(crossinline block: (ITEM) -> Embed.EmbedField){
        renderer = { it.map { block(it.value) } }
    }

    inline fun render(title: String, crossinline block: (ITEM) -> String){
        renderer = {
            listOf(embedFieldBuilder {
                this.title = title
                description = it.joinToString(separator = "\n") { block(it.value) }
            })
        }
    }

    override fun addControl(controlTrigger: String, block: (ListField<ITEM>, event: ReactionAddEvent) -> Unit) {
        controls.add(controlTrigger to block)
    }

    override fun removeControl(controlTrigger: String) {
        controls.removeIf { it.first == controlTrigger }
    }

    fun build(items: ObservableList<ITEM>, observables: Iterable<Observable>) : ListField<ITEM>{
        return ListField(items, renderer ?: noRenderer(), navigationType, itemsOnScreen, observables)
    }

    private fun noRenderer() : Nothing {
        throw IllegalArgumentException("no renderer supplied")
    }
}


class ListField<ITEM>(
        private val items: ObservableList<ITEM>,
        private val renderer: ((Iterable<IndexedValue<ITEM>>) -> Iterable<Embed.EmbedField>),
        private val navigationType : NavigationType,
        itemsOnScreen : Int,
        observables: Iterable<Observable>
) : ScreenItem, ObservableScreenItem(observables) {
    var itemsOnScreen : Int = itemsOnScreen
    set(value) {
        field = value
        invalidate()
    }

    private var currentIndex : Int = 0
    private val maxIndex : Int get() = items.chunked(itemsOnScreen).count()

    private val currentPage : Iterable<Embed.EmbedField> inline get() {
        return if(items.isEmpty()) emptyList()
        else items.withIndex().chunked(itemsOnScreen) { renderer(it) }[currentIndex]
        }

    fun nextPage(){
        currentIndex = navigationType.nextIndex(currentIndex, 0, maxIndex)
        invalidate()
    }

    fun previousPage(){
        currentIndex = navigationType.previousIndex(currentIndex, 0, maxIndex)
        invalidate()
    }

    override fun render(): Iterable<Embed.EmbedField> = currentPage

}
interface ListBuilderApplicable{
    fun apply(listBuilder: ListBuilder<*>)
}

object Controlled : ListBuilderApplicable {

    override fun apply(listBuilder: ListBuilder<*>) {
        listBuilder.controls {
            forEmote(Emote.prev) {  screen, _ -> screen.previousPage() }
            forEmote(Emote.next) {  screen, _ -> screen.nextPage()  }
        }
    }

}

interface NavigationType : ListBuilderApplicable{

    fun nextIndex(current: Int, min: Int, max: Int) : Int
    fun previousIndex(current: Int, min: Int, max: Int) : Int

    override fun apply(listBuilder: ListBuilder<*>) {
        listBuilder.navigationType = this
    }

    companion object {
        val pageNavigation = object : NavigationType{
            override fun nextIndex(current: Int, min: Int, max: Int) = max(current + 1, max)
            override fun previousIndex(current: Int, min: Int, max: Int)  = min(current - 1, min)
        }

        val roundRobinNavigation = object : NavigationType{
            override fun nextIndex(current: Int, min: Int, max: Int): Int {
                val increment = current + 1
                return if(increment >= max) 0
                else increment
            }

            override fun previousIndex(current: Int, min: Int, max: Int): Int {
                val decrement = current - 1
                return if(decrement < min) max - 1
                else decrement
            }
        }
    }
}

inline fun<ITEM> ScreenBuilder.list(list: ObservableList<ITEM>, vararg properties: Observable, block: ListBuilder<ITEM>.() -> Unit){
    val builder = ListBuilder<ITEM>()
    builder.apply(block)
    val listField = builder.build(list, properties.asIterable())
    this.add(listField)
    builder.controls.forEach { control -> addControl(control.first) { _, event ->  control.second.invoke(listField, event) } }
}

inline fun<ITEM> ScreenBuilder.list(list: List<ITEM>, vararg properties: Observable, block: ListBuilder<ITEM>.() -> Unit) {
    list(list.observable, *properties, block =  block)
}
