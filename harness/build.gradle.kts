import calamansi.gradle.assets.SceneAsset

plugins {
    id("com.github.raniejade.calamansi")
}

calamansi {
    assets {
        named<SceneAsset>("default.scn")
    }
}