package calamansi.harness

import calamansi.component.Component
import calamansi.component.Property

enum class MyEnum {
    A,
    B,
    C,
}

class MyComponent : Component {
    @Property
    var someInt: Int = 2

    @Property
    var float: Float =  0f

    @Property
    var enum: MyEnum = MyEnum.A
}