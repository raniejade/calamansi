package calamansi.runtime

import calamansi.runtime.registry.Registry

interface Entry {
    fun bootstrap(registry: Registry)
}