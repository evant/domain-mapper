plugins {
    idea
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(project(":domain-mapper-annotations"))
    ksp(project(":domain-mapper-compiler"))

    testImplementation(libs.junit5)
    testImplementation(libs.assertk)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

idea {
    module {
        // Not using += due to https://github.com/gradle/gradle/issues/8749
        sourceDirs = sourceDirs + file("build/generated/ksp/main/kotlin")
        testSourceDirs = testSourceDirs + file("build/generated/ksp/test/kotlin")
        generatedSourceDirs = generatedSourceDirs + file("build/generated/ksp/main/kotlin") + file("build/generated/ksp/test/kotlin")
    }
}