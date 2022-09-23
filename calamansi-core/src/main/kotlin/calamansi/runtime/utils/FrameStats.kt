package calamansi.runtime.utils

class FrameStats(private val size: Int) {
    private var total = 0f
    private var index = 0
    private val samples = FloatArray(size) { 0f }

    fun addSample(sample: Float) {
        total -= samples[index]
        samples[index] = sample
        total += sample
        if (++index == size) {
            index = 0 // cheaper than modulus
        }
    }

    val avgFrameTime: Float
        get() = total / size

    val avgFps: Float
        get() = 1000f / avgFrameTime
}