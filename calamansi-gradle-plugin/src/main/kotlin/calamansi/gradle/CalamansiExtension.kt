package calamansi.gradle

import calamansi.gradle.assets.Asset
import calamansi.gradle.assets.Assets
import org.gradle.api.PolymorphicDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

open class CalamansiExtension @Inject constructor(objectFactory: ObjectFactory) {
    val assets: PolymorphicDomainObjectContainer<Asset> =
        objectFactory.polymorphicDomainObjectContainer(Asset::class.java)
            .also { Assets.registerFactories(it, objectFactory) }
}