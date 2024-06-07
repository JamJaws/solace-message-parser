plugins {
    alias(libs.plugins.jvm)
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.jackson)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.kotlin.test.junit5)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.kotest.assertions.core.jvm)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "com.jamjaws.solace.message.parser.AppKt"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
