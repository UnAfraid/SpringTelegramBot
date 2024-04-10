plugins {
    id("org.springframework.boot") version "2.6.6"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    java
    `java-library`
    distribution
}

group = "com.github.unafraid"
version = "1.0.0-SNAPSHOT"

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

configurations {
    compileOnly {
        extendsFrom(annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.telegram:telegrambots-client:7.2.0")
    api("com.github.unafraid.telegram-apis:InlineMenuAPI:2.0.0")
    api("org.jetbrains:annotations:23.0.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.slf4j" && requested.name == "slf4j-api") {
            useVersion("1.7.36")
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<Jar> {
    archiveFileName.set("${rootProject.name}.jar")
    manifest {
        attributes["Built-By"] = System.getProperty("user.name")
        attributes["Implementation-URL"] = "https://github.com/UnAfraid/SpringTelegramBot/"
        attributes["Main-Class"] = "com.github.unafraid.spring.Application"
        attributes["Class-Path"] = configurations.runtimeClasspath.get().joinToString(" ") { it.name }
    }
}

tasks.register("stage") {
    dependsOn("installDist")
}

distributions {
    main {
        contents {
            into("lib") {
                from(configurations.runtimeClasspath.get())
                from(tasks.withType<Jar>())
            }
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        }
    }
}
