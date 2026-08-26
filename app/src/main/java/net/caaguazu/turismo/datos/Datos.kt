package net.caaguazu.turismo.datos

import android.content.Context
import net.caaguazu.turismo.BuildConfig
import net.caaguazu.turismo.core.Cache
import net.caaguazu.turismo.core.Http
import net.caaguazu.turismo.core.Registro
import net.caaguazu.turismo.core.Resultado
import net.caaguazu.turismo.core.Textos
import java.io.File

/**
 * Punto unico de acceso a los datos.
 *
 * Sin inyeccion de dependencias: la app tiene una sola implementacion viva a la
 * vez y un solo lugar donde se elige cual. Un contenedor de dependencias aca
 * seria maquinaria para resolver un problema que no existe.
 */
object Datos {

    private const val ETIQUETA = "Datos"

    lateinit var api: Contrato
        private set

    lateinit var cache: Cache
        private set

    /** De donde salen los datos ahora mismo. Se muestra en la pantalla de diagnostico. */
    var origen: String = ""
        private set

    fun iniciar(contexto: Context) {
        cache = Cache(File(contexto.cacheDir, "api"))

        api = if (BuildConfig.USAR_MOCKS) {
            origen = "mocks (assets)"
            ApiMock(contexto.assets)
        } else {
            origen = BuildConfig.URL_BASE
            ApiHttp(Http(cache), BuildConfig.URL_BASE)
        }

        Registro.info(ETIQUETA, "origen de datos: $origen")
    }

    /**
     * Refresca los textos de interfaz desde el servidor.
     *
     * Si falla no pasa nada visible: queda el respaldo embebido, que es
     * exactamente para lo que existe.
     */
    suspend fun refrescarTextos(idioma: String = "es") {
        when (val respuesta = api.textos(idioma)) {
            is Resultado.Bien -> Textos.aplicarMapa(respuesta.valor, "servidor/$idioma")
            is Resultado.Mal -> Registro.aviso(
                ETIQUETA,
                "no se pudieron refrescar los textos (${respuesta.falla}); sigue el respaldo embebido",
            )
        }
    }
}
