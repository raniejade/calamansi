package calamansi.runtime

import calamansi.ExecutionContext
import calamansi.Node
import calamansi.Scene
import calamansi.Script
import calamansi.logging.Logger
import calamansi.resource.ResourceRef
import java.lang.ref.WeakReference
import java.util.*
import kotlin.reflect.KClass

class ExecutionContextImpl(
    override val logger: Logger,
    private val componentManager: ComponentManager,
    private val scriptManager: ScriptManager
) : ExecutionContext {
    // use weak map here so key can be gc-ed
    // value is a weak ref as well since node keeps a strong reference to the script (key)
    // the only time script can be gc-ed is when node is gc-ed.
    private val owners = WeakHashMap<Script, WeakReference<NodeImpl>>()

    override val Script.owner: Node
        get() = checkNotNull(owners[this]?.get()) { "No owner found for script: $this" }

    override fun Node(name: String, script: KClass<out Script>?): Node {
        val instance = script?.let { scriptManager.createScript(it) }
        return NodeImpl(name, componentManager, instance).also {
            if (instance == null) {
                return@also
            }

            owners[instance] = WeakReference(it)
        }
    }

    override fun setCurrentScene(scene: ResourceRef<Scene>) {
        TODO("Not yet implemented")
    }
}