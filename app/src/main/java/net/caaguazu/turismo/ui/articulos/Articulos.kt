package net.caaguazu.turismo.ui.articulos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.datos.AutorArticulo
import net.caaguazu.turismo.datos.Datos
import net.caaguazu.turismo.datos.ResumenArticulo
import net.caaguazu.turismo.ui.piezas.BotonIcono
import net.caaguazu.turismo.ui.piezas.CabeceraPantalla
import net.caaguazu.turismo.ui.piezas.CampoBusqueda
import net.caaguazu.turismo.ui.piezas.Cargador
import net.caaguazu.turismo.ui.piezas.ChipFiltro
import net.caaguazu.turismo.ui.piezas.Estado
import net.caaguazu.turismo.ui.piezas.Foto
import net.caaguazu.turismo.ui.piezas.Icono
import net.caaguazu.turismo.ui.piezas.Tarjeta
import net.caaguazu.turismo.ui.piezas.Texto
import net.caaguazu.turismo.ui.piezas.cargar
import net.caaguazu.turismo.ui.tema.Letra
import net.caaguazu.turismo.ui.tema.Medida
import net.caaguazu.turismo.ui.tema.Tono

/** Cuanto se espera despues de la ultima letra antes de pedirle a la API. */
private const val ESPERA_BUSQUEDA_MS = 350L

/** Pila propia de la seccion: lista y articulo abierto. */
class PilaArticulos {
    var abierto by mutableStateOf<Int?>(null)
        private set

    fun abrir(id: Int) { abierto = id }

    fun volver(): Boolean {
        if (abierto == null) return false
        abierto = null
        return true
    }

    fun raiz() { abierto = null }
}

@Composable
fun Articulos(
    pila: PilaArticulos,
    alAbrirPerfil: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val id = pila.abierto
    if (id == null) {
        ListaArticulos(alAbrir = pila::abrir, alAbrirPerfil = alAbrirPerfil, modifier = modifier)
    } else {
        PantallaArticulo(id = id, alVolver = { pila.volver() }, modifier = modifier)
    }
}

@Composable
private fun ListaArticulos(
    alAbrir: (Int) -> Unit,
    alAbrirPerfil: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var busqueda by remember { mutableStateOf("") }
    var buscarPor by remember { mutableStateOf("") }
    LaunchedEffect(busqueda) {
        delay(ESPERA_BUSQUEDA_MS)
        buscarPor = busqueda
    }
    var etiquetaElegida by remember { mutableStateOf<Int?>(null) }
    val (estadoEtiquetas, _) = cargar { Datos.api.etiquetas() }

    val (estado, reintentar) = cargar(buscarPor, etiquetaElegida) {
        Datos.api.articulos(etiqueta = etiquetaElegida, buscar = buscarPor.ifBlank { null })
    }

    Column(modifier.fillMaxSize().background(Tono.fondo)) {
        CabeceraPantalla(Textos.t("nav.articulos")) {
            BotonIcono(
                icono = Icono.perfil,
                descripcion = Textos.t("barra.perfil"),
                alTocar = alAbrirPerfil,
            )
        }
        CampoBusqueda(
            valor = busqueda,
            alCambiar = { busqueda = it },
            marcador = Textos.t("barra.buscar"),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Medida.margen)
                .padding(bottom = 12.dp),
        )
        val etiquetas = (estadoEtiquetas.value as? Estado.Listo)?.valor.orEmpty()
        if (etiquetas.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = Medida.margen),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp),
            ) {
                items(etiquetas, key = { it.id }) { etiqueta ->
                    ChipFiltro(
                        texto = etiqueta.nombre,
                        activo = etiquetaElegida == etiqueta.id,
                        alTocar = {
                            etiquetaElegida = if (etiquetaElegida == etiqueta.id) null else etiqueta.id
                        },
                    )
                }
            }
        }
        Cargador(
            estado = estado.value,
            reintentar = reintentar,
            vacio = { it.items.isEmpty() },
            modifier = Modifier.fillMaxSize(),
        ) { pagina ->
            LazyColumn(
                contentPadding = PaddingValues(
                    start = Medida.margen,
                    end = Medida.margen,
                    bottom = Medida.colaDeLista,
                ),
                verticalArrangement = Arrangement.spacedBy(Medida.entreTarjetas),
            ) {
                items(pagina.items, key = { it.id }) { articulo ->
                    TarjetaArticulo(articulo) { alAbrir(articulo.id) }
                }
            }
        }
    }
}

/**
 * Tarjeta de articulo: titular serif, bajada y firma.
 *
 * La foto va debajo del texto y no arriba, como en la referencia del diario: lo
 * que decide si alguien entra a leer es el titular, no la imagen.
 */
@Composable
private fun TarjetaArticulo(articulo: ResumenArticulo, alTocar: () -> Unit) {
    Tarjeta(modifier = Modifier.fillMaxWidth(), alTocar = alTocar) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Medida.dentroTarjeta),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (articulo.antetitulo.isNotBlank()) {
                Texto(
                    texto = articulo.antetitulo.uppercase(),
                    estilo = Letra.etiquetaNav,
                    color = Tono.acento,
                    maxLineas = 1,
                )
            }
            Texto(
                texto = articulo.titulo,
                estilo = Letra.titularTarjeta,
                color = Tono.tinta,
                maxLineas = 3,
            )
            if (articulo.entradilla.isNotBlank()) {
                Texto(
                    texto = articulo.entradilla,
                    estilo = Letra.descripcion,
                    color = Tono.tintaSuave,
                    maxLineas = 3,
                )
            }
            Foto(
                imagen = articulo.portada,
                descripcion = articulo.titulo,
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
            )
            Firma(nombreAutores(articulo.autores), articulo.publicado)
        }
    }
}

/** Una nota puede llevar mas de una firma; se muestran juntas, separadas por coma. */
internal fun nombreAutores(autores: List<AutorArticulo>): String? =
    autores.joinToString(", ") { it.nombre }.ifBlank { null }

@Composable
internal fun Firma(autor: String?, publicado: String?) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        if (!autor.isNullOrBlank()) {
            Texto(autor.uppercase(), Letra.etiquetaNav, Tono.tinta, maxLineas = 1)
        }
        val fecha = fechaCorta(publicado)
        if (fecha != null) {
            Texto(fecha, Letra.etiquetaNav, Tono.tintaSuave, maxLineas = 1)
        }
    }
}

/**
 * ISO 8601 a DD.MM.AAAA, que es el formato del sistema.
 * Se corta la cadena en vez de instanciar un formateador: la fecha ya viene
 * normalizada por el servidor y siempre en el mismo formato.
 */
internal fun fechaCorta(iso: String?): String? {
    if (iso == null || iso.length < 10) return null
    val a = iso.substring(0, 4)
    val m = iso.substring(5, 7)
    val d = iso.substring(8, 10)
    return "$d.$m.$a"
}
