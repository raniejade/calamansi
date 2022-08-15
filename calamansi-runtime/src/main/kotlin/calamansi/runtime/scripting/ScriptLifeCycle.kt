package calamansi.runtime.scripting

sealed class ScriptLifeCycle {
    object Attached : ScriptLifeCycle()
    object Detached : ScriptLifeCycle()
    class Update(val delta: Float) : ScriptLifeCycle()
}