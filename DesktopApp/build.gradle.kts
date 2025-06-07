import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    kotlin("plugin.serialization") version "1.8.0"
}

group = "com.example"
version = "1.0-SNAPSHOT"

// Configure Java toolchain to use Java 17
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17)) // Use Java 17 which is installed on your system
    }
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Compose dependencies
    implementation(compose.desktop.currentOs)
    
    // Web scraping dependencies
    implementation("org.jsoup:jsoup:1.16.1")
    implementation("org.json:json:20231013")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // Selenium dependencies
    implementation("org.seleniumhq.selenium:selenium-java:4.15.0")
    implementation("org.seleniumhq.selenium:selenium-edge-driver:4.15.0")
    implementation("org.seleniumhq.selenium:selenium-support:4.15.0")
    
    // WebDriverManager for driver management
    implementation("io.github.bonigarcia:webdrivermanager:5.6.2")
    
    // Ktor client for API communication
    implementation("io.ktor:ktor-client-core:1.6.8")
    implementation("io.ktor:ktor-client-cio:1.6.8")
    implementation("io.ktor:ktor-client-serialization:1.6.8")
    implementation("io.ktor:ktor-client-logging:1.6.8")
    
    // Kotlinx serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.6.0")
    
    // Add this if you want Gradle to automatically download the JDK if needed
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17" // Set this to match your toolchain version
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "DesktopApp"
            packageVersion = "1.0.0"
        }
    }
}
