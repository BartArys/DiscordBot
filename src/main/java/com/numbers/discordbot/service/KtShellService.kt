package com.numbers.discordbot.service

import com.numbers.discordbot.dsl.CommandContext

interface KtShellService{

    fun executeForContext(context: CommandContext, code: String) : Any?

}

internal class InternalKtShellService : KtShellService{
    private val engine = org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory().scriptEngine
    private val bindings = engine.createBindings()

    override fun executeForContext(context: CommandContext, code: String) : Any? {
        return try {
            bindings["context"] = context
            engine.eval("""var context = bindings["context"] as ${context::class.java.canonicalName}""", bindings)
            engine.eval(code, bindings)
        }catch (t: Throwable){
            t.message ?: "something went wrong and this asshole didn't give an error message $t"
        }
    }


}