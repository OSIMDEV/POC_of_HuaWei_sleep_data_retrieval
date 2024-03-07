// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.github.ben-manes.versions") version "0.47.0"
}

buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.0")
        classpath("com.huawei.agconnect:agcp:1.9.1.301")
        classpath("io.objectbox:objectbox-gradle-plugin:3.8.0")
    }
}