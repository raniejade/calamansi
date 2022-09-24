package calamansi.runtime.resource

import calamansi.resource.Resource
import calamansi.runtime.service.Services
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.reflect.KClass

@Serializable
data class ResourceRefSurrogate(
    val path: String,
    val type: String,
    val index: Int,
)

class ResourceSerializer : KSerializer<Resource> {
    private val resourceService: ResourceService by Services.get()
    override val descriptor: SerialDescriptor = ResourceRefSurrogate.serializer().descriptor

    override fun deserialize(decoder: Decoder): Resource {
        val surrogate = decoder.decodeSerializableValue(ResourceRefSurrogate.serializer())
        val path = surrogate.path
        // TODO: fetch this from registry
        val type = Class.forName(surrogate.type).kotlin
        val index = surrogate.index
        return resourceService.loadResource(path, type as KClass<out Resource>, index)
    }

    override fun serialize(encoder: Encoder, value: Resource) {
        val surrogate = ResourceRefSurrogate(value.path, value::class.qualifiedName!!, value.index)
        encoder.encodeSerializableValue(ResourceRefSurrogate.serializer(), surrogate)
    }
}