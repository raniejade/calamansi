package calamansi.runtime

import calamansi.runtime.registry.RuntimeRegistry
import java.util.ServiceLoader
import kotlin.jvm.optionals.getOrNull

class Runtime {
    private val entryLoader = ServiceLoader.load(Entry::class.java)
    private val registry = RuntimeRegistry()

    @OptIn(ExperimentalStdlibApi::class)
    fun execute() {
        val entry = entryLoader.findFirst().getOrNull()
        if (entry != null) {
            execute(entry)
        } else {
            println("No entry implementation loaded")
        }
    }

    private fun execute(entry: Entry) {
        println("Starting bootstrap: $entry")
        entry.bootstrap(registry)
    }
}

fun main(vararg args: String) {
    Runtime().execute()
}