package net.caaguazu.turismo.core

import android.content.Context
import android.content.SharedPreferences

/**
 * `SharedPreferences`, que es lo que la app usaba antes de que esto cruzara.
 *
 * Mismo archivo y mismas claves, asi que una actualizacion encuentra el idioma
 * y la memoria de avisos donde los dejo: nadie vuelve a elegir su idioma ni
 * recibe de nuevo los avisos de lo que ya vio.
 */
actual object Preferencias {

    private const val ARCHIVO = "ajustes"

    private val preferencias: SharedPreferences by lazy {
        ArranqueAndroid.contexto.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
    }

    actual fun texto(clave: String): String? =
        runCatching { preferencias.getString(clave, null) }.getOrNull()

    actual fun ponerTexto(clave: String, valor: String?) {
        runCatching {
            preferencias.edit().apply {
                if (valor == null) remove(clave) else putString(clave, valor)
            }.apply()
        }
    }

    actual fun booleano(clave: String, porOmision: Boolean): Boolean =
        runCatching { preferencias.getBoolean(clave, porOmision) }.getOrDefault(porOmision)

    actual fun ponerBooleano(clave: String, valor: Boolean) {
        runCatching { preferencias.edit().putBoolean(clave, valor).apply() }
    }

    actual fun tiene(clave: String): Boolean =
        runCatching { preferencias.contains(clave) }.getOrDefault(false)

    actual fun quitar(clave: String) {
        runCatching { preferencias.edit().remove(clave).apply() }
    }

    actual fun conjunto(clave: String): Set<String> =
        runCatching { preferencias.getStringSet(clave, emptySet()).orEmpty() }
            .getOrDefault(emptySet())

    actual fun ponerConjunto(clave: String, valores: Set<String>) {
        runCatching { preferencias.edit().putStringSet(clave, valores).apply() }
    }
}
