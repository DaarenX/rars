import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("jvm") version "1.9.22"
    application
}

repositories {
    mavenCentral()
}

sourceSets.main {
    java.srcDir("src")
    resources.srcDir("src")
}

sourceSets.test {
    java.srcDir("test")
    resources.srcDir("test")
}

application {
    mainClass = "rars.Launch"
}

tasks {
    named<ShadowJar>("shadowJar") {
        destinationDirectory.set(file("../Bachelorarbeit"))
    }
}