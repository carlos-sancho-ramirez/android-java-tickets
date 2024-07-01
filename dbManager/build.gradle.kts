plugins {
    java
    `java-test-fixtures`
    checkstyle
}

dependencies {
    implementation(libs.androidx.annotation)
    implementation(libs.sword.collections)
    implementation(libs.sword.database)
    implementation(project(":dbSchema"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.sword.collections.assertions)
    testFixturesImplementation(libs.androidx.annotation)
    testFixturesImplementation(libs.sword.collections)
    testFixturesImplementation(libs.sword.database)
    testFixturesImplementation(project(":dbSchema"))
}

tasks.test {
    useJUnitPlatform()
}
