package calamansi.runtime.helpers

import calamansi.Script
import calamansi.component.Component
import calamansi.runtime.registry.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.reflect.KClass

class TestComponent : Component {
    var int: Int = 0

    @Serializable
    data class Data(var int: Int) : ComponentData<TestComponent> {
        override val type: KClass<TestComponent>
            get() = TestComponent::class
    }

    companion object : ComponentDefinition<TestComponent> {
        override val dependencies: List<ComponentDefinition<*>>
            get() = emptyList()
        override val properties: List<Property<TestComponent, *>>
            get() = listOf(SimpleProperty(Int::class, "int", TestComponent::int))

        override fun toData(component: Component): ComponentData<*> {
            require(component is TestComponent)
            return Data(component.int)
        }

        override fun fromData(data: ComponentData<*>, component: Component) {
            require(data is Data)
            require(component is TestComponent)
            component.int = data.int
        }

        override fun serializersModule(): SerializersModule {
            return SerializersModule {
                polymorphic(ComponentData::class) {
                    subclass(Data::class)
                }
            }
        }

        override val type: KClass<TestComponent>
            get() = TestComponent::class

        override fun create(): TestComponent {
            return TestComponent()
        }

    }
}

class TestScript : Script() {
    companion object : ScriptDefinition<TestScript> {
        override val type: KClass<TestScript>
            get() = TestScript::class

        override fun create(): TestScript {
            return TestScript()
        }

    }
}