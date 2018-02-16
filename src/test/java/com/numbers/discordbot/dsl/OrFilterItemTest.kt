package com.numbers.discordbot.dsl

import kotlinx.coroutines.experimental.runBlocking
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.mockito.Matchers
import org.mockito.Mockito
import org.mockito.Mockito.*
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

private fun <T> any(): T {
    Mockito.any<T>()
    return uninitialized()
}
private fun <T> uninitialized(): T = null as T

internal class OrFilterItemTest {

    var arg1: Argument = mock(Argument::class.java)
    var arg2: Argument = mock(Argument::class.java)

    var orFilter: OrFilterItem

    init{
        `when`(arg1.minLength).thenReturn(1)
        `when`(arg1.maxLength).thenReturn(2)

        `when`(arg2.minLength).thenReturn(3)
        `when`(arg2.maxLength).thenReturn(4)

        orFilter = OrFilterItem(arg1, arg2)
    }

    @Test
    fun minLengthShouldReturnMinChildren(){
        assertEquals(1, orFilter.minLength)
    }

    @Test
    fun maxLengthShouldReturnMaxChildren(){
        assertEquals(4, orFilter.maxLength)
    }

    @Test
    fun returnTrueWhenOneMatch(){
        val tokens = (0..1).map { mock(Token::class.java) }
        val event = mock(MessageReceivedEvent::class.java)
        val services = mock(Services::class.java)
        val args = mock(CommandArguments::class.java)
        runBlocking {
            `when`(arg1.apply(Matchers.anyListOf(Token::class.java), Matchers.any(MessageReceivedEvent::class.java), Matchers.any(Services::class.java), Matchers.any(CommandArguments::class.java))).thenReturn(true)
        }

        val acceptable = runBlocking { orFilter.apply(tokens, event, services, args) }
        runBlocking { verify(arg1).apply(tokens, event, services, args) }
        assertEquals(false, acceptable)
    }

    @Test
    fun returnsFalseWhenNoneMatch(){
        val tokens = (1..10).map { mock(Token::class.java) }
        val event = mock(MessageReceivedEvent::class.java)
        val services = mock(Services::class.java)
        val args = mock(CommandArguments::class.java)
        val acceptable = runBlocking { orFilter.apply(tokens, event, services, args) }
        assertEquals(false, acceptable)
    }


    @Test
    fun emptyOrFilterShouldThrowError(){
        assertThrows(IllegalArgumentException::class.java) {
            OrFilterItem()
        }
    }


}