plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.smartdevice"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.smartdevice"
        minSdk = 36
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.camera.camera2.pipe)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // --- Retrofit 및 네트워크 관련 라이브러리 (정리된 버전) ---

    // 1. Retrofit 본체
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    // 2. Retrofit의 Gson 변환기
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // 3. OkHttp (네트워크 통신 엔진)
    implementation("com.squareup.okhttp3:okhttp:5.3.2")

    // 4. OkHttp 로깅 인터셉터 (요청/응답을 로그로 보기 위함)
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
}
