package calamansi.math

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.infra.Blackhole

open class Transform3dBenchmark {
    @Benchmark
    fun compose(bh: Blackhole) {
        bh.consume(
            Transform3d().translate(10f, -12f, 0.2f).transform(Vector3f(1f, 2f, -5f))
        )
    }
}