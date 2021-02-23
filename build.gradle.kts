buildscript {
    val kotlin_version by extra("1.4.30")
    val coroutines_version by extra("1.4.2")
    val robolectric_version by extra("4.4")

    repositories {
        google()
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven(url = "https://dl.bintray.com/andreyberyukhov/FlowReactiveNetwork")
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}