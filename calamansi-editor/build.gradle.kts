plugins {
    jvm
    application // for testing only
}

dependencies {
    implementation(project(":calamansi-api"))
    implementation(project(":calamansi-runtime"))
}

application {
    mainClass.set("calamansi.runtime.RuntimeKt")
}