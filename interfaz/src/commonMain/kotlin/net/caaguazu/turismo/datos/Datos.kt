package net.caaguazu.turismo.datos

import net.caaguazu.turismo.core.Cache
import net.caaguazu.turismo.core.Entorno
import net.caaguazu.turismo.core.Http
import net.caaguazu.turismo.core.Idioma
import net.caaguazu.turismo.core.Bitacora
import net.caaguazu.turismo.core.Resultado
import net.caaguazu.turismo.core.Textos

/**
 * Punto unico de acceso a los datos.
 *
 * Sin inyeccion de dependencias: la app tiene una sola implementacion viva a la
 * vez y un solo lugar donde se elige cual. Un contenedor de dependencias aca
 * seria maquinaria para resolver un problema que no existe.
 */
object Datos {

    private const val ETIQUETA = "Datos"

    lateinit var api: ApiHttp
        private set

    lateinit var cache: Cache
        private set

    /**
     * Las dos rutas y la URL base salen de `Entorno`, que cada cascara rellena
     * al arrancar. Antes salian de un `Context` y de `BuildConfig`, o sea de
     * dos cosas que solo existen en Android.
     */
    fun iniciar() {
        cache = Cache(Entorno.carpetaCache)
        api = ApiHttp(Http(cache), Entorno.urlBase)
    }

    /**
     * Refresca los textos de interfaz desde el servidor.
     *
     * Si falla no pasa nada visible: queda el respaldo embebido, que es
     * exactamente para lo que existe.
     */
    /**
     * Trae la lista de idiomas del panel. Si el endpoint no esta —un servidor
     * anterior a 0.8.0 responde 404— queda la lista de respaldo, que alcanza
     * para que el selector funcione igual.
     */
    suspend fun refrescarIdiomas() {
        when (val respuesta = api.idiomas()) {
            is Resultado.Bien -> Idioma.aplicarDisponibles(
                respuesta.valor.idiomas.map { Idioma.Disponible(it.codigo, it.nombre.ifBlank { it.codigo }) },
            )
            is Resultado.Mal -> Bitacora.aviso(
                ETIQUETA,
                "sin lista de idiomas (${respuesta.falla}); sigue la de respaldo",
            )
        }
    }

    suspend fun refrescarTextos(idioma: String = Idioma.actual) {
        when (val respuesta = api.textos(idioma)) {
            is Resultado.Bien -> Textos.aplicarMapa(respuesta.valor, "servidor/$idioma")
            is Resultado.Mal -> Bitacora.aviso(
                ETIQUETA,
                "no se pudieron refrescar los textos (${respuesta.falla}); sigue el respaldo embebido",
            )
        }
    }
}
