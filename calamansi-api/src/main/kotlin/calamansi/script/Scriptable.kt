package calamansi.script

import calamansi.component.Component
import calamansi.component.Property
import kotlin.reflect.KClass

class Scriptable : Component {
    @Property
    var script: KClass<out Script>? = null
}