package com.numbers.discordbot.service

import com.numbers.discordbot.dsl.CommandContext

interface KtShellService{

    fun executeForContext(context: CommandContext, code: String) : Any?

}

internal class InternalKtShellService : KtShellService{
    private val engine = org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory().scriptEngine
    private val bindings = engine.createBindings()

    override fun executeForContext(context: CommandContext, code: String) : Any? {
        bindings["context"] = context
        engine.eval("""var context = bindings["context"] as com.numbers.discordbot.dsl.CommandContext""", bindings)
        return engine.eval(code, bindings)
    }


}