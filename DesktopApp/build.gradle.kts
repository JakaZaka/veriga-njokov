import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.9.10"
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)

    implementation("org.jsoup:jsoup:1.16.1")
    implementation("org.json:json:20231013")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Selenium dependencies
    implementation("org.seleniumhq.selenium:selenium-java:4.19.1")
    implementation("org.seleniumhq.selenium:selenium-edge-driver:4.19.1")
    implementation("org.seleniumhq.selenium:selenium-support:4.19.1")

    // WebDriverManager for automatic driver management
    implementation("io.github.bonigarcia:webdrivermanager:5.6.2")
    implementation("it.skrape:skrapeit:1.2.2")
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
