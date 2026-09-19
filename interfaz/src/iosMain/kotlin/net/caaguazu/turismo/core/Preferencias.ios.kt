package net.caaguazu.turismo.core

import platform.Foundation.NSUserDefaults

/**
 * `NSUserDefaults`, que es lo mismo que `SharedPreferences` con otro nombre.
 *
 * Las claves son identicas a las de Android a proposito: no se comparten entre
 * dispositivos, pero que se llamen igual es lo que permite leer el codigo de
 * `Ajustes` una sola vez y saber que dice lo mismo en los dos telefonos.
 *
 * `runCatching` en todo por la misma razon que del lado Android: perder el
 * idioma elegido es una molestia, no abrir es un fallo.
 */
actual object Preferencias {

    private val defaults: NSUserDefaults get() = NSUserDefaults.standardUserDefaults

    actual fun texto(clave: String): String? =
        runCatching { defaults.stringForKey(clave) }.getOrNull()

    actual fun ponerTexto(clave: String, valor: String?) {
        runCatching {
            if (valor == null) defaults.removeObjectForKey(clave)
            else defaults.setObject(valor, clave)
        }
    }

    /**
     * `boolForKey` devuelve false cuando la clave no esta, asi que no alcanza
     * para un interruptor que arranca encendido: hay que preguntar primero si
     * existe. Es exactamente el caso de los avisos.
     */
    actual fun booleano(clave: String, porOmision: Boolean): Boolean =
        runCatching {
            if (!tiene(clave)) porOmision else defaults.boolForKey(clave)
        }.getOrDefault(porOmision)

    actual fun ponerBooleano(clave: String, valor: Boolean) {
        runCatching { defaults.setBool(valor, clave) }
    }

    actual fun tiene(clave: String): Boolean =
        runCatching { defaults.objectForKey(clave) != null }.getOrDefault(false)

    actual fun quitar(clave: String) {
        runCatching { defaults.removeObjectForKey(clave) }
    }

    actual fun conjunto(clave: String): Set<String> =
        runCatching {
            // NSUserDefaults no guarda conjuntos: guarda arreglos. Se convierte
            // aca, y la unicidad la garantiza el Set de Kotlin al leer.
            defaults.arrayForKey(clave)
                .orEmpty()
                .mapNotNull { it as? String }
                .toSet()
        }.getOrDefault(emptySet())

    actual fun ponerConjunto(clave: String, valores: Set<String>) {
        runCatching { defaults.setObject(valores.toList(), clave) }
    }
}
