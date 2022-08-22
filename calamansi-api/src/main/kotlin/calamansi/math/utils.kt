package calamansi.math

// not used
internal fun transformPoint(matrix: FloatArray, vec: FloatArray): FloatArray {
    val n = vec.size
    require(matrix.size == n * n)
    val dest = FloatArray(n) { 0f }

    var x = 0
    while (x < n) {
        val scalar = vec[x]
        var y = 0
        while (y < n) {
            dest[y] += scalar * matrix[(x * n) + y]
            y++
        }
        x++
    }

    return dest
}

// not used, manually written matrix multiplication is faster than doing loops
internal fun composeMatrices(a: FloatArray, b: FloatArray, n: Int, result: FloatArray) {
    require(a.size == n * n)
    require(a.size == b.size)
    require(result.size == a.size)

    var x = 0
    while (x < n) {
        var y = 0
        val basis = FloatArray(n)
        while (y < n) {
            basis[y] = b[(x * n) + y]
            y++
        }
        val transformedBasis = transformPoint(a, basis)
        y = 0
        while (y < n) {
            result[(x * n) + y] = transformedBasis[y]
            y++
        }
        x++
    }
}