plugins {
    java
}

group = "xland.ioutils"
version = "0.1.0-SNAPSHOT"

val env: Map<String, String> = System.getenv()

repositories {
    if (!env.containsKey("MAVEN_CENTRAL_FIRST")) {
        maven(url = "https://maven.aliyun.com/repository/public") {
            name = "Aliyun Mirror"
        }
    }
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    testImplementation("com.google.guava", "guava", "31.0.1-jre")
    testImplementation("commons-codec:commons-codec:1.15")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

