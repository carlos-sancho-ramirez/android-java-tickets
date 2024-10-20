plugins {
    id("com.android.library")
    checkstyle
}

android {
    namespace = "sword.tickets.android.app_layout_binders"
    compileSdk = 35
}

dependencies {
    implementation(libs.androidx.annotation)
}

tasks.register("checkstyleMain", Checkstyle::class.java) {
    source("src/main/java")
    classpath = files()
}

afterEvaluate {
    tasks.findByName("check")!!.dependsOn("checkstyleMain")
}