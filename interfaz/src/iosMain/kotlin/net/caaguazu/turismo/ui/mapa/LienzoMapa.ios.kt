package net.caaguazu.turismo.ui.mapa

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.convertToColor
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.interaction.ClickResult
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position

/** El mismo color de respaldo que usa `colorDePin` cuando el panel no manda uno. */
private const val COLOR_DE_RESPALDO = 0xFFE9503F

/**
 * El lienzo de iOS, con `maplibre-compose`.
 *
 * Es el mismo estilo, el mismo recorte embebido y el mismo encuadre que
 * Android. MapLibre iOS trae soporte nativo de `pmtiles://` desde la 6.10, asi
 * que no hace falta una sola linea de Swift para leerlo.
 *
 * La atribucion `© OpenStreetMap` la dibuja `MapaCaaguazu` encima, igual que en
 * Android, y no se deja en manos del overlay de `MaplibreMap`, que la esconde
 * detras de un boton que hay que tocar. Es obligatoria por la ODbL y tiene que
 * verse sin que nadie la busque.
 */
@Composable
actual fun LienzoMapa(
    estilo: String,
    marcadores: List<Pin>,
    alTocarMarcador: (Int) -> Unit,
) {
    // Igual que en Android: el callback se lee al tocar y no al armar el mapa,
    // asi una recomposicion de la pantalla de arriba no rearma el estado.
    val tocar by rememberUpdatedState(alTocarMarcador)

    // Argumentos nombrados a proposito: Position es (longitud, latitud) —el
    // orden de GeoJSON, no el que se dice en voz alta—, y CameraPosition abre
    // con bearing. Por posicion, cualquiera de los dos se invierte en silencio
    // y el mapa aparece en otro continente.
    val estado = rememberMapState(
        baseStyle = BaseStyle.Json(estilo),
        initialCameraPosition = CameraPosition(
            target = Position(longitude = BaseMapa.LON_CENTRO, latitude = BaseMapa.LAT_CENTRO),
            zoom = BaseMapa.ZOOM_INICIAL,
        ),
    ) {
        // Las capas van adentro del estado del mapa, no adentro de MaplibreMap:
        // pertenecen al estilo, no al arbol de interfaz que flota encima.
        //
        // La fuente se declara siempre, aunque la lista este vacia: una
        // coleccion vacia dibuja cero circulos, y asi no hay una capa que
        // aparece y desaparece segun si los pines ya llegaron.
        val fuente = rememberGeoJsonSource(
            data = GeoJsonData.JsonString(geoJson(marcadores)),
        )

        // El color sale de la propiedad del GeoJSON, igual que en Android, asi
        // que el color de categoria que manda el panel se respeta en las dos
        // plataformas. El borde blanco es lo que hace que el pin se lea sobre
        // cualquier fondo del mapa.
        CircleLayer(
            id = "capa-pines",
            source = fuente,
            radius = const(7.dp),
            color = feature["color"].convertToColor(const(Color(COLOR_DE_RESPALDO))),
            strokeWidth = const(2.dp),
            strokeColor = const(Color.White),
            onClick = { rasgos ->
                val id = rasgos.firstNotNullOfOrNull { rasgo ->
                    rasgo.properties?.get("id")?.jsonPrimitive?.intOrNull
                }
                if (id != null) {
                    tocar(id)
                    ClickResult.Consume
                } else {
                    // Sin id no hay ficha que abrir: el toque sigue su camino
                    // en vez de morir aca.
                    ClickResult.Pass
                }
            },
        )
    }

    MaplibreMap(modifier = Modifier.fillMaxSize(), state = estado)
}
