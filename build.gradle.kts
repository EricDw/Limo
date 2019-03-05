import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `build-scan`
    kotlin("jvm") version "1.3.21"
}

group = "kbus"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation("junit:junit:4.12")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

buildScan {

    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")

    publishAlways()
}