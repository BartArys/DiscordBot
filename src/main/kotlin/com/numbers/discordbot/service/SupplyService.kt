package com.numbers.discordbot.service

interface SupplyService<in T> {

    fun attachTo(target: T)

    fun detachFrom(target: T)

}