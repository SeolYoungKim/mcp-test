plugins {
    kotlin("jvm") version "2.1.10"
    application
}

group = "kakao"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.modelcontextprotocol:kotlin-sdk:0.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("kakao.mcp.MCPExample")
}

// Fat JAR 생성 설정
tasks.jar {
    manifest {
        attributes["Main-Class"] = "kakao.mcp.MCPExample"
    }
    
    // 모든 의존성을 JAR에 포함
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    
    // 중복 파일 처리
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}