import sword.tickets.android.gradle.tasks.CreateLayoutWrappersTask

plugins {
    alias(libs.plugins.android.application)
    checkstyle
}

android {
    namespace = "sword.tickets.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "sword.tickets.android"
        minSdk = 34
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets["main"].java {
        srcDir("build/generated/sources/layoutWrappers/main/java")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.androidx.annotation)
    implementation(libs.sword.collections)
    implementation(libs.sword.database)
    implementation(project(":collectionExtensions"))
    implementation(project(":dbSchema"))
    implementation(project(":dbManager"))
    implementation(project(":appLayoutBinders"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.espresso.intents)
    androidTestImplementation(testFixtures(project(":dbManager")))
}

tasks.register("checkstyleMain", Checkstyle::class.java) {
    source("src/main/java")
    classpath = files()
}

afterEvaluate {
    tasks.register("createLayoutWrappers", CreateLayoutWrappersTask::class.java) {
        resourcesDir = project.file("src/main/res")
        bootClassPath = android.bootClasspath
        interfacesClasspath = project(":appLayoutBinders").layout.buildDirectory.dir("intermediates/javac/release/compileReleaseJavaWithJavac/classes")
        outputDir = layout.buildDirectory.dir("generated/sources/layoutWrappers/main/java")
        dependsOn(":appLayoutBinders:compileReleaseJavaWithJavac")
    }

    android.applicationVariants.forEach { variant ->
        val compileTask = tasks.findByName("compile${variant.name.capitalize()}JavaWithJavac")!!
        variant.productFlavors.forEach {
            compileTask.dependsOn("create${it.name.capitalize()}StringWrappers")
        }

        compileTask.dependsOn( "createLayoutWrappers")
    }

    tasks.findByName("check")!!.dependsOn("checkstyleMain")
}
