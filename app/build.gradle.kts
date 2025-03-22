plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "io.github.romantsisyk.nfccardreader"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.github.romantsisyk.nfccardreader"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    // Enable test options
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

repositories {
    google()
    mavenCentral()
}

dependencies {

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
//    implementation(libs.androidx.hilt.lifecycle.viewmodel)
    ksp(libs.androidx.hilt.compiler)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    
    // Adding Material Icons Extended for additional icons
    implementation(libs.androidx.material.icons.extended)
    
    // Base testing dependencies
    testImplementation(libs.junit)
    
    // Mockito for mocking
    testImplementation("org.mockito:mockito-core:4.5.1")
    testImplementation("org.mockito:mockito-inline:4.5.1") // For mocking final classes and static methods
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0") // Kotlin-friendly Mockito API
    
    // Coroutines testing
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    
    // AndroidX test dependencies
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("androidx.test:runner:1.5.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0") // For InstantTaskExecutorRule
    
    // Robolectric for Android framework simulation in unit tests
    testImplementation("org.robolectric:robolectric:4.9")
    
    // Truth for easier assertions
    testImplementation("com.google.truth:truth:1.1.3")
    
    // JUnit 5 compatibility (optional, if you want to use JUnit 5 features)
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.3")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.3")
    
    // Instrumented test dependencies
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation("androidx.test:rules:1.5.0")
    
    // Hilt testing
    testImplementation("com.google.dagger:hilt-android-testing:${libs.versions.hiltAndroid.get()}")
    kspTest("com.google.dagger:hilt-android-compiler:${libs.versions.hiltAndroid.get()}")
    
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
