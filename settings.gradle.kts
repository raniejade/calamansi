/*
 * This file was generated by the Gradle 'init' task.
 *
 * The settings file is used to specify which projects to include in your build.
 *
 * Detailed information about configuring a multi-project build in Gradle can be found
 * in the user manual at https://docs.gradle.org/7.4.2/userguide/multi_project_builds.html
 */

rootProject.name = "calamansi"

include("calamansi-api")
include("calamansi-symbol-processor")
include("calamansi-gradle-plugin")
include("calamansi-runtime")
include("calamansi-editor")

include("symbol-processor-harness")


include("calamansi-api-v2")
include("calamansi-runtime-v2")
include("calamansi-editor-v2")
include("calamansi-symbol-processor-v2")