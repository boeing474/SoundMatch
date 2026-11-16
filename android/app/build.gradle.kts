plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.soundmatch"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.soundmatch"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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

    // ViewBinding: gera uma classe para cada layout XML (ex.: ActivityMainBinding),
    // eliminando o findViewById. Telas em XML clássico, SEM Jetpack Compose.
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Interface (XML Views)
    // 1.15.0: última versão compatível com compileSdk 35 + AGP 8.7.3 (a 1.17.0 exige SDK 36 / AGP 8.9.1)
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

    // Coroutines + lifecycleScope (chamadas de rede fora da thread principal)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Rede: Retrofit + conversor JSON (Gson) para consumir a API FastAPI
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Testes unitários da regra de negócio (pacote logic)
    testImplementation("junit:junit:4.13.2")
}
