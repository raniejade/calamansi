package calamansi.runtime.registry

interface Registry {
    fun registerComponent(definition: ComponentDefinition<*>)
    fun registerScript(definition: ScriptDefinition<*>)
}