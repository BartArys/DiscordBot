package com.numbers.discordbot.service

interface SupplyService<T> {

    fun attachTo(target: T)

    fun detachFrom(target: T)

}