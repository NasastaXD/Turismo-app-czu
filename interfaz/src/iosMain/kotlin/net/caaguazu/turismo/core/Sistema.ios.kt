package net.caaguazu.turismo.core

import platform.Foundation.NSLocale
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.preferredLanguages
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIAccessibilityIsReduceMotionEnabled
import platform.UIKit.UIApplication

/**
 * La version de iOS.
 *
 * Dos de las cuatro cosas no tienen equivalente directo y se resuelven de otra
 * forma, no se dejan sin hacer:
 *
 * - **Agendar** no existe como intencion del sistema. Lo que si existe es que
 *   iOS entiende un archivo `.ics`: se arma uno y se entrega a la hoja de
 *   compartir, donde "Agregar al calendario" aparece como opcion. Son dos
 *   toques mas que en Android, y a cambio sigue sin pedirse **ningun permiso**
 *   —EventKit exigiria acceso a la agenda entera para agregar un evento— que es
 *   la misma decision que tomo el lado Android por otro camino.
 *
 * - **Compartir** es `UIActivityViewController`, que hay que presentar desde
 *   algun controlador. El de la raiz lo guarda `ArranqueIos`.
 */
actual object Sistema {

    private const val ETIQUETA = "SistemaIos"

    actual val idiomaDelTelefono: String?
        get() = runCatching {
            // Llega como "es-419" o "pt-BR": lo que se compara con la lista de
            // idiomas son las dos primeras letras.
            (NSLocale.preferredLanguages.firstOrNull() as? String)
                ?.take(2)
                ?.lowercase()
        }.getOrNull()

    /**
     * En iOS no hay un interruptor general de animaciones: hay "Reducir
     * movimiento", en Accesibilidad. Es la misma intencion dicha de otra forma,
     * y respetarla es lo que corresponde.
     */
    actual fun animacionesActivas(): Boolean =
        runCatching { !UIAccessibilityIsReduceMotionEnabled() }.getOrDefault(true)

    actual fun abrirUrl(url: String): Boolean {
        val destino = NSURL.URLWithString(url) ?: run {
            Bitacora.aviso(ETIQUETA, "URL que iOS no entiende: $url")
            return false
        }
        val aplicacion = UIApplication.sharedApplication
        if (!aplicacion.canOpenURL(destino)) {
            Bitacora.aviso(ETIQUETA, "no hay app que abra $url")
            return false
        }
        aplicacion.openURL(destino, options = emptyMap<Any?, Any>(), completionHandler = null)
        return true
    }

    actual fun agendar(
        titulo: String,
        inicioMs: Long,
        finMs: Long,
        lugar: String?,
        descripcion: String?,
    ): Boolean {
        val archivo = escribirIcs(titulo, inicioMs, finMs, lugar, descripcion)
            ?: return false
        return presentarCompartir(listOf(archivo), "agendar $titulo")
    }

    actual fun compartirTexto(nombreSugerido: String, contenido: String): Boolean {
        val archivo = escribirTemporal(nombreSugerido, contenido) ?: return false
        return presentarCompartir(listOf(archivo), "compartir $nombreSugerido")
    }

    /* ------------------------------------------------------------------ */

    private fun presentarCompartir(cosas: List<Any>, que: String): Boolean {
        val desde = ArranqueIos.raiz ?: run {
            Bitacora.fallo(ETIQUETA, "sin controlador raiz para $que")
            return false
        }
        return runCatching {
            val hoja = UIActivityViewController(
                activityItems = cosas,
                applicationActivities = null,
            )
            // En iPad la hoja se ancla a una vista o el sistema lanza una
            // excepcion. La app es solo para iPhone, pero anclarla igual cuesta
            // dos lineas y evita una caida si algun dia deja de serlo.
            hoja.popoverPresentationController?.sourceView = desde.view
            desde.presentViewController(hoja, animated = true, completion = null)
            true
        }.getOrElse {
            Bitacora.fallo(ETIQUETA, "no se pudo presentar la hoja para $que", it)
            false
        }
    }

    private fun escribirTemporal(nombre: String, contenido: String): NSURL? =
        runCatching {
            val ruta = NSTemporaryDirectory().trimEnd('/') + "/" + nombre
            val escrito = NSString.create(string = contenido).writeToFile(
                path = ruta,
                atomically = true,
                encoding = NSUTF8StringEncoding,
                error = null,
            )
            if (escrito) NSURL.fileURLWithPath(ruta) else null
        }.getOrNull()

    /**
     * Un `.ics` minimo: lo suficiente para que el calendario lo entienda.
     *
     * Las fechas van en UTC con la `Z` final, que es la forma que no depende de
     * que el archivo lleve la definicion de una zona horaria.
     */
    private fun escribirIcs(
        titulo: String,
        inicioMs: Long,
        finMs: Long,
        lugar: String?,
        descripcion: String?,
    ): NSURL? {
        val cuerpo = buildString {
            append("BEGIN:VCALENDAR\r\n")
            append("VERSION:2.0\r\n")
            append("PRODID:-//caaguazu.net//turismo//ES\r\n")
            append("BEGIN:VEVENT\r\n")
            append("UID:").append(inicioMs).append("@caaguazu.net\r\n")
            append("DTSTAMP:").append(enFormatoIcs(inicioMs)).append("\r\n")
            append("DTSTART:").append(enFormatoIcs(inicioMs)).append("\r\n")
            append("DTEND:").append(enFormatoIcs(finMs)).append("\r\n")
            append("SUMMARY:").append(escapadoIcs(titulo)).append("\r\n")
            if (!lugar.isNullOrBlank()) {
                append("LOCATION:").append(escapadoIcs(lugar)).append("\r\n")
            }
            if (!descripcion.isNullOrBlank()) {
                append("DESCRIPTION:").append(escapadoIcs(descripcion)).append("\r\n")
            }
            append("END:VEVENT\r\n")
            append("END:VCALENDAR\r\n")
        }
        return escribirTemporal("evento.ics", cuerpo)
    }

    /** En un `.ics`, la coma, el punto y coma y la barra son separadores. */
    private fun escapadoIcs(valor: String): String = valor
        .replace("\\", "\\\\")
        .replace(";", "\\;")
        .replace(",", "\\,")
        .replace("\n", "\\n")
}
