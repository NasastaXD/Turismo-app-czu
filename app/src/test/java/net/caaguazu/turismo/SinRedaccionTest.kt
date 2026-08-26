package net.caaguazu.turismo

import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.File

/**
 * Impide que un texto visible se escriba directamente en el codigo.
 *
 * El requisito del proyecto es duro: cualquier texto de interfaz tiene que poder
 * cambiarse sin publicar un APK nuevo, y la redaccion la hace una persona. Si un
 * literal se cuela en una pantalla, deja de cumplirse lo uno y lo otro.
 *
 * Por eso esto no es una convencion documentada sino una prueba: el unico camino de
 * un texto a la pantalla es Textos.t("clave").
 */
class SinRedaccionTest {

    /** Donde no aplica: definen el mecanismo, no son pantallas. */
    private val exentos = setOf("Textos.kt", "Basicos.kt")

    @Test
    fun `ninguna pantalla escribe texto visible`() {
        val faltas = fuentesDeInterfaz().flatMap { archivo ->
            buscarLiterales(archivo.readText()).map { "${archivo.name} → \"$it\"" }
        }

        if (faltas.isNotEmpty()) {
            fail(
                "Hay texto escrito directamente en la interfaz. Tiene que salir de " +
                    "Textos.t(\"clave\") y del JSON de textos:\n" + faltas.joinToString("\n"),
            )
        }
    }

    /**
     * La propia comprobacion se verifica contra un caso que debe detectar. Una prueba
     * de este tipo que dejara de detectar seria peor que no tenerla: daria confianza
     * sin darla.
     */
    @Test
    fun `la comprobacion detecta un texto redactado a mano`() {
        val ejemplo = """
            @Composable
            fun Pantalla() {
                Texto(
                    texto = "Esta seccion estara disponible pronto",
                    estilo = Letra.descripcion,
                )
            }
        """.trimIndent()

        assertTrue(
            "La comprobacion dejo pasar un literal en un parametro de texto",
            buscarLiterales(ejemplo).isNotEmpty(),
        )
        assertTrue(
            "La comprobacion marco como falta un texto que si viene de Textos",
            buscarLiterales("""Texto(texto = Textos.t("nav.principal"), estilo = e)""").isEmpty(),
        )
    }

    /**
     * Busca literales en los parametros por los que un texto llega a la pantalla.
     * Se analiza el archivo entero y no linea a linea, porque en Compose el literal
     * casi siempre queda en una linea distinta de la llamada.
     */
    private fun buscarLiterales(fuente: String): List<String> {
        val limpio = fuente
            .replace(Regex("""/\*.*?\*/""", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("""//[^\n]*"""), "")

        val enParametro = Regex("""\b(texto|text|descripcion|contentDescription)\s*=\s*"([^"]{2,})"""")
        val posicional = Regex("""\b(Texto|BasicText|Text)\s*\(\s*"([^"]{2,})"""")

        return (enParametro.findAll(limpio) + posicional.findAll(limpio))
            .map { it.groupValues[2] }
            .toList()
    }

    private fun fuentesDeInterfaz(): List<File> =
        File("src/main/java/net/caaguazu/turismo/ui")
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" && it.name !in exentos }
            .toList()
            .also { check(it.isNotEmpty()) { "No se encontraron fuentes de interfaz que revisar" } }
}
