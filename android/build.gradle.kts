// Arquivo de build da RAIZ: só declara as versões dos plugins.
// "apply false" = o plugin é aplicado de verdade apenas no módulo :app.
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
}
