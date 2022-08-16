plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":domain-mapper-annotations"))
    implementation(libs.ksp)
    implementation(libs.kotlinpoet.ksp)
}