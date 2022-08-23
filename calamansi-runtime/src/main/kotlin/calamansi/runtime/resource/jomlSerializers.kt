package calamansi.runtime.resource

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.FloatArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModuleBuilder
import org.joml.Matrix3x2f
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import java.nio.FloatBuffer

abstract class DelegatingFloatArraySerializer<T>(val expectedSize: Int) : KSerializer<T> {
    private val delegate = FloatArraySerializer()
    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun deserialize(decoder: Decoder): T {
        val array = decoder.decodeSerializableValue(delegate)
        check(array.size == expectedSize) { "Expected size was $expectedSize but got ${array.size}." }
        return toValue(array)
    }

    override fun serialize(encoder: Encoder, value: T) {
        val array = toArray(value)
        check(array.size == expectedSize) { "Expected size was $expectedSize but got ${array.size}." }
        encoder.encodeSerializableValue(delegate, array)
    }


    protected abstract fun toValue(array: FloatArray): T
    protected abstract fun toArray(value: T): FloatArray
}

class Vector2fSerializer : DelegatingFloatArraySerializer<Vector2f>(2) {
    override fun toValue(array: FloatArray): Vector2f {
        return Vector2f(array[0], array[1])
    }

    override fun toArray(value: Vector2f): FloatArray {
        return floatArrayOf(value.x, value.y)
    }
}

class Vector3fSerializer : DelegatingFloatArraySerializer<Vector3f>(3) {
    override fun toValue(array: FloatArray): Vector3f {
        return Vector3f(array[0], array[1], array[2])
    }

    override fun toArray(value: Vector3f): FloatArray {
        return floatArrayOf(value.x, value.y, value.z)
    }
}

class Matrix4fSerializer : DelegatingFloatArraySerializer<Matrix4f>(4 * 4) {
    override fun toValue(array: FloatArray): Matrix4f {
        return Matrix4f(FloatBuffer.wrap(array))
    }

    override fun toArray(value: Matrix4f): FloatArray {
        val result = FloatArray(expectedSize)
        value.get(result)
        return result
    }
}

class Matrix3x2fSerializer : DelegatingFloatArraySerializer<Matrix3x2f>(3 * 2) {
    override fun toValue(array: FloatArray): Matrix3x2f {
        return Matrix3x2f(FloatBuffer.wrap(array))
    }

    override fun toArray(value: Matrix3x2f): FloatArray {
        val result = FloatArray(expectedSize)
        value.get(result)
        return result
    }
}

fun SerializersModuleBuilder.registerJomlSerializers() {
    contextual(Vector2f::class, Vector2fSerializer())
    contextual(Vector3f::class, Vector3fSerializer())
    contextual(Matrix4f::class, Matrix4fSerializer())
    contextual(Matrix3x2f::class, Matrix3x2fSerializer())
}