plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    implementation(project(":shared:common-model"))
    implementation(libs.testcontainers)
    implementation(libs.testcontainers.postgresql)
    implementation(libs.testcontainers.minio)
    implementation(libs.testcontainers.junit.jupiter)
    implementation(libs.mockito.core)
    implementation(libs.mockito.junit.jupiter)
    implementation(libs.spring.boot.starter.test)
}
