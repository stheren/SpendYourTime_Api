import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
    application
}

val javalinVersion: String by project
val slf4jVersion: String by project
val jacksonVersion: String by project
val surenessVersion: String by project

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.javalin:javalin:$javalinVersion")

    implementation("org.slf4j:slf4j-simple:$slf4jVersion")

    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")

    implementation("com.usthe.sureness:sureness-core:$surenessVersion")

}

application {
    mainClassName = "com.spendyourtime.ServerTest"
}

tasks.getByName<ShadowJar>("shadowJar") {
    archiveClassifier.set("fat")
    archiveVersion.set(project.version.toString())
    archiveBaseName.set(project.name)
}