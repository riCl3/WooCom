plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.woocom"
    // 36 is required by androidx.browser 1.10, pulled in by supabase-kt's auth module.
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.woocom"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "2.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Payment key is interpolated from a gradle property so it never lives in
        // source control. Supply it via gradle.properties or a CI secret.
        val razorpayKeyId =
            providers.gradleProperty("RAZORPAY_KEY_ID")
                .orElse("rzp_test_XXXXXXXXXXXX")
                .get()
        buildConfigField("String", "RAZORPAY_KEY_ID", "\"$razorpayKeyId\"")

        // Supabase identifiers. Both are public (the anon key is shipped in every APK);
        // the real protection is RLS. Leave them blank to keep running on Firestore.
        val supabaseUrl = providers.gradleProperty("SUPABASE_URL").orElse("").get()
        val supabaseAnonKey = providers.gradleProperty("SUPABASE_ANON_KEY").orElse("").get()
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
    }

    // Release signing is opt-in: point WOOCOM_KEYSTORE / WOOCOM_KEYSTORE_PASSWORD /
    // WOOCOM_KEY_ALIAS / WOOCOM_KEY_PASSWORD at a keystore that is never committed.
    // Without them the release build still compiles (unsigned), which keeps CI green.
    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("WOOCOM_KEYSTORE")
            if (!keystorePath.isNullOrBlank() && file(keystorePath).exists()) {
                storeFile = file(keystorePath)
                storePassword = System.getenv("WOOCOM_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("WOOCOM_KEY_ALIAS")
                keyPassword = System.getenv("WOOCOM_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig =
                signingConfigs.getByName("release")
                    .takeIf { it.storeFile != null }
        }
    }

    lint {
        abortOnError = true
        warningsAsErrors = false
        htmlReport = true
        textReport = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.coil.compose)
    implementation(libs.dots.indicator)
    implementation(libs.androidx.material)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.razorpay.checkout)

    // Supabase backend (see MIGRATION.md). Selected at runtime by ServiceLocator when
    // SUPABASE_URL is configured; the Firestore implementations remain the fallback.
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.auth)
    implementation(libs.supabase.functions)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.kotlinx.serialization.json)
}
