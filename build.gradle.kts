plugins {
    `java-library`
    `maven-publish`
}

group = "marais"
version = "0.7.0"

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}

tasks {
    withType(JavaCompile::class).configureEach {
        options.encoding = "UTF-8"
    }

    java {
        targetCompatibility = JavaVersion.VERSION_11
        withSourcesJar()
        withJavadocJar()
    }

    jar {
        manifest {
            attributes(
                "Automatic-Module-Name" to "marais.udtjava"
            )
        }
    }

    test {
        useJUnitPlatform()
        maxParallelForks = 1
    }

    publishing {
        publications {
            create<MavenPublication>("udt-java") {
                from(project.components["java"])
                pom {
                    name.set("udt-java")
                    description.set("UDT Java implementation")
                    url.set("https://github.com/Gui-Yom/udt-java")
                    developers {
                        developer {
                            name.set("Bernd Schuller")
                            email.set("bschuller at users.sourceforge.net")
                            organization.set("Forschungszentrum Juelich")
                            organizationUrl.set("http://www.fz-juelich.de")
                        }
                        developer {
                            name.set("Melanie Ngoatchou")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/Gui-Yom/udt-java.git")
                        developerConnection.set("scm:git:ssh://github.com/Gui-Yom/udt-java.git")
                        url.set("https://github.com/Gui-Yom/udt-java/")
                    }
                }
            }
        }
    }
}

dependencies {
    val log4jVersion: String by project
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")

    val junitVersion: String by project
    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
