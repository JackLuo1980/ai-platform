pluginManagement {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://maven.aliyun.com/repository/spring-plugin") }
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/spring") }
        mavenCentral()
    }
}

rootProject.name = "ai-platform"

include(
    "shared:common-model",
    "shared:common-security",
    "shared:common-storage",
    "shared:common-testing",
    "services:gateway",
    "services:console",
    "services:operation",
    "services:lab",
    "services:inference",
    "services:fastlabel",
    "services:scorecard"
)
