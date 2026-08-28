package net.caaguazu.turismo.datos

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import net.caaguazu.turismo.core.Analizador
import net.caaguazu.turismo.core.DecodificadorTolerante
import net.caaguazu.turismo.core.Falla
import net.caaguazu.turismo.core.Http
import net.caaguazu.turismo.core.Registro
import net.caaguazu.turismo.core.Resultado
import java.net.URLEncoder

/**
 * La API real del panel, bajo /wp-json/czu-app/v1/.
 *
 * Solo arma URLs, pide y traduce. Toda la logica de red, ETag y copia offline
 * vive en Http, y toda la tolerancia a campos nuevos o nulos vive en Analizador.
 */
class ApiHttp(
    private val http: Http,
    private val urlBase: String,
) {

    private companion object { const val ETIQUETA = "Api" }

    suspend fun categorias() =
        pedirLista("categorias", Categoria.serializer())

    suspend fun etiquetas() =
        pedirLista("etiquetas", Etiqueta.serializer())

    suspend fun zonas() =
        pedirLista("zonas", Zona.serializer())

    suspend fun inventario(
        categoria: Int? = null,
        zona: Int? = null,
        etiqueta: Int? = null,
        buscar: String? = null,
        /** "sitio" o "evento". Es como se arma la agenda sin depender de `/eventos`. */
        tipoItem: String? = null,
        pagina: Int = 1,
        porPagina: Int = 20,
    ) = pedirPagina(
        ruta("inventario") {
            si("categoria", categoria)
            si("zona", zona)
            si("etiqueta", etiqueta)
            si("buscar", buscar)
            si("tipo_item", tipoItem)
            si("pagina", pagina)
            si("por_pagina", porPagina)
        },
        ItemInventario.serializer(),
    )

    suspend fun ficha(id: Int) =
        pedir("inventario/$id", Ficha.serializer())

    suspend fun marcadores() =
        pedirLista("mapa/markers", Marcador.serializer())

    suspend fun eventos(desde: String? = null, hasta: String? = null) = pedirPagina(
        ruta("eventos") {
            si("desde", desde)
            si("hasta", hasta)
        },
        Evento.serializer(),
    )

    suspend fun evento(id: Int) =
        pedir("eventos/$id", Evento.serializer())

    suspend fun recorridos() =
        pedirPagina("recorridos", Recorrido.serializer())

    suspend fun recorrido(id: Int) =
        pedir("recorridos/$id", Recorrido.serializer())

    suspend fun articulos(
        pagina: Int = 1,
        categoria: Int? = null,
        etiqueta: Int? = null,
        buscar: String? = null,
    ) = pedirPagina(
        ruta("articulos") {
            si("pagina", pagina)
            si("categoria", categoria)
            si("etiqueta", etiqueta)
            si("buscar", buscar)
        },
        ResumenArticulo.serializer(),
    )

    suspend fun articulo(id: Int) =
        pedir("articulos/$id", Articulo.serializer())

    suspend fun textos(idioma: String) =
        pedir("strings/$idioma", MapSerializer(String.serializer(), String.serializer()))

    suspend fun medios() =
        pedir("media-manifest", MapSerializer(String.serializer(), Medio.serializer()))

    suspend fun delta(desde: String?) = pedir(
        ruta("sync") { si("since", desde) },
        Delta.serializer(),
    )

    /* --------------------------------------------------------------------- */

    private suspend fun <T> pedir(ruta: String, serializador: KSerializer<T>): Resultado<T> =
        when (val respuesta = http.obtener(urlBase + ruta)) {
            is Resultado.Mal -> respuesta
            is Resultado.Bien -> interpretar(ruta) {
                Analizador.decodeFromString(serializador, respuesta.valor.texto)
            }
        }

    /**
     * Version tolerante para lo que trae muchos elementos: un solo campo fuera de
     * tipo en un elemento no puede tumbar toda la pagina o la lista completa, asi
     * que el elemento roto se omite y se registra en vez de fallar entero.
     */
    private suspend fun <T> pedirLista(ruta: String, elemento: KSerializer<T>): Resultado<List<T>> =
        when (val respuesta = http.obtener(urlBase + ruta)) {
            is Resultado.Mal -> respuesta
            is Resultado.Bien -> interpretar(ruta) {
                DecodificadorTolerante.lista(respuesta.valor.texto, ruta, elemento)
            }
        }

    private suspend fun <T> pedirPagina(ruta: String, elemento: KSerializer<T>): Resultado<Pagina<T>> =
        when (val respuesta = http.obtener(urlBase + ruta)) {
            is Resultado.Mal -> respuesta
            is Resultado.Bien -> interpretar(ruta) {
                DecodificadorTolerante.pagina(respuesta.valor.texto, ruta, elemento)
            }
        }

    /**
     * Un JSON que no encaja con el modelo es un fallo de datos, no una caida.
     * Queda registrado para poder rastrearlo desde el log que mande el usuario.
     */
    private fun <T> interpretar(ruta: String, decodificar: () -> T): Resultado<T> =
        try {
            Resultado.Bien(decodificar())
        } catch (e: Throwable) {
            Registro.fallo(ETIQUETA, "respuesta ilegible en $ruta", e)
            Resultado.Mal(Falla.DATOS_INVALIDOS)
        }

    private fun ruta(base: String, construir: Consulta.() -> Unit): String =
        base + Consulta().apply(construir).armar()

    /** Arma la cadena de consulta omitiendo lo que no se paso. */
    private class Consulta {
        private val partes = mutableListOf<String>()

        fun si(clave: String, valor: Any?) {
            if (valor == null) return
            if (valor is String && valor.isBlank()) return
            partes += "$clave=" + URLEncoder.encode(valor.toString(), "UTF-8")
        }

        fun armar(): String = if (partes.isEmpty()) "" else "?" + partes.joinToString("&")
    }
}
