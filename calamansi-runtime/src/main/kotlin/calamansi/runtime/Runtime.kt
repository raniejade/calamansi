package calamansi.runtime

import calamansi.runtime.logging.ConsoleLogger
import calamansi.runtime.logging.LogLevel
import calamansi.runtime.registry.RuntimeRegistry
import java.util.*
import kotlin.jvm.optionals.getOrNull

class Runtime {
    private val logger = ConsoleLogger(LogLevel.DEBUG)
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
        logger.info { "Starting bootstrap: $entry" }
        entry.bootstrap(registry)
        bootstrapInitialScene()
        loop()
    }

    private fun bootstrapInitialScene() {
    }

    private fun loop() {
        while (!shouldExit()) {
            // invoke queued attached callbacks

            // traverse scene graph and invoke scripts (DFS)

            // invoke queued detached callbacks
        }
    }

    private fun shouldExit(): Boolean {
        return false
    }
}

fun main(vararg args: String) {
    Runtime().execute()
}