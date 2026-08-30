package net.caaguazu.turismo

import kotlinx.serialization.json.Json
import net.caaguazu.turismo.core.HtmlSencillo
import net.caaguazu.turismo.datos.Ficha
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * La ficha tal como la sirve el panel hoy, de punta a punta.
 *
 * A diferencia del resto de fixtures —que son la forma del contrato— esta es
 * una copia literal de una respuesta de produccion. Existe porque el cuerpo de
 * la ficha nunca se habia dibujado: el campo llegaba con un nombre que el
 * modelo no leia, asi que el analizador de HTML jamas se ejercito con contenido
 * real. Un fallo ahi ahora se ve como una ficha que se cae al abrirse.
 */
class FichaRealTest {

    private val analizador = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    private val ficha: Ficha = analizador.decodeFromString(
        Ficha.serializer(),
        File("src/test/resources/contrato/ficha-produccion.json").readText(),
    )

    @Test
    fun `la ficha de produccion trae el cuerpo y se puede dibujar`() {
        assertEquals(260, ficha.id)
        assertTrue("la ficha no trajo cuerpo", ficha.articuloHtml.isNotBlank())

        val bloques = HtmlSencillo.bloques(ficha.articuloHtml)
        assertTrue("el cuerpo real no produjo ningun bloque", bloques.isNotEmpty())

        // Cinco parrafos de historia: si el analizador se come alguno, la ficha
        // se ve incompleta sin que nada falle a la vista.
        val parrafos = bloques.filterIsInstance<HtmlSencillo.Bloque.Parrafo>()
        assertEquals("se perdieron parrafos del cuerpo", 5, parrafos.size)
        assertTrue(
            "el primer parrafo llego vacio",
            parrafos.first().texto.text.startsWith("Ykua La Patria es el manantial"),
        )
    }

    @Test
    fun `los datos que pinta la ficha llegan completos`() {
        assertEquals("Ykua La Patria", ficha.titulo)
        assertEquals("Sitio Natural", ficha.categoria?.nombre)
        assertTrue("falta el horario", ficha.practicos.horario.isNotBlank())
        assertTrue("falta el costo", ficha.practicos.costo.isNotBlank())
        assertTrue("falta el estado del camino", ficha.acceso.estadoCamino.isNotBlank())
        assertTrue("faltan las fuentes", ficha.fuentes.isNotBlank())
        assertEquals(0, ficha.practicos.rangoPrecio)
        assertTrue("falta la portada", ficha.portada != null)
        assertTrue("faltan las coordenadas", ficha.coordenadas != null)
    }
}
