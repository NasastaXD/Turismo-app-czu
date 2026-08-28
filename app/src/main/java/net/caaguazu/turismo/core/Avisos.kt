package net.caaguazu.turismo.core

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import net.caaguazu.turismo.R

/**
 * Avisos del telefono.
 *
 * Dos canales separados a proposito: un articulo nuevo y un evento que empieza
 * manana no son la misma clase de aviso, y alguien puede querer uno y no el
 * otro. Android deja apagar un canal sin apagar el otro, y eso solo funciona si
 * estan separados desde el principio.
 *
 * Todo se decide en el telefono. No hay push, no hay token, no hay servidor de
 * notificaciones: la app revisa cada tanto lo que ya sabe pedir. Eso deja al
 * proyecto sin un servicio externo mas, que es un objetivo explicito, y de paso
 * sin mandar a ningun lado quien tiene la app instalada.
 */
object Avisos {

    private const val ETIQUETA = "Avisos"

    const val CANAL_ARTICULOS = "articulos"
    const val CANAL_EVENTOS = "eventos"

    /**
     * El id de cada aviso sale del id del contenido, corrido por canal. Asi un
     * mismo articulo no puede notificar dos veces, y un evento y un articulo
     * con el mismo id no se pisan.
     */
    private const val BASE_ARTICULOS = 100_000
    private const val BASE_EVENTOS = 200_000

    fun crearCanales(contexto: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val gestor = contexto.getSystemService(NotificationManager::class.java) ?: return
        listOf(
            CANAL_ARTICULOS to "avisos.canal.articulos",
            CANAL_EVENTOS to "avisos.canal.eventos",
        ).forEach { (id, clave) ->
            // El nombre del canal se ve en los ajustes del telefono, asi que sale
            // de los textos como cualquier otro texto de producto.
            val canal = NotificationChannel(id, Textos.t(clave), NotificationManager.IMPORTANCE_DEFAULT)
            gestor.createNotificationChannel(canal)
        }
    }

    /** Sin permiso no se intenta: en Android 13+ enviar sin el es un no-op silencioso. */
    fun hayPermiso(contexto: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            contexto,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun avisarArticulo(contexto: Context, id: Int, titulo: String) {
        enviar(contexto, CANAL_ARTICULOS, BASE_ARTICULOS + id, Textos.t("avisos.articulo"), titulo)
    }

    fun avisarEvento(contexto: Context, id: Int, titulo: String, cuando: String) {
        val encabezado = Textos.t("avisos.evento")
        val cuerpo = if (cuando.isBlank()) titulo else "$titulo · $cuando"
        enviar(contexto, CANAL_EVENTOS, BASE_EVENTOS + id, encabezado, cuerpo)
    }

    private fun enviar(
        contexto: Context,
        canal: String,
        id: Int,
        titulo: String,
        cuerpo: String,
    ) {
        if (!hayPermiso(contexto)) {
            Registro.aviso(ETIQUETA, "sin permiso de notificaciones; no se envia $id")
            return
        }

        // Abrir la app al tocar. No se navega a la ficha concreta: la pila de
        // navegacion vive en memoria y no hay un enlace profundo que reconstruya
        // ese estado. Prometer menos y cumplirlo.
        val abrir = contexto.packageManager
            .getLaunchIntentForPackage(contexto.packageName)
            ?.apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }

        val pendiente = abrir?.let {
            android.app.PendingIntent.getActivity(
                contexto,
                id,
                it,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or
                    android.app.PendingIntent.FLAG_IMMUTABLE,
            )
        }

        val aviso = NotificationCompat.Builder(contexto, canal)
            .setSmallIcon(R.drawable.ic_aviso)
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setStyle(NotificationCompat.BigTextStyle().bigText(cuerpo))
            .setAutoCancel(true)
            .setContentIntent(pendiente)
            .build()

        val resultado = intentar(ETIQUETA, "enviar el aviso $id") {
            NotificationManagerCompat.from(contexto).notify(id, aviso)
        }
        if (resultado is Resultado.Bien) {
            Registro.detalle(ETIQUETA, "aviso enviado: $canal/$id")
        }
    }
}
