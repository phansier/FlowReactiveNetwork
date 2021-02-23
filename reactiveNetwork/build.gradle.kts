plugins {
    id("com.android.library")
    kotlin("android")
    id("maven")
}

android {
    compileSdkVersion(29)
    //testOptions { unitTests { includeAndroidResources = true } }

    defaultConfig {
        minSdkVersion(14)
    }
    compileOptions {
        targetCompatibility = JavaVersion.VERSION_1_8
        sourceCompatibility = JavaVersion.VERSION_1_8
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFile(getDefaultProguardFile("proguard-android.txt"))
            proguardFile("proguard-rules.pro")
        }
    }

    sourceSets["main"].java {
        srcDir("src/main/kotlin")
    }

    sourceSets["test"].java {
        srcDir("src/test/kotlin")
    }

    kotlin {
        explicitApi()
    }

    dependencies {
        val kotlin_version = rootProject.extra["kotlin_version"]
        val coroutines_version = rootProject.extra["coroutines_version"]
        val robolectric_version = rootProject.extra["robolectric_version"]

        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines_version")

        implementation("androidx.annotation:annotation:1.1.0")


        testImplementation ("org.jetbrains.kotlin:kotlin-test-common:$kotlin_version")
        testImplementation ("org.jetbrains.kotlin:kotlin-test-annotations-common:$kotlin_version")

        testImplementation ("com.google.truth:truth:1.0.1")
        testImplementation ("org.robolectric:robolectric:$robolectric_version")
        testImplementation ("io.mockk:mockk:1.10.4")

        testImplementation ("at.florianschuster.test:coroutines-test-extensions:0.1.2")
        testImplementation ("androidx.test:core:1.3.0")
    }

}

apply {from("../deploy.gradle")}