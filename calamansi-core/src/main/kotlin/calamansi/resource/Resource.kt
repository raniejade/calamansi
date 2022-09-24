package calamansi.resource

abstract class Resource {
    @PublishedApi
    internal lateinit var _path: String

    @PublishedApi
    internal var _index: Int = 0

    inline val path: String
        get() = _path

    inline val index: Int
        get() = _index
}