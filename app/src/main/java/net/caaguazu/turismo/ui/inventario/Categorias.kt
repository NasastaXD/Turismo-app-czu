package net.caaguazu.turismo.ui.inventario

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import net.caaguazu.turismo.ui.piezas.Texto
import net.caaguazu.turismo.ui.piezas.cargar
import net.caaguazu.turismo.ui.tema.Letra
import net.caaguazu.turismo.ui.tema.Medida

/**
 * Grilla de categorias: dos columnas, proporcion 16:10, esquina viva.
 *
 * La etiqueta va anclada arriba a la izquierda y no centrada, que es lo que
 * hace que la grilla se lea como una pila de tiles y no como botones.
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .clickable(onClick = alTocar),
    ) {
        FotoConVelo(
            imagen = fondo,
            descripcion = categoria.nombre,
            colorSinFoto = categoria.color,
            modifier = Modifier.fillMaxSize(),
        )
        Texto(
            texto = categoria.nombre,
            estilo = Letra.sobreFoto,
            color = Color.White,
            maxLineas = 2,
            modifier = Modifier.align(Alignment.TopStart).padding(20.dp),
        )
    }
}
