package calamansi.runtime.resource.source

import java.io.InputStream
import java.io.OutputStream

class RootedFileSource(private val root: String, private val fileSource: FileSource) : FileSource {
    override fun getReader(path: String): InputStream {
        return fileSource.getReader(computeActualPath(path))
    }

    override fun getWriter(path: String, append: Boolean): OutputStream {
        return fileSource.getWriter(computeActualPath(path), append)
    }

    private fun computeActualPath(path: String): String {
        return if (root != null) {
            "$root/$path"
        } else {
            path
        }
    }
}