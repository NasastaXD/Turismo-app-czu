package net.caaguazu.turismo.core

/**
 * Cache de respuestas en disco.
 *
 * Descartable por definicion: la fuente de verdad es el panel. Esto solo hace que
 * la app sirva contenido cuando no hay senal, y que un arranque con red no
 * descargue lo que no cambio.
 *
 * Cada entrada son dos archivos: el cuerpo y su ETag. Separarlos evita tener que
 * leer y reescribir el cuerpo entero solo para tocar la etiqueta.
 *
 * Vive en codigo compartido porque la politica —los dos archivos, el limite, la
 * poda por antiguedad— es identica en las dos plataformas. Lo unico que cambia
 * es abrir y escribir un archivo, y eso esta detras de `Archivos`.
 */
class Cache(raiz: String) {

    private companion object {
        const val ETIQUETA = "Cache"
        const val LIMITE_BYTES = 24L * 1024 * 1024
    }

    private val archivos = Archivos(raiz)

    data class Entrada(val cuerpo: String, val etag: String?)

    fun leer(url: String): Entrada? {
        val base = huellaDeUrl(url)
        val cuerpo = archivos.leer("$base.json") ?: return null
        val etag = archivos.leer("$base.etag")?.ifBlank { null }
        return Entrada(cuerpo, etag)
    }

    fun guardar(url: String, cuerpo: String, etag: String?) {
        val base = huellaDeUrl(url)
        archivos.escribir("$base.json", cuerpo)
        if (etag != null) archivos.escribir("$base.etag", etag)
        podar()
    }

    fun vaciar() {
        archivos.listar().forEach { archivos.borrar(it.nombre) }
        Bitacora.info(ETIQUETA, "cache vaciada")
    }

    fun tamano(): Long = archivos.listar().sumOf { it.bytes }

    /**
     * Si la cache se pasa del limite, se borran las entradas mas viejas hasta
     * volver a la mitad. Borrar hasta el limite justo haria que la proxima
     * escritura vuelva a disparar la poda.
     */
    private fun podar() {
        val listado = archivos.listar()
        var total = listado.sumOf { it.bytes }
        if (total <= LIMITE_BYTES) return

        Bitacora.info(ETIQUETA, "podando: ${total / 1024} KB supera el limite")
        for (anotacion in listado.sortedBy { it.modificado }) {
            if (total <= LIMITE_BYTES / 2) return
            total -= anotacion.bytes
            archivos.borrar(anotacion.nombre)
        }
    }
}
