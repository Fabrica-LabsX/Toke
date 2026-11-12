import com.android.build.api.dsl.ApplicationExtension
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    id("com.google.gms.google-services")
}

extensions.configure<ApplicationExtension> {
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
            // shortcut: R8 apagado hasta probar la versión final en un teléfono (./gradlew assembleRelease -PfirmarConDebug); luego enable = true.
            optimization {
                enable = false
            }
            // Solo para probar la versión final en un teléfono propio: ./gradlew assembleRelease -PfirmarConDebug
            if (project.hasProperty("firmarConDebug")) {
                signingConfig = signingConfigs.getByName("debug")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.add("-opt-in=androidx.compose.material3.ExperimentalMaterial3Api")
    }
}


dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.profileinstaller)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.material)
    implementation(platform(libs.composerbom))
    implementation(libs.composeUi)
    implementation(libs.composeGraphics)
    implementation(libs.composeToolingPreview)
    implementation(libs.composeMaterial3)
    implementation(libs.composeMaterialIconsCore)
    implementation(libs.composeMaterialIconsExtended)
    implementation(libs.activityCompose)
    implementation(libs.navigationCompose)
    implementation(libs.lifecycleViewmodelCompose)
    implementation(libs.lifecycleRuntimeCompose)
    implementation(libs.datastorePreferences)
    implementation(libs.androidx.security.crypto)

    // Supabase SDK
    implementation(platform(libs.supabaseBom))
    implementation(libs.supabasePostgrest)
    implementation(libs.supabaseAuth)
    implementation(libs.supabaseRealtime)
    implementation(libs.supabaseStorage)
    implementation(libs.coil)
    implementation(libs.ktorClientOkhttp)
    implementation(libs.kotlinxSerializationJson)
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    implementation(libs.google.mlkit.barcode.scanning)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
}
