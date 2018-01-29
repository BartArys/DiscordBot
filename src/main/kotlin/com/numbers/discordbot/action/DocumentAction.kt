package com.numbers.discordbot.action

/*
class DocumentAction{

    @Guards("display documents",Guard("$ docs"))
    fun document(event: MessageReceivedEvent, documents: List<Document>){
        RequestBuffer.request {
            with(EmbedBuilder()){
                documents.first().toEmbeds().forEach { appendField(it) }
                event.channel.sendMessage(build())
            }
        }
    }

}
*/