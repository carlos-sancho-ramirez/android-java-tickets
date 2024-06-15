plugins {
    java
}

dependencies {
    implementation(libs.androidx.annotation)
    implementation(libs.sword.collections)
    implementation(libs.sword.database)
    implementation(project(":dbSchema"))
}