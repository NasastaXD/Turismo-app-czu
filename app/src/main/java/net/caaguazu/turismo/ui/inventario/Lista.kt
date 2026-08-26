package net.caaguazu.turismo.ui.inventario

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.datos.Categoria
import net.caaguazu.turismo.datos.Datos
import net.caaguazu.turismo.datos.ItemInventario
import net.caaguazu.turismo.ui.mapa.MapaCaaguazu
import net.caaguazu.turismo.ui.mapa.Pin
import net.caaguazu.turismo.ui.piezas.Cargador
import net.caaguazu.turismo.ui.piezas.Foto
import net.caaguazu.turismo.ui.piezas.Glifo
import net.caaguazu.turismo.ui.piezas.Hairline
import net.caaguazu.turismo.ui.piezas.Icono
import net.caaguazu.turismo.ui.piezas.InterruptorListaMapa
import net.caaguazu.turismo.ui.piezas.RangoPrecio
import net.caaguazu.turismo.ui.piezas.Texto
import net.caaguazu.turismo.ui.piezas.cargar
import net.caaguazu.turismo.ui.tema.Letra
import net.caaguazu.turismo.ui.tema.Medida
import net.caaguazu.turismo.ui.tema.Tono

/**
 * Los atractivos de una categoria, en lista o sobre el mapa.
 *
 * Es la misma pantalla y el mismo conjunto de datos: el interruptor solo cambia
 * como se dibujan. Separarlas en dos pantallas obligaria a mantener dos veces el
 * filtrado y a que el usuario perdiera el contexto al saltar de una a otra.
 */
@Composable
fun PantallaLista(
    categoria: Categoria?,
    enMapa: () -> Boolean,
    alCambiarVista: (Boolean) -> Unit,
    alAbrir: (Int) -> Unit,
    alVolver: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (estado, reintentar) = cargar(categoria?.id) {
        Datos.api.inventario(categoria = categoria?.id, porPagina = 50)
    }

    Column(modifier.fillMaxSize().background(Tono.papel)) {
        Encabezado(
            titulo = categoria?.nombre ?: Textos.t("nav.inventario"),
            enMapa = enMapa,
            alCambiarVista = alCambiarVista,
            alVolver = alVolver,
        )

        Cargador(
            estado = estado.value,
            reintentar = reintentar,
            vacio = { it.items.isEmpty() },
            modifier = Modifier.fillMaxSize(),
        ) { pagina ->
            if (enMapa()) {
                MapaCaaguazu(
                    marcadores = pagina.items.mapNotNull { item ->
                        item.coordenadas?.let { Pin(item.id, it.lat, it.lng, item.categoria?.color) }
                    },
                    alTocarMarcador = alAbrir,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = Medida.margen),
                    verticalArrangement = Arrangement.spacedBy(Medida.entreTarjetas),
                ) {
                    items(pagina.items, key = { it.id }) { item ->
                        TarjetaLista(item) { alAbrir(item.id) }
                    }
                }
            }
        }
    }
}

@Composable
private fun Encabezado(
    titulo: String,
    enMapa: () -> Boolean,
    alCambiarVista: (Boolean) -> Unit,
    alVolver: () -> Unit,
) {
    Column(Modifier.fillMaxWidth().background(Tono.papel)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Medida.margen, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f),
            ) {
                Box(Modifier.size(32.dp).clickable(onClick = alVolver), Alignment.Center) {
                    Glifo(Icono.volver, titulo, Tono.tinta, Modifier.size(22.dp))
                }
                Texto(
                    texto = titulo,
                    estilo = Letra.tituloTarjeta,
                    color = Tono.tinta,
                    maxLineas = 1,
                )
            }
            InterruptorListaMapa(enMapa = enMapa, alCambiar = alCambiarVista)
        }
        Hairline(Modifier.fillMaxWidth())
    }
}

/**
 * Tarjeta de lista: foto cuadrada a la izquierda y los datos que decide el
 * turista al lado — nombre, precio y horario.
 */
@Composable
private fun TarjetaLista(item: ItemInventario, alTocar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = alTocar)
            .background(Tono.papel)
            .padding(Medida.margen),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Foto(
            imagen = item.portada,
            descripcion = item.titulo,
            modifier = Modifier.width(116.dp).aspectRatio(1f),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            item.categoria?.let {
                Texto(texto = it.nombre, estilo = Letra.chip, color = Tono.acento, maxLineas = 1)
            }
            Texto(
                texto = item.titulo,
                estilo = Letra.tituloTarjeta,
                color = Tono.tinta,
                maxLineas = 2,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RangoPrecio(item.rangoPrecio)
                if (item.horarioResumen.isNotBlank()) {
                    Texto(
                        texto = item.horarioResumen,
                        estilo = Letra.descripcion,
                        color = Tono.tintaSuave,
                        maxLineas = 1,
                    )
                }
            }
        }
    }
}
