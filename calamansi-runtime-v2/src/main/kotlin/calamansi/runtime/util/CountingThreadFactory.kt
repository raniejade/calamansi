package calamansi.runtime.util

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

class CountingThreadFactory(private val baseName: String) : ThreadFactory {
    private val counter = AtomicInteger(0)
    override fun newThread(r: Runnable): Thread {
        val t = Thread(r)
        t.isDaemon = true
        t.name = "$baseName-${counter.getAndIncrement()}"
        return t
    }

}