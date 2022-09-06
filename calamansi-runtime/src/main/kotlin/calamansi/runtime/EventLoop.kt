package calamansi.runtime

import calamansi.runtime.util.CountingThreadFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Supplier

sealed interface EventLoop {
    fun <T> schedule(task: Supplier<T>): CompletableFuture<T>
    fun <T> scheduleNow(task: Supplier<T>): T {
        return schedule(task).get()
    }
    fun shutdown()
}

class BlockingEventLoop : EventLoop {
    private data class Task<T>(val supplier: Supplier<T>, val result: CompletableFuture<Any?>)

    private val tasks = LinkedBlockingDeque<Task<*>>()
    private val stop = AtomicBoolean(false)

    override fun <T> schedule(task: Supplier<T>): CompletableFuture<T> {
        val result = CompletableFuture<Any?>()
        tasks.put(Task(task, result))
        return result as CompletableFuture<T>
    }

    fun run() {
        while (!stop.get()) {
            val task = tasks.poll()
            task?.result?.complete(task.supplier.get())
        }
    }

    override fun shutdown() {
        stop.compareAndSet(false, true)
    }
}

class FixedThreadPoolEventLoop(size: Int, baseName: String) : EventLoop {
    private val executor = Executors.newFixedThreadPool(size, CountingThreadFactory(baseName))

    override fun <T> schedule(task: Supplier<T>): CompletableFuture<T> {
        return CompletableFuture.supplyAsync(task, executor)
    }

    override fun shutdown() {
        executor.shutdown()
    }
}

