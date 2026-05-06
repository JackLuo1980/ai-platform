plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    implementation(libs.spring.cloud.starter.gateway)
    implementation(libs.spring.cloud.starter.loadbalancer)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.springdoc.openapi.starter.webflux.ui)
    implementation(project(":shared:common-model"))
    implementation(project(":shared:common-security"))
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.spring.boot.configuration.processor)
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${libs.versions.spring.cloud.get()}")
    }
}
