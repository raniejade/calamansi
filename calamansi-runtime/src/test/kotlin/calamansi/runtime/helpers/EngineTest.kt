package calamansi.runtime.helpers

import calamansi.runtime.RuntimeModule
import calamansi.runtime.SceneModule
import calamansi.runtime.logging.LogLevel
import calamansi.runtime.logging.LoggerModule
import calamansi.runtime.module.Module
import calamansi.runtime.registry.RegistryModule
import calamansi.runtime.resource.ResourceModule
import calamansi.runtime.scripting.ScriptModule
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

abstract class EngineTest {
    protected val loggerModule = LoggerModule()
    protected val registryModule = RegistryModule()
    protected val resourceModule = ResourceModule()
    protected val sceneModule = SceneModule()
    protected val scriptModule = ScriptModule()
    protected val runtimeModule = RuntimeModule()

    private val modules = listOf(
        loggerModule,
        registryModule,
        resourceModule,
        sceneModule,
        scriptModule,
        runtimeModule,
    )

    @BeforeTest
    fun start() {
        loggerModule.configure(LogLevel.WARN)
        modules.forEach(Module::start)
        registryModule.pushContext(this::class.java.classLoader)
    }

    @AfterTest
    fun shutdown() {
        registryModule.popContext()
        modules.forEach(Module::start)
    }
}