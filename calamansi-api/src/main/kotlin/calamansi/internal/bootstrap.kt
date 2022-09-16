package calamansi.internal

import calamansi.meta.CalamansiInternal

@CalamansiInternal
interface Registry {
    fun registerNode(definition: NodeDefinition)
}

@CalamansiInternal
interface Bootstrap {
    fun init(registry: Registry)
}