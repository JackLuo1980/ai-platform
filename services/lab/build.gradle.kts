plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.mybatis.plus.spring.boot3.starter)
    implementation("com.baomidou:mybatis-plus-jsqlparser:3.5.9")
    implementation(libs.minio)
    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    implementation("com.alibaba.fastjson2:fastjson2:2.0.52")
    implementation("commons-net:commons-net:3.10.0")
    implementation("org.apache.commons:commons-csv:1.10.0")
    implementation("org.apache.commons:commons-math3:3.6.1")
    runtimeOnly(libs.postgresql)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.spring.boot.configuration.processor)
    testImplementation(libs.spring.boot.starter.test)
}
