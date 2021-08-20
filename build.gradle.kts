buildscript {
    val kotlin_version by extra("1.4.30")
    val coroutines_version by extra("1.4.2")
    val robolectric_version by extra("4.4")

    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
    }
}

plugins {
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}