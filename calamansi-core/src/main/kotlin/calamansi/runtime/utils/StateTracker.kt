package calamansi.runtime.utils

import kotlin.reflect.KMutableProperty0

internal class StateTracker private constructor(
    private val properties: List<KMutableProperty0<out Any?>>
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
        fun create(vararg props: KMutableProperty0<out Any?>): StateTracker {
            return StateTracker(props.toList())
        }
    }
}