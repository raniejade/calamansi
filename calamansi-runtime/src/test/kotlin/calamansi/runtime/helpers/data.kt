package calamansi.runtime.helpers

import calamansi.Script
import calamansi.Component
import calamansi.Dependencies
import calamansi.Property

class TestComponent : Component {
    @Property
    var int: Int = 0
}

@Dependencies([TestComponent::class])
class ComponentWithDependency : Component

@Dependencies([ComponentWithDependency::class])
class ComponentWithNestedDependency : Component

inline fun <reified T : Component> dataType(): String =
    "calamansi._gen.${checkNotNull(T::class.qualifiedName).replace(".", "_")}Data"

class TestScript : Script() {
}