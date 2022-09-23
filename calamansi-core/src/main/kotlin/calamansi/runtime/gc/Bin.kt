package calamansi.runtime.gc

import java.lang.ref.Cleaner

internal object Bin {
    private val cleaner = Cleaner.create()

    fun register(ref: Any, cleanup: () -> Unit) {
        cleaner.register(ref, cleanup)
    }
}