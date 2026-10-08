package net.caaguazu.turismo

import net.caaguazu.turismo.core.Analizador
import net.caaguazu.turismo.datos.EstadoAsistente
import net.caaguazu.turismo.datos.PreguntaAsistente
import net.caaguazu.turismo.datos.RespuestaAsistente
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * El contrato del asistente, de ida y de vuelta.
 *
 * Va en `commonTest` para que corra tambien en el simulador de iOS: la
 * pregunta se arma aca, en codigo compartido, y una diferencia de
 * serializacion entre plataformas se veria como un asistente que en un
 * telefono contesta y en el otro no.
 */
class AsistenteContratoTest {

    @Test
    fun `la primera pregunta no manda conversacion`() {
        val json = Analizador.encodeToString(
            PreguntaAsistente.serializer(),
            PreguntaAsistente(mensaje = "que hay hoy", idioma = "es"),
        )
        // Sin la clave, no con un null: el servidor arranca una conversacion
        // nueva cuando no viene, y asi no depende de como lea un null.
        assertFalse(json.contains("conversacion"), json)
        assertTrue(json.contains("\"idioma\":\"es\""), json)
    }

    @Test
    fun `la segunda pregunta lleva la conversacion que devolvio la primera`() {
        val json = Analizador.encodeToString(
            PreguntaAsistente.serializer(),
            PreguntaAsistente(mensaje = "y manana", conversacion = "3f2a9c1e-0b7d", idioma = "pt"),
        )
        assertTrue(json.contains("\"conversacion\":\"3f2a9c1e-0b7d\""), json)
    }

    @Test
    fun `la respuesta trae el texto y las fuentes para enlazar`() {
        val r = Analizador.decodeFromString(
            RespuestaAsistente.serializer(),
            """
            {"respuesta":"Abre de 8 a 17.","conversacion":"abc-123-def","idioma":"es",
             "fuentes":[{"tipo":"ficha","id":12,"titulo":"Salto Cristal","tipo_item":"sitio"},
                        {"tipo":"articulo","id":3,"titulo":"Historia"}],
             "campo_nuevo":{"x":1}}
            """.trimIndent(),
        )
        assertEquals("Abre de 8 a 17.", r.respuesta)
        assertEquals("abc-123-def", r.conversacion)
        assertEquals(2, r.fuentes.size)
        assertEquals("sitio", r.fuentes[0].tipoItem)
        // Un articulo no trae tipo_item: cae al vacio, no rompe.
        assertEquals("", r.fuentes[1].tipoItem)
    }

    @Test
    fun `sin fuentes la respuesta igual se lee`() {
        val r = Analizador.decodeFromString(RespuestaAsistente.serializer(), """{"respuesta":"Hola"}""")
        assertEquals("Hola", r.respuesta)
        assertTrue(r.fuentes.isEmpty())
    }

    @Test
    fun `el estado apagado es el de por defecto`() {
        assertFalse(Analizador.decodeFromString(EstadoAsistente.serializer(), "{}").disponible)
        assertTrue(Analizador.decodeFromString(EstadoAsistente.serializer(), """{"disponible":true}""").disponible)
    }
}
