plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    id("com.google.protobuf") version "0.9.4"
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.29.3"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.69.0"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins.create("grpc")
        }
    }
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)
    implementation(libs.mybatis.plus.spring.boot3.starter)
    implementation("com.baomidou:mybatis-plus-jsqlparser:3.5.9")
    implementation(libs.nats)
    implementation(libs.minio)
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation(libs.grpc.protobuf)
    implementation(libs.grpc.stub)
    implementation(libs.grpc.server.spring.boot)
    runtimeOnly("io.grpc:grpc-core:1.69.0")
    runtimeOnly("io.grpc:grpc-netty-shaded:1.69.0")
    implementation(libs.protobuf.java)
    implementation(libs.javax.annotation)
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    runtimeOnly(libs.postgresql)
    implementation(project(":shared:common-model"))
    implementation(project(":shared:common-security"))
    implementation(project(":shared:common-storage"))
    testImplementation(project(":shared:common-testing"))
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.spring.boot.configuration.processor)
}
