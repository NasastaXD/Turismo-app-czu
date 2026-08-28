package net.caaguazu.turismo.core

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Salida hacia la app de calendario del telefono.
 *
 * Se delega igual que con los mapas, y por la misma razon: quien agenda un
 * evento quiere que le suene el recordatorio en el calendario que ya usa. Para
 * eso Google Calendar —o el que tenga instalado— hace un trabajo que esta app
 * no puede igualar, y que ademas le sincroniza con sus otros dispositivos.
 *
 * `ACTION_INSERT` abre el formulario del calendario ya completado y deja que la
 * persona confirme. No pide ningun permiso: la app nunca lee ni escribe en el
 * calendario, solo propone. Un permiso de calendario para esto seria pedir
 * acceso a la agenda entera de alguien a cambio de nada.
 */
object Calendario {

    private const val ETIQUETA = "Calendario"

    /** Si un evento no dice cuando termina, se agenda de una hora. */
    private const val DURACION_POR_DEFECTO_MS = 60L * 60L * 1000L

    /**
     * Propone agendar un evento. Devuelve false si no hay app de calendario o
     * si la fecha no se pudo interpretar, para que la interfaz no ofrezca un
     * boton que no hace nada.
     */
    fun agendar(
        contexto: Context,
        titulo: String,
        inicioIso: String?,
        finIso: String? = null,
        lugar: String? = null,
        descripcion: String? = null,
    ): Boolean {
        val inicio = enMilisegundos(inicioIso) ?: run {
            Registro.aviso(ETIQUETA, "no se pudo interpretar la fecha de inicio: $inicioIso")
            return false
        }
        // Un fin anterior al inicio es un dato malo del panel, no una duracion
        // negativa: se trata como si no hubiera fin.
        val fin = enMilisegundos(finIso)?.takeIf { it > inicio }
            ?: (inicio + DURACION_POR_DEFECTO_MS)

        val intencion = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(CalendarContract.Events.TITLE, titulo)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, inicio)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, fin)
            if (!lugar.isNullOrBlank()) {
                putExtra(CalendarContract.Events.EVENT_LOCATION, lugar)
            }
            if (!descripcion.isNullOrBlank()) {
                putExtra(CalendarContract.Events.DESCRIPTION, descripcion)
            }
        }

        return try {
            contexto.startActivity(intencion)
            true
        } catch (e: Throwable) {
            Registro.aviso(ETIQUETA, "no hay app de calendario que atienda la invitacion")
            false
        }
    }

    /** True si la fecha se puede interpretar; es lo que decide si va el boton. */
    fun sePuedeAgendar(inicioIso: String?): Boolean = enMilisegundos(inicioIso) != null

    /**
     * ISO 8601 con zona a milisegundos de epoca.
     *
     * Sin libreria de fechas: `java.time` pide API 26 o desugaring, y esto es lo
     * unico de la app que necesita convertir una fecha. Se prueban las formas
     * que devuelve el panel —con offset, con Z, y sin zona— y si ninguna encaja
     * se devuelve null en vez de una fecha inventada.
     *
     * Lo usa tambien el vigilante, para saber si un evento cae dentro de la
     * ventana en la que corresponde avisar.
     */
    fun enMilisegundos(iso: String?): Long? {
        val texto = iso?.trim().orEmpty()
        if (texto.isEmpty()) return null

        for (patron in PATRONES) {
            val formato = SimpleDateFormat(patron, Locale.US).apply {
                isLenient = false
                // Una fecha sin zona se lee como UTC, que es como el panel las
                // guarda. Interpretarla en la zona del telefono correria el
                // evento varias horas segun donde este parado el turista.
                if (patron == SIN_ZONA) timeZone = TimeZone.getTimeZone("UTC")
            }
            val fecha = try {
                formato.parse(texto)
            } catch (e: Throwable) {
                null
            }
            if (fecha != null) return fecha.time
        }
        return null
    }

    private const val SIN_ZONA = "yyyy-MM-dd'T'HH:mm:ss"

    private val PATRONES = listOf(
        // "2026-09-10T22:00:00+00:00" — lo que devuelve el panel hoy.
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        // "2026-09-10T22:00:00Z"
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        SIN_ZONA,
    )
}
