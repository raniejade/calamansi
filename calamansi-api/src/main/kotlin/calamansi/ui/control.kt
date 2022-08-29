package calamansi.ui

import calamansi.meta.CalamansiInternal

interface MutableState<T> {
    val value: T
    val setter: (T) -> Unit

    operator fun component1(): T
    operator fun component2(): (T) -> Unit
}

typealias Body = context(Scope) () -> Unit
interface Scope {
    fun container(body: Body)
    fun text(text: String)
    fun button(text: String, onClick: () -> Unit = {})

    fun <T> state(getter: () -> T): T
    fun <T> mutableState(initial: () -> T): MutableState<T>
    fun control(cb: Body)

    @CalamansiInternal
    fun build(body: Body)
}

context(Scope)
abstract class Control(body: Body) {
    init {
        @OptIn(CalamansiInternal::class)
        build(body)
    }
}

context(Scope)
class CheckBox(checked: Boolean, setChecked: (Boolean) -> Unit) : Control({
})

context(Scope)
class Counter(count: Int, setCounter: (Int) -> Unit) : Control({
    text("Clicked $count times!")
    button("Click me!") { setCounter(count + 1) }
})

context(Scope)
class MyControl : Control({
    val (counter, setCounter) = mutableState { 0 }
    Counter(counter, setCounter)
})