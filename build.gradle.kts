plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "net.itskev"
version = "1.1.0"

repositories {
    mavenCentral()
    maven(url = "https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    implementation("io.fabric8:kubernetes-client:5.7.0")
    compileOnly("io.github.waterfallmc:waterfall-api:1.17-R0.1-SNAPSHOT")

    compileOnly("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.20")
}

val tokens = mapOf("VERSION" to project.version)

tasks.withType<ProcessResources> {
    filesMatching("*.yml") {
        filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to tokens)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withJavadocJar()
}
