package calamansi.runtime.utils

class FrameStats(private val size: Int) {
    private var total = 0f
    private var index = 0
    private val samples = FloatArray(size) { 0f }
    private var _frameNo = 0L

    fun frame(delta: Float) {
        total -= samples[index]
        samples[index] = delta
        total += delta
        if (++index == size) {
            index = 0 // cheaper than modulus
        }
        _frameNo++
    }

    val frameNo: Long
        get() = _frameNo

    val avgFrameTime: Float
        get() = total / size

    val avgFps: Float
        get() = 1000f / avgFrameTime
}