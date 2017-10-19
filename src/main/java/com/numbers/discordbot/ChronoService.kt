package com.numbers.discordbot

import com.google.inject.Injector
import com.numbers.discordbot.chrono.TimedAction
import com.numbers.discordbot.loader.CommandLoader

class ChronoService {
        private val injector : Injector

        constructor(directory: String, injector: Injector){
            this.injector = injector
            print("CHECKING")
            CommandLoader().getClasses(TimedAction::class.java, directory).forEach{injector.getInstance(it) }
        }

}