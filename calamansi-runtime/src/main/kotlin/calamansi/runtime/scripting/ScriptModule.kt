package calamansi.runtime.scripting

import calamansi.ExecutionContext
import calamansi.Node
import calamansi.Scene
import calamansi.Script
import calamansi.input.InputContext
import calamansi.input.InputState
import calamansi.input.Key
import calamansi.input.MouseButton
import calamansi.logging.Logger
import calamansi.resource.Resource
import calamansi.resource.ResourceRef
import calamansi.runtime.NodeImpl
import calamansi.runtime.RuntimeModule
import calamansi.runtime.SceneModule
import calamansi.runtime.module.Handle
import calamansi.runtime.module.Module
import calamansi.runtime.registry.RegistryModule
import calamansi.runtime.resource.ResourceModule
import java.lang.ref.Cleaner
import java.lang.ref.WeakReference
import java.util.*
import kotlin.reflect.KClass

class ScriptModule : Module() {
    private val cleaner = Cleaner.create()

    private data class ScriptMetadata(val instance: Script, val owner: WeakReference<Node>)

    private val scripts = mutableMapOf<Handle, ScriptMetadata>()

    override fun start() {
        logger.info { "Script module started." }
    }

    fun createScript(script: String, owner: Node): Handle {
        val registryModule = getModule<RegistryModule>()
        val metadata = ScriptMetadata(registryModule.createScriptInstance(script), WeakReference(owner))
        val scriptHandle = Handle(UUID.randomUUID().toString())
        scripts[scriptHandle] = metadata

        cleaner.register(owner) {
            checkNotNull(scripts.remove(scriptHandle))
        }
        return scriptHandle
    }

    fun getScriptInstance(script: Handle): Script {
        return checkNotNull(scripts[script]).instance
    }

    fun invokeLifeCycle(script: Handle, lifeCycle: ScriptLifeCycle) {
        val runtimeModule = getModule<RuntimeModule>()
        val (scriptInstance, owner) = checkNotNull(scripts[script])
        val executionContext = object : ExecutionContext, InputContext by runtimeModule {
            override val Script.owner: Node
                get() = checkNotNull(owner.get())

            override fun Node(name: String, script: KClass<out Script>?): Node {
                return NodeImpl(name, script?.let { it.qualifiedName })
            }

            override fun setCurrentScene(scene: ResourceRef<Scene>) {
                getModule<SceneModule>().setCurrentScene(scene)
            }

            override fun <T : Resource> loadResource(resource: String): ResourceRef<T> {
                return getModule<ResourceModule>().loadResource(resource) as ResourceRef<T>
            }

            override fun exit(exitCode: Int) {
                getModule<RuntimeModule>().requestExit(exitCode)
            }

            override val logger: Logger by lazy {
                getLogger(scriptInstance::class)
            }
        }

        with(executionContext) {
            when (lifeCycle) {
                ScriptLifeCycle.Attached -> scriptInstance.attached()
                ScriptLifeCycle.Detached -> scriptInstance.detached()
                is ScriptLifeCycle.Update -> scriptInstance.update(lifeCycle.delta)
            }
        }
    }

    override fun shutdown() {
        logger.info { "Script module shutting down." }
    }
}