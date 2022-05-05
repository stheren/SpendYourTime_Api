import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
    application
}

val javalinVersion: String by project
val slf4jVersion: String by project
val jacksonVersion: String by project
val kjwtVersion: String by project
val ktormVersion: String by project

repositories {
    mavenCentral()
    maven("https://repo.panda-lang.org/releases")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.javalin:javalin:$javalinVersion")
    implementation("io.javalin:javalin-openapi:$javalinVersion")
    //implementation("io.javalin:javalin-bundle:$javalinVersion")

    implementation("org.slf4j:slf4j-simple:$slf4jVersion")

    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")

    implementation("io.github.nefilim.kjwt:kjwt-core:$kjwtVersion")
    implementation("io.github.nefilim.kjwt:kjwt-google-kms-grpc:$kjwtVersion")
    implementation("io.github.nefilim.kjwt:kjwt-jwks:$kjwtVersion")

    implementation("org.ktorm:ktorm-core:$ktormVersion")
}

application {
    mainClassName = "com.spendyourtime.Server"
}

tasks.getByName<ShadowJar>("shadowJar") {
    archiveClassifier.set("fat")
    archiveVersion.set(project.version.toString())
    archiveBaseName.set(project.name)
}

