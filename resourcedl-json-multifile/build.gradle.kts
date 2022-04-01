plugins {
    java
}

version = rootProject.property("version").toString()

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":"))
    implementation("org.sharegov:mjson:1.4.1") {
        exclude("junit")
    }

    implementation("org.slf4j:slf4j-api:2.0.0-alpha6")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
}
