package calamansi.component

import kotlin.reflect.KClass

/**
 * Establish a dependency between the marked component and other components.
 *
 * When a component is added to a [calamansi.scene.Node], all of its dependencies will be automatically added.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class Dependencies(val components: Array<KClass<out Component>>)