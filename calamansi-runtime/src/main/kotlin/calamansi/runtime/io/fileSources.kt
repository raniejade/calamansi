package calamansi.runtime.io

import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path

sealed interface FileSource {
    fun getReader(path: String): InputStream
    fun getWriter(path: String, append: Boolean): OutputStream
}

class JarFileSource(private val classLoader: ClassLoader) : FileSource {
    override fun getReader(path: String): InputStream {
        return classLoader.getResourceAsStream(path) ?: throw IOException("Unable to open '$path' for reading")
    }

    override fun getWriter(path: String, append: Boolean): OutputStream {
        throw NotImplementedError("Writing not supported for jar file paths")
    }
}

class DiskRootFileSource(private val root: Path) : FileSource {
    override fun getReader(path: String): InputStream {
        val file = resolve(path).toFile()
        if (!file.exists()) {
            throw IOException("'$path' does not exist")
        }

        return file.inputStream()
    }

    override fun getWriter(path: String, append: Boolean): OutputStream {
        val file = resolve(path).toFile()
        return FileOutputStream(file, append)
    }

    private fun resolve(path: String): Path {
        return root.resolve(path)
    }
}