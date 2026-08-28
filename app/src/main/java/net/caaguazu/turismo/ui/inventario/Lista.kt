package net.caaguazu.turismo.ui.inventario

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import net.caaguazu.turismo.core.Guardado
import net.caaguazu.turismo.core.MapasExternos
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.datos.Categoria
import net.caaguazu.turismo.datos.Datos
import net.caaguazu.turismo.datos.ItemInventario
import net.caaguazu.turismo.datos.Pagina
import net.caaguazu.turismo.ui.articulos.fechaCorta
import net.caaguazu.turismo.ui.mapa.MapaCaaguazu
import net.caaguazu.turismo.ui.mapa.Pin
import net.caaguazu.turismo.ui.piezas.BotonIcono
import net.caaguazu.turismo.ui.piezas.CabeceraPantalla
import net.caaguazu.turismo.ui.piezas.CampoBusqueda
import net.caaguazu.turismo.ui.piezas.Cargador
import net.caaguazu.turismo.ui.piezas.ChipFiltro
import net.caaguazu.turismo.ui.piezas.Corazon
import net.caaguazu.turismo.ui.piezas.Cruce
import net.caaguazu.turismo.ui.piezas.Estado
import net.caaguazu.turismo.ui.piezas.FilaCompacta
import net.caaguazu.turismo.ui.piezas.Icono
import net.caaguazu.turismo.ui.piezas.InterruptorListaMapa
import net.caaguazu.turismo.ui.piezas.Texto
import net.caaguazu.turismo.ui.piezas.cargar
import net.caaguazu.turismo.ui.tema.Letra
import net.caaguazu.turismo.ui.tema.Medida
import net.caaguazu.turismo.ui.tema.Tono

/** Cuanto se espera despues de la ultima letra antes de pedirle a la API. */
private const val ESPERA_BUSQUEDA_MS = 350L

/**
 * Los atractivos de una categoria, en lista o sobre el mapa.
 *
 * Es la misma pantalla y el mismo conjunto de datos: el interruptor solo cambia
 * como se dibujan. Separarlas en dos pantallas obligaria a mantener dos veces el
 * filtrado y a que el usuario perdiera el contexto al saltar de una a otra.
 *
 * La lista es de filas compactas. Antes cada resultado era una tarjeta con una
 * foto de 180 y entraban dos y medio en la pantalla; ahora entran seis, que es
 * la diferencia entre recorrer una lista y hacer scroll a ciegas.
 */
@Composable
fun PantallaLista(
    categoria: Categoria?,
    enMapa: () -> Boolean,
    alCambiarVista: (Boolean) -> Unit,
    alAbrir: (Int) -> Unit,
    alVolver: () -> Unit,
    alAbrirPerfil: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var busqueda by remember { mutableStateOf("") }
    var buscarPor by remember { mutableStateOf("") }
    LaunchedEffect(busqueda) {
        delay(ESPERA_BUSQUEDA_MS)
        buscarPor = busqueda
    }
    var etiquetaElegida by remember { mutableStateOf<Int?>(null) }
    val (estadoEtiquetas, _) = cargar { Datos.api.etiquetas() }

    val (estado, reintentar) = cargar(categoria?.id, buscarPor, etiquetaElegida) {
        Datos.api.inventario(
            categoria = categoria?.id,
            etiqueta = etiquetaElegida,
            buscar = buscarPor.ifBlank { null },
            porPagina = 50,
        )
    }
    val titulo = categoria?.nombre ?: Textos.t("nav.inventario")

    Column(modifier.fillMaxSize().background(Tono.fondo)) {
        // Volver es un boton, no un rastro de migas. El breadcrumb repetia la
        // seccion que el titulo de abajo ya dice, y para volver un nivel una
        // flecha alcanza.
        CabeceraPantalla(titulo) {
            BotonIcono(
                icono = Icono.perfil,
                descripcion = Textos.t("barra.perfil"),
                alTocar = alAbrirPerfil,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Medida.margen)
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BotonIcono(
                icono = Icono.volver,
                descripcion = Textos.t("accion.volver"),
                alTocar = alVolver,
            )
            CampoBusqueda(
                valor = busqueda,
                alCambiar = { busqueda = it },
                marcador = Textos.t("barra.buscar"),
                modifier = Modifier.weight(1f),
            )
        }

        val etiquetas = (estadoEtiquetas.value as? Estado.Listo)?.valor.orEmpty()
        if (etiquetas.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = Medida.margen),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 14.dp),
            ) {
                items(etiquetas, key = { it.id }) { etiqueta ->
                    ChipFiltro(
                        texto = etiqueta.nombre,
                        activo = etiquetaElegida == etiqueta.id,
                        alTocar = {
                            etiquetaElegida = if (etiquetaElegida == etiqueta.id) null else etiqueta.id
                        },
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Medida.margen)
                .padding(bottom = 14.dp),
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
                            bottom = Medida.colaDeLista,
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(pagina.items, key = { item -> item.id }) { item ->
                            FilaResultado(item) { alAbrir(item.id) }
                        }
                    }
                }
            }
        }
    }
}

/** Cuantos resultados hay. Dato en vivo, no navegacion. */
@Composable
private fun RangoResultados(estado: Estado<Pagina<ItemInventario>>) {
    val total = (estado as? Estado.Listo)?.valor?.total
    Texto(
        texto = if (total == null) "" else "$total " + Textos.t("inv.resultados"),
        estilo = Letra.chip,
        color = Tono.tintaSuave,
        maxLineas = 1,
    )
}

/**
 * Una fila de resultado.
 *
 * Miniatura, nombre, el gancho en una linea y el metadato de cuando o cuanto.
 * El corazon va suelto al final —sin el circulo de papel, que aca no tiene una
 * foto de la que despegarse— y el atajo al mapa solo aparece si el lugar tiene
 * coordenadas: un boton que no puede hacer nada no se dibuja.
 */
@Composable
private fun FilaResultado(item: ItemInventario, alTocar: () -> Unit) {
    val contexto = LocalContext.current
    val coordenadas = item.coordenadas
    val cuando = fechaCorta(item.fechas?.inicio) ?: item.horarioResumen

    FilaCompacta(
        imagen = item.portada,
        titulo = item.titulo,
        detalle = item.gancho.ifBlank { item.zona?.nombre },
        meta = cuando.ifBlank { null },
        // Un evento en curso es lo unico de la lista que cambia solo, y es el
        // unico lugar donde aparece el mango.
        colorMeta = if (item.fechas?.enCurso == true) Tono.destacado else Tono.acento,
        alTocar = alTocar,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (coordenadas != null) {
                BotonIcono(
                    icono = Icono.pin,
                    descripcion = item.titulo,
                    tinta = Tono.tintaSuave,
                    alTocar = {
                        MapasExternos.abrirPunto(
                            contexto, coordenadas.lat, coordenadas.lng, item.titulo,
                        )
                    },
                )
            }
            Corazon(
                // Lectura diferida: el estado se lee dentro del corazon, no en
                // el cuerpo de la fila. Marcar un favorito redibuja un corazon,
                // no la lista entera.
                marcado = { Guardado.esFavorito(item.id) },
                alTocar = { Guardado.alternarFavorito(item.id) },
                descripcion = item.titulo,
                sobreFoto = false,
            )
        }
    }
}

