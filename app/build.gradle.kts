import java.io.FileInputStream
import java.util.Properties
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
}
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}
val fbAppId = localProperties.getProperty("FACEBOOK_APP_ID") ?: "0"
val fbClientToken = localProperties.getProperty("FACEBOOK_CLIENT_TOKEN") ?: "0"
val googleWebClientId = localProperties.getProperty("GOOGLE_WEB_CLIENT_ID") ?: ""
val fbProtocolScheme = "fb$fbAppId"


android {
    namespace = "com.example.fooddelivery"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.fooddelivery"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        resValue("string", "facebook_app_id", fbAppId)
        resValue("string", "facebook_client_token", fbClientToken)
        resValue("string", "fb_login_protocol_scheme", fbProtocolScheme)

        // SECURITY: Đưa Google Client ID vào BuildConfig
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleWebClientId\"")

        buildConfigField("String", "API_BASE_URL", "\"http://localhost:4000/api/\"")

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"http://localhost:4000/api/\"")
            buildConfigField("String", "SOCKET_URL", "\"http://localhost:4000\"")
        }
        release {
            isMinifyEnabled = true // Bật obfuscation để bảo vệ code
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "API_BASE_URL", "\"https://api.yourapp.com/api/\"")
            buildConfigField("String", "SOCKET_URL", "\"https://api.yourapp.com\"")
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.foundation)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation("androidx.navigation:navigation-compose:2.8.3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")

    implementation("io.socket:socket.io-client:2.1.0") {
        exclude(group = "org.json", module = "json")
    }

    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.coil-kt:coil-gif:2.7.0")

    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")

    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation("com.facebook.android:facebook-login:latest.release")

    implementation("androidx.compose.material:material-icons-extended")

    implementation("org.osmdroid:osmdroid-android:6.1.18")
    
    implementation("com.airbnb.android:lottie-compose:6.7.1")

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.messaging)
    implementation(libs.play.services.auth)
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // Security
    implementation(libs.androidx.security.crypto)

    // Location
    implementation(libs.play.services.location)
}
