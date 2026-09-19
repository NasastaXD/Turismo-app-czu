package net.caaguazu.turismo

import net.caaguazu.turismo.core.huellaDeUrl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * La huella nombra archivos de cache: si dos URL distintas dieran la misma,
 * la app serviria una respuesta por otra. Es un fallo silencioso, asi que
 * conviene tenerlo cubierto.
 */
class HuellaTest {

    @Test
    fun `es de largo fijo y hexadecimal`() {
        val huella = huellaDeUrl("https://caaguazu.net/wp-json/czu-app/v1/categorias?idioma=es")
        assertEquals(32, huella.length)
        assertTrue(huella.all { it in "0123456789abcdef" }, "solo hexadecimal: $huella")
    }

    @Test
    fun `la misma URL da siempre la misma huella`() {
        val url = "https://caaguazu.net/wp-json/czu-app/v1/inventario?pagina=2"
        assertEquals(huellaDeUrl(url), huellaDeUrl(url))
    }

    /** El caso que importa: URL que se parecen mucho no pueden colisionar. */
    @Test
    fun `URL parecidas dan huellas distintas`() {
        val parecidas = listOf(
            "https://caaguazu.net/v1/inventario?pagina=1",
            "https://caaguazu.net/v1/inventario?pagina=2",
            "https://caaguazu.net/v1/inventario?pagina=11",
            "https://caaguazu.net/v1/inventario?idioma=es",
            "https://caaguazu.net/v1/inventario?idioma=en",
            "https://caaguazu.net/v1/fichas/260?idioma=es",
            "https://caaguazu.net/v1/fichas/261?idioma=es",
            "",
        )
        val huellas = parecidas.map { huellaDeUrl(it) }
        assertEquals(parecidas.size, huellas.toSet().size, "hubo una colision: $huellas")
    }

    @Test
    fun `las dos mitades no son iguales entre si`() {
        // Si las dos pasadas dieran lo mismo, seria un hash de 64 bits
        // disfrazado de 128 y el margen contra colisiones no existiria.
        val huella = huellaDeUrl("https://caaguazu.net/v1/categorias")
        assertNotEquals(huella.take(16), huella.drop(16))
    }

    /**
     * Ocho URL no prueban nada sobre colisiones. Esto genera muchas mas de las
     * que la app va a cachear en su vida y comprueba que ninguna pisa a otra.
     */
    @Test
    fun `veinte mil URL del contrato no colisionan`() {
        val urls = mutableListOf<String>()
        for (id in 1..4000) {
            for (idioma in listOf("es", "en", "pt")) {
                urls += "https://caaguazu.net/wp-json/czu-app/v1/fichas/$id?idioma=$idioma"
            }
            urls += "https://caaguazu.net/wp-json/czu-app/v1/inventario?pagina=$id"
            urls += "https://caaguazu.net/wp-json/czu-app/v1/articulos?pagina=$id"
        }

        val huellas = HashSet<String>(urls.size * 2)
        val repetidas = urls.filterNot { huellas.add(huellaDeUrl(it)) }

        assertEquals(20_000, urls.size, "el caso de prueba tiene que ser grande de verdad")
        assertTrue(repetidas.isEmpty(), "colisionaron ${repetidas.size}: ${repetidas.take(4)}")
    }

    @Test
    fun `un cambio de un solo caracter cambia la huella entera`() {
        val a = huellaDeUrl("https://caaguazu.net/v1/fichas/260")
        val b = huellaDeUrl("https://caaguazu.net/v1/fichas/261")
        val igualesEnPosicion = a.indices.count { a[it] == b[it] }
        assertTrue(igualesEnPosicion < 12, "demasiado parecidas: $a vs $b")
    }
}
