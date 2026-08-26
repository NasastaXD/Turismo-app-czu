package net.caaguazu.turismo.core

import java.io.File
import java.security.MessageDigest

/**
 * Cache de respuestas en disco.
 *
 * Descartable por definicion: la fuente de verdad es el panel. Esto solo hace que
 * la app sirva contenido cuando no hay senal, y que un arranque con red no
 * descargue lo que no cambio.
 *
 * Cada entrada son dos archivos: el cuerpo y su ETag. Separarlos evita tener que
 * leer y reescribir el cuerpo entero solo para tocar la etiqueta.
 */
class Cache(private val carpeta: File) {

    private companion object {
        const val ETIQUETA = "Cache"
        const val LIMITE_BYTES = 24L * 1024 * 1024
    }

    data class Entrada(val cuerpo: String, val etag: String?)

    init {
        if (!carpeta.exists()) carpeta.mkdirs()
    }

    fun leer(url: String): Entrada? {
        val base = nombre(url)
        val cuerpo = File(carpeta, "$base.json")
        if (!cuerpo.exists()) return null

        return when (val texto = intentar(ETIQUETA, "leer la copia de $url") { cuerpo.readText() }) {
            is Resultado.Bien -> {
                val etag = File(carpeta, "$base.etag").takeIf { it.exists() }?.readText()?.ifBlank { null }
                Entrada(texto.valor, etag)
            }
            is Resultado.Mal -> null
        }
    }

    fun guardar(url: String, cuerpo: String, etag: String?) {
        val base = nombre(url)
        intentar(ETIQUETA, "guardar la copia de $url") {
            File(carpeta, "$base.json").writeText(cuerpo)
            if (etag != null) File(carpeta, "$base.etag").writeText(etag)
        }
        podar()
    }

    fun vaciar() {
        intentar(ETIQUETA, "vaciar la cache") { carpeta.listFiles()?.forEach { it.delete() } }
        Registro.info(ETIQUETA, "cache vaciada")
    }

    fun tamano(): Long = carpeta.listFiles()?.sumOf { it.length() } ?: 0L

    /**
     * Si la cache se pasa del limite, se borran las entradas mas viejas hasta
     * volver a la mitad. Borrar hasta el limite justo haria que la proxima
     * escritura vuelva a disparar la poda.
     */
    private fun podar() {
        val archivos = carpeta.listFiles() ?: return
        var total = archivos.sumOf { it.length() }
        if (total <= LIMITE_BYTES) return

        Registro.info(ETIQUETA, "podando: ${total / 1024} KB supera el limite")
        archivos.sortedBy { it.lastModified() }.forEach { archivo ->
            if (total <= LIMITE_BYTES / 2) return
            total -= archivo.length()
            archivo.delete()
        }
    }

    /** La URL no sirve de nombre de archivo; su huella si, y es de largo fijo. */
    private fun nombre(url: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(url.toByteArray())
            .joinToString("") { "%02x".format(it) }
            .take(32)
}
