package net.caaguazu.turismo.ui.articulos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.caaguazu.turismo.datos.AutorArticulo
import net.caaguazu.turismo.datos.Datos
import net.caaguazu.turismo.datos.ResumenArticulo
import net.caaguazu.turismo.ui.piezas.Cargador
import net.caaguazu.turismo.ui.piezas.Foto
import net.caaguazu.turismo.ui.piezas.Hairline
import net.caaguazu.turismo.ui.piezas.Texto
import net.caaguazu.turismo.ui.piezas.cargar
import net.caaguazu.turismo.ui.tema.Letra
import net.caaguazu.turismo.ui.tema.Medida
import net.caaguazu.turismo.ui.tema.Tono

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
fun Articulos(pila: PilaArticulos, modifier: Modifier = Modifier) {
    val id = pila.abierto
    if (id == null) {
        ListaArticulos(alAbrir = pila::abrir, modifier = modifier)
    } else {
        PantallaArticulo(id = id, alVolver = { pila.volver() }, modifier = modifier)
    }
}

@Composable
private fun ListaArticulos(alAbrir: (Int) -> Unit, modifier: Modifier = Modifier) {
    val (estado, reintentar) = cargar { Datos.api.articulos() }

    Cargador(
        estado = estado.value,
        reintentar = reintentar,
        vacio = { it.items.isEmpty() },
        modifier = modifier.fillMaxSize().background(Tono.papel),
    ) { pagina ->
        LazyColumn(contentPadding = PaddingValues(vertical = Medida.margen)) {
            items(pagina.items, key = { it.id }) { articulo ->
                TarjetaArticulo(articulo) { alAbrir(articulo.id) }
                Hairline(Modifier.fillMaxWidth().padding(horizontal = Medida.margen))
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = alTocar)
            .padding(Medida.margen),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
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
