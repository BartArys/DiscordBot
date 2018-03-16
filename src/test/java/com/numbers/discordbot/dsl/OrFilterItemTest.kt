package com.numbers.discordbot.dsl

import kotlinx.coroutines.experimental.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

internal class OrFilterItemTest {

    private var arg1: Argument = mock(Argument::class.java)
    private var arg2: Argument = mock(Argument::class.java)

    private var orFilter: OrFilterItem

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
        val fakeArg = object: Argument{

            override val minLength: Int
                get() = 1

            override val maxLength: Int
                get() = 2

            override fun toKeyedArguments(): Map<String, Argument> {
                return  emptyMap()
            }

            override fun apply(tokens: List<Token>, event: MessageReceivedEvent, services: Services, args: CommandArguments): Boolean {
                return true
            }
        }

        val orFilter = OrFilterItem(fakeArg, arg2)

        val acceptable = runBlocking { orFilter.apply(tokens, event, services, args) }
        assertEquals(true, acceptable)
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