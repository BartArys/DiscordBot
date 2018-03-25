package com.numbers.discordbot.commands

import com.numbers.discordbot.dsl.*
import com.numbers.discordbot.dsl.gui.builder.Emote
import com.numbers.discordbot.dsl.gui2.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import sx.blah.discord.handle.obj.IUser

@CommandsSupplier
fun pollCommand() = commands {

    command("£ poll {question} : {options}"){
        arguments(words("question"), Sequence.of(words("option"), withKey = "options", separatedBy = literal("|")))

        execute {
            respondScreen {
                val poll = PollResult()

                property(deletable)

                title = args("question")

                val list = args.listOf<String>("options").orEmpty()

                list(list){
                    properties(Controlled, NavigationType.roundRobinNavigation)

                    renderIndexed("options") { index, item -> "$index: $item" }
                }

                list(poll.observableResult) {

                    render("results") {
                        "${it.first.name} : ${list[it.second]}"
                    }
                }

                controls {
                    for(i in 0 until list.size){
                        when (i) {
                            0 -> forEmote(Emote.zero)   { _ , event -> poll.addVote(event.user, 0) }
                            1 -> forEmote(Emote.one)    { _ , event -> poll.addVote(event.user, 1) }
                            2 -> forEmote(Emote.two)    { _ , event -> poll.addVote(event.user, 2) }
                            3 -> forEmote(Emote.three)  { _ , event -> poll.addVote(event.user, 3) }
                            4 -> forEmote(Emote.four)   { _ , event -> poll.addVote(event.user, 4) }
                            5 -> forEmote(Emote.five)   { _ , event -> poll.addVote(event.user, 5) }
                            6 -> forEmote(Emote.six)    { _ , event -> poll.addVote(event.user, 6) }
                            7 -> forEmote(Emote.seven)  { _ , event -> poll.addVote(event.user, 7) }
                            8 -> forEmote(Emote.eight)  { _ , event -> poll.addVote(event.user, 8) }
                            9 -> forEmote(Emote.nine)   { _ , event -> poll.addVote(event.user, 9) }
                        }
                    }
                }

            }
        }

    }
}

data class PollResult(private val votes : MutableMap<IUser,Int> = mutableMapOf()){
    fun addVote(user: IUser, voteResult : Int){
        votes[user] = voteResult
        observableResult.removeIf { it.first.longID == user.longID }
        observableResult.add(user to voteResult)
    }

    val observableResult : ObservableList<Pair<IUser, Int>> = FXCollections.observableArrayList()
}