package calamansi

import calamansi.resource.Resource

interface Scene : Resource {
    suspend fun create(preloadResources: Boolean = true): Node?
}