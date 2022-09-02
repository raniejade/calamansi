package calamansi

import kotlin.reflect.KClass

/**
 * Establish a dependency between the marked component and other components.
 *
 * A component can only be added to a node if and only if the target node contains
 * all the required components.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class RequiredComponents(val components: Array<KClass<out Component>>)