package calamansi.runtime.helpers

import calamansi.Script
import calamansi.Component
import calamansi.RequiredComponents
import calamansi.Property

class TestComponent : Component {
    @Property
    var int: Int = 0
}

@RequiredComponents([TestComponent::class])
class ComponentWithDependency : Component

@RequiredComponents([ComponentWithDependency::class])
class ComponentWithNestedDependency : Component

inline fun <reified T : Component> dataType(): String =
    "calamansi._gen.${checkNotNull(T::class.qualifiedName).replace(".", "_")}Data"

class TestScript : Script() {
}