import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

android {
    buildFeatures {
        compose = true
        buildConfig = true
    }
    namespace = "com.flabs.cobrate"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.flabs.cobrate"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Propiedades de Supabase (solo locales, NO versionadas)
        val supabaseProps = Properties()
        val supabasePropsFile = rootProject.file("local.properties")
        if (supabasePropsFile.exists()) {
            supabaseProps.load(supabasePropsFile.inputStream())
        }
        val supabaseUrl = supabaseProps.getProperty("SUPABASE_URL") ?: ""
        val supabaseAnonKey = supabaseProps.getProperty("SUPABASE_ANON_KEY") ?: ""
        buildConfigField("String", "SUPABASE_URL", "\"" + supabaseUrl + "\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"" + supabaseAnonKey + "\"")
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(platform(libs.composerbom))
    implementation(libs.composeUi)
    implementation(libs.composeGraphics)
    implementation(libs.composeToolingPreview)
    implementation(libs.composeMaterial3)
    implementation(libs.activityCompose)
    implementation(libs.navigationCompose)
    implementation(libs.lifecycleViewmodelCompose)
    implementation(libs.lifecycleRuntimeCompose)
    implementation(libs.datastorePreferences)
    implementation(libs.androidx.security.crypto)

    // Supabase SDK (solo plomería, sin uso todavía)
    implementation(platform(libs.supabaseBom))
    implementation(libs.supabasePostgrest)
    implementation(libs.supabaseAuth)
    implementation(libs.supabaseRealtime)
    implementation(libs.ktorClientAndroid)
    implementation(libs.kotlinxSerializationJson)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}