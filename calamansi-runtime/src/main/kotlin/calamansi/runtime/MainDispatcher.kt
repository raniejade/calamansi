package calamansi.runtime

import kotlinx.coroutines.CoroutineDispatcher
import java.util.concurrent.LinkedBlockingQueue
import kotlin.coroutines.CoroutineContext

object MainDispatcher : CoroutineDispatcher() {
    private lateinit var thread: Thread
    private val queue = LinkedBlockingQueue<Runnable>()
    private var shouldShutdown = false

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return thread !== Thread.currentThread()
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        queue.put(block)
    }

    fun shutdown() {
        shouldShutdown = true
    }

    fun loop() {
        thread = Thread.currentThread()

        while (!shouldShutdown) {
            queue.poll()?.run()
        }
    }
}