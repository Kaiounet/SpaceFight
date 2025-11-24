plugins {
    id("java")
}

group = "com.kaiounet"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("uk.co.electronstudio.jaylib:jaylib:5.5.+")
}

tasks.test {
    useJUnitPlatform()
}