plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-reload4j:2.0.16")
    implementation("org.apache.opennlp:opennlp-tools:2.4.0")
    implementation("com.andylibrian.jsastrawi:jsastrawi:0.1")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}