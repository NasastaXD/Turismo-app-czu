package net.caaguazu.turismo.core

/**
 * Unas pocas claves que sobreviven al cierre de la app.
 *
 * El idioma elegido, si los avisos estan encendidos y la memoria de lo ya
 * avisado. Nada mas: los favoritos y el recorrido van a un archivo JSON, que
 * es lo que corresponde a dos listas de enteros que crecen.
 *
 * `SharedPreferences` en Android y `NSUserDefaults` en iOS son la misma idea
 * con dos nombres, asi que lo unico que se duplica es esto —cinco pares de
 * leer y escribir— y la politica entera vive una sola vez en `Ajustes`.
 *
 * Nada de aca lanza: una preferencia que no se puede leer devuelve el valor por
 * omision. Perder el idioma elegido es una molestia; no abrir, es un fallo.
 */
expect object Preferencias {

    fun texto(clave: String): String?

    /** null borra la clave, que no es lo mismo que guardar cadena vacia. */
    fun ponerTexto(clave: String, valor: String?)

    fun booleano(clave: String, porOmision: Boolean): Boolean

    fun ponerBooleano(clave: String, valor: Boolean)

    /**
     * Si la clave fue escrita alguna vez.
     *
     * Hace falta de verdad: el interruptor de avisos arranca encendido, asi que
     * "no esta" y "esta en true" tienen que poder distinguirse — uno significa
     * que todavia hay que pedir el permiso del sistema y el otro que ya se
     * decidio.
     */
    fun tiene(clave: String): Boolean

    fun quitar(clave: String)

    /** Conjunto de cadenas. Vacio si no hay nada o si no se pudo leer. */
    fun conjunto(clave: String): Set<String>

    fun ponerConjunto(clave: String, valores: Set<String>)
}
