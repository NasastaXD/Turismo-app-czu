package net.caaguazu.turismo.ui.principal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.caaguazu.turismo.core.Guardado
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.datos.Categoria
import net.caaguazu.turismo.datos.Datos
import net.caaguazu.turismo.datos.Imagen
import net.caaguazu.turismo.datos.ItemInventario
import net.caaguazu.turismo.datos.Pagina
import net.caaguazu.turismo.ui.articulos.fechaCorta
import net.caaguazu.turismo.ui.piezas.AtajoFoto
import net.caaguazu.turismo.ui.piezas.Badge
import net.caaguazu.turismo.ui.piezas.BotonIcono
import net.caaguazu.turismo.ui.piezas.CabeceraPantalla
import net.caaguazu.turismo.ui.piezas.Corazon
import net.caaguazu.turismo.ui.piezas.EncabezadoSeccion
import net.caaguazu.turismo.ui.piezas.Estado
import net.caaguazu.turismo.ui.piezas.Icono
import net.caaguazu.turismo.ui.piezas.TarjetaFoto
import net.caaguazu.turismo.ui.piezas.TileEtiquetado
import net.caaguazu.turismo.ui.piezas.cargar
import net.caaguazu.turismo.ui.tema.Medida
import net.caaguazu.turismo.ui.tema.Tono

/**
 * La pantalla de inicio.
 *
 * Es una galeria, no un indice: fotos grandes sueltas sobre el fondo, sin caja
 * blanca alrededor y sin bandas de color alternadas. Lo que separa una seccion
 * de la siguiente es el aire, que es la unica forma de que las fotos —que son
 * el contenido— no compitan con la estructura que las ordena.
 *
 * Arriba, los atajos de categoria: es el camino corto al inventario para quien
 * ya sabe que esta buscando. Debajo, cada seccion con su carrusel, con la
 * tercera tarjeta cortada por el borde. Ese corte es la unica señal de que hay
 * mas: el sistema no lleva flechas, ni puntos, ni barra de progreso.
 */
@Composable
fun Principal(
    alVerArticulo: (Int) -> Unit,
    alVerRecorrido: (Int) -> Unit,
    alVerFicha: (Int) -> Unit,
    alVerCategoria: (Categoria) -> Unit,
    alVerInventario: () -> Unit,
    alVerArticulos: () -> Unit,
    alVerRecorridos: () -> Unit,
    alAbrirPerfil: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Cinco pedidos en paralelo y ninguno puede tumbar a los otros: aca un
    // fallo se traduce en una seccion que no aparece, no en una pantalla de
    // error. Por eso no hay Cargador: no hay una sola cosa que reintentar.
    val (categorias, _) = cargar { Datos.api.categorias() }
    val (inventario, _) = cargar { Datos.api.inventario(porPagina = 12) }
    // La agenda sale de clonar el inventario y filtrar del lado del telefono,
    // no de /eventos: es lo que permite que funcione sin conexion, y evita
    // arrastrar la forma distinta de un evento legado a esta pantalla.
    val (eventos, _) = cargar { Datos.api.inventario(tipoItem = "evento", porPagina = 20) }
    val (articulos, _) = cargar { Datos.api.articulos() }
    val (recorridos, _) = cargar { Datos.api.recorridos() }

    BoxWithConstraints(modifier.fillMaxSize().background(Tono.fondo)) {
        // 45% del ancho: dos tarjetas enteras y el borde de la tercera.
        val anchoTarjeta = maxWidth * Medida.FRACCION_TARJETA

        LazyColumn(contentPadding = PaddingValues(bottom = Medida.colaDeLista)) {

            item {
                CabeceraPantalla(Textos.t("app.nombre")) {
                    BotonIcono(
                        icono = Icono.perfil,
                        descripcion = Textos.t("barra.perfil"),
                        alTocar = alAbrirPerfil,
                    )
                }
            }

            item {
                val lista = (categorias.value as? Estado.Listo)?.valor.orEmpty()
                val fotos = fotoPorCategoria(inventario.value)
                if (lista.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = Medida.margen),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = Medida.entreSecciones),
                    ) {
                        items(lista.size) { indice ->
                            val categoria = lista[indice]
                            AtajoFoto(
                                imagen = categoria.portada ?: fotos[categoria.id],
                                etiqueta = categoria.nombre,
                                colorSinFoto = categoria.color,
                                alTocar = { alVerCategoria(categoria) },
                            )
                        }
                    }
                }
            }

            seccion(
                titulo = Textos.t("nav.inventario"),
                estado = inventario.value,
                alVerTodo = alVerInventario,
            ) { item ->
                TarjetaFoto(
                    imagen = item.portada,
                    titulo = item.titulo,
                    encima = item.zona?.nombre,
                    modifier = Modifier.width(anchoTarjeta),
                    alTocar = { alVerFicha(item.id) },
                    esquina = {
                        Corazon(
                            // Lectura diferida: marcar un favorito redibuja un
                            // corazon, no el carrusel entero.
                            marcado = { Guardado.esFavorito(item.id) },
                            alTocar = { Guardado.alternarFavorito(item.id) },
                            descripcion = item.titulo,
                        )
                    },
                )
            }

            seccion(
                titulo = Textos.t("principal.eventos"),
                estado = eventos.value,
                alVerTodo = alVerInventario,
                ordenar = ::proximosEventos,
            ) { evento ->
                TarjetaFoto(
                    imagen = evento.portada,
                    titulo = evento.titulo,
                    encima = fechaCorta(evento.fechas?.inicio),
                    modifier = Modifier.width(anchoTarjeta),
                    alTocar = { alVerFicha(evento.id) },
                    esquina = {
                        // Un evento que esta pasando ahora es la unica cosa de
                        // la pantalla que cambia sola. Por eso lleva el unico
                        // badge, y el unico mango.
                        if (evento.fechas?.enCurso == true) {
                            Badge(Textos.t("evento.enCurso"))
                        }
                    },
                )
            }

            seccion(
                titulo = Textos.t("nav.articulos"),
                estado = articulos.value,
                alVerTodo = alVerArticulos,
            ) { articulo ->
                TarjetaFoto(
                    imagen = articulo.portada,
                    titulo = articulo.titulo,
                    encima = fechaCorta(articulo.publicado),
                    proporcion = 3f / 4f,
                    modifier = Modifier.width(anchoTarjeta),
                    alTocar = { alVerArticulo(articulo.id) },
                )
            }

            // Los recorridos cierran la pantalla a lo ancho: un recorrido se
            // elige por la foto y por el paisaje que promete, no comparando dos
            // miniaturas de al lado.
            item {
                val lista = (recorridos.value as? Estado.Listo)?.valor?.items.orEmpty()
                if (lista.isNotEmpty()) {
                    Column {
                        EncabezadoSeccion(Textos.t("nav.recorridos"), alVerTodo = alVerRecorridos)
                        Box(Modifier.height(Medida.tituloACarrusel))
                        LazyRow(
                            contentPadding = PaddingValues(
                                start = Medida.margen,
                                end = Medida.margen,
                                bottom = Medida.entreSecciones,
                            ),
                            horizontalArrangement = Arrangement.spacedBy(Medida.entreTarjetas),
                        ) {
                            items(lista.size) { indice ->
                                val recorrido = lista[indice]
                                TileEtiquetado(
                                    imagen = recorrido.portada,
                                    etiqueta = recorrido.titulo,
                                    proporcion = 16f / 10f,
                                    modifier = Modifier.width(anchoTarjeta * 2),
                                    alTocar = { alVerRecorrido(recorrido.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Una seccion con su encabezado y su carrusel.
 *
 * Si la seccion no tiene nada, no se dibuja ni el titulo: un encabezado sobre
 * una fila vacia deja un hueco que parece un error de carga. Y si falla, falla
 * sola — el resto de la pantalla de inicio sigue funcionando, que es mejor que
 * una pantalla entera rota por una seccion.
 */
private fun <T> LazyListScope.seccion(
    titulo: String,
    estado: Estado<Pagina<T>>,
    alVerTodo: () -> Unit,
    ordenar: (List<T>) -> List<T> = { it },
    tarjeta: @Composable (T) -> Unit,
) {
    item {
        val elementos = ordenar((estado as? Estado.Listo)?.valor?.items.orEmpty())
        if (elementos.isNotEmpty()) {
            Column {
                EncabezadoSeccion(titulo, alVerTodo = alVerTodo)
                Box(Modifier.height(Medida.tituloACarrusel))
                LazyRow(
                    // Solo margen a la izquierda: el carrusel sangra hasta el
                    // borde derecho, que es lo que deja la tercera cortada.
                    contentPadding = PaddingValues(
                        start = Medida.margen,
                        bottom = Medida.entreSecciones,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(Medida.entreTarjetas),
                ) {
                    items(elementos.size) { indice -> tarjeta(elementos[indice]) }
                }
            }
        }
    }
}

/** La foto del primer atractivo de cada categoria, para los atajos sin portada. */
private fun fotoPorCategoria(
    inventario: Estado<Pagina<ItemInventario>>,
): Map<Int, Imagen?> =
    (inventario as? Estado.Listo)?.valor?.items
        ?.mapNotNull { item -> item.categoria?.id?.let { it to item.portada } }
        ?.filter { it.second != null }
        ?.toMap()
        .orEmpty()

/** Los que no terminaron todavia, del mas cercano al mas lejano. */
private fun proximosEventos(items: List<ItemInventario>): List<ItemInventario> =
    items.filter { it.fechas?.terminado != true }.sortedBy { it.fechas?.inicio ?: "" }
