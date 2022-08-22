package calamansi.math

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.infra.Blackhole

open class Transform2dBenchmark {
    @Benchmark
    fun compose(bh: Blackhole) {
        bh.consume(Transform2d().translate(10f, -12f).transform(Vector2f(1f, 2f)))
    }
}