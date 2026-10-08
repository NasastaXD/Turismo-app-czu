package net.caaguazu.turismo.core

/**
 * Los assets del APK. Es de donde salian antes, cuando esto vivia en :app: los
 * archivos no se movieron de sitio relativo, solo de modulo, y Android fusiona
 * los assets de una biblioteca con los de la app.
 */
actual object Empaquetado {

    actual fun texto(ruta: String): String? =
        runCatching {
            ArranqueAndroid.contexto.assets.open(ruta)
                .bufferedReader()
                .use { it.readText() }
        }.getOrNull()
}
