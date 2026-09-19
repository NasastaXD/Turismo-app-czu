package net.caaguazu.turismo.core

import java.io.File

/**
 * La version de la JVM. Es el mismo `java.io.File` que usaba `Cache` antes de
 * cruzar a codigo compartido, asi que del lado Android no cambia nada de como
 * se guarda ni donde.
 */
actual class Archivos actual constructor(raiz: String) {

    private val carpeta = File(raiz)

    init {
        // Igual que antes: la carpeta se crea al construir, no al primer uso.
        if (!carpeta.exists()) carpeta.mkdirs()
    }

    actual fun leer(nombre: String): String? =
        runCatching {
            val archivo = File(carpeta, nombre)
            if (archivo.exists()) archivo.readText() else null
        }.getOrNull()

    actual fun escribir(nombre: String, contenido: String) {
        runCatching { File(carpeta, nombre).writeText(contenido) }
    }

    actual fun borrar(nombre: String) {
        runCatching { File(carpeta, nombre).delete() }
    }

    actual fun listar(): List<Anotacion> =
        runCatching {
            carpeta.listFiles().orEmpty().map {
                Anotacion(it.name, it.length(), it.lastModified())
            }
        }.getOrDefault(emptyList())
}
