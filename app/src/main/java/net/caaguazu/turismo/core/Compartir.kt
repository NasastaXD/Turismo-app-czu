package net.caaguazu.turismo.core

import android.content.Context
import android.content.Intent
import net.caaguazu.turismo.BuildConfig

/**
 * Compartir un lugar, articulo o recorrido fuera de la app.
 *
 * Se delega en el selector del sistema (`ACTION_SEND`) en vez de construir
 * nada propio, mismo criterio que `MapasExternos` y `Calendario`: quien
 * comparte ya eligio con que — WhatsApp, mensajes, copiar — y competir con
 * eso no aporta nada.
 *
 * El enlace apunta a `caaguazu-web`, el espejo temporal para quien no tiene
 * la app (o esta en iOS, que todavia no la tiene). No hay forma de compartir
 * "la ficha 260" con alguien sin la app instalada mas que esa: sin enlace,
 * el share solo podria mandar el titulo suelto.
 */
object Compartir {

    private const val ETIQUETA = "Compartir"

    fun compartir(contexto: Context, titulo: String, ruta: String) {
        val enlace = BuildConfig.URL_ESPEJO + "#/" + ruta
        val intencion = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "$titulo\n$enlace")
        }
        try {
            contexto.startActivity(Intent.createChooser(intencion, null))
        } catch (e: Throwable) {
            // Un telefono sin ninguna app que reciba un share es raro pero posible.
            Registro.aviso(ETIQUETA, "no hay app que reciba el share")
        }
    }
}
