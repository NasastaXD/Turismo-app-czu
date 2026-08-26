package net.caaguazu.turismo

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import net.caaguazu.turismo.datos.Articulo
import net.caaguazu.turismo.datos.Categoria
import net.caaguazu.turismo.datos.Delta
import net.caaguazu.turismo.datos.Evento
import net.caaguazu.turismo.datos.Ficha
import net.caaguazu.turismo.datos.ItemInventario
import net.caaguazu.turismo.datos.Marcador
import net.caaguazu.turismo.datos.Medio
import net.caaguazu.turismo.datos.Pagina
import net.caaguazu.turismo.datos.Recorrido
import net.caaguazu.turismo.datos.ResumenArticulo
import net.caaguazu.turismo.datos.Zona
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Los modelos contra los payloads.
 *
 * Los mocks estan calcados de lo que devuelve el plugin czu-app/v1, campo por
 * campo. Si un modelo se desalinea del contrato, esto falla antes de que se note
 * como una pantalla vacia en un telefono.
 */
class ContratoTest {

    // El analizador de la app: la tolerancia a campos desconocidos es parte de
    // lo que se esta probando, no una comodidad del test.
    private val analizador = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    private fun <T> mock(archivo: String, serializador: KSerializer<T>): T =
        analizador.decodeFromString(serializador, File("src/main/assets/mocks/$archivo").readText())

    @Test
    fun `cada payload del contrato se interpreta`() {
        val categorias = mock("categorias.json", ListSerializer(Categoria.serializer()))
        assertTrue("sin categorias", categorias.isNotEmpty())
        assertTrue("una categoria sin color", categorias.all { it.color.isNotBlank() })

        assertTrue(mock("zonas.json", ListSerializer(Zona.serializer())).isNotEmpty())

        val inventario = mock("inventario.json", Pagina.serializer(ItemInventario.serializer()))
        assertEquals("el total no coincide con los items", inventario.items.size, inventario.total)
        assertTrue("una ficha sin coordenadas", inventario.items.all { it.coordenadas != null })

        val fichas = mock("fichas.json", ListSerializer(Ficha.serializer()))
        assertTrue("hay fichas de mas o de menos", fichas.size == inventario.items.size)
        assertNotNull("la ficha no trae autor", fichas.first().autor)

        val marcadores = mock("markers.json", ListSerializer(Marcador.serializer()))
        assertTrue("faltan marcadores", marcadores.size >= inventario.items.size)

        mock("eventos.json", Pagina.serializer(Evento.serializer()))
        mock("articulos.json", Pagina.serializer(ResumenArticulo.serializer()))
        mock("articulos-detalle.json", ListSerializer(Articulo.serializer()))
        mock("recorridos.json", Pagina.serializer(Recorrido.serializer()))
        mock("media-manifest.json", MapSerializer(String.serializer(), Medio.serializer()))
        mock("sync.json", Delta.serializer())

        for (locale in listOf("es", "en", "gn")) {
            val textos = mock("strings-$locale.json", MapSerializer(String.serializer(), String.serializer()))
            assertTrue("textos vacios en $locale", textos.isNotEmpty())
        }
    }

    /**
     * Una parada cuyo lugar se despublico llega con `disponible: false` y sin los
     * campos del sitio. Es el caso que rompe si el modelo los da por seguros.
     */
    @Test
    fun `una parada colgada no rompe el recorrido`() {
        val recorridos = mock("recorridos-detalle.json", ListSerializer(Recorrido.serializer()))
        val colgadas = recorridos.flatMap { it.paradas }.filter { !it.disponible }

        assertTrue("los mocks deberian incluir una parada colgada", colgadas.isNotEmpty())
        assertTrue("una parada colgada no deberia traer titulo", colgadas.all { it.titulo.isBlank() })
    }

    /** Un campo que el servidor agregue manana no puede tumbar una app publicada. */
    @Test
    fun `un campo desconocido no rompe nada`() {
        val conExtra = """
            {"id":1,"titulo":"x","campo_que_no_existia":{"anidado":true},"otro":[1,2]}
        """.trimIndent()
        val item = analizador.decodeFromString(ItemInventario.serializer(), conExtra)
        assertEquals(1, item.id)
    }

    /** Un JSON roto tiene que dar un fallo manejable, no una caida. */
    @Test
    fun `un json malformado falla de forma controlada`() {
        val roto = """{"id": "no soy un numero"}"""
        val fallo = try {
            analizador.decodeFromString(ItemInventario.serializer(), roto)
            null
        } catch (e: Throwable) {
            e
        }
        assertNotNull("deberia haber fallado al interpretar", fallo)
        assertTrue(
            "el fallo tiene que ser de serializacion, no un error de programa",
            fallo is kotlinx.serialization.SerializationException || fallo is IllegalArgumentException,
        )
    }
}
