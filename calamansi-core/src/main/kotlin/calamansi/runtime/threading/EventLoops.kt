package calamansi.runtime.threading

internal object EventLoops {
    val Main = BlockingEventLoop()
    val Script = FixedThreadPoolEventLoop(1, "calamansi-script")
    val Resource = FixedThreadPoolEventLoop(2, "calamansi-resource")
}