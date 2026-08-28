package net.caaguazu.turismo.ui.recorridos

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import net.caaguazu.turismo.core.MapasExternos
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.datos.Datos
import net.caaguazu.turismo.datos.Parada
import net.caaguazu.turismo.ui.mapa.MapaCaaguazu
import net.caaguazu.turismo.ui.mapa.Pin
import net.caaguazu.turismo.ui.piezas.Cargador
import net.caaguazu.turismo.ui.piezas.Foto
import net.caaguazu.turismo.ui.piezas.Glifo
import net.caaguazu.turismo.ui.piezas.Hairline
import net.caaguazu.turismo.ui.piezas.Icono
import net.caaguazu.turismo.ui.piezas.PildoraNegra
import net.caaguazu.turismo.ui.piezas.Texto
import net.caaguazu.turismo.ui.piezas.cargar
import net.caaguazu.turismo.ui.tema.Letra
import net.caaguazu.turismo.ui.tema.Medida
import net.caaguazu.turismo.ui.tema.Radio
import net.caaguazu.turismo.ui.tema.Tono

/**
 * Un recorrido prehecho.
 *
 * El trazado se dibuja en el mapa propio porque Google Maps traza SU camino
 * entre las paradas, no el que penso quien armo el recorrido. La salida a Maps
 * queda para navegar, que es lo que Maps hace mejor.
 */
@Composable
fun PantallaRecorrido(
    id: Int,
    alAbrirFicha: (Int) -> Unit,
    alVolver: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contexto = LocalContext.current
    val (estado, reintentar) = cargar(id) { Datos.api.recorrido(id) }

    Box(modifier.fillMaxSize().background(Tono.papel)) {
        Cargador(estado = estado.value, reintentar = reintentar) { recorrido ->
            val disponibles = recorrido.paradas.filter { it.disponible }
            val puntos = disponibles.mapNotNull { it.coordenadas?.let { c -> c.lat to c.lng } }

            Column(Modifier.fillMaxSize()) {
                LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 16.dp)) {

                    item {
                        Box(Modifier.fillMaxWidth().aspectRatio(16f / 10f)) {
                            MapaCaaguazu(
                                marcadores = disponibles.mapNotNull { parada ->
                                    parada.coordenadas?.let {
                                        Pin(parada.refId, it.lat, it.lng, parada.categoria?.color)
                                    }
                                },
                                alTocarMarcador = alAbrirFicha,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }

                    item {
                        Column(
                            Modifier.padding(Medida.margen),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Texto(recorrido.titulo, Letra.tituloPagina, Tono.tinta)
                            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                Texto(recorrido.duracionEstimada, Letra.chip, Tono.acento, maxLineas = 1)
                                recorrido.costoTotal?.takeIf { it.hayPago }?.let { costo ->
                                    Texto(
                                        costo.detalle.joinToString(" · "),
                                        Letra.chip,
                                        Tono.tintaSuave,
                                        maxLineas = 1,
                                    )
                                }
                            }
                            if (recorrido.resumen.isNotBlank()) {
                                Texto(recorrido.resumen, Letra.descripcion, Tono.tintaSuave)
                            }
                        }
                    }

                    recorrido.historia?.let { historia ->
                        if (historia.introduccion.isNotBlank()) {
                            item {
                                Texto(
                                    texto = historia.introduccion,
                                    estilo = Letra.descripcion,
                                    color = Tono.tinta,
                                    modifier = Modifier.padding(horizontal = Medida.margen, vertical = 4.dp),
                                )
                            }
                        }
                    }

                    items(recorrido.paradas, key = { it.orden }) { parada ->
                        FilaParadaPrehecha(parada) { if (parada.disponible) alAbrirFicha(parada.refId) }
                        Hairline(Modifier.fillMaxWidth().padding(horizontal = Medida.margen))
                    }
                }

                if (puntos.size >= 2) {
                    Box(Modifier.fillMaxWidth().background(Tono.papel).padding(Medida.margen)) {
                        PildoraNegra(
                            texto = Textos.t("rec.abrir"),
                            alTocar = { MapasExternos.abrirRecorrido(contexto, puntos) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(12.dp)
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.9f), CircleShape)
                .clickable(onClick = alVolver),
            contentAlignment = Alignment.Center,
        ) {
            Glifo(Icono.volver, Textos.t("accion.volver"), Tono.tinta, Modifier.size(22.dp))
        }
    }
}

/**
 * Una parada. Si el lugar se despublico despues de que se armo el recorrido,
 * se muestra igual y apagada: que desapareciera sin avisar dejaria al usuario
 * con un recorrido que cambio solo y sin saber por que.
 */
@Composable
private fun FilaParadaPrehecha(parada: Parada, alTocar: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(enabled = parada.disponible, onClick = alTocar)
            .padding(Medida.margen),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(28.dp).clip(RoundedCornerShape(Radio.completo))
                .background(if (parada.disponible) Tono.negro else Tono.linea),
            contentAlignment = Alignment.Center,
        ) {
            Texto(
                parada.orden.toString(),
                Letra.etiquetaNav,
                if (parada.disponible) Color.White else Tono.tintaSuave,
                maxLineas = 1,
            )
        }

        if (parada.disponible) {
            Foto(parada.portada, parada.titulo, Modifier.size(56.dp))
        }

        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Texto(
                texto = if (parada.disponible) parada.titulo else Textos.t("rec.noDisponible"),
                estilo = Letra.tituloTarjeta,
                color = if (parada.disponible) Tono.tinta else Tono.tintaSuave,
                maxLineas = 2,
            )
            if (parada.texto.isNotBlank()) {
                Texto(parada.texto, Letra.descripcion, Tono.tintaSuave, maxLineas = 2)
            }
        }
    }
}
