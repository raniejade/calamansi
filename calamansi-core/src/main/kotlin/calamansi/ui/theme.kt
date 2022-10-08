package calamansi.ui

import calamansi.gfx.Color
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

@PublishedApi
internal sealed class ThemeItem(val name: String)
internal class FloatThemeItem(name: String, val value: Float) : ThemeItem(name)
internal class ColorThemeItem(name: String, val color: Color) : ThemeItem(name)
internal class FontThemeItem(name: String, val font: Font) : ThemeItem(name)
internal class StyledBoxThemeItem(name: String, val styledBox: StyledBox) : ThemeItem(name)

@PublishedApi
internal class ThemeElement(
    val type: KClass<out CanvasElement>,
    private val items: Map<String, ThemeItem>,
    val parent: KClass<out CanvasElement>?
) {
    fun getItem(name: String): ThemeItem? {
        return items[name]
    }
}

class Theme internal constructor(private val elements: Map<KClass<out CanvasElement>, ThemeElement>) {
    fun getFloat(element: KClass<out CanvasElement>, name: String): Float {
        return (checkNotNull(getItem(element, name)) as FloatThemeItem).value
    }

    fun getColor(element: KClass<out CanvasElement>, name: String): Color {
        return (checkNotNull(getItem(element, name)) as ColorThemeItem).color
    }

    fun getFont(element: KClass<out CanvasElement>, name: String): Font {
        return (checkNotNull(getItem(element, name)) as FontThemeItem).font
    }

    fun getStyledBox(element: KClass<out CanvasElement>, name: String): StyledBox {
        return (checkNotNull(getItem(element, name)) as StyledBoxThemeItem).styledBox
    }

    private fun getItem(element: KClass<out CanvasElement>, name: String): ThemeItem? {
        val themeElement = elements.getValue(element)
        var item = themeElement.getItem(name)
        if (item == null && themeElement.parent != null) {
            item = getItem(themeElement.parent, name)
        }
        return item
    }

    companion object {
        private lateinit var current: Theme

        internal fun setCurrent(theme: Theme) {
            current = theme
        }

        fun getCurrent() = current

        fun styledBox(): ReadWriteProperty<CanvasElement, StyledBox> {
            return ThemedProperty(StyledBoxThemeItem::class)
        }

        fun font(): ReadWriteProperty<CanvasElement, Font> {
            return ThemedProperty(FontThemeItem::class)
        }

        fun float(): ReadWriteProperty<CanvasElement, Float> {
            return ThemedProperty(FloatThemeItem::class)
        }

        fun color(): ReadWriteProperty<CanvasElement, Color> {
            return ThemedProperty(ColorThemeItem::class)
        }

        internal class ThemedProperty<T : Any>(val type: KClass<out ThemeItem>) :
            ReadWriteProperty<CanvasElement, T> {
            private sealed class Value {
                class Override<T : Any>(val value: T) : Value()
                class FromTheme<T : Any>(val value: T) : Value()
                object None : Value()
            }

            private lateinit var memoizedTheme: Theme
            private var value: Value = Value.None

            override fun getValue(thisRef: CanvasElement, property: KProperty<*>): T {
                if (value is Value.None || (themeChanged() && value !is Value.Override<*>)) {
                    memoizedTheme = getCurrent()
                    value = when (type) {
                        FloatThemeItem::class -> Value.FromTheme(memoizedTheme.getFloat(thisRef::class, property.name))
                        ColorThemeItem::class -> Value.FromTheme(memoizedTheme.getColor(thisRef::class, property.name))
                        FontThemeItem::class -> Value.FromTheme(memoizedTheme.getFont(thisRef::class, property.name))
                        StyledBoxThemeItem::class -> Value.FromTheme(
                            getCurrent().getStyledBox(
                                thisRef::class,
                                property.name
                            )
                        )

                        else -> throw AssertionError("Unsupported theme item type: $type")
                    }
                }
                if (value is Value.FromTheme<*>) {
                    return (value as Value.FromTheme<*>).value as T
                }
                return (value as Value.Override<*>).value as T
            }

            override fun setValue(thisRef: CanvasElement, property: KProperty<*>, value: T) {
                this.value = Value.Override(value)
            }

            private fun themeChanged(): Boolean {
                return !::memoizedTheme.isInitialized || memoizedTheme !== getCurrent()
            }
        }
    }
}

class ElementBuilder {
    private val items = mutableMapOf<String, ThemeItem>()

    fun float(name: String, value: Float) {
        putItem(name, FloatThemeItem(name, value))
    }

    fun color(name: String, color: Color) {
        putItem(name, ColorThemeItem(name, color))
    }

    fun font(name: String, font: Font) {
        putItem(name, FontThemeItem(name, font))
    }

    fun styledBox(name: String, styledBox: StyledBox) {
        putItem(name, StyledBoxThemeItem(name, styledBox))
    }

    @PublishedApi
    internal fun build(): Map<String, ThemeItem> {
        return items.toMap()
    }

    private fun putItem(name: String, item: ThemeItem) {
        check(!items.containsKey(name)) { "Item $name already exists." }
        items[name] = item
    }
}

class ThemeBuilder {
    @PublishedApi
    internal val elements = mutableMapOf<KClass<out CanvasElement>, ThemeElement>()

    inline fun <reified T : CanvasElement> element(
        parent: KClass<out CanvasElement>? = null,
        body: ElementBuilder.() -> Unit
    ): ThemeBuilder {
        val type = T::class
        check(!elements.containsKey(type)) { "Element $type already exists." }
        val builder = ElementBuilder()
        builder.body()
        elements[type] = ThemeElement(type, builder.build(), parent)
        return this
    }

    fun build(): Theme {
        return Theme(elements.toMap())
    }
}