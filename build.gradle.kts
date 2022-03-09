plugins {
    java
    `maven-publish`
    signing
}

val env: Map<String, String> = System.getenv()

allprojects {
    apply (plugin="maven-publish")
    apply (plugin="signing")

    version = project.property("version").toString()
    group = "xland.ioutils.resourcedl"

    logger.info("Building $name version: $version")

    repositories {
        if (!env.containsKey("MAVEN_CENTRAL_FIRST")) {
            maven(url = "https://maven.aliyun.com/repository/public") {
                name = "Aliyun Mirror"
            }
        }
        mavenCentral()
    }

    publishing {
        publications.create("mavenJava", MavenPublication::class) {
            artifactId = project.name
            groupId = "xland.ioutils.resourcedl"
            version = project.version.toString()
            this.pom {
                this.url.set("https://github.com/teddyxlandlee/resourcedl")
                this.developers {
                    this.developer {
                        this.id.set("teddyxlandlee")
                        this.name.set("Teddy Li")
                        this.organization.set("Pigeonia Featurehouse")
                        this.organizationUrl.set("https://featurehouse.github.io")
                        this.email.set("tedexe_work.top@foxmail.com")
                        this.timezone.set("CST")
                        this.roles.add("Core Developer")
                    }
                }
                this.inceptionYear.set("2022")
                this.licenses {
                    this.license {
                        this.name.set("Apache-2.0")
                        this.url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
            }
            repositories {
                if (!env.containsKey("DISABLE_MAVEN_LOCAL")) {
                    mavenLocal()
                }
            }
        }
    }
    signing {
        useGpgCmd()

        this.sign(publishing.publications)
    }
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.0-alpha6")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    //testImplementation("org.apache.commons:commons-lang3:3.20")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.processResources {
    this.from("LICENSE.txt") {
        rename { "META-INF/LICENSE_resourcedl" }
    }
    this.exclude("0ab8fee2-09d1-4d41-baa3-67852cd28a4e")
}

publishing {
    publications.getByName("mavenJava", MavenPublication::class) {
        artifact(tasks.jar)
        artifact(tasks.getByName("sourcesJar"))
    }
}
