/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2015-2024 Stefan Niedermann <info@niedermann.it>
 * SPDX-FileCopyrightText: 2023 Álvaro Brey <alvaro.brey@nextcloud.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    val kotlinVersion by extra("2.4.21")
    val commonsVersion by extra("2.4.1")
    val androidCommonsVersion by extra("1.1.1")
    val nextcloudAndroidCommonLib by extra("0.34.0")
    val singleSignOnVersion by extra("1.3.5")

    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:9.5.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.3.12")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
    }
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven { url = uri("https://jitpack.io") }
    }
}

subprojects {
    tasks.withType<Test>().configureEach {
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
    }
}
