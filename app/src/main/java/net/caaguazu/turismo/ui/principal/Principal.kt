package net.caaguazu.turismo.ui.principal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.caaguazu.turismo.core.Guardado
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.datos.Datos
import net.caaguazu.turismo.datos.Imagen
import net.caaguazu.turismo.datos.ItemInventario
import net.caaguazu.turismo.datos.nombreVisible
import net.caaguazu.turismo.datos.Pagina
import net.caaguazu.turismo.ui.articulos.fechaCorta
import net.caaguazu.turismo.ui.piezas.AtajoFoto
import net.caaguazu.turismo.ui.piezas.Badge
import net.caaguazu.turismo.ui.piezas.BandaPromocional
import net.caaguazu.turismo.ui.piezas.BotonIcono
import net.caaguazu.turismo.ui.piezas.CabeceraPantalla
import net.caaguazu.turismo.ui.piezas.Corazon
import net.caaguazu.turismo.ui.piezas.EncabezadoSeccion
import net.caaguazu.turismo.ui.piezas.EntradaBusqueda
import net.caaguazu.turismo.ui.piezas.Estado
import net.caaguazu.turismo.ui.piezas.FilaCompacta
import net.caaguazu.turismo.ui.piezas.Icono
import net.caaguazu.turismo.ui.piezas.TarjetaGrande
import net.caaguazu.turismo.ui.piezas.TileEtiquetado
import net.caaguazu.turismo.ui.piezas.cargar
import net.caaguazu.turismo.ui.tema.Medida
import net.caaguazu.turismo.ui.tema.Tono

/** Cuantos lugares entran en el mosaico del inicio antes de mandar a buscar. */
private const val LUGARES_EN_PORTADA = 6

/** Cuantos articulos se listan antes de mandar a la seccion. */
private const val ARTICULOS_EN_PORTADA = 3

/**
 * El inicio.
 *
 * La version anterior eran cuatro carruseles horizontales iguales, uno debajo
 * del otro. Eso es un indice disfrazado de portada: cuatro rieles de miniaturas
 * donde ninguna foto se ve, y donde las cuatro secciones pesan lo mismo aunque
 * no lo valgan.
 *
 * Esta portada tiene jerarquia. Arriba la busqueda, que es como llega quien ya
 * sabe que quiere. Despues los atajos de categoria. Despues **una** cosa
 * grande: el evento que esta pasando o el que viene, que es lo unico de la app
 * que caduca. Despues el mosaico de lugares, que es lo que se mira sin buscar
 * nada. La banda del recorrido, y al final los articulos como lista, porque un
 * articulo se elige por el titular y no por la foto.
 */
@Composable
fun Principal(
    alBuscar: () -> Unit,
    alBuscarCategoria: (Int) -> Unit,
    alAbrirFicha: (Int) -> Unit,
    alVerArticulo: (Int) -> Unit,
    alVerRecorrido: (Int) -> Unit,
    alVerArticulos: () -> Unit,
    alVerRecorridos: () -> Unit,
    alAbrirPerfil: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Cinco pedidos en paralelo y ninguno puede tumbar a los otros: aca un
    // fallo se traduce en una seccion que no aparece, no en una pantalla de
    // error. Por eso no hay Cargador: no hay una sola cosa que reintentar.
    val (categorias, _) = cargar { Datos.api.categorias() }
    val (inventario, _) = cargar { Datos.api.inventario(porPagina = 24) }
    // La agenda sale de clonar el inventario y filtrar del lado del telefono,
    // no de /eventos: es lo que permite que funcione sin conexion, y evita
    // arrastrar la forma distinta de un evento legado a esta pantalla.
    val (eventos, _) = cargar { Datos.api.inventario(tipoItem = "evento", porPagina = 20) }
    val (articulos, _) = cargar { Datos.api.articulos() }
    val (recorridos, _) = cargar { Datos.api.recorridos() }

    val lugares = itemsDe(inventario.value)
    val proximo = proximoEvento(itemsDe(eventos.value))

    LazyColumn(
        modifier = modifier.fillMaxSize().background(Tono.fondo),
        contentPadding = PaddingValues(bottom = Medida.colaDeLista),
    ) {

        item {
            CabeceraPantalla(Textos.t("app.nombre")) {
                BotonIcono(
                    icono = Icono.ajustes,
                    descripcion = Textos.t("barra.ajustes"),
                    alTocar = alAbrirPerfil,
                )
            }
        }

        item {
            EntradaBusqueda(
                marcador = Textos.t("barra.buscar"),
                alTocar = alBuscar,
                modifier = Modifier.padding(horizontal = Medida.margen),
            )
        }

        item {
            val lista = (categorias.value as? Estado.Listo)?.valor.orEmpty()
            val fotos = fotoPorCategoria(lugares)
            if (lista.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Medida.margen),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = Medida.entreTarjetas),
                ) {
                    items(lista.size) { indice ->
                        val categoria = lista[indice]
                        AtajoFoto(
                            imagen = categoria.portada ?: fotos[categoria.id],
                            etiqueta = categoria.nombreVisible(),
                            colorSinFoto = categoria.color,
                            alTocar = { alBuscarCategoria(categoria.id) },
                        )
                    }
                }
            }
        }

        // Lo unico de la app que caduca va primero y va grande. Si no hay
        // ningun evento por delante, la portada no reserva el hueco: arranca
        // directamente en los lugares.
        if (proximo != null) {
            item {
                Column(Modifier.padding(top = Medida.entreSecciones)) {
                    EncabezadoSeccion(Textos.t("principal.eventos"))
                    Box(Modifier.height(Medida.tituloACarrusel))
                    TarjetaGrande(
                        imagen = proximo.portada,
                        titulo = proximo.titulo,
                        encima = fechaCorta(proximo.fechas?.inicio),
                        modifier = Modifier.padding(horizontal = Medida.margen),
                        alTocar = { alAbrirFicha(proximo.id) },
                        esquina = {
                            if (proximo.fechas?.enCurso == true) {
                                Badge(Textos.t("evento.enCurso"))
                            } else {
                                Corazon(
                                    marcado = { Guardado.esFavorito(proximo.id) },
                                    alTocar = { Guardado.alternarFavorito(proximo.id) },
                                    descripcion = proximo.titulo,
                                )
                            }
                        },
                    )
                }
            }
        }

        // El mosaico de lugares. Se arma a mano en filas de dos y no con una
        // grilla perezosa: una grilla dentro de una columna perezosa no tiene
        // alto que medir, y para seis elementos la fila a mano no cuesta nada.
        if (lugares.isNotEmpty()) {
            item {
                Column(Modifier.padding(top = Medida.entreSecciones)) {
                    EncabezadoSeccion(Textos.t("nav.inventario"), alVerTodo = alBuscar)
                    Box(Modifier.height(Medida.tituloACarrusel))
                }
            }
            items(lugares.take(LUGARES_EN_PORTADA).chunked(2)) { fila ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Medida.margen)
                        .padding(bottom = Medida.entreTarjetas),
                    horizontalArrangement = Arrangement.spacedBy(Medida.entreTarjetas),
                ) {
                    fila.forEach { lugar ->
                        Box(Modifier.weight(1f)) {
                            TileEtiquetado(
                                imagen = lugar.portada,
                                etiqueta = lugar.titulo,
                                proporcion = 16f / 13f,
                                alTocar = { alAbrirFicha(lugar.id) },
                            )
                        }
                    }
                    // Si la ultima fila queda impar, el hueco se reserva para
                    // que el tile solitario no se estire al doble de ancho.
                    if (fila.size == 1) Box(Modifier.weight(1f))
                }
            }
        }

        item {
            val recorrido = (recorridos.value as? Estado.Listo)?.valor?.items?.firstOrNull()
            if (recorrido != null) {
                Column(Modifier.padding(top = Medida.entreTarjetas)) {
                    EncabezadoSeccion(Textos.t("nav.recorridos"), alVerTodo = alVerRecorridos)
                    Box(Modifier.height(Medida.tituloACarrusel))
                    BandaPromocional(
                        imagen = recorrido.portada,
                        titulo = recorrido.titulo,
                        textoAccion = Textos.t("rec.abrir"),
                        alTocar = { alVerRecorrido(recorrido.id) },
                        modifier = Modifier.padding(horizontal = Medida.margen),
                    )
                }
            }
        }

        item {
            val lista = (articulos.value as? Estado.Listo)?.valor?.items.orEmpty()
            if (lista.isNotEmpty()) {
                Column(Modifier.padding(top = Medida.entreSecciones)) {
                    EncabezadoSeccion(Textos.t("nav.articulos"), alVerTodo = alVerArticulos)
                    Box(Modifier.height(Medida.tituloACarrusel))
                    lista.take(ARTICULOS_EN_PORTADA).forEach { articulo ->
                        FilaCompacta(
                            imagen = articulo.portada,
                            titulo = articulo.titulo,
                            detalle = articulo.entradilla.ifBlank { null },
                            meta = fechaCorta(articulo.publicado),
                            modifier = Modifier
                                .padding(horizontal = Medida.margen)
                                .padding(bottom = 10.dp),
                            alTocar = { alVerArticulo(articulo.id) },
                        )
                    }
                }
            }
        }

    }
}

private fun <T> itemsDe(estado: Estado<Pagina<T>>): List<T> =
    (estado as? Estado.Listo)?.valor?.items.orEmpty()

/** La foto del primer atractivo de cada categoria, para los atajos sin portada. */
private fun fotoPorCategoria(lugares: List<ItemInventario>): Map<Int, Imagen?> =
    lugares
        .mapNotNull { item -> item.categoria?.id?.let { it to item.portada } }
        .filter { it.second != null }
        .toMap()

/**
 * El evento que hay que mostrar: el que esta ocurriendo, y si no hay ninguno,
 * el mas cercano que todavia no termino.
 */
private fun proximoEvento(items: List<ItemInventario>): ItemInventario? {
    val vigentes = items.filter { it.fechas?.terminado != true }
    return vigentes.firstOrNull { it.fechas?.enCurso == true }
        ?: vigentes.minByOrNull { it.fechas?.inicio ?: "" }
}
