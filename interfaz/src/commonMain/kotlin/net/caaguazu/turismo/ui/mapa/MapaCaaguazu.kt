package net.caaguazu.turismo.ui.mapa

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.caaguazu.turismo.core.Resultado
import net.caaguazu.turismo.ui.tema.Tono

/** Un pin del inventario sobre el mapa. */
data class Pin(val id: Int, val lat: Double, val lng: Double, val color: String?)

/**
 * El mapa de Caaguazu, dibujado desde el archivo local.
 *
 * Sin red, sin clave de API y sin cuenta: el archivo de tiles viaja dentro de
 * la app. Si no puede cargarse, la pantalla no queda en blanco — se dibuja el
 * aviso.
 *
 * Los pines NO estan quemados en los tiles: viajan aparte y se dibujan encima.
 * Eso es lo que hace el mapa retroactivo — se registra un lugar nuevo y el pin
 * aparece sin regenerar ni redistribuir nada.
 *
 * Esta funcion es la misma en las dos plataformas: los cuatro estados, la
 * atribucion y el GeoJSON de los pines. Lo unico que cambia es `LienzoMapa`,
 * que en Android es un `MapView` clasico dentro de un `AndroidView` y en iOS es
 * `maplibre-compose`.
 */
@Composable
fun MapaCaaguazu(
    modifier: Modifier = Modifier,
    marcadores: List<Pin> = emptyList(),
    alTocarMarcador: (Int) -> Unit = {},
) {
    // Resolver el estilo es trabajo de disco: la primera vez, en Android, son
    // 2 MB del .pmtiles saliendo de los assets. Esto vivia en un `remember`, o
    // sea corriendo en el hilo que dibuja y en medio de la composicion, y el
    // mapa aparece en mas de un lugar —entre ellos como cabecera de un item de
    // lista en la pantalla de recorrido—, asi que se repetia por instancia.
    //
    // El estado es `Resultado?`, con null para "todavia cargando": son tres
    // casos y no dos, y distinguirlos importa porque dibujar "no disponible"
    // mientras carga seria avisar de un fallo que no ocurrio.
    val estilo by produceState<Resultado<String>?>(initialValue = null) {
        value = estiloDelMapa()
    }

    Box(modifier.fillMaxSize().background(Tono.banda)) {
        when (val listo = estilo) {
            // Cargando: el hueco de la banda ya dice que algo viene. Sin texto
            // ni indicador, que para un segundo de disco serian un parpadeo.
            null -> Unit

            is Resultado.Mal -> MapaNoDisponible(Modifier.align(Alignment.Center))

            is Resultado.Bien -> {
                LienzoMapa(listo.valor, marcadores, alTocarMarcador)
                AtribucionMapa(Modifier.align(Alignment.BottomStart).padding(8.dp))
            }
        }
    }
}

/**
 * El lienzo del mapa, lo unico que cada plataforma dibuja a su manera.
 *
 * Recibe el estilo ya resuelto y la lista de pines. Quien lo implementa se
 * encarga de que tocar un pin llame a `alTocarMarcador` con su id, y de que el
 * encuadre sea el de `BaseMapa` — mismo centro, mismos topes de zoom, sin
 * rotacion ni inclinacion.
 */
@Composable
expect fun LienzoMapa(
    estilo: String,
    marcadores: List<Pin>,
    alTocarMarcador: (Int) -> Unit,
)

/** El color del pin cuando la categoria no trae uno usable. */
private const val COLOR_POR_OMISION = "#E9503F"

private val HEXADECIMAL = Regex("^#?[0-9A-Fa-f]{6}([0-9A-Fa-f]{2})?$")

/**
 * El color que manda el panel solo entra al GeoJSON si es un hexadecimal.
 *
 * Es texto libre del otro lado, y pegado sin revisar rompe el JSON entero en
 * cuanto trae una comilla o una barra: MapLibre no puede leer la coleccion y
 * desaparecen TODOS los pines, no el de la categoria mal cargada. Es justo lo
 * que el proyecto no permite — un elemento roto no tumba la lista entera.
 */
internal fun colorDePin(crudo: String?): String {
    val limpio = crudo?.trim().orEmpty()
    if (!HEXADECIMAL.matches(limpio)) return COLOR_POR_OMISION
    return if (limpio.startsWith("#")) limpio else "#$limpio"
}

/**
 * GeoJSON armado a mano: son cuatro campos y evita una dependencia entera.
 *
 * Compartido porque las dos plataformas dibujan los mismos pines desde la misma
 * lista. Antes habia dos versiones —una en el mapa de Android y otra en el de
 * iOS— y ya se habian separado: la de iOS mandaba `tipo` y `categoria`, la de
 * Android mandaba `color`. Una sola version es una sola cosa que revisar.
 */
internal fun geoJson(pines: List<Pin>): String = buildString {
    append("""{"type":"FeatureCollection","features":[""")
    pines.forEachIndexed { indice, pin ->
        if (indice > 0) append(',')
        append("""{"type":"Feature","properties":{"id":${pin.id},"color":"""")
        append(colorDePin(pin.color))
        append(""""},"geometry":{"type":"Point","coordinates":[${pin.lng},${pin.lat}]}}""")
    }
    append("]}")
}
