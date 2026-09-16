package net.caaguazu.turismo.ios

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import net.caaguazu.turismo.core.Resultado
import net.caaguazu.turismo.datos.Encuadre
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position

/**
 * El mapa de Caaguazu en iOS.
 *
 * Es el mismo estilo, el mismo recorte embebido y el mismo encuadre que Android:
 * lo unico distinto es de donde sale el archivo. MapLibre iOS trae soporte
 * nativo de `pmtiles://` desde la 6.10, asi que no hace falta una sola linea de
 * Swift para leerlo.
 *
 * La atribucion `© OpenStreetMap` la dibuja el overlay que `MaplibreMap` pone
 * por omision, y el texto sale del campo `attribution` del propio `estilo.json`.
 * Eso cumple la ODbL sin que aparezca un literal en el codigo. Con una
 * diferencia respecto de Android, que la dibuja siempre visible: aca queda
 * detras de un boton que se despliega. Igualarlas es trabajo para cuando haya
 * pantallas de verdad.
 */
@Composable
fun MapaCaaguazu(estilo: String, modifier: Modifier = Modifier) {
    // Argumentos nombrados a proposito: Position es (longitud, latitud) —el
    // orden de GeoJSON, no el que se dice en voz alta—, y CameraPosition abre
    // con bearing. Por posicion, cualquiera de los dos se invierte en silencio
    // y el mapa aparece en otro continente.
    val estado = rememberMapState(
        baseStyle = BaseStyle.Json(estilo),
        initialCameraPosition = CameraPosition(
            target = Position(longitude = Encuadre.LON_CENTRO, latitude = Encuadre.LAT_CENTRO),
            zoom = Encuadre.ZOOM_INICIAL,
        ),
    )

    MaplibreMap(modifier = modifier.fillMaxSize(), state = estado)
}

/**
 * La pantalla completa: resuelve el estilo y dibuja.
 *
 * El caso de fallo queda como superficie vacia y no como mensaje porque todo
 * texto visible sale de `Textos.t(...)`, y `Textos` todavia no cruzo a
 * multiplataforma. Es el pendiente mas visible de este modulo, no un olvido.
 */
@Composable
fun PantallaMapaIos(modifier: Modifier = Modifier) {
    val estilo = remember { BaseMapaIos.estilo() }

    Box(modifier.fillMaxSize()) {
        if (estilo is Resultado.Bien) {
            MapaCaaguazu(estilo = estilo.valor)
        }
    }
}
