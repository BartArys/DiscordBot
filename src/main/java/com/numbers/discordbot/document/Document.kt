package com.numbers.discordbot.document

/*
data class Document(val description: String, val permissions: List<String>,  val functions: List<Function>){
    companion object {
        fun from(guards: Guards, permissions: Permissions?) : Document {
            val description = guards.description
            val functions = guards.guards.map {
                if(it.params.isEmpty()){
                    Function(it.format, emptyList())
                }else{
                    val args = GuardParser().parse(it)().filterBuilder.items.flatMap { it.flatMap() }.mapNotNull { it as? ArgumentFilterItem }

                    Function(it.format, it.params.mapIndexed { counter, param -> Argument(param.type, it.description, args[counter].key) })

                }
            }
            val permissions = permissions?.permission?.map { it.name.toLowerCase() }.orEmpty()
            
            return Document(description, permissions, functions)
        }
    }
}

fun Document.toEmbeds() : Iterable<Embed.EmbedField>{
    val description = Embed.EmbedField("description: ", "```\n${this.description.trimIndent()}```", false)

    val permissions = this.permissions
            .joinToString(separator = " , ", prefix = "[  ", postfix = " ]")
            .let { if(it.isEmpty()) "none" else it }
            .let { Embed.EmbedField("permissions:", it, false) }

    val functions = functions.flatMap { it.toEmbeds() }

    val list = mutableListOf(description, permissions)
    list.addAll(functions)

    return list
}

data class Function(val description: String, val arguments: List<Argument>)

fun Function.toEmbeds() : Iterable<Embed.EmbedField>{
    return this.arguments.map {Embed.EmbedField("${it.key} : ${it.type.name.toLowerCase()}", it.description, false) }

}

data class Argument(val type: ArgumentType, val description: String, val key: String)

*/
