import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType

fun Project.enableContextReceivers() {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
        kotlinOptions.freeCompilerArgs += listOf(
            "-Xcontext-receivers" // context receivers
        )
    }
}
