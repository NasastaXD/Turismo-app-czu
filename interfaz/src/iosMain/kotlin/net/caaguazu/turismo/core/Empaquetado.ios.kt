package net.caaguazu.turismo.core

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfFile

/**
 * El bundle de la app, que en iOS son archivos de verdad.
 *
 * La carpeta tiene que estar en el proyecto de Xcode como **referencia de
 * carpeta** y no como grupo: es la misma regla que el mapa, y por la misma
 * razon — si Xcode aplana la estructura, `textos/es.json` deja de resolver y no
 * hay nada que avise, la interfaz sale marcada entre angulos.
 */
@OptIn(ExperimentalForeignApi::class)
actual object Empaquetado {

    actual fun texto(ruta: String): String? {
        // Con `inDirectory` explicito y no con la ruta metida en `name`: esa
        // forma funciona a veces y depende de como quedo armado el bundle. Es
        // la misma llamada que ya usa el mapa, que esta comprobada.
        val carpeta = ruta.substringBeforeLast('/', "")
        val archivo = ruta.substringAfterLast('/')
        val completa = NSBundle.mainBundle.pathForResource(
            name = archivo.substringBeforeLast('.'),
            ofType = archivo.substringAfterLast('.', ""),
            inDirectory = carpeta.ifEmpty { null },
        ) ?: return null
        val datos: NSData = NSData.dataWithContentsOfFile(completa) ?: return null
        return NSString.create(data = datos, encoding = NSUTF8StringEncoding) as String?
    }
}
