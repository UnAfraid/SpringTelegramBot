plugins {
    id("org.springframework.boot") version "2.6.1"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    java
    `java-library`
}

group = "com.github.unafraid"
version = "1.0.0-SNAPSHOT"

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
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
    api("org.springframework.boot:spring-boot-starter-validation:2.6.1")
    api("org.springframework.boot:spring-boot-starter-data-jpa:2.6.1")
    api("org.springframework.boot:spring-boot-starter-web:2.6.1")
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client:2.7.3")
    api("org.telegram:telegrambots-meta:5.4.0.1")
    api("com.github.unafraid.telegram-apis:InlineMenuAPI:1.0.12")
    api("org.jetbrains:annotations:22.0.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test:2.6.1")
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.apache.logging.log4j") {
            useVersion("2.16.0")
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
    dependsOn("bootJar")
}
