package net.caaguazu.turismo

import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Las claves de texto y el codigo, sincronizados.
 *
 * Dos derivas distintas, ambas silenciosas: una clave que el codigo pide y que
 * no existe se ve como un hueco marcado en pantalla; una clave declarada que
 * nadie usa es trabajo de redaccion que no sirve a nadie.
 */
class ClavesDeTextoTest {

    /** Filenames y rutas tienen la misma forma que una clave; no lo son. */
    private val extensiones = setOf("json", "txt", "pmtiles", "pbf", "png", "webp", "kt")

    private val declaradas: Set<String> by lazy {
        val json = java.io.File("src/main/assets/textos/es.json").readText()
        Regex("""^\s*"([^"]+)"\s*:""", RegexOption.MULTILINE)
            .findAll(json)
            .map { it.groupValues[1] }
            .toSet()
            .also { check(it.isNotEmpty()) { "No se pudieron leer las claves de es.json" } }
    }

    private val usadas: Set<String> by lazy {
        // La app entera, no solo las pantallas: los avisos del sistema piden
        // claves desde core y tambien tienen que estar declaradas.
        java.io.File("src/main/java/net/caaguazu/turismo")
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { archivo ->
                Regex(""""([a-z]+(?:\.[a-zA-Z]+)+)"""")
                    .findAll(archivo.readText())
                    .map { it.groupValues[1] }
            }
            .filterNot { it.substringAfterLast('.') in extensiones }
            .toSet()
    }

    @Test
    fun `toda clave que el codigo pide esta declarada`() {
        val sinDeclarar = (usadas - declaradas).sorted()
        assertTrue(
            "El codigo pide claves que no existen en es.json, y saldrian marcadas " +
                "en pantalla:\n" + sinDeclarar.joinToString("\n"),
            sinDeclarar.isEmpty(),
        )
    }

    @Test
    fun `no se pide redactar textos que nadie muestra`() {
        val sinUsar = (declaradas - usadas).sorted()
        assertTrue(
            "Estas claves estan declaradas pero ninguna pantalla las usa:\n" +
                sinUsar.joinToString("\n"),
            sinUsar.isEmpty(),
        )
    }
}
