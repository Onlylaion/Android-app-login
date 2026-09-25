plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.loggingym"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.loggingym"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
    // Cliente MQTT: fork mantenido (org.eclipse.paho.android.service:1.1.1 está abandonado
    // y crashea en Android 13+ por no declarar RECEIVER_EXPORTED/RECEIVER_NOT_EXPORTED)
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    implementation("com.github.hannesa2:paho.mqtt.android:4.5")

    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}