package net.caaguazu.turismo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Las reglas duras del sistema "Alpine Editorial", verificadas sobre el codigo.
 *
 * El sistema tiene unas pocas reglas que no admiten excepcion, y son justo las
 * que se erosionan sin que nadie lo note: una sombra de mas aca, un radio
 * inventado alla. Revisarlas a ojo en cada cambio no funciona; revisarlas en
 * cada compilacion, si.
 */
class SistemaDeDisenoTest {

    private val fuentes: List<File> by lazy {
        File("src/main/java/net/caaguazu/turismo/ui")
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .toList()
            .also { check(it.isNotEmpty()) { "No se encontraron fuentes de interfaz" } }
    }

    /** "Cero sombras salvo el FAB." Es literal: una en toda la app. */
    @Test
    fun `hay una sola sombra en toda la app`() {
        val conSombra = fuentes.filter { it.readText().contains(".shadow(") }
        val cuantas = fuentes.sumOf { archivo ->
            Regex("""\.shadow\(""").findAll(archivo.readText()).count()
        }

        assertEquals(
            "El sistema admite una sola sombra, la del boton central. Aparecen en: " +
                conSombra.joinToString { it.name },
            1,
            cuantas,
        )
        assertEquals(
            "La unica sombra tiene que ser la del boton central de la barra inferior",
            "BarraInferior.kt",
            conSombra.single().name,
        )
    }

    /**
     * "Toda superficie de contenido a radio 0; todo control a radio 999, u 8 en
     * tarjetas de lista." Un radio escrito a mano es una tercera opcion que el
     * sistema no tiene.
     */
    @Test
    fun `los radios salen del sistema y no de un numero suelto`() {
        val permitidos = setOf("Radio.completo", "Radio.lista")
        val sueltos = fuentes.flatMap { archivo ->
            Regex("""RoundedCornerShape\(([^)]+)\)""")
                .findAll(archivo.readText())
                .map { it.groupValues[1].trim() }
                .filterNot { it in permitidos }
                .map { "${archivo.name} → RoundedCornerShape($it)" }
        }

        assertTrue(
            "Los radios tienen que salir de Radio.*, no de un numero escrito a mano:\n" +
                sueltos.joinToString("\n"),
            sueltos.isEmpty(),
        )
    }

    /**
     * La paleta vive en Tono. Un color escrito a mano en una pantalla es un
     * token fuera del sistema que despues nadie encuentra para cambiar.
     */
    @Test
    fun `los colores salen de la paleta`() {
        // Se exceptua el archivo de tokens, que es donde la paleta se define, y
        // los iconos, cuyo color base lo reemplaza siempre quien los dibuja.
        val exentos = setOf("Tokens.kt", "Iconos.kt")
        val sueltos = fuentes
            .filterNot { it.name in exentos }
            .flatMap { archivo ->
                Regex("""Color\(0x[0-9A-Fa-f]{8}\)""")
                    .findAll(archivo.readText())
                    .map { "${archivo.name} → ${it.value}" }
            }

        assertTrue(
            "Estos colores tienen que estar en Tono y no sueltos en una pantalla:\n" +
                sueltos.joinToString("\n"),
            sueltos.isEmpty(),
        )
    }
}
