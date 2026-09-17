package net.caaguazu.turismo.core

/**
 * Lo minimo de disco que necesita la cache, una vez por plataforma.
 *
 * `java.io.File` no existe en iOS y `NSFileManager` no existe en Android, pero
 * la politica de la cache —dos archivos por entrada, podar cuando se pasa del
 * limite, borrar lo mas viejo primero— es la misma y no tiene por que
 * escribirse dos veces. Asi que lo unico que se duplica es esto: abrir, leer,
 * escribir, borrar y listar.
 *
 * Deliberadamente chico y sin nada de cache adentro: si aparece una regla
 * nueva de la cache, va en `Cache` y se escribe una sola vez.
 */
internal expect class Archivos(raiz: String) {

    /** null si no existe o no se pudo leer. No lanza. */
    fun leer(nombre: String): String?

    fun escribir(nombre: String, contenido: String)

    fun borrar(nombre: String)

    /** Lo que hay en la carpeta, para poder podar. Vacio si no se pudo listar. */
    fun listar(): List<Anotacion>
}

/**
 * Un archivo visto por la poda: cuanto ocupa y cuando se toco.
 *
 * `modificado` son milisegundos de epoch en las dos plataformas. Solo se usa
 * para ordenar de mas viejo a mas nuevo, asi que lo unico que importa es que
 * sea comparable consigo mismo.
 */
internal data class Anotacion(
    val nombre: String,
    val bytes: Long,
    val modificado: Long,
)
