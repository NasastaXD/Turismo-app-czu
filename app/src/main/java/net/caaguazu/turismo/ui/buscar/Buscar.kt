package net.caaguazu.turismo.ui.buscar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as itemsDeGrilla
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import net.caaguazu.turismo.core.Guardado
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.datos.Datos
import net.caaguazu.turismo.datos.ItemInventario
import net.caaguazu.turismo.ui.Filtros
import net.caaguazu.turismo.ui.PilaBusqueda
import net.caaguazu.turismo.ui.RutaBusqueda
import net.caaguazu.turismo.ui.articulos.fechaCorta
import net.caaguazu.turismo.ui.inventario.PantallaFicha
import net.caaguazu.turismo.ui.mapa.MapaCaaguazu
import net.caaguazu.turismo.ui.mapa.Pin
import net.caaguazu.turismo.ui.piezas.BarraFlotanteMapa
import net.caaguazu.turismo.ui.piezas.BotonFiltros
import net.caaguazu.turismo.ui.piezas.BotonIcono
import net.caaguazu.turismo.ui.piezas.CabeceraPantalla
import net.caaguazu.turismo.ui.piezas.CampoBusqueda
import net.caaguazu.turismo.ui.piezas.Cargador
import net.caaguazu.turismo.ui.piezas.Corazon
import net.caaguazu.turismo.ui.piezas.Cruce
import net.caaguazu.turismo.ui.piezas.Estado
import net.caaguazu.turismo.ui.piezas.FilaCompacta
import net.caaguazu.turismo.ui.piezas.Icono
import net.caaguazu.turismo.ui.piezas.TarjetaDeMapa
import net.caaguazu.turismo.ui.piezas.TarjetaGrande
import net.caaguazu.turismo.ui.piezas.Texto
import net.caaguazu.turismo.ui.piezas.TileEtiquetado
import net.caaguazu.turismo.ui.piezas.cargar
import net.caaguazu.turismo.ui.tema.Letra
import net.caaguazu.turismo.ui.tema.Medida
import net.caaguazu.turismo.ui.tema.Tono

/** Cuanto se espera despues de la ultima letra antes de pedirle a la API. */
private const val ESPERA_BUSQUEDA_MS = 350L

/**
 * La seccion Buscar.
 *
 * Reemplaza al arbol categorias -> lista -> ficha por una sola pantalla que
 * cambia de cara segun lo que se pidio. Mientras no se busco ni se filtro nada,
 * muestra las categorias como mosaico; en cuanto hay algo pedido, se convierte
 * en resultados. No son dos pantallas: es la misma, y por eso escribir no
 * pierde el contexto ni obliga a volver atras para cambiar de idea.
 *
 * El mapa tampoco es un destino aparte. Es la misma busqueda dibujada sobre el
 * lienzo, con la barra flotando encima y una tarjeta abajo para el pin tocado.
 */
@Composable
fun Buscar(
    pila: PilaBusqueda,
    alAbrirPerfil: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val ruta = pila.actual) {
        is RutaBusqueda.Ficha -> PantallaFicha(
            id = ruta.id,
            alVolver = { pila.volver() },
            modifier = modifier,
        )

        is RutaBusqueda.Explorar -> Explorar(pila, alAbrirPerfil, modifier)
    }
}

@Composable
private fun Explorar(
    pila: PilaBusqueda,
    alAbrirPerfil: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Lo escrito se aplica con retardo: pedirle a la API por cada letra seria
    // una peticion por pulsacion sobre una red que en el distrito no sobra.
    var aplicada by remember { mutableStateOf(pila.consulta) }
    LaunchedEffect(pila.consulta) {
        delay(ESPERA_BUSQUEDA_MS)
        aplicada = pila.consulta
    }

    val filtros = pila.filtros
    val (categorias, _) = cargar { Datos.api.categorias() }
    val (resultados, reintentar) = cargar(aplicada, filtros) {
        Datos.api.inventario(
            categoria = filtros.categoria,
            zona = filtros.zona,
            etiqueta = filtros.etiqueta,
            buscar = aplicada.ifBlank { null },
            porPagina = 60,
        )
    }

    // El precio se filtra en el telefono: el contrato no tiene parametro para
    // el, y pedirle al panel que lo agregue para poder dibujar esto seria
    // esperar una version del servidor para mover un control.
    val items = ((resultados.value as? Estado.Listo)?.valor?.items.orEmpty())
        .filter { item -> filtros.precioMaximo == null || (item.rangoPrecio ?: 0) <= filtros.precioMaximo }

    Box(modifier.fillMaxSize().background(Tono.fondo)) {
        Cruce(pila.enMapa) { enMapa ->
            if (enMapa) {
                VistaMapa(pila, items)
            } else {
                VistaLista(pila, categorias.value, resultados.value, items, reintentar, alAbrirPerfil)
            }
        }

        HojaFiltros(pila)
    }
}

/* -------------------------------------------------------------------------
 * Cara de lista
 * ----------------------------------------------------------------------- */

@Composable
private fun VistaLista(
    pila: PilaBusqueda,
    categorias: Estado<List<net.caaguazu.turismo.datos.Categoria>>,
    resultados: Estado<*>,
    items: List<ItemInventario>,
    reintentar: () -> Unit,
    alAbrirPerfil: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        CabeceraPantalla(Textos.t("nav.inventario")) {
            BotonIcono(
                icono = Icono.capas,
                descripcion = Textos.t("inv.mapa"),
                alTocar = { pila.enMapa = true },
            )
            BotonIcono(
                icono = Icono.ajustes,
                descripcion = Textos.t("barra.ajustes"),
                alTocar = alAbrirPerfil,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Medida.margen)
                .padding(bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CampoBusqueda(
                valor = pila.consulta,
                alCambiar = { pila.consulta = it },
                marcador = Textos.t("barra.buscar"),
                modifier = Modifier.weight(1f),
            )
            BotonFiltros(
                cantidad = pila.filtros.cantidad,
                descripcion = Textos.t("filtro.titulo"),
                alTocar = pila::abrirFiltros,
            )
        }

        if (pila.buscando) {
            Cargador(
                estado = resultados,
                reintentar = reintentar,
                vacio = { items.isEmpty() },
                modifier = Modifier.fillMaxSize(),
            ) {
                Resultados(items) { id -> pila.ir(RutaBusqueda.Ficha(id)) }
            }
        } else {
            Mosaico(categorias) { id -> pila.filtros = Filtros(categoria = id) }
        }
    }
}

/**
 * Lo que se ve cuando todavia no se pidio nada: las categorias como mosaico.
 *
 * Antes esto era una pantalla propia por la que habia que pasar si o si para
 * llegar a cualquier lugar. Ahora es el estado de reposo de la busqueda —
 * sugerencia, no peaje.
 */
@Composable
private fun Mosaico(
    categorias: Estado<List<net.caaguazu.turismo.datos.Categoria>>,
    alElegir: (Int) -> Unit,
) {
    val lista = (categorias as? Estado.Listo)?.valor.orEmpty()
    if (lista.isEmpty()) return

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Medida.margen,
            end = Medida.margen,
            bottom = Medida.colaDeLista,
        ),
        horizontalArrangement = Arrangement.spacedBy(Medida.entreTarjetas),
        verticalArrangement = Arrangement.spacedBy(Medida.entreTarjetas),
    ) {
        itemsDeGrilla(lista, key = { it.id }) { categoria ->
            TileEtiquetado(
                imagen = categoria.portada,
                etiqueta = categoria.nombre,
                colorSinFoto = categoria.color,
                proporcion = 16f / 13f,
                alTocar = { alElegir(categoria.id) },
            )
        }
    }
}

/**
 * Los resultados.
 *
 * El primero va como tarjeta grande y el resto como filas. No es capricho: el
 * primer resultado de una busqueda es el que la mayoria abre, y darle la foto
 * entera convierte una lista de nombres en algo que se mira. Los demas van
 * compactos, que es lo que hace que entren seis en la pantalla.
 */
@Composable
private fun Resultados(items: List<ItemInventario>, alAbrir: (Int) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Medida.margen,
            end = Medida.margen,
            bottom = Medida.colaDeLista,
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Texto(
                texto = "${items.size} " + Textos.t("inv.resultados"),
                estilo = Letra.chip,
                color = Tono.tintaSuave,
                maxLineas = 1,
            )
        }
        items.firstOrNull()?.let { primero ->
            item(key = primero.id) {
                TarjetaGrande(
                    imagen = primero.portada,
                    titulo = primero.titulo,
                    encima = primero.zona?.nombre,
                    alTocar = { alAbrir(primero.id) },
                    esquina = {
                        Corazon(
                            marcado = { Guardado.esFavorito(primero.id) },
                            alTocar = { Guardado.alternarFavorito(primero.id) },
                            descripcion = primero.titulo,
                        )
                    },
                )
            }
        }
        items(items.drop(1), key = { it.id }) { item ->
            FilaResultado(item) { alAbrir(item.id) }
        }
    }
}

@Composable
private fun FilaResultado(item: ItemInventario, alTocar: () -> Unit) {
    // El horario es una frase entera —"Parque abierto, se visita de dia"— y va
    // en la linea gris de descripcion, no en la etiqueta de metadato: en 12sp y
    // en color de fecha entraba media frase cortada. La etiqueta queda para lo
    // que de verdad es una fecha, que es de lo que habla el acento.
    val descripcion = listOf(item.gancho, item.zona?.nombre.orEmpty(), item.horarioResumen)
        .firstOrNull { it.isNotBlank() }

    FilaCompacta(
        imagen = item.portada,
        titulo = item.titulo,
        detalle = descripcion,
        meta = fechaCorta(item.fechas?.inicio),
        // Un evento en curso es lo unico de la lista que cambia solo, y es el
        // unico lugar donde aparece el mango.
        colorMeta = if (item.fechas?.enCurso == true) Tono.destacado else Tono.acento,
        alTocar = alTocar,
    ) {
        Corazon(
            // Lectura diferida: marcar un favorito redibuja un corazon, no la
            // lista entera.
            marcado = { Guardado.esFavorito(item.id) },
            alTocar = { Guardado.alternarFavorito(item.id) },
            descripcion = item.titulo,
            sobreFoto = false,
        )
    }
}

/* -------------------------------------------------------------------------
 * Cara de mapa
 * ----------------------------------------------------------------------- */

/**
 * El mapa a pantalla completa.
 *
 * Antes el mapa era el ultimo tercio de una columna con cabecera, buscador,
 * chips y contador encima: en un telefono quedaba del tamaño de una estampilla,
 * que para un distrito de 942 km no alcanza para ubicarse. Ahora ocupa todo y
 * los controles flotan encima.
 */
@Composable
private fun VistaMapa(pila: PilaBusqueda, items: List<ItemInventario>) {
    val elegido = pila.seleccionEnMapa?.let { id -> items.firstOrNull { it.id == id } }

    Box(Modifier.fillMaxSize()) {
        MapaCaaguazu(
            marcadores = items.mapNotNull { item ->
                item.coordenadas?.let { Pin(item.id, it.lat, it.lng, item.categoria?.color) }
            },
            alTocarMarcador = { pila.seleccionEnMapa = it },
            modifier = Modifier.fillMaxSize(),
        )

        BarraFlotanteMapa {
            CampoBusqueda(
                valor = pila.consulta,
                alCambiar = { pila.consulta = it },
                marcador = Textos.t("barra.buscar"),
                modifier = Modifier.weight(1f),
            )
            BotonFiltros(
                cantidad = pila.filtros.cantidad,
                descripcion = Textos.t("filtro.titulo"),
                alTocar = pila::abrirFiltros,
            )
            BotonIcono(
                icono = Icono.lista,
                descripcion = Textos.t("inv.lista"),
                alTocar = {
                    pila.seleccionEnMapa = null
                    pila.enMapa = false
                },
            )
        }

        if (elegido != null) {
            TarjetaDeMapa(
                imagen = elegido.portada,
                titulo = elegido.titulo,
                detalle = elegido.gancho.ifBlank { elegido.zona?.nombre },
                alAbrir = { pila.ir(RutaBusqueda.Ficha(elegido.id)) },
                alCerrar = { pila.seleccionEnMapa = null },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(Medida.margen),
            )
        }
    }
}
