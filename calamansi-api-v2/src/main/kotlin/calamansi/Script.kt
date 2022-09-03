package calamansi

import calamansi.event.Event

abstract class Script {
    /**
     * Invoked after a script is instantiated.
     */
    context(ExecutionContext) open fun onInit() = Unit

    /**
     * Invoked before the [owner][ExecutionContext.owner] of this script is destroyed.
     */
    context(ExecutionContext) open fun onFinalize() = Unit

    /**
     * Invoked after the [owner][ExecutionContext.owner] this script is added to the scene tree.
     */
    context(ExecutionContext) open fun onAttached() = Unit

    /**
     * Invoked before the [owner][ExecutionContext.owner] of this script is removed from the scene tree.
     */
    context(ExecutionContext) open fun onDetached() = Unit

    /**
     * Invoked every frame.
     *
     * @param delta elapsed time (in milliseconds) from the previous [onUpdate] invocation.
     */
    context(ExecutionContext) open fun onUpdate(delta: Long) = Unit

    /**
     * Invoked when an [event][Event] has occurred.
     */
    context(ExecutionContext) open fun onEvent(event: Event) = Unit
}