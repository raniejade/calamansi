package calamansi.runtime.resource.source

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class JarFileSource(private val classLoader: ClassLoader) : FileSource {
    override fun getReader(path: String): InputStream {
        return classLoader.getResourceAsStream(path)
            ?: throw IOException("Unable to open '$path' for reading")
    }

    override fun getWriter(path: String, append: Boolean): OutputStream {
        throw NotImplementedError("Writing not supported for jar file paths")
    }
}