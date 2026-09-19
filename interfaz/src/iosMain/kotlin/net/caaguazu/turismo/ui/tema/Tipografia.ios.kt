package net.caaguazu.turismo.ui.tema

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import net.caaguazu.turismo.core.Bitacora
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile
import platform.posix.memcpy

/**
 * Las mismas tipografias, leidas del bundle.
 *
 * `Font(identity, getData = { ... })` es lo que hace que esto pueda ser un
 * `val` y no algo que haya que cargar antes de dibujar: la lambda se llama
 * recien cuando el motor de texto necesita los bytes, no ahora. Verificado en
 * las fuentes del artefacto de iOS de Compose, no supuesto.
 *
 * Si un `.ttf` no esta en el bundle, la familia queda con una fuente menos y el
 * sistema cae a la mas parecida. Queda anotado: es un error de empaquetado
 * —falta la referencia a la carpeta `font` en el proyecto de Xcode— y se ve
 * como una app con la tipografia equivocada, que es de las cosas mas difíciles
 * de diagnosticar mirando una captura.
 */
private const val ETIQUETA = "Tipografia"

/** La carpeta tal como la referencia el proyecto de Xcode. */
private const val CARPETA = "font"

actual val Sans: FontFamily = FontFamily(
    fuente("poppins_regular", FontWeight.Normal),
    fuente("poppins_medium", FontWeight.Medium),
    fuente("poppins_semibold", FontWeight.SemiBold),
    fuente("poppins_bold", FontWeight.Bold),
)

actual val Serif: FontFamily = FontFamily(
    fuente("serif_regular", FontWeight.Normal),
    fuente("serif_semibold", FontWeight.SemiBold),
    fuente("serif_bold", FontWeight.Bold),
)

private fun fuente(nombre: String, peso: FontWeight) = Font(
    identity = nombre,
    getData = { bytesDelBundle(nombre) },
    weight = peso,
)

@OptIn(ExperimentalForeignApi::class)
private fun bytesDelBundle(nombre: String): ByteArray {
    val ruta = NSBundle.mainBundle.pathForResource(
        name = nombre,
        ofType = "ttf",
        inDirectory = CARPETA,
    )
    if (ruta == null) {
        Bitacora.fallo(ETIQUETA, "falta $nombre.ttf en el bundle")
        return ByteArray(0)
    }
    val datos: NSData = NSData.dataWithContentsOfFile(ruta) ?: run {
        Bitacora.fallo(ETIQUETA, "no se pudo leer $nombre.ttf")
        return ByteArray(0)
    }
    val largo = datos.length.toInt()
    if (largo == 0) return ByteArray(0)
    // NSData a ByteArray: hay que fijar el arreglo de Kotlin para que el
    // recolector no lo mueva mientras memcpy escribe adentro.
    val destino = ByteArray(largo)
    destino.usePinned { fijado ->
        memcpy(fijado.addressOf(0), datos.bytes, datos.length)
    }
    return destino
}
