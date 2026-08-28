# MapLibre usa JNI: sus clases nativas no pueden renombrarse.
-keep class org.maplibre.android.** { *; }
-keep class com.mapbox.** { *; }
-dontwarn org.maplibre.**

# kotlinx.serialization genera serializadores por companion.
-keepclassmembers class ** {
    *** Companion;
}
-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}

# WorkManager instancia el worker por reflexion, a partir del nombre de la clase
# que guardo en su base de datos. Si R8 lo renombra, la revision periodica falla
# solo en release y solo despues de la primera vuelta — el peor error posible.
# El artefacto trae su propia regla, pero esto no depende de que siga trayendola.
-keep class net.caaguazu.turismo.core.Vigilante { <init>(...); }

# Los numeros de linea son lo unico que hace legible una traza ofuscada.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SRC
