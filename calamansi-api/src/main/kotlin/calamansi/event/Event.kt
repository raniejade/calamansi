package calamansi.event

abstract class Event {
    private var consumed = false

    fun consume() {
        check(!consumed) { "Event already consumed." }
        consumed = true
    }

    fun isConsumed() = consumed
}