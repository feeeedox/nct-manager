plugins {
    id("java")
    application
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.0.0"
    id("com.gradleup.shadow") version "9.6.1"
}

group = "ovh.fedox"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.williamcallahan:tui4j:0.3.3")
    implementation("io.ktor:ktor-client-core:3.0.0")
    implementation("io.ktor:ktor-client-cio:3.0.0")
    implementation("io.ktor:ktor-client-content-negotiation:3.0.0")
    implementation("org.slf4j:slf4j-simple:2.0.13")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(25)
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest {
        attributes["Main-Class"] = "ovh.fedox.nct.NctToolKt"
    }
}

application {
    mainClass.set("ovh.fedox.nct.NctToolKt")
}