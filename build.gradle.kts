buildscript {
    val kotlin_version by extra("1.5.20")
    val coroutines_version by extra("1.5.1")
    val robolectric_version by extra("4.6.1")

    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.1")
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