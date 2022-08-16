package calamansi.runtime.helpers

import calamansi.Script
import calamansi.component.Component
import calamansi.component.Property

class TestComponent : Component {
    @Property
    var int: Int = 0
}

inline fun <reified T : Component> dataType(): String =
    "calamansi._gen.${checkNotNull(T::class.qualifiedName).replace(".", "_")}Data"

class TestScript : Script() {
}