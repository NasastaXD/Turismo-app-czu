package net.caaguazu.turismo.ui.recorridos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import net.caaguazu.turismo.core.Guardado
import net.caaguazu.turismo.core.MapasExternos
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.datos.Datos
import net.caaguazu.turismo.datos.ItemInventario
import net.caaguazu.turismo.datos.Recorrido
import net.caaguazu.turismo.ui.piezas.Cargador
import net.caaguazu.turismo.ui.piezas.Foto
import net.caaguazu.turismo.ui.piezas.Hairline
import net.caaguazu.turismo.ui.piezas.PildoraContorno
import net.caaguazu.turismo.ui.piezas.PildoraNegra
import net.caaguazu.turismo.ui.piezas.Texto
import net.caaguazu.turismo.ui.piezas.cargar
import net.caaguazu.turismo.ui.tema.Letra
import net.caaguazu.turismo.ui.tema.Medida
import net.caaguazu.turismo.ui.tema.Radio
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
fun Recorridos(pila: PilaRecorridos, alAbrirFicha: (Int) -> Unit, modifier: Modifier = Modifier) {
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

    Column(modifier.fillMaxSize().background(Tono.papel)) {
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
            .padding(horizontal = Medida.margen, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Pestana.entries.forEach { pestana ->
            val activa = pila.pestana == pestana
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(Radio.lista))
                    .background(if (activa) Tono.papel else Tono.banda)
                    // Activa: fondo blanco con borde de 1.5 en tinta.
                    // Inactivas: fondo banda y sin borde.
                    .then(
                        if (activa) {
                            Modifier.border(1.5.dp, Tono.tinta, RoundedCornerShape(Radio.lista))
                        } else {
                            Modifier
                        },
                    )
                    .clickable { pila.pestana = pestana }
                    .padding(horizontal = 18.dp, vertical = 10.dp),
            ) {
                Texto(
                    texto = Textos.t(pestana.clave),
                    estilo = Letra.chip,
                    color = Tono.tinta,
                    maxLineas = 1,
                )
            }
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
                    Hairline(Modifier.fillMaxWidth().padding(horizontal = Medida.margen))
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
                PildoraNegra(
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
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = alAbrir).padding(Medida.margen),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(28.dp).clip(RoundedCornerShape(Radio.completo)).background(Tono.negro),
            contentAlignment = Alignment.Center,
        ) {
            Texto(orden.toString(), Letra.etiquetaNav, Color.White, maxLineas = 1)
        }
        Foto(item.portada, item.titulo, Modifier.size(56.dp))
        Texto(item.titulo, Letra.tituloTarjeta, Tono.tinta, maxLineas = 2, modifier = Modifier.weight(1f))
        PildoraContorno(texto = Textos.t("rec.quitar"), alTocar = alQuitar)
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
        LazyColumn(contentPadding = PaddingValues(bottom = Medida.margen)) {
            items(pagina.items, key = { it.id }) { recorrido ->
                TarjetaRecorrido(recorrido) { alAbrir(recorrido.id) }
            }
        }
    }
}

@Composable
private fun TarjetaRecorrido(recorrido: Recorrido, alTocar: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clickable(onClick = alTocar).padding(bottom = Medida.margen),
    ) {
        Foto(
            imagen = recorrido.portada,
            descripcion = recorrido.titulo,
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 10f),
        )
        Column(
            modifier = Modifier.padding(horizontal = Medida.margen, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Texto(recorrido.titulo, Letra.tituloTarjeta, Tono.tinta, maxLineas = 2)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Texto(recorrido.duracionEstimada, Letra.chip, Tono.acento, maxLineas = 1)
                Texto(
                    texto = "${recorrido.cantidadParadas} " + Textos.t("rec.paradas"),
                    estilo = Letra.chip,
                    color = Tono.tintaSuave,
                    maxLineas = 1,
                )
            }
        }
    }
}
