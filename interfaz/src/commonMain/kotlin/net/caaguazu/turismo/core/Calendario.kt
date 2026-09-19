package net.caaguazu.turismo.core

/**
 * Salida hacia la app de calendario del telefono.
 *
 * Se delega igual que con los mapas, y por la misma razon: quien agenda un
 * evento quiere que le suene el recordatorio en el calendario que ya usa. Para
 * eso Google Calendar —o el que tenga instalado— hace un trabajo que esta app
 * no puede igualar, y que ademas le sincroniza con sus otros dispositivos.
 *
 * **No se pide ningun permiso en ninguna de las dos plataformas.** La app nunca
 * lee ni escribe en el calendario: solo propone, y la persona confirma. Android
 * lo resuelve con `ACTION_INSERT`, que abre el formulario ya completado; iOS no
 * tiene equivalente, asi que arma un `.ics` y lo pasa a la hoja de compartir.
 * Son dos toques mas alla, y a cambio ninguna de las dos pide acceso a la
 * agenda entera de alguien.
 *
 * Interpretar la fecha vive en `:compartido` y esta cubierto por pruebas: de
 * eso depende que el boton se dibuje, y un boton que no puede hacer nada no se
 * dibuja.
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
        titulo: String,
        inicioIso: String?,
        finIso: String? = null,
        lugar: String? = null,
        descripcion: String? = null,
    ): Boolean {
        val inicio = isoEnMilisegundos(inicioIso) ?: run {
            Bitacora.aviso(ETIQUETA, "no se pudo interpretar la fecha de inicio: $inicioIso")
            return false
        }
        // Un fin anterior al inicio es un dato malo del panel, no una duracion
        // negativa: se trata como si no hubiera fin.
        val fin = isoEnMilisegundos(finIso)?.takeIf { it > inicio }
            ?: (inicio + DURACION_POR_DEFECTO_MS)

        return Sistema.agendar(
            titulo = titulo,
            inicioMs = inicio,
            finMs = fin,
            lugar = lugar,
            descripcion = descripcion,
        )
    }

    /** True si la fecha se puede interpretar; es lo que decide si va el boton. */
    fun sePuedeAgendar(inicioIso: String?): Boolean = isoEnMilisegundos(inicioIso) != null

    /**
     * ISO 8601 con zona a milisegundos de epoca.
     *
     * Se conserva el nombre para no tocar a quien ya lo llamaba —el vigilante,
     * que decide con esto si un evento entra en la ventana de dos dias— pero el
     * trabajo esta en `:compartido`, donde hay pruebas que lo cubren y donde lo
     * puede usar tambien el lado iOS.
     */
    fun enMilisegundos(iso: String?): Long? = isoEnMilisegundos(iso)
}
