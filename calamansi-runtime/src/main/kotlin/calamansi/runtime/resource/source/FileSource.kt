package calamansi.runtime.resource.source

import java.io.InputStream
import java.io.OutputStream

interface FileSource {
    fun getReader(path: String): InputStream
    fun getWriter(path: String, append: Boolean): OutputStream
}