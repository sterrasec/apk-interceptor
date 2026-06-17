plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.sterrasec.apkinterceptor"
    compileSdk = 35

    defaultConfig {
        val configuredAppId = project.findProperty("appId") as String? ?: "com.sterrasec.apkinterceptor"
        require(Regex("""(?:[A-Za-z][A-Za-z0-9_]*\.)+[A-Za-z][A-Za-z0-9_]*""").matches(configuredAppId)) {
            "appId must be a valid Android application ID"
        }
        applicationId = configuredAppId
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        val scheme = project.findProperty("interceptScheme") as String? ?: "intercept-poc-example"
        require(Regex("""[A-Za-z][A-Za-z0-9+.-]*""").matches(scheme)) {
            "interceptScheme must be a valid URI scheme"
        }
        manifestPlaceholders["interceptScheme"] = scheme
        buildConfigField("String", "INTERCEPT_SCHEME", "\"$scheme\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    debugImplementation("androidx.compose.ui:ui-tooling")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.14.1")
    testImplementation("androidx.test:core:1.6.1")
}
