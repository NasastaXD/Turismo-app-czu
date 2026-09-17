package net.caaguazu.turismo.core

import platform.Foundation.NSDate
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileModificationDate
import platform.Foundation.NSFileSize
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.writeToFile

/**
 * La version de iOS, sobre NSFileManager.
 *
 * Todo va envuelto en runCatching por la misma razon que del lado Android: la
 * cache es descartable, asi que un fallo de disco tiene que degradar a "no hay
 * copia guardada" y nunca tumbar la pantalla.
 */
internal actual class Archivos actual constructor(raiz: String) {

    private val carpeta = raiz.trimEnd('/')
    private val gestor = NSFileManager.defaultManager

    init {
        if (!gestor.fileExistsAtPath(carpeta)) {
            runCatching {
                gestor.createDirectoryAtPath(
                    path = carpeta,
                    withIntermediateDirectories = true,
                    attributes = null,
                    error = null,
                )
            }
        }
    }

    private fun ruta(nombre: String) = "$carpeta/$nombre"

    actual fun leer(nombre: String): String? =
        runCatching {
            val ruta = ruta(nombre)
            if (!gestor.fileExistsAtPath(ruta)) return null
            NSString.create(contentsOfFile = ruta, encoding = NSUTF8StringEncoding) as String?
        }.getOrNull()

    actual fun escribir(nombre: String, contenido: String) {
        runCatching {
            @Suppress("CAST_NEVER_SUCCEEDS")
            (contenido as NSString).writeToFile(
                path = ruta(nombre),
                atomically = true,
                encoding = NSUTF8StringEncoding,
                error = null,
            )
        }
    }

    actual fun borrar(nombre: String) {
        runCatching { gestor.removeItemAtPath(ruta(nombre), null) }
    }

    actual fun listar(): List<Anotacion> =
        runCatching {
            val nombres = gestor.contentsOfDirectoryAtPath(carpeta, null).orEmpty()
            nombres.mapNotNull { crudo ->
                val nombre = crudo as? String ?: return@mapNotNull null
                val atributos = gestor.attributesOfItemAtPath(ruta(nombre), null)
                    ?: return@mapNotNull null

                val bytes = (atributos[NSFileSize] as? Number)?.toLong() ?: 0L
                // NSDate cuenta en segundos con decimales; la cache compara
                // milisegundos, asi que se convierte aca y no en la politica.
                val fecha = atributos[NSFileModificationDate] as? NSDate
                val modificado = ((fecha?.timeIntervalSince1970 ?: 0.0) * 1000).toLong()

                Anotacion(nombre, bytes, modificado)
            }
        }.getOrDefault(emptyList())
}
