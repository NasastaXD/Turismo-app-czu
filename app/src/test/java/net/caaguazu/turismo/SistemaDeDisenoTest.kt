package net.caaguazu.turismo

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Las reglas duras del sistema visual, verificadas sobre el codigo.
 *
 * El sistema tiene unas pocas reglas que no admiten excepcion, y son justo las
 * que se erosionan sin que nadie lo note: un radio inventado aca, una elevacion
 * escrita a mano alla, un color suelto que despues nadie encuentra para
 * cambiar. Revisarlas a ojo en cada cambio no funciona; revisarlas en cada
 * compilacion, si.
 *
 * Ninguna de estas prueba que el sistema sea lindo. Prueban que sea UNO: que la
 * app entera se pueda cambiar desde los tokens y no archivo por archivo.
 */
class SistemaDeDisenoTest {

    private val fuentes: List<File> by lazy {
        File("src/main/java/net/caaguazu/turismo/ui")
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .toList()
            .also { check(it.isNotEmpty()) { "No se encontraron fuentes de interfaz" } }
    }

    /**
     * Contenido redondeado, control a radio completo, y nada mas. Un radio
     * escrito a mano es una opcion fuera del sistema.
     */
    @Test
    fun `los radios salen del sistema y no de un numero suelto`() {
        val permitidos = setOf(
            "Radio.ninguno",
            "Radio.tarjeta",
            "Radio.media",
            "Radio.lista",
            "Radio.hoja",
            "Radio.completo",
            // El radio que recibe una pieza reutilizable ya salio de Radio.*
            // en quien la llama; volver a exigirlo aca seria pedirlo dos veces.
            "radio",
        )
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
     * La elevacion tiene dos alturas y salen de Elevacion. Una sombra con un
     * numero escrito a mano es una tercera altura que nadie decidio.
     */
    @Test
    fun `las sombras salen de la escala de elevacion`() {
        val permitidos = setOf("Elevacion.tarjeta", "Elevacion.flotante", "elevacion")
        val sueltas = fuentes.flatMap { archivo ->
            Regex("""\.shadow\(\s*([^,)]+)""")
                .findAll(archivo.readText())
                .map { it.groupValues[1].trim() }
                .filterNot { it in permitidos }
                .map { "${archivo.name} → shadow($it)" }
        }

        assertTrue(
            "La elevacion tiene que salir de Elevacion.*, no de un numero suelto:\n" +
                sueltas.joinToString("\n"),
            sueltas.isEmpty(),
        )
    }

    /**
     * Toda sombra lleva el color de sombra del sistema.
     *
     * Sin esto la sombra sale del negro por omision, que en modo oscuro pinta
     * un halo sucio alrededor de cada tarjeta. Es el error que solo se ve
     * cuando alguien abre la app de noche.
     */
    @Test
    fun `toda sombra usa el color de sombra del sistema`() {
        // Se mira la ventana que sigue a la llamada en vez de intentar casar los
        // parentesis con una expresion regular: una llamada de varias lineas
        // rompe cualquier intento de contarlos, y esto alcanza para lo que se
        // esta comprobando.
        val ventana = 260
        val sinColor = fuentes.flatMap { archivo ->
            val texto = archivo.readText()
            Regex("""\.shadow\(""")
                .findAll(texto)
                .filterNot { encontrado ->
                    val hasta = minOf(encontrado.range.first + ventana, texto.length)
                    texto.substring(encontrado.range.first, hasta).contains("Tono.sombra")
                }
                .map { "${archivo.name} → shadow(...) sin Tono.sombra" }
        }

        assertTrue(
            "Una sombra sin ambientColor/spotColor en Tono.sombra ensucia el modo oscuro:\n" +
                sinColor.joinToString("\n"),
            sinColor.isEmpty(),
        )
    }

    /**
     * La paleta vive en Tono. Un color escrito a mano en una pantalla es un
     * token fuera del sistema, y ademas no se entera del modo oscuro.
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

    /**
     * Todo color de Tono que dependa del modo tiene que estar definido en los
     * dos. Uno definido en uno solo se ve bien en claro y desaparece en oscuro.
     */
    @Test
    fun `la paleta cubre los dos modos`() {
        val tokens = File("src/main/java/net/caaguazu/turismo/ui/tema/Tokens.kt").readText()

        val condicionales = Regex("""val (\w+) get\(\) = if \(oscuro\)([^\n]+)""")
            .findAll(tokens)
            .toList()

        assertTrue(
            "Tono deberia tener colores que cambian con el modo; no se encontro ninguno",
            condicionales.isNotEmpty(),
        )

        val incompletos = condicionales
            .filterNot { it.groupValues[2].contains(" else ") }
            .map { it.groupValues[1] }

        assertTrue(
            "Estos tokens no definen su valor en modo claro:\n" + incompletos.joinToString("\n"),
            incompletos.isEmpty(),
        )
    }
}
