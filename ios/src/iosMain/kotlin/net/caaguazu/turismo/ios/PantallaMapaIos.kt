package net.caaguazu.turismo.ios

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import net.caaguazu.turismo.core.Resultado
import net.caaguazu.turismo.datos.Encuadre
import net.caaguazu.turismo.datos.Marcador
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position

/**
 * El mapa de Caaguazu en iOS, con los pines del panel encima.
 *
 * Es el mismo estilo, el mismo recorte embebido y el mismo encuadre que
 * Android: lo unico distinto es de donde sale el archivo. MapLibre iOS trae
 * soporte nativo de `pmtiles://` desde la 6.10, asi que no hace falta una sola
 * linea de Swift para leerlo.
 *
 * Los pines viajan aparte de los tiles y se dibujan encima. Eso es lo que
 * mantiene el mapa retroactivo: se registra un lugar nuevo y el pin aparece sin
 * regenerar ni redistribuir nada.
 *
 * La atribucion `© OpenStreetMap` la dibuja el overlay que `MaplibreMap` pone
 * por omision, y el texto sale del campo `attribution` del propio
 * `estilo.json`. Eso cumple la ODbL sin que aparezca un literal en el codigo.
 * Con una diferencia respecto de Android, que la dibuja siempre visible: aca
 * queda detras de un boton que se despliega. Igualarlas es trabajo pendiente.
 */
@Composable
fun MapaCaaguazu(
    estilo: String,
    marcadores: List<Marcador>,
    modifier: Modifier = Modifier,
) {
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
    ) {
        // Las capas van adentro del estado del mapa, no adentro de MaplibreMap:
        // pertenecen al estilo, no al arbol de interfaz que flota encima.
        if (marcadores.isNotEmpty()) {
            val fuente = rememberGeoJsonSource(
                data = GeoJsonData.JsonString(marcadores.comoGeoJson()),
            )

            // Colores provisionales y marcados como tales: los pines de verdad
            // son los PNG por categoria que manda `/media-manifest`, y los
            // tokens de color viven en `Tono`, que todavia no cruzo a
            // multiplataforma. Mientras tanto va un circulo neutro de maximo
            // contraste, que no le roba el rol a ningun color de marca — el
            // verde es accion y el coral es favoritos, y un pin no es ninguna
            // de las dos cosas.
            CircleLayer(
                id = "pines",
                source = fuente,
                radius = const(7.dp),
                color = const(Color(0xFF1F1F21)),
                strokeWidth = const(2.dp),
                strokeColor = const(Color.White),
            )
        }
    }

    MaplibreMap(modifier = modifier.fillMaxSize(), state = estado)
}

/**
 * La pantalla completa: resuelve el estilo, trae los pines y dibuja.
 *
 * El mapa se dibuja aunque los pines no lleguen. Es deliberado y es la misma
 * decision que del lado Android: sin senal, un mapa sin pines sigue sirviendo
 * para ubicarse, y en un distrito de 942 km eso es la situacion normal.
 *
 * El caso de fallo del estilo queda como superficie vacia y no como mensaje
 * porque todo texto visible sale de `Textos.t(...)`, y `Textos` todavia no
 * cruzo a multiplataforma. Es el pendiente mas visible de este modulo, no un
 * olvido.
 */
@Composable
fun PantallaMapaIos(modifier: Modifier = Modifier) {
    val estilo = remember { BaseMapaIos.estilo() }
    var marcadores by remember { mutableStateOf(emptyList<Marcador>()) }

    LaunchedEffect(Unit) {
        val traidos = Marcadores.traer()
        if (traidos is Resultado.Bien) marcadores = traidos.valor
    }

    Box(modifier.fillMaxSize()) {
        if (estilo is Resultado.Bien) {
            MapaCaaguazu(estilo = estilo.valor, marcadores = marcadores)
        }
    }
}
