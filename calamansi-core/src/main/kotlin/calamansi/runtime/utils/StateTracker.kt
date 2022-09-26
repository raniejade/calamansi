package calamansi.runtime.utils

import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty0

internal class StateTracker private constructor(
    private val properties: List<KProperty0<out Any?>>
) {
    private var previousState = mutableListOf<Any?>()

    fun isDirty(): Boolean {
        val newState = properties.map { it.get() }
        if (previousState == newState) {
            return false
        }
        previousState.clear()
        previousState.addAll(newState)
        return true
    }

    companion object {
        fun create(vararg props: KProperty0<out Any?>): StateTracker {
            return StateTracker(props.toList())
        }
    }
}