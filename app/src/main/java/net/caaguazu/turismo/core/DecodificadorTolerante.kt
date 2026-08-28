package net.caaguazu.turismo.core

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.caaguazu.turismo.datos.Pagina

/**
 * Decodifica listas y paginas elemento por elemento, en vez de todo o nada.
 *
 * `decodeFromString` sobre un array entero tumba la respuesta completa si un solo
 * campo de un solo elemento no encaja con el modelo. Con miles de sitios eso
 * convierte un dato malo en una pagina entera, cuando lo unico realmente invalido
 * es ese elemento: el resto sigue siendo bueno y no hay motivo para no mostrarlo.
 */
object DecodificadorTolerante {

    private const val ETIQUETA = "Decodificador"

    fun <T> pagina(texto: String, origen: String, elemento: KSerializer<T>): Pagina<T> {
        val objeto = Analizador.parseToJsonElement(texto).jsonObject
        return Pagina(
            items = elementos(objeto["items"]?.jsonArray, origen, elemento),
            total = objeto["total"]?.jsonPrimitive?.int ?: 0,
            pagina = objeto["pagina"]?.jsonPrimitive?.int ?: 1,
            porPagina = objeto["por_pagina"]?.jsonPrimitive?.int ?: 20,
        )
    }

    fun <T> lista(texto: String, origen: String, elemento: KSerializer<T>): List<T> =
        elementos(Analizador.parseToJsonElement(texto).jsonArray, origen, elemento)

    private fun <T> elementos(array: JsonArray?, origen: String, elemento: KSerializer<T>): List<T> {
        if (array == null) return emptyList()
        return array.mapNotNull { item ->
            try {
                Analizador.decodeFromJsonElement(elemento, item)
            } catch (e: Throwable) {
                Registro.aviso(ETIQUETA, "un elemento de $origen no encaja con el modelo, se omite: ${e.message}")
                null
            }
        }
    }
}
