package net.caaguazu.turismo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Los tres juegos de textos embebidos, alineados.
 *
 * Traducir es facil; mantener tres archivos sincronizados a mano no lo es. Una
 * clave que se agrega solo en castellano sale en castellano dentro de la app en
 * ingles —el piso la cubre— y nadie se entera de que falta. Esto lo dice en
 * cada compilacion.
 */
class TraduccionesTest {

    private val idiomas = listOf("es", "en", "pt")

    private fun claves(codigo: String): Map<String, String> {
        val texto = File("src/main/assets/textos/$codigo.json").readText()
        return Regex(""""([^"]+)"\s*:\s*"((?:[^"\\]|\\.)*)"""")
            .findAll(texto)
            .associate { it.groupValues[1] to it.groupValues[2] }
    }

    @Test
    fun `los tres idiomas tienen exactamente las mismas claves`() {
        val base = claves("es")
        assertTrue("el castellano deberia tener textos", base.isNotEmpty())

        for (codigo in idiomas.drop(1)) {
            val otro = claves(codigo)
            assertEquals(
                "a $codigo le faltan claves que si estan en castellano",
                emptySet<String>(),
                base.keys - otro.keys,
            )
            assertEquals(
                "$codigo tiene claves que ya no existen en castellano",
                emptySet<String>(),
                otro.keys - base.keys,
            )
        }
    }

    @Test
    fun `ningun texto quedo vacio ni sin traducir`() {
        for (codigo in idiomas) {
            claves(codigo).forEach { (clave, valor) ->
                assertTrue("$codigo.$clave esta vacia", valor.isNotBlank())
                assertTrue(
                    "$codigo.$clave quedo marcada sin redactar",
                    !valor.startsWith("‹"),
                )
            }
        }
    }

    /**
     * La atribucion de OpenStreetMap es obligatoria por licencia y no es texto
     * de producto: dice lo mismo en cualquier idioma. Traducirla o recortarla
     * deja a la app incumpliendo la ODbL.
     */
    @Test
    fun `la atribucion del mapa es identica en los tres`() {
        val esperada = claves("es")["mapa.atribucion"]
        assertEquals("© OpenStreetMap", esperada)
        for (codigo in idiomas) {
            assertEquals(
                "la atribucion cambio en $codigo",
                esperada,
                claves(codigo)["mapa.atribucion"],
            )
        }
    }

    /** El nombre del lugar es un nombre propio: no se traduce. */
    @Test
    fun `el nombre de la app no se traduce`() {
        for (codigo in idiomas) {
            assertEquals("Caaguazú", claves(codigo)["app.nombre"])
        }
    }
}
