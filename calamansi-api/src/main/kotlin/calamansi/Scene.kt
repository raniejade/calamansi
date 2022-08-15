package calamansi

import calamansi.resource.Resource

interface Scene : Resource {
    fun create(): Node?
}