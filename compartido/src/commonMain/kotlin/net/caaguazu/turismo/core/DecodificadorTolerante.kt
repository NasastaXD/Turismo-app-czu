package net.caaguazu.turismo.core

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
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
            // `as?` y `intOrNull` y no `jsonArray`/`int`: los dos lanzan, y
            // `JsonNull` cuenta como primitivo, asi que un `"total": null` —o
            // un `"items": null`— tumbaba la pagina completa aunque los mil
            // elementos vinieran bien. Este decodificador existe justo para
            // que un dato flojo de la envoltura no borre el contenido.
            items = elementos(objeto["items"] as? JsonArray, origen, elemento),
            total = entero(objeto["total"]) ?: 0,
            pagina = entero(objeto["pagina"]) ?: 1,
            porPagina = entero(objeto["por_pagina"]) ?: 20,
        )
    }

    private fun entero(elemento: JsonElement?): Int? = (elemento as? JsonPrimitive)?.intOrNull

    fun <T> lista(texto: String, origen: String, elemento: KSerializer<T>): List<T> =
        elementos(Analizador.parseToJsonElement(texto).jsonArray, origen, elemento)

    private fun <T> elementos(array: JsonArray?, origen: String, elemento: KSerializer<T>): List<T> {
        if (array == null) return emptyList()
        return array.mapNotNull { item ->
            try {
                Analizador.decodeFromJsonElement(elemento, item)
            } catch (e: Throwable) {
                Bitacora.aviso(ETIQUETA, "un elemento de $origen no encaja con el modelo, se omite: ${e.message}")
                null
            }
        }
    }
}
