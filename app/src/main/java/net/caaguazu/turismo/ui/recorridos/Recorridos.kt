package net.caaguazu.turismo.ui.recorridos

import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import net.caaguazu.turismo.core.Guardado
import net.caaguazu.turismo.core.MapasExternos
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.datos.Datos
import net.caaguazu.turismo.datos.ItemInventario
import net.caaguazu.turismo.datos.Recorrido
import net.caaguazu.turismo.ui.piezas.BotonIcono
import net.caaguazu.turismo.ui.piezas.CabeceraPantalla
import net.caaguazu.turismo.ui.piezas.Cargador
import net.caaguazu.turismo.ui.piezas.ChipFiltro
import net.caaguazu.turismo.ui.piezas.FilaCompacta
import net.caaguazu.turismo.ui.piezas.Icono
import net.caaguazu.turismo.ui.piezas.PildoraMeta
import net.caaguazu.turismo.ui.piezas.TileEtiquetado
import net.caaguazu.turismo.ui.piezas.PildoraPrimaria
import net.caaguazu.turismo.ui.piezas.Texto
import net.caaguazu.turismo.ui.piezas.cargar
import net.caaguazu.turismo.ui.tema.Letra
import net.caaguazu.turismo.ui.tema.Medida
import net.caaguazu.turismo.ui.tema.Tono

/** Las dos caras de la seccion: los armados por el equipo y el propio. */
enum class Pestana(val clave: String) {
    PREHECHOS("rec.prehechos"),
    MIO("rec.mio"),
}

class PilaRecorridos {
    var pestana by mutableStateOf(Pestana.MIO)
    var abierto by mutableStateOf<Int?>(null)
        private set

    fun abrir(id: Int) { abierto = id }

    fun volver(): Boolean {
        if (abierto == null) return false
        abierto = null
        return true
    }

    fun raiz() { abierto = null }
}

@Composable
fun Recorridos(
    pila: PilaRecorridos,
    alAbrirFicha: (Int) -> Unit,
    alAbrirPerfil: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val abierto = pila.abierto
    if (abierto != null) {
        PantallaRecorrido(
            id = abierto,
            alAbrirFicha = alAbrirFicha,
            alVolver = { pila.volver() },
            modifier = modifier,
        )
        return
    }

    Column(modifier.fillMaxSize().background(Tono.fondo)) {
        CabeceraPantalla(Textos.t("nav.recorridos")) {
            BotonIcono(
                icono = Icono.perfil,
                descripcion = Textos.t("barra.perfil"),
                alTocar = alAbrirPerfil,
            )
        }
        Pestanas(pila)
        when (pila.pestana) {
            Pestana.MIO -> MiRecorrido(alAbrirFicha)
            Pestana.PREHECHOS -> ListaPrehechos(pila::abrir)
        }
    }
}

@Composable
private fun Pestanas(pila: PilaRecorridos) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Medida.margen)
            .padding(bottom = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Pestana.entries.forEach { pestana ->
            ChipFiltro(
                texto = Textos.t(pestana.clave),
                activo = pila.pestana == pestana,
                alTocar = { pila.pestana = pestana },
            )
        }
    }
}

/**
 * El recorrido que arma el usuario.
 *
 * Vive en el telefono: la primera version no tiene cuenta, asi que no hay donde
 * sincronizarlo. Se resuelve pidiendo el inventario una vez y filtrando, en vez
 * de una ficha por parada.
 */
@Composable
private fun MiRecorrido(alAbrirFicha: (Int) -> Unit) {
    val contexto = LocalContext.current
    val guardadas = Guardado.recorrido
    val (estado, reintentar) = cargar(guardadas.size) { Datos.api.inventario(porPagina = 100) }

    if (guardadas.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(32.dp), Alignment.Center) {
            Texto(Textos.t("rec.vacio"), Letra.descripcion, Tono.tintaSuave)
        }
        return
    }

    Cargador(estado = estado.value, reintentar = reintentar, modifier = Modifier.fillMaxSize()) { pagina ->
        // Se respeta el orden en que la persona las fue agregando.
        // Un indice por id: buscar linealmente dentro del bucle que dibuja la
        // lista convierte el dibujado en cuadratico.
        val porId = pagina.items.associateBy { it.id }
        val paradas = guardadas.mapNotNull(porId::get)
        val orden = Guardado.ordenDeParada
        val puntos = paradas.mapNotNull { it.coordenadas?.let { c -> c.lat to c.lng } }
        val entra = puntos.size >= 2 &&
            puntos.size - 2 <= MapasExternos.MAX_PARADAS_INTERMEDIAS

        Column(Modifier.fillMaxSize()) {
            LazyColumn(Modifier.weight(1f)) {
                items(paradas, key = { it.id }) { parada ->
                    FilaParada(
                        orden = orden[parada.id] ?: 0,
                        item = parada,
                        alAbrir = { alAbrirFicha(parada.id) },
                        alQuitar = { Guardado.alternarEnRecorrido(parada.id) },
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(Medida.margen),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Google Maps corta en nueve paradas intermedias. Cuando no
                // entra se dice, en vez de abrir un recorrido incompleto sin
                // que nadie se entere.
                if (!entra && puntos.size > 2) {
                    Texto(Textos.t("rec.demasiadas"), Letra.chip, Tono.tinta)
                }
                PildoraPrimaria(
                    texto = Textos.t("rec.abrir"),
                    alTocar = { MapasExternos.abrirRecorrido(contexto, puntos) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun FilaParada(
    orden: Int,
    item: ItemInventario,
    alAbrir: () -> Unit,
    alQuitar: () -> Unit,
) {
    FilaCompacta(
        imagen = item.portada,
        titulo = item.titulo,
        detalle = item.zona?.nombre,
        meta = orden.toString(),
        modifier = Modifier.padding(horizontal = Medida.margen, vertical = 5.dp),
        alTocar = alAbrir,
    ) {
        BotonIcono(
            icono = Icono.quitar,
            descripcion = Textos.t("rec.quitar"),
            tinta = Tono.tintaSuave,
            alTocar = alQuitar,
        )
    }
}

@Composable
private fun ListaPrehechos(alAbrir: (Int) -> Unit) {
    val (estado, reintentar) = cargar { Datos.api.recorridos() }

    Cargador(
        estado = estado.value,
        reintentar = reintentar,
        vacio = { it.items.isEmpty() },
        modifier = Modifier.fillMaxSize(),
    ) { pagina ->
        LazyColumn(contentPadding = PaddingValues(bottom = Medida.colaDeLista)) {
            items(pagina.items, key = { it.id }) { recorrido ->
                TarjetaRecorrido(recorrido) { alAbrir(recorrido.id) }
            }
        }
    }
}

@Composable
private fun TarjetaRecorrido(recorrido: Recorrido, alTocar: () -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = Medida.margen, vertical = 7.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        TileEtiquetado(
            imagen = recorrido.portada,
            etiqueta = recorrido.titulo,
            proporcion = 16f / 10f,
            alTocar = alTocar,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (recorrido.duracionEstimada.isNotBlank()) {
                PildoraMeta(recorrido.duracionEstimada, tinta = Tono.acento)
            }
            PildoraMeta("${recorrido.cantidadParadas} " + Textos.t("rec.paradas"))
        }
    }
}
