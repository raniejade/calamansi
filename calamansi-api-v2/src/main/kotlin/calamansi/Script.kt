package calamansi

import calamansi.event.Event
import calamansi.logging.Logger
import calamansi.meta.CalamansiInternal

abstract class Script {
    @CalamansiInternal
    var _owner: Node? = null
        set(value) {
            check(field == null) { "_owner can only be set once." }
            field = value
        }

    @CalamansiInternal
    var _logger: Logger? = null
        set(value) {
            check(field == null) { "_logger can only be set once." }
            field = value
        }

    inline val owner: Node
        get() = @OptIn(CalamansiInternal::class) _owner!!

    inline val logger: Logger
        get() = @OptIn(CalamansiInternal::class) _logger!!

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