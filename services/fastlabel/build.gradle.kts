plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    implementation(libs.mybatis.plus.spring.boot3.starter)
    implementation(libs.minio)
    implementation(libs.jackson.databind)
    runtimeOnly(libs.postgresql)
    implementation(project(":shared:common-model"))
    implementation(project(":shared:common-security"))
    implementation(project(":shared:common-storage"))
    testImplementation(libs.spring.boot.starter.test)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.spring.boot.configuration.processor)
}
