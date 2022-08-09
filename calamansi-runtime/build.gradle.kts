plugins {
    jvm
}

dependencies {
    implementation(project(":calamansi-api"))
    implementation(Deps.kotlinSerializationJson)
}