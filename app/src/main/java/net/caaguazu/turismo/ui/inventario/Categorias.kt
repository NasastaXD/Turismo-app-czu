package net.caaguazu.turismo.ui.inventario

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import net.caaguazu.turismo.datos.Categoria
import net.caaguazu.turismo.datos.Datos
import net.caaguazu.turismo.datos.Imagen
import net.caaguazu.turismo.ui.piezas.Cargador
import net.caaguazu.turismo.ui.piezas.Estado
import net.caaguazu.turismo.ui.piezas.FotoConVelo
import net.caaguazu.turismo.ui.piezas.Tarjeta
import net.caaguazu.turismo.ui.piezas.Texto
import net.caaguazu.turismo.ui.piezas.cargar
import net.caaguazu.turismo.ui.tema.Letra
import net.caaguazu.turismo.ui.tema.Medida
import net.caaguazu.turismo.ui.tema.Radio

/**
 * Grilla de categorias: dos columnas, proporcion 16:10, tile redondeado.
 *
 * La etiqueta va anclada abajo a la izquierda, sobre el velo, que es donde la
 * referencia la pone y donde menos tapa la foto.
 */
@Composable
fun PantallaCategorias(
    alElegir: (Categoria) -> Unit,
    alVerTodo: () -> Unit,
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

    Cargador(
        estado = estado.value,
        reintentar = reintentar,
        modifier = modifier,
        vacio = { it.isEmpty() },
    ) { categorias ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Medida.margen),
            horizontalArrangement = Arrangement.spacedBy(Medida.margen),
            verticalArrangement = Arrangement.spacedBy(Medida.margen),
        ) {
            items(categorias, key = { it.id }) { categoria ->
                TileCategoria(
                    categoria = categoria,
                    fondo = categoria.portada ?: fotoPorCategoria[categoria.id],
                    alTocar = { alElegir(categoria) },
                )
            }
        }
    }
}

@Composable
private fun TileCategoria(categoria: Categoria, fondo: Imagen?, alTocar: () -> Unit) {
    Tarjeta(
        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 10f),
        alTocar = alTocar,
    ) {
        FotoConVelo(
            imagen = fondo,
            descripcion = categoria.nombre,
            colorSinFoto = categoria.color,
            modifier = Modifier.fillMaxSize(),
            // Lo recorta la tarjeta: redondear la foto tambien dejaria una
            // esquina doble.
            radio = Radio.ninguno,
        )
        Texto(
            texto = categoria.nombre,
            estilo = Letra.sobreFoto,
            color = Color.White,
            maxLineas = 2,
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
        )
    }
}
