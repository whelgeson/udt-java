plugins {
    `java-library`
    `maven-publish`
}

group = "marais"
version = "0.6.0"

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}

java {
    targetCompatibility = JavaVersion.VERSION_11
}

tasks {
    withType(JavaCompile::class).configureEach {
        options.encoding = "UTF-8"
    }

    test {
        useJUnitPlatform()
        maxParallelForks = 1
    }
}

dependencies {
    val junitVersion: String by project
    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    val log4jVersion: String by project
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
}
