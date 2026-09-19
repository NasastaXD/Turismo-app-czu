package net.caaguazu.turismo.core

import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.provider.Settings
import java.util.Locale

/**
 * La version de Android: intents, que es la forma que tiene este sistema de
 * pedirle a otra app que haga algo.
 *
 * Todo con `try` alrededor de `startActivity` y no con `resolveActivity`, y la
 * diferencia importa desde Android 11: la visibilidad de paquetes hace que
 * `resolveActivity` devuelva null para apps que si estan instaladas, a menos
 * que se declare un `<queries>` en el manifiesto. Intentar y atrapar
 * `ActivityNotFoundException` responde la pregunta de verdad —si alguien
 * atiende esto— sin declarar nada.
 */
actual object Sistema {

    private const val ETIQUETA = "Sistema"

    actual val idiomaDelTelefono: String?
        get() = runCatching { Locale.getDefault().language }.getOrNull()

    actual fun animacionesActivas(): Boolean =
        runCatching {
            Settings.Global.getFloat(
                ArranqueAndroid.contexto.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f,
            ) > 0f
        }.getOrDefault(true)

    actual fun abrirUrl(url: String): Boolean =
        lanzar(Intent(Intent.ACTION_VIEW, Uri.parse(url)), "abrir $url")

    actual fun agendar(
        titulo: String,
        inicioMs: Long,
        finMs: Long,
        lugar: String?,
        descripcion: String?,
    ): Boolean {
        val intencion = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, titulo)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, inicioMs)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, finMs)
            if (!lugar.isNullOrBlank()) putExtra(CalendarContract.Events.EVENT_LOCATION, lugar)
            if (!descripcion.isNullOrBlank()) putExtra(CalendarContract.Events.DESCRIPTION, descripcion)
        }
        return lanzar(intencion, "agendar $titulo")
    }

    actual fun compartirTexto(nombreSugerido: String, contenido: String): Boolean {
        val intencion = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, nombreSugerido)
            putExtra(Intent.EXTRA_TEXT, contenido)
        }
        return lanzar(Intent.createChooser(intencion, null), "compartir $nombreSugerido")
    }

    private fun lanzar(intencion: Intent, que: String): Boolean =
        try {
            // FLAG_ACTIVITY_NEW_TASK porque el contexto es el de la aplicacion
            // y no el de una Activity: sin el, Android rechaza el arranque.
            ArranqueAndroid.contexto.startActivity(
                intencion.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
            true
        } catch (e: Throwable) {
            Bitacora.aviso(ETIQUETA, "no hay app que atienda: $que")
            false
        }
}
