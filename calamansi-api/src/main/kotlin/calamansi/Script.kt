package calamansi

abstract class Script {
    context(ExecutionContext) open fun setup() = Unit
    context(ExecutionContext) open fun cleanup() = Unit
    context(ExecutionContext) open fun update(delta: Float) = Unit
}