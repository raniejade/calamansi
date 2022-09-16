package calamansi.node

sealed interface NodeRef {
    fun resolve(node: Node): Node?

    interface Descendant : NodeRef
    interface Child : NodeRef
    interface Sibling : NodeRef
}