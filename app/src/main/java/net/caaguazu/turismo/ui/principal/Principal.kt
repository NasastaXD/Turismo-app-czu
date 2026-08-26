package net.caaguazu.turismo.ui.principal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.datos.Datos
import net.caaguazu.turismo.datos.Imagen
import net.caaguazu.turismo.datos.Pagina
import net.caaguazu.turismo.ui.articulos.fechaCorta
import net.caaguazu.turismo.ui.piezas.Estado
import net.caaguazu.turismo.ui.piezas.Foto
import net.caaguazu.turismo.ui.piezas.Texto
import net.caaguazu.turismo.ui.piezas.cargar
import net.caaguazu.turismo.ui.tema.Letra
import net.caaguazu.turismo.ui.tema.Medida
import net.caaguazu.turismo.ui.tema.Tono

/**
 * La pantalla de inicio: una pila de bandas a sangre completa que alternan
 * papel y gris calido.
 *
 * Cada banda es un carrusel que sangra hasta el borde derecho, con la tercera
 * tarjeta cortada. Ese corte es la unica señal de que hay mas: el sistema no
 * lleva flechas, ni puntos, ni barra de progreso.
 */
@Composable
fun Principal(
    alVerArticulo: (Int) -> Unit,
    alVerRecorrido: (Int) -> Unit,
    alVerInventario: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (articulos, _) = cargar { Datos.api.articulos() }
    val (recorridos, _) = cargar { Datos.api.recorridos() }
    val (eventos, _) = cargar { Datos.api.eventos() }
    val (inventario, _) = cargar { Datos.api.inventario(porPagina = 12) }

    LazyColumn(modifier.fillMaxSize().background(Tono.papel)) {

        item {
            Banda(Tono.papel, Textos.t("nav.articulos")) { ancho ->
                Carrusel(articulos.value) { articulo ->
                    TarjetaCarrusel(
                        ancho = ancho,
                        imagen = articulo.portada,
                        encima = fechaCorta(articulo.publicado),
                        titulo = articulo.titulo,
                        alTocar = { alVerArticulo(articulo.id) },
                    )
                }
            }
        }

        item {
            Banda(Tono.banda, Textos.t("nav.recorridos")) { ancho ->
                Carrusel(recorridos.value) { recorrido ->
                    TarjetaCarrusel(
                        ancho = ancho,
                        imagen = recorrido.portada,
                        encima = recorrido.duracionEstimada,
                        titulo = recorrido.titulo,
                        alTocar = { alVerRecorrido(recorrido.id) },
                    )
                }
            }
        }

        item {
            Banda(Tono.papel, Textos.t("nav.inventario")) { ancho ->
                Carrusel(inventario.value) { item ->
                    TarjetaCarrusel(
                        ancho = ancho,
                        imagen = item.portada,
                        encima = item.categoria?.nombre,
                        titulo = item.titulo,
                        alTocar = alVerInventario,
                    )
                }
            }
        }

        item {
            Banda(Tono.banda, Textos.t("principal.eventos")) { ancho ->
                Carrusel(eventos.value) { evento ->
                    TarjetaCarrusel(
                        ancho = ancho,
                        imagen = evento.portada,
                        encima = fechaCorta(evento.inicio),
                        titulo = evento.titulo,
                        alTocar = alVerInventario,
                    )
                }
            }
        }
    }
}

/**
 * Unidad estructural de la pantalla. Titulo y contenido, nada intermedio: el
 * sistema no lleva descripcion bajo el titulo de seccion.
 */
@Composable
private fun Banda(
    fondo: Color,
    titulo: String,
    contenido: @Composable (Dp) -> Unit,
) {
    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .background(fondo)
            .padding(top = Medida.bandaArriba, bottom = Medida.bandaAbajo),
    ) {
        // 43% del ancho de pantalla: es lo que deja la tercera tarjeta cortada.
        val anchoTarjeta = maxWidth * Medida.FRACCION_TARJETA

        Column {
            Texto(
                texto = titulo,
                estilo = Letra.tituloSeccion,
                color = Tono.tinta,
                modifier = Modifier.padding(horizontal = Medida.margen),
            )
            Box(Modifier.height(Medida.tituloACarrusel))
            contenido(anchoTarjeta)
        }
    }
}

/**
 * Un carrusel solo dibuja cuando hay datos. Si falla no se pone un error en
 * medio de la pantalla de inicio: la banda queda vacia y las demas siguen
 * funcionando, que es mejor que una pantalla entera rota por una seccion.
 */
@Composable
private fun <T> Carrusel(estado: Estado<Pagina<T>>, tarjeta: @Composable (T) -> Unit) {
    val elementos = (estado as? Estado.Listo)?.valor?.items.orEmpty()
    if (elementos.isEmpty()) return

    LazyRow(
        contentPadding = PaddingValues(start = Medida.margen),
        horizontalArrangement = Arrangement.spacedBy(Medida.entreTarjetas),
    ) {
        items(elementos.size) { indice -> tarjeta(elementos[indice]) }
    }
}

@Composable
private fun TarjetaCarrusel(
    ancho: Dp,
    imagen: Imagen?,
    encima: String?,
    titulo: String,
    alTocar: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(ancho)
            .clickable(onClick = alTocar)
            .background(Tono.superficie),
    ) {
        Foto(imagen, titulo, Modifier.fillMaxWidth().aspectRatio(1f))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
                .padding(Medida.dentroTarjeta),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (!encima.isNullOrBlank()) {
                Texto(encima, Letra.fecha, Tono.acento, maxLineas = 1)
            }
            Texto(titulo, Letra.tituloTarjeta, Tono.tinta, maxLineas = 2)
        }
    }
}
