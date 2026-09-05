package net.caaguazu.turismo

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import net.caaguazu.turismo.datos.Articulo
import net.caaguazu.turismo.datos.Categoria
import net.caaguazu.turismo.datos.Delta
import net.caaguazu.turismo.datos.Etiqueta
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
 * Los datos de esta prueba viven en src/test/resources y no en los assets de la
 * app, y esa separacion es deliberada: lo que la app sirve como ejemplo cambia
 * segun lo que haga falta demostrar, pero la forma del contrato no. Vaciar el
 * contenido de ejemplo no puede dejar de verificar el contrato.
 *
 * Los payloads estan calcados de lo que devuelve el plugin czu-app/v1, campo por
 * campo. Si un modelo se desalinea, esto falla antes de que se note como una
 * pantalla vacia en un telefono.
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
        analizador.decodeFromString(serializador, File("src/test/resources/contrato/$archivo").readText())

    @Test
    fun `cada payload del contrato se interpreta`() {
        val categorias = mock("categorias.json", ListSerializer(Categoria.serializer()))
        assertTrue("sin categorias", categorias.isNotEmpty())
        assertTrue("una categoria sin color", categorias.all { it.color.isNotBlank() })

        val etiquetas = mock("etiquetas.json", ListSerializer(Etiqueta.serializer()))
        assertTrue("sin etiquetas", etiquetas.isNotEmpty())

        assertTrue(mock("zonas.json", ListSerializer(Zona.serializer())).isNotEmpty())

        val inventario = mock("inventario.json", Pagina.serializer(ItemInventario.serializer()))
        assertEquals("el total no coincide con los items", inventario.items.size, inventario.total)
        assertTrue("una ficha sin coordenadas", inventario.items.all { it.coordenadas != null })
        assertTrue("un item de inventario sin etiquetas", inventario.items.any { it.etiquetas.isNotEmpty() })

        val eventos = inventario.items.filter { it.tipoItem == "evento" }
        assertTrue("los mocks deberian incluir al menos un evento", eventos.isNotEmpty())
        assertTrue("un evento sin fecha de inicio", eventos.all { it.fechas?.inicio != null })
        assertTrue(
            "el filtro de proximos deberia dejar afuera al menos uno ya terminado",
            eventos.any { it.fechas?.terminado == true },
        )

        val fichas = mock("fichas.json", ListSerializer(Ficha.serializer()))
        assertTrue("hay fichas de mas o de menos", fichas.size == inventario.items.size)
        assertNotNull("la ficha no trae autor", fichas.first().autor)
        assertTrue("la ficha no trae cuerpo", fichas.all { it.articuloHtml.isNotBlank() })

        val marcadores = mock("markers.json", ListSerializer(Marcador.serializer()))
        assertTrue("faltan marcadores", marcadores.size >= inventario.items.size)

        mock("eventos.json", Pagina.serializer(Evento.serializer()))
        val articulos = mock("articulos.json", Pagina.serializer(ResumenArticulo.serializer()))
        assertTrue("un articulo sin etiquetas", articulos.items.any { it.etiquetas.isNotEmpty() })
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

    /**
     * `costo_total` paso de string a objeto (`hay_pago` + `detalle[]`). Antes
     * de este cambio de modelo, esto tumbaba la decodificacion entera del
     * recorrido en vez de quedar en un campo vacio.
     */
    @Test
    fun `el costo total del recorrido decodifica como objeto`() {
        val recorridos = mock("recorridos-detalle.json", ListSerializer(Recorrido.serializer()))

        assertTrue("los mocks deberian traer costo_total", recorridos.all { it.costoTotal != null })
        assertTrue(
            "hay_pago en true deberia traer detalle",
            recorridos.all { r -> r.costoTotal?.let { !it.hayPago || it.detalle.isNotEmpty() } ?: true },
        )

        val paradas = recorridos.flatMap { it.paradas }.filter { it.disponible }
        assertTrue("una parada disponible sin texto", paradas.any { it.texto.isNotBlank() })
        assertTrue("falta el audio/video de alguna parada", paradas.any { it.medio != null })
    }

    /**
     * El servidor real manda el cuerpo de la ficha como `descripcion` y la foto
     * de categoria como `imagen`, no como `articulo_html` ni `portada` que
     * preveia el contrato original. Sin aceptar el nombre real, una ficha se
     * veia sin cuerpo, sin galeria de texto y sin "leer mas" — el campo
     * llegaba, pero con un nombre que el modelo no sabia leer.
     */
    @Test
    fun `el modelo acepta los nombres de campo que el servidor real usa hoy`() {
        val fichaConDescripcion = """
            {"id":1,"titulo":"x","descripcion":"<p>Cuerpo real</p>"}
        """.trimIndent()
        val ficha = analizador.decodeFromString(Ficha.serializer(), fichaConDescripcion)
        assertEquals("<p>Cuerpo real</p>", ficha.articuloHtml)

        val categoriaConImagen = """
            {"id":1,"nombre":"x","imagen":{"url":"https://x/y.jpg","w":10,"h":10}}
        """.trimIndent()
        val categoria = analizador.decodeFromString(Categoria.serializer(), categoriaConImagen)
        assertNotNull("la categoria no trajo la foto por 'imagen'", categoria.portada)
        assertEquals("https://x/y.jpg", categoria.portada?.url)
    }

    /**
     * `caaguazu-app-api` 0.7.0: `/categorias` suma `descripcion` e `imagen`.
     * Solo llega en el catalogo — la `categoria` resumida embebida en una
     * ficha o un articulo se queda con el resumen de siempre, sin estos dos.
     */
    @Test
    fun `las categorias del catalogo traen descripcion, embebidas no`() {
        val delCatalogo = analizador.decodeFromString(
            Categoria.serializer(),
            """{"id":12,"nombre":"Sitio Natural","descripcion":"Saltos, cerros y reservas."}""",
        )
        assertEquals("Saltos, cerros y reservas.", delCatalogo.descripcion)

        val embebida = analizador.decodeFromString(
            Categoria.serializer(),
            """{"id":12,"nombre":"Sitio Natural","color":"#2E7D32"}""",
        )
        assertTrue("una categoria embebida no deberia traer descripcion", embebida.descripcion.isEmpty())
    }

    /**
     * El multi-idioma llega en 0.8.0 y la app tiene que andar contra las dos
     * versiones del panel: la vieja no manda `idioma` ni `traducido`, y sin un
     * valor por defecto sensato una respuesta de hoy dejaria de decodificar.
     */
    @Test
    fun `el idioma de una pieza decodifica con y sin los campos nuevos`() {
        val vieja = analizador.decodeFromString(
            Ficha.serializer(),
            """{"id":1,"titulo":"x"}""",
        )
        assertEquals("sin el campo, se asume el original", "es", vieja.idioma)
        assertTrue("en el original no hay nada traducido", !vieja.traducido)

        val nueva = analizador.decodeFromString(
            Ficha.serializer(),
            """{"id":1,"titulo":"x","idioma":"en","traducido":true}""",
        )
        assertEquals("en", nueva.idioma)
        assertTrue(nueva.traducido)
    }

    /**
     * `/categorias` y `/etiquetas` traducen `nombre` solos cuando se les pasa
     * `?idioma`. No hay que buscar nada aparte: lo que trae el campo es lo que
     * se muestra.
     */
    @Test
    fun `categorias y etiquetas ya traen el nombre traducido`() {
        val categoria = analizador.decodeFromString(
            Categoria.serializer(),
            """{"id":66,"slug":"sitio-natural","nombre":"Natural Site"}""",
        )
        assertEquals("Natural Site", categoria.nombre)

        val etiqueta = analizador.decodeFromString(
            Etiqueta.serializer(),
            """{"id":70,"slug":"al-aire-libre","nombre":"outdoors"}""",
        )
        assertEquals("outdoors", etiqueta.nombre)
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
