package calamansi.runtime.scripting

import calamansi.event.Event

sealed class ScriptLifeCycle {
    object Attached : ScriptLifeCycle()
    object Detached : ScriptLifeCycle()
    class HandleEvent(val event: Event) : ScriptLifeCycle()
    class Update(val delta: Float) : ScriptLifeCycle()
}