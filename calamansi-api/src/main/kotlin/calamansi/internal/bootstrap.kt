package calamansi.internal

import calamansi.Script
import calamansi.meta.CalamansiInternal
import kotlin.reflect.KClass

@CalamansiInternal
interface Registry {
    fun registerScript(type: KClass<out Script>, definition: ScriptDefinition)
}

@CalamansiInternal
interface Bootstrap {
    fun init(registry: Registry)
}