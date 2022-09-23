package calamansi.internal

import calamansi.meta.CalamansiInternal

@CalamansiInternal
interface Registry {
    fun registerNode(definition: NodeDefinition)
}