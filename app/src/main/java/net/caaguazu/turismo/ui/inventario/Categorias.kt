package net.caaguazu.turismo.ui.inventario

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.datos.Categoria
import net.caaguazu.turismo.datos.Datos
import net.caaguazu.turismo.ui.piezas.BotonIcono
import net.caaguazu.turismo.ui.piezas.CabeceraPantalla
import net.caaguazu.turismo.ui.piezas.Cargador
import net.caaguazu.turismo.ui.piezas.Estado
import net.caaguazu.turismo.ui.piezas.Icono
import net.caaguazu.turismo.ui.piezas.TileEtiquetado
import net.caaguazu.turismo.ui.piezas.Texto
import net.caaguazu.turismo.ui.piezas.cargar
import net.caaguazu.turismo.ui.tema.Letra
import net.caaguazu.turismo.ui.tema.Medida
import net.caaguazu.turismo.ui.tema.Tono

/**
 * Grilla de categorias: dos columnas de foto redondeada con la etiqueta encima.
 *
 * Los tiles perdieron la caja blanca que los envolvia. Una foto dentro de una
 * tarjeta blanca deja un marco de papel entre la foto y el ojo que no dice
 * nada; sin el, la grilla se lee como lo que es, un mosaico de lugares.
 */
@Composable
fun PantallaCategorias(
    alElegir: (Categoria) -> Unit,
    alVerTodo: () -> Unit,
    alAbrirPerfil: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (estado, reintentar) = cargar { Datos.api.categorias() }

    // El contrato todavia no trae foto de categoria. Mientras tanto se toma la
    // del primer atractivo de cada una: es contenido real del propio destino,
    // no una imagen de archivo.
    val (respaldo, _) = cargar { Datos.api.inventario(porPagina = 100) }
    val fotoPorCategoria = (respaldo.value as? Estado.Listo)?.valor?.items
        ?.mapNotNull { item -> item.categoria?.id?.let { it to item.portada } }
        ?.filter { it.second != null }
        ?.toMap()
        .orEmpty()

    Column(modifier.fillMaxSize().background(Tono.fondo)) {
        // "Ver todo" vive en la cabecera y no sobre la grilla: es la salida a
        // la lista sin filtrar, y la grilla no necesita un titulo propio cuando
        // la pantalla entera ya se llama como ella.
        CabeceraPantalla(Textos.t("nav.inventario")) {
            Texto(
                texto = Textos.t("banda.verTodo"),
                estilo = Letra.enlace,
                color = Tono.tintaSuave,
                maxLineas = 1,
                modifier = Modifier.clickable(onClick = alVerTodo),
            )
            BotonIcono(
                icono = Icono.perfil,
                descripcion = Textos.t("barra.perfil"),
                alTocar = alAbrirPerfil,
            )
        }

        Cargador(
            estado = estado.value,
            reintentar = reintentar,
            modifier = Modifier.fillMaxSize(),
            vacio = { it.isEmpty() },
        ) { categorias ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Medida.margen,
                    end = Medida.margen,
                    top = 4.dp,
                    bottom = Medida.colaDeLista,
                ),
                horizontalArrangement = Arrangement.spacedBy(Medida.entreTarjetas),
                verticalArrangement = Arrangement.spacedBy(Medida.entreTarjetas),
            ) {
                items(categorias, key = { it.id }) { categoria ->
                    TileEtiquetado(
                        imagen = categoria.portada ?: fotoPorCategoria[categoria.id],
                        etiqueta = categoria.nombre,
                        colorSinFoto = categoria.color,
                        proporcion = 16f / 13f,
                        alTocar = { alElegir(categoria) },
                    )
                }
            }
        }
    }
}
