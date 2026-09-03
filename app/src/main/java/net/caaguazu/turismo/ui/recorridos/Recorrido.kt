package net.caaguazu.turismo.ui.recorridos

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import net.caaguazu.turismo.core.Compartir
import net.caaguazu.turismo.core.MapasExternos
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.datos.Datos
import net.caaguazu.turismo.datos.Parada
import net.caaguazu.turismo.ui.mapa.MapaCaaguazu
import net.caaguazu.turismo.ui.mapa.Pin
import net.caaguazu.turismo.ui.piezas.BotonFlotante
import net.caaguazu.turismo.ui.piezas.BarraAccion
import net.caaguazu.turismo.ui.piezas.Cargador
import net.caaguazu.turismo.ui.piezas.Estado
import net.caaguazu.turismo.ui.piezas.FilaCompacta
import net.caaguazu.turismo.ui.piezas.Icono
import net.caaguazu.turismo.ui.piezas.PildoraMeta
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

    Box(modifier.fillMaxSize().background(Tono.fondo)) {
        Cargador(estado = estado.value, reintentar = reintentar) { recorrido ->
            val disponibles = recorrido.paradas.filter { it.disponible }
            val puntos = disponibles.mapNotNull { it.coordenadas?.let { c -> c.lat to c.lng } }

            Column(Modifier.fillMaxSize()) {
                LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 16.dp)) {

                    item {
                        // El mapa hace de cabecera: mismo lugar y misma forma
                        // que la foto de una ficha, redondeado solo abajo
                        // contra el contenido.
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 10f)
                                .clip(
                                    RoundedCornerShape(
                                        bottomStart = Radio.hoja,
                                        bottomEnd = Radio.hoja,
                                    ),
                                ),
                        ) {
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
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (recorrido.duracionEstimada.isNotBlank()) {
                                    PildoraMeta(recorrido.duracionEstimada, tinta = Tono.acento)
                                }
                                PildoraMeta(
                                    "${recorrido.cantidadParadas} " + Textos.t("rec.paradas"),
                                )
                            }
                            recorrido.costoTotal?.takeIf { it.hayPago }?.let { costo ->
                                Texto(
                                    costo.detalle.joinToString(" · "),
                                    Letra.fecha,
                                    Tono.tintaSuave,
                                )
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
                        FilaParadaPrehecha(parada) { alAbrirFicha(parada.refId) }
                    }
                }

                if (puntos.size >= 2) {
                    BarraAccion(
                        textoBoton = Textos.t("rec.abrir"),
                        alTocar = { MapasExternos.abrirRecorrido(contexto, puntos) },
                    )
                }
            }
        }

        BotonFlotante(
            icono = Icono.volver,
            descripcion = Textos.t("accion.volver"),
            alTocar = alVolver,
            modifier = Modifier.statusBarsPadding().padding(12.dp),
        )
        (estado.value as? Estado.Listo)?.valor?.let { recorrido ->
            BotonFlotante(
                icono = Icono.compartir,
                descripcion = Textos.t("diag.compartir"),
                alTocar = { Compartir.compartir(contexto, recorrido.titulo, "recorrido/${recorrido.id}") },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(12.dp),
            )
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
    val disponible = parada.disponible
    FilaCompacta(
        imagen = if (disponible) parada.portada else null,
        titulo = if (disponible) parada.titulo else Textos.t("rec.noDisponible"),
        detalle = parada.texto.ifBlank { null },
        meta = parada.orden.toString(),
        colorMeta = if (disponible) Tono.acento else Tono.tintaSuave,
        modifier = Modifier.padding(horizontal = Medida.margen, vertical = 5.dp),
        // Una parada despublicada se muestra igual y apagada: que desapareciera
        // sin avisar dejaria al usuario con un recorrido que cambio solo y sin
        // saber por que. Apagada tampoco lleva a ningun lado.
        alTocar = if (disponible) alTocar else null,
    )
}
