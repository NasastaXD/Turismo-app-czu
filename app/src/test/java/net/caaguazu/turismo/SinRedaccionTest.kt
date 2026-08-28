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
        assertTrue(
            "Un valor interpolado desde los datos no es redaccion",
            buscarLiterales("Texto(texto = \"\${item.total}\", estilo = e)").isEmpty(),
        )
        assertTrue(
            "Una frase con un valor interpolado adentro si es redaccion",
            buscarLiterales("Texto(texto = \"Quedan \${item.total} lugares\", estilo = e)").isNotEmpty(),
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

        val soloInterpolacion = Regex("""\s*(\$\{[^}]*\}\s*)+""")
        val enParametro = Regex("""\b(texto|text|descripcion|contentDescription)\s*=\s*"([^"]{2,})"""")
        val posicional = Regex("""\b(Texto|BasicText|Text)\s*\(\s*"([^"]{2,})"""")

        return (enParametro.findAll(limpio) + posicional.findAll(limpio))
            .map { it.groupValues[2] }
            // Un valor interpolado desde los datos no es redaccion: "${'$'}{item.total}"
            // es un numero que viene del servidor, no una frase escrita a mano.
            .filterNot { soloInterpolacion.matches(it) }
            .toList()
    }

    /**
     * Se revisa la app entera y no solo las pantallas: un aviso del sistema es
     * texto que la persona lee igual que un titulo, y sale del mismo JSON.
     */
    private fun fuentesDeInterfaz(): List<File> =
        File("src/main/java/net/caaguazu/turismo")
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" && it.name !in exentos }
            .toList()
            .also { check(it.isNotEmpty()) { "No se encontraron fuentes de interfaz que revisar" } }
}

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
