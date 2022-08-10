package calamansi.runtime.io

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class FileSystem(private val dataFileSource: FileSource, private val userFileSource: FileSource) {
    private enum class Source(val prefix: String) {
        Data("res"), User("usr");

        companion object {
            fun fromPrefix(prefix: String): Source? {
                for (value in values()) {
                    if (value.prefix == prefix) {
                        return value
                    }
                }
                return null
            }
        }
    }

    private data class PathRef(val source: Source, val path: String) {
        companion object {
            private val regex = Regex("(?<source>\\w+):\\/\\/(?<path>.+)")
            fun from(path: String): PathRef {
                val match = regex.matchEntire(path) ?: throw IOException("Unable to parse path: $path")
                val source = Source.fromPrefix(checkNotNull(match.groups["source"]).value)
                    ?: throw IOException("Unable to determine source from path: $path")
                val path = checkNotNull(match.groups["path"]).value
                return PathRef(source, path)
            }
        }
    }

    fun getReader(path: String): InputStream {
        val ref = PathRef.from(path)
        return fileSourceFor(ref.source).getReader(ref.path)
    }

    fun getWriter(path: String, append: Boolean): OutputStream {
        val ref = PathRef.from(path)
        return fileSourceFor(ref.source).getWriter(ref.path, append)
    }

    private fun fileSourceFor(source: Source): FileSource {
        return when (source) {
            Source.Data -> dataFileSource
            Source.User -> userFileSource
        }
    }
}