package calamansi.runtime.helpers

import calamansi.runtime.RuntimeModule
import calamansi.runtime.SceneModule
import calamansi.runtime.logging.LogLevel
import calamansi.runtime.module.Module
import calamansi.runtime.registry.RegistryModule
import calamansi.runtime.resource.ResourceModule
import calamansi.runtime.resource.SceneLoader
import calamansi.runtime.resource.source.JarFileSource
import calamansi.runtime.resource.source.RelativeFileSource
import calamansi.runtime.scripting.ScriptModule
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

abstract class EngineTest {
    protected val registryModule = RegistryModule()
    protected val resourceModule = ResourceModule()
    protected val sceneModule = SceneModule()
    protected val scriptModule = ScriptModule()
    // protected val runtimeModule = RuntimeModule()

    private val modules = listOf(
        registryModule,
        resourceModule,
        sceneModule,
        scriptModule,
        // runtimeModule,
    )

    @BeforeTest
    fun start() {
        Module.configureLogging(LogLevel.WARN)
        modules.forEach(Module::start)
        registryModule.pushContext(this::class.java.classLoader)
        resourceModule.registerSource("test", RelativeFileSource("assets", JarFileSource(this::class.java.classLoader)))
        resourceModule.registerLoader(SceneLoader())
    }

    @AfterTest
    fun shutdown() {
        registryModule.popContext()
        modules.forEach(Module::start)
    }
}