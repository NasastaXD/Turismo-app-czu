package net.caaguazu.turismo.core

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import net.caaguazu.turismo.datos.Datos
import java.util.concurrent.TimeUnit

/**
 * Revisa cada tanto si hay articulos nuevos o eventos que se vienen.
 *
 * Es lo contrario de un push: no hay servidor que avise, la app pregunta. Eso
 * cuesta una revision cada seis horas y ahorra un servicio externo entero —sin
 * Firebase, sin token de dispositivo, sin que nadie del otro lado sepa quien
 * tiene la app instalada.
 *
 * Seis horas es deliberado: el contenido de un inventario turistico cambia por
 * semana, no por minuto, y una revision mas seguida solo gastaria bateria para
 * encontrar lo mismo.
 */
class Vigilante(
    contexto: Context,
    parametros: WorkerParameters,
) : CoroutineWorker(contexto, parametros) {

    override suspend fun doWork(): Result {
        if (!Ajustes.avisosActivos) {
            Registro.detalle(ETIQUETA, "avisos apagados; no se revisa")
            return Result.success()
        }
        if (!Avisos.hayPermiso(applicationContext)) {
            Registro.detalle(ETIQUETA, "sin permiso de avisos; no se revisa")
            return Result.success()
        }

        // Los textos tienen que estar cargados, pero cargarlos de nuevo aca no
        // era gratis: `cargarEmbebido` REEMPLAZA el mapa por el respaldo del
        // APK, asi que con la app abierta —este trabajador corre en el mismo
        // proceso— borraba de la pantalla todo lo que el panel habia mandado.
        // Y era redundante: `App.onCreate` corre antes que cualquier trabajador.
        if (!Textos.cargados) Textos.cargarEmbebido(applicationContext, Idioma.actual)

        revisarArticulos()
        revisarEventos()

        // Un fallo de red no es un fallo del trabajo: la proxima vuelta lo
        // reintenta sola dentro de seis horas. Pedir un reintento inmediato
        // gastaria bateria por contenido que no es urgente.
        return Result.success()
    }

    private suspend fun revisarArticulos() {
        val pagina = Datos.api.articulos().oNulo() ?: return
        val vistos = Ajustes.articulosVistos

        // Primer arranque: se toma nota de lo que ya existe y no se avisa nada.
        // Avisar de los cuarenta articulos del historico seria inutilizable.
        if (vistos.isEmpty()) {
            Ajustes.articulosVistos = pagina.items.map { it.id }.toSet()
            Registro.info(ETIQUETA, "primera revision: ${pagina.items.size} articulos anotados sin avisar")
            return
        }

        val nuevos = pagina.items.filter { it.id !in vistos }
        // Se anota solo lo que se aviso de verdad. Anotar los `nuevos` enteros
        // marcaba como avisado lo que quedo afuera del tope, asi que de veinte
        // articulos nuevos se avisaban cinco y los otros quince no se avisaban
        // nunca. Los que sobran esperan la vuelta que viene, que es de lo que
        // habla el tope: cinco por vuelta, no cinco y el resto al silencio.
        val avisados = nuevos.take(MAX_AVISOS_POR_VUELTA)
        avisados.forEach { articulo ->
            Avisos.avisarArticulo(applicationContext, articulo.id, articulo.titulo)
        }
        if (avisados.isNotEmpty()) {
            Ajustes.articulosVistos = vistos + avisados.map { it.id }
            Registro.info(ETIQUETA, "${avisados.size} de ${nuevos.size} articulos nuevos avisados")
        }
    }

    /**
     * Un evento se avisa una sola vez, cuando entra en la ventana de los
     * proximos dos dias. Avisar en cuanto se publica seria inutil —puede faltar
     * un mes— y avisar todos los dias hasta que ocurra seria insoportable.
     */
    private suspend fun revisarEventos() {
        val pagina = Datos.api.inventario(tipoItem = "evento", porPagina = 50).oNulo() ?: return
        val avisados = Ajustes.eventosAvisados
        val ahora = System.currentTimeMillis()
        val limite = ahora + VENTANA_MS

        val proximos = pagina.items.filter { evento ->
            if (evento.id in avisados) return@filter false
            val inicio = Calendario.enMilisegundos(evento.fechas?.inicio) ?: return@filter false
            inicio in ahora..limite
        }

        // Igual que con los articulos: solo se recuerda lo que salio. Marcar
        // los que no entraron en el tope los dejaba sin aviso para siempre, y a
        // un evento eso le pasa dentro de la ventana de dos dias — o se avisa
        // en la vuelta siguiente o ya ocurrio.
        val salieron = proximos.take(MAX_AVISOS_POR_VUELTA)
        salieron.forEach { evento ->
            Avisos.avisarEvento(
                contexto = applicationContext,
                id = evento.id,
                titulo = evento.titulo,
                cuando = fechaLegible(evento.fechas?.inicio),
            )
        }
        if (salieron.isNotEmpty()) {
            Ajustes.eventosAvisados = avisados + salieron.map { it.id }
            Registro.info(ETIQUETA, "${salieron.size} de ${proximos.size} eventos proximos avisados")
        }
    }

    /** DD.MM, que es como el resto de la app escribe una fecha corta. */
    private fun fechaLegible(iso: String?): String {
        if (iso == null || iso.length < 10) return ""
        return iso.substring(8, 10) + "." + iso.substring(5, 7)
    }

    companion object {
        private const val ETIQUETA = "Vigilante"
        private const val TRABAJO = "vigilante-novedades"

        /** Dos dias: alcanza para acomodar el fin de semana y no llega tarde. */
        private const val VENTANA_MS = 2L * 24 * 60 * 60 * 1000

        /**
         * Tope por vuelta. Si el panel carga veinte articulos de una, la persona
         * recibe cinco avisos y no veinte: pasado ese punto no informa, molesta.
         */
        private const val MAX_AVISOS_POR_VUELTA = 5

        /**
         * Programa la revision. Es idempotente: llamarlo en cada arranque
         * mantiene el trabajo vivo sin duplicarlo.
         */
        fun programar(contexto: Context) {
            val trabajo = PeriodicWorkRequestBuilder<Vigilante>(6, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder()
                        // Sin red no hay nada que revisar; que espere en vez de
                        // despertar el telefono para fallar.
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build(),
                )
                .build()

            intentar(ETIQUETA, "programar la revision periodica") {
                WorkManager.getInstance(contexto).enqueueUniquePeriodicWork(
                    TRABAJO,
                    ExistingPeriodicWorkPolicy.KEEP,
                    trabajo,
                )
            }
        }

        /** Se cancela al apagar los avisos: un trabajo que no va a avisar no corre. */
        fun cancelar(contexto: Context) {
            intentar(ETIQUETA, "cancelar la revision periodica") {
                WorkManager.getInstance(contexto).cancelUniqueWork(TRABAJO)
            }
        }
    }
}
