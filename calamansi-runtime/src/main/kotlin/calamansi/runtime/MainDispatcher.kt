package calamansi.runtime

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.newSingleThreadContext
import java.util.concurrent.LinkedBlockingQueue
import kotlin.coroutines.CoroutineContext

object ScriptDispatcher : CoroutineDispatcher() {
    private val delegate = newSingleThreadContext("calamansi-script")

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return delegate.isDispatchNeeded(context)
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        delegate.dispatch(context, block)
    }
}

object ResourceDispatcher : CoroutineDispatcher() {
    private val delegate = newFixedThreadPoolContext(2, "calamansi-resource")

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return delegate.isDispatchNeeded(context)
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        delegate.dispatch(context, block)
    }
}

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