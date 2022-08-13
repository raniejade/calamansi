package calamansi.runtime

import calamansi.Script
import calamansi.runtime.registry.RuntimeRegistry
import kotlin.reflect.KClass

class ScriptManager(private val registry: RuntimeRegistry) {
    fun createScript(script: KClass<out Script>): Script {
        return registry.getScriptDefinition(script).create()
    }

    fun createScriptByQualifiedName(qualifiedName: String): Script {
        return registry.getScriptDefinitionByQualifiedName(qualifiedName).create()
    }
}