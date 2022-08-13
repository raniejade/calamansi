package calamansi.runtime.helpers

import calamansi.runtime.registry.ComponentDefinition
import calamansi.runtime.registry.RuntimeRegistry
import calamansi.runtime.registry.ScriptDefinition

private val AUTO_REGISTER_COMPONENTS = listOf(
    TestComponent
)

private val AUTO_REGISTER_SCRIPTS = listOf(
    TestScript
)

fun RuntimeRegistry.registerTestDefinitions(
    components: List<ComponentDefinition<*>>,
    scripts: List<ScriptDefinition<*>>
) {
    components.forEach(this::registerComponent)
    scripts.forEach(this::registerScript)
}

fun RuntimeRegistry.autoRegisterTestDefinitions() {
    registerTestDefinitions(AUTO_REGISTER_COMPONENTS, AUTO_REGISTER_SCRIPTS)
}