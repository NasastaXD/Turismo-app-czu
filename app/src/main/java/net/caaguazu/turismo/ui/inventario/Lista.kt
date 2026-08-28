package net.caaguazu.turismo.ui.inventario

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.caaguazu.turismo.core.Guardado
import net.caaguazu.turismo.core.MapasExternos
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.datos.Categoria
import net.caaguazu.turismo.datos.Datos
import net.caaguazu.turismo.datos.ItemInventario
import net.caaguazu.turismo.ui.mapa.MapaCaaguazu
import net.caaguazu.turismo.ui.mapa.Pin
import net.caaguazu.turismo.ui.articulos.fechaCorta
import net.caaguazu.turismo.ui.piezas.Badge
import net.caaguazu.turismo.ui.piezas.Cargador
import net.caaguazu.turismo.ui.piezas.Cruce
import net.caaguazu.turismo.ui.piezas.Corazon
import net.caaguazu.turismo.ui.piezas.Foto
import net.caaguazu.turismo.ui.piezas.Glifo
import net.caaguazu.turismo.ui.piezas.IconoAccion
import net.caaguazu.turismo.ui.piezas.Icono
import net.caaguazu.turismo.ui.piezas.InterruptorListaMapa
import net.caaguazu.turismo.ui.piezas.RangoPrecio
import net.caaguazu.turismo.ui.piezas.Tarjeta
import net.caaguazu.turismo.ui.piezas.Texto
import net.caaguazu.turismo.ui.piezas.cargar
import net.caaguazu.turismo.ui.tema.Letra
import net.caaguazu.turismo.ui.tema.Medida
import net.caaguazu.turismo.ui.tema.Radio
import net.caaguazu.turismo.ui.tema.Tono

/** Media cuadrada de la tarjeta de lista, en la medida que fija el sistema. */
private val LADO_MEDIA = 180.dp

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
    val titulo = categoria?.nombre ?: Textos.t("nav.inventario")

    Column(modifier.fillMaxSize().background(Tono.fondo)) {
        Breadcrumb(seccion = titulo, alVolver = alVolver)

        Texto(
            texto = titulo,
            estilo = Letra.tituloPagina,
            color = Tono.tinta,
            alinear = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Medida.margen, vertical = 20.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Medida.margen)
                .padding(bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            RangoResultados(estado.value)
            InterruptorListaMapa(enMapa = enMapa, alCambiar = alCambiarVista)
        }

        Cargador(
            estado = estado.value,
            reintentar = reintentar,
            vacio = { it.items.isEmpty() },
            modifier = Modifier.fillMaxSize(),
        ) { pagina ->
            Cruce(enMapa()) { mostrandoMapa ->
            if (mostrandoMapa) {
                MapaCaaguazu(
                    marcadores = pagina.items.mapNotNull { item ->
                        item.coordenadas?.let { Pin(item.id, it.lat, it.lng, item.categoria?.color) }
                    },
                    alTocarMarcador = alAbrir,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = Medida.margen,
                        end = Medida.margen,
                        bottom = Medida.margen,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Medida.entreTarjetas),
                ) {
                    items(pagina.items, key = { item -> item.id }) { item ->
                        TarjetaLista(item) { alAbrir(item.id) }
                    }
                }
            }
            }
        }
    }
}

/**
 * Breadcrumb del sistema: casa y nivel padre en acento, seccion actual en tinta,
 * flecha solida. Tocarlo vuelve, que es lo que un breadcrumb promete.
 */
@Composable
private fun Breadcrumb(seccion: String, alVolver: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Medida.margen)
            .padding(top = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.clickable(onClick = alVolver),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Glifo(Icono.casa, Textos.t("nav.principal"), Tono.acento, Modifier.size(18.dp))
            Texto(Textos.t("nav.principal"), Letra.chip, Tono.acento, maxLineas = 1)
        }
        Glifo(Icono.flechaBreadcrumb, "", Tono.tinta, Modifier.size(14.dp))
        Texto(seccion, Letra.chip, Tono.tinta, maxLineas = 1)
    }
}

/** Cuantos resultados hay. Dato en vivo, no navegacion. */
@Composable
private fun RangoResultados(estado: net.caaguazu.turismo.ui.piezas.Estado<*>) {
    val total = (estado as? net.caaguazu.turismo.ui.piezas.Estado.Listo)
        ?.valor
        ?.let { it as? net.caaguazu.turismo.datos.Pagina<*> }
        ?.total
    Texto(
        texto = if (total == null) "" else "$total " + Textos.t("inv.resultados"),
        estilo = Letra.chip,
        color = Tono.tintaSuave,
        maxLineas = 1,
    )
}

/**
 * Tarjeta de lista.
 *
 * Media cuadrada arriba a la izquierda, iconos de accion a su derecha alineados
 * arriba, y debajo el texto a todo el ancho. Lo que separa una tarjeta de la
 * siguiente es su propia sombra: no hace falta alternar el fondo ni dibujar una
 * linea entre ellas.
 */
@Composable
private fun TarjetaLista(item: ItemInventario, alTocar: () -> Unit) {
    val contexto = LocalContext.current
    val coordenadas = item.coordenadas

    Tarjeta(
        modifier = Modifier.fillMaxWidth(),
        radio = Radio.lista,
        alTocar = alTocar,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Medida.dentroTarjeta),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(Modifier.size(LADO_MEDIA)) {
                    Foto(item.portada, item.titulo, Modifier.fillMaxSize())
                    Corazon(
                        // Lectura diferida: el estado se lee dentro del corazon,
                        // no en el cuerpo de la tarjeta. Marcar un favorito
                        // redibuja un corazon, no la lista entera.
                        marcado = { Guardado.esFavorito(item.id) },
                        alTocar = { Guardado.alternarFavorito(item.id) },
                        descripcion = item.titulo,
                        modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                    )
                    // Un evento que esta pasando ahora es la unica cosa de la
                    // lista que cambia sola. Por eso lleva el unico badge.
                    if (item.fechas?.enCurso == true) {
                        Badge(
                            texto = Textos.t("evento.enCurso"),
                            modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                        )
                    }
                }

                // Solo los iconos disponibles. Sin coordenadas no hay adonde ir,
                // y el bloque no reserva el hueco de un boton que no existe.
                if (coordenadas != null) {
                    IconoAccion(
                        icono = Icono.inventario,
                        descripcion = item.titulo,
                        alTocar = {
                            MapasExternos.abrirPunto(
                                contexto, coordenadas.lat, coordenadas.lng, item.titulo,
                            )
                        },
                    )
                }
            }

            Texto(item.titulo, Letra.tituloTarjeta, Tono.tinta, maxLineas = 2)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RangoPrecio(item.rangoPrecio)
                val cuando = fechaCorta(item.fechas?.inicio) ?: item.horarioResumen
                if (cuando.isNotBlank()) {
                    Texto(cuando, Letra.fecha, Tono.acento, maxLineas = 1)
                }
            }

            if (item.gancho.isNotBlank()) {
                Texto(
                    texto = item.gancho,
                    estilo = Letra.descripcion,
                    color = Tono.tintaSuave,
                    maxLineas = 3,
                    // La descripcion termina sin puntos suspensivos.
                    conPuntosSuspensivos = false,
                )
            }
        }
    }
}
