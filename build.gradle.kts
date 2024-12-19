plugins {
    kotlin("jvm") version "2.0.0"
    `maven-publish`
}

group = "me.freique"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

java {
    withSourcesJar()
}

tasks.jar {
    manifest {
        attributes(mapOf("Implementation-Title" to project.name,
            "Implementation-Version" to project.version))
    }
    from(sourceSets.main.get().output)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = "me.freique"
            artifactId = "ttcp"
            version = "0.1.0"
        }
    }
}