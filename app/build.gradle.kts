plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.akari.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.akari.app"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "2.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    // Release signing key comes from the environment (CI decodes it from a
    // GitHub secret; locally point AKARI_KEYSTORE at the .jks). Unset → the
    // release build stays unsigned and only debug is installable.
    val keystorePath: String? = System.getenv("AKARI_KEYSTORE")
    if (keystorePath != null) {
        signingConfigs {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = System.getenv("AKARI_KEYSTORE_PASSWORD")
                keyAlias = "akari"
                keyPassword = System.getenv("AKARI_KEYSTORE_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            // The demo build: seeded data for design review, own appId so it
            // can live next to the real app.
            applicationIdSuffix = ".debug"
            buildConfigField("boolean", "SEED_DEMO", "true")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("boolean", "SEED_DEMO", "false")
            if (keystorePath != null) signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.health.connect)

    testImplementation(libs.junit)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
}
