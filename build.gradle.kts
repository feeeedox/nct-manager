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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    implementation("org.json:json:20260814")
    implementation("org.slf4j:slf4j-simple:2.0.13")

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(25)
}

tasks.shadowJar {
    archiveClassifier.set("")

    minimize {
        exclude(dependency("com.ibm.icu:icu4j:.*"))
        exclude(dependency("com.williamcallahan:.*:.*"))
        exclude(dependency("org.slf4j:.*:.*"))
    }

    mergeServiceFiles()

    manifest {
        attributes["Main-Class"] = "ovh.fedox.nct.NctToolKt"
    }
}

application {
    mainClass.set("ovh.fedox.nct.NctToolKt")
}