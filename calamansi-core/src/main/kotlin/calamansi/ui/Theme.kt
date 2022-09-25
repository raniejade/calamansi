package calamansi.ui

import calamansi.gfx.Color
import calamansi.meta.CalamansiInternal
import calamansi.node.Node
import calamansi.resource.Resource
import kotlin.reflect.KClass

// TODO: how to support nested resources
class Theme(
    private val items: Map<KClass<out CanvasElement>, Map<String, Any>>
) : Resource() {

    fun getColor(type: KClass<out CanvasElement>, name: String): Color {
        return getItem(type, name)
    }

    fun getConstant(type: KClass<out CanvasElement>, name: String): Float {
        return getItem(type, name)
    }

    fun getFont(type: KClass<out CanvasElement>, name: String): Font {
        return getItem(type, name)
    }

    fun getStyledBox(type: KClass<out CanvasElement>, name: String): StyledBox {
        return getItem(type, name)
    }

    private fun <T> getItem(type: KClass<out CanvasElement>, name: String): T {
        check(items.containsKey(type) && items.getValue(type).containsKey(name)) { "Missing item for type: $type, name: $name"}
        return items.getValue(type).getValue(name) as T
    }
}

@CalamansiInternal
class ThemeBuilder {
    private val items = mutableMapOf<KClass<out CanvasElement>, MutableMap<String, Any>>()

    fun addColor(type: KClass<out CanvasElement>, name: String, value: Color): ThemeBuilder {
        addItem(type, name, value)
        return this
    }

    fun addConstant(type: KClass<out CanvasElement>, name: String, value: Float): ThemeBuilder {
        addItem(type, name, value)
        return this
    }

    fun addFont(type: KClass<out CanvasElement>, name: String, value: Font): ThemeBuilder {
        addItem(type, name, value)
        return this
    }

    fun addStyledBox(type: KClass<out CanvasElement>, name: String, value: StyledBox): ThemeBuilder {
        addItem(type, name, value)
        return this
    }

    private fun addItem(type: KClass<out CanvasElement>, name: String, value: Any) {
        items.getOrPut(type) { mutableMapOf() }[name] = value
    }

    fun build(): Theme {
        return Theme(items.mapValues { it.value.toMap() }.toMap())
    }
}