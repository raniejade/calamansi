package calamansi.runtime.serializer

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

class Serializer(module: SerializersModule) {
    private val json = Json {
        serializersModule = module

        // don't encode nulls
        explicitNulls = false

        // make serialized files somewhat readable via diffs
        prettyPrint = true
        prettyPrintIndent = "  "
    }
}