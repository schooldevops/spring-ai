import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.3.0"
    id("io.spring.dependency-management") version "1.1.5"
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.spring") version "1.9.24"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Web
    implementation("org.springframework.boot:spring-boot-starter-web")
    
    // Spring AI OpenAI (Vision 지원 모델 사용)
    // 주의: Vision API를 사용하려면 GPT-4o 또는 GPT-4 Turbo 사용 필요
    implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter:1.0.0-M6")
    
    // Spring AI Anthropic (Claude 3 Vision 지원)
    // implementation("org.springframework.ai:spring-ai-anthropic-spring-boot-starter:1.0.0-M6")
    
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    
    // Jackson (JSON 처리)
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    
    // 테스트
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

