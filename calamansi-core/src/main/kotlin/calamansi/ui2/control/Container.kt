package calamansi.ui2.control

abstract class Container : Control() {
    public override val children: MutableList<Control>
        get() = super.children
}