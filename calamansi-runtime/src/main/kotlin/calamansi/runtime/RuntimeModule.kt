package calamansi.runtime

import calamansi.runtime.module.Module

class RuntimeModule : Module() {
    private var shouldExit = false
    private var exitCode = 0

    override fun start() {
        logger.info { "Runtime module started." }
    }

    fun getExitCode(): Int = exitCode

    fun shouldExit(): Boolean {
        return shouldExit
    }

    fun requestExit(exitCode: Int) {
        this.exitCode = exitCode
        shouldExit = true
    }

    override fun shutdown() {
        logger.info { "Runtime module shutting down." }
    }
}