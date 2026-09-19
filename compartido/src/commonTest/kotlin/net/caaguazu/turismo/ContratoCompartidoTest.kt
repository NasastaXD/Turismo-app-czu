package net.caaguazu.turismo

import kotlinx.serialization.builtins.ListSerializer
import net.caaguazu.turismo.core.Analizador
import net.caaguazu.turismo.datos.Categoria
import net.caaguazu.turismo.datos.Ficha
import net.caaguazu.turismo.datos.ItemInventario
import net.caaguazu.turismo.datos.Pagina
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * El contrato, decodificado en cada plataforma.
 *
 * `ContratoTest` en :app ya cubre esto mismo contra los payloads completos,
 * pero corre solo en la JVM. Esta version corre tambien en el simulador de
 * iOS, que es la unica forma de saber que los modelos y el analizador se
 * comportan igual en Kotlin/Native — la serializacion no siempre se comporta
 * igual fuera de la JVM, y descubrirlo en un telefono seria tarde.
 *
 * Los payloads van embebidos y recortados a proposito: leer archivos no es
 * portable sin sumar una dependencia, y lo que se prueba aca es la forma del
 * contrato, no el contenido de ejemplo.
 */
class ContratoCompartidoTest {

    @Test
    fun `un item de inventario decodifica con los campos del contrato`() {
        val item = Analizador.decodeFromString(
            ItemInventario.serializer(),
            """
            {"id":260,"tipo_item":"evento","titulo":"x","gancho":"y",
             "categoria":{"id":12,"slug":"natural","nombre":"Natural","color":"#2E7D32"},
             "coordenadas":{"lat":-25.47,"lng":-56.02},
             "rango_precio":2,"horario_resumen":"8 a 17",
             "fechas":{"inicio":"2026-10-01T09:00:00","en_curso":false,"terminado":false},
             "google_maps":"https://maps.example/x","idioma":"es","traducido":false}
            """.trimIndent(),
        )

        assertEquals(260, item.id)
        assertEquals("evento", item.tipoItem)
        assertEquals(12, item.categoria?.id)
        assertEquals(-25.47, item.coordenadas?.lat)
        assertEquals(2, item.rangoPrecio)
        assertEquals("8 a 17", item.horarioResumen)
        assertNotNull(item.fechas?.inicio)
    }

    /** Los nombres con guion bajo del panel tienen que caer en el campo correcto. */
    @Test
    fun `el renombrado de campos del contrato se respeta`() {
        val ficha = Analizador.decodeFromString(
            Ficha.serializer(),
            """{"id":1,"titulo":"x","descripcion":"<p>cuerpo</p>","articulos_relacionados":[]}""",
        )
        assertEquals("<p>cuerpo</p>", ficha.articuloHtml)
    }

    /** Un campo que el panel agregue manana no puede tumbar una app publicada. */
    @Test
    fun `un campo desconocido no rompe la decodificacion`() {
        val item = Analizador.decodeFromString(
            ItemInventario.serializer(),
            """{"id":7,"titulo":"x","campo_que_no_existia":{"anidado":true}}""",
        )
        assertEquals(7, item.id)
    }

    @Test
    fun `una pagina cuenta bien su total`() {
        val pagina = Analizador.decodeFromString(
            Pagina.serializer(ItemInventario.serializer()),
            """{"items":[{"id":1,"titulo":"a"},{"id":2,"titulo":"b"}],
                "total":40,"pagina":1,"por_pagina":20}""".trimIndent(),
        )
        assertEquals(2, pagina.items.size)
        assertEquals(40, pagina.total)
        assertTrue(pagina.hayMas(), "con 40 de total y 20 por pagina tiene que haber mas")
    }

    /** `imagen` es el nombre que usa el panel; `portada` el que usa el modelo. */
    @Test
    fun `la categoria acepta la foto por su nombre real y trae descripcion`() {
        val categorias = Analizador.decodeFromString(
            ListSerializer(Categoria.serializer()),
            """[{"id":12,"nombre":"Natural","descripcion":"Saltos y cerros.",
                "imagen":{"url":"https://x/y.jpg","w":1600,"h":900}},
               {"id":13,"nombre":"Otra"}]""".trimIndent(),
        )

        assertEquals("Saltos y cerros.", categorias[0].descripcion)
        assertEquals("https://x/y.jpg", categorias[0].portada?.url)
        assertEquals(16f / 9f, categorias[0].portada?.proporcion())
        assertTrue(categorias[1].descripcion.isEmpty(), "sin descripcion tiene que quedar vacia, no null")
    }
}
