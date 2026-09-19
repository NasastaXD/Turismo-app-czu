package net.caaguazu.turismo.ui.mapa

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import net.caaguazu.turismo.core.Bitacora
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource

private const val ETIQUETA = "Mapa"
private const val FUENTE_PINES = "pines"
private const val CAPA_PINES = "capa-pines"

/** Limites del recorte que viaja en el APK: fuera de aqui no hay datos que mostrar. */
private val LIMITES = LatLngBounds.from(-25.3342, -55.8696, -25.6118, -56.1751)

/**
 * El MapView es una vista clasica con ciclo de vida propio, que hay que
 * alimentar a mano. Se crea una sola vez y se conserva mientras la pantalla
 * exista.
 */
@Composable
actual fun LienzoMapa(
    estilo: String,
    marcadores: List<Pin>,
    alTocarMarcador: (Int) -> Unit,
) {
    val contexto = LocalContext.current
    val duenoCiclo = LocalLifecycleOwner.current

    // El callback se lee al tocar, no al crear la vista: asi el mapa no se
    // reconstruye porque la pantalla de arriba se recompuso.
    val tocar by rememberUpdatedState(alTocarMarcador)

    // Los pines tambien se leen diferido, y eso no es una comodidad: el estilo
    // carga asincrono, asi que el efecto de abajo puede llegar antes de que la
    // fuente exista. Cuando pasa —y pasa siempre que la lista ya estaba lista
    // al abrir el mapa, que es el camino normal— no habia nada que volviera a
    // aplicarla y el mapa se quedaba sin un solo pin. Sembrando la fuente con
    // lo que haya en ese momento, gane la carrera quien la gane, se dibujan.
    val pines by rememberUpdatedState(marcadores)

    val vista = remember {
        MapLibre.getInstance(contexto)
        MapView(contexto).apply {
            onCreate(null)
            getMapAsync { mapa ->
                configurar(mapa)
                mapa.setStyle(Style.Builder().fromJson(estilo)) { cargado ->
                    Bitacora.info(ETIQUETA, "estilo cargado con ${cargado.layers.size} capas")
                    cargado.addSource(GeoJsonSource(FUENTE_PINES, geoJson(pines)))
                    cargado.addLayer(capaPines())
                    Bitacora.detalle(ETIQUETA, "${pines.size} pines al sembrar la fuente")
                }
                mapa.addOnMapClickListener { punto ->
                    val enPantalla = mapa.projection.toScreenLocation(punto)
                    val encontrados = mapa.queryRenderedFeatures(enPantalla, CAPA_PINES)
                    val id = encontrados.firstNotNullOfOrNull {
                        it.getNumberProperty("id")?.toInt()
                    }
                    if (id != null) tocar(id)
                    id != null
                }
            }
        }
    }

    // Los pines se actualizan solos cuando cambia la lista, sin tocar el mapa.
    DisposableEffect(marcadores) {
        vista.getMapAsync { mapa ->
            val fuente = mapa.style?.getSourceAs<GeoJsonSource>(FUENTE_PINES)
            if (fuente == null) {
                Bitacora.detalle(ETIQUETA, "el estilo todavia no esta listo para ${marcadores.size} pines")
            } else {
                fuente.setGeoJson(geoJson(marcadores))
                Bitacora.detalle(ETIQUETA, "${marcadores.size} pines dibujados")
            }
        }
        onDispose { }
    }

    DisposableEffect(duenoCiclo) {
        val observador = LifecycleEventObserver { _, evento ->
            when (evento) {
                Lifecycle.Event.ON_START -> vista.onStart()
                Lifecycle.Event.ON_RESUME -> vista.onResume()
                Lifecycle.Event.ON_PAUSE -> vista.onPause()
                Lifecycle.Event.ON_STOP -> vista.onStop()
                else -> Unit
            }
        }
        duenoCiclo.lifecycle.addObserver(observador)
        onDispose {
            duenoCiclo.lifecycle.removeObserver(observador)
            vista.onStop()
            vista.onDestroy()
        }
    }

    AndroidView(factory = { vista }, modifier = Modifier.fillMaxSize())
}

private fun configurar(mapa: MapLibreMap) {
    mapa.setMinZoomPreference(BaseMapa.ZOOM_MIN)
    mapa.setMaxZoomPreference(BaseMapa.ZOOM_MAX)
    mapa.setLatLngBoundsForCameraTarget(LIMITES)
    mapa.cameraPosition = CameraPosition.Builder()
        .target(LatLng(BaseMapa.LAT_CENTRO, BaseMapa.LON_CENTRO))
        .zoom(BaseMapa.ZOOM_INICIAL)
        .build()

    mapa.uiSettings.apply {
        // La atribucion se dibuja como parte de la interfaz, no con la de MapLibre.
        isAttributionEnabled = false
        isLogoEnabled = false
        // Girar e inclinar solo desorienta a quien no espera que el mapa rote.
        isRotateGesturesEnabled = false
        isTiltGesturesEnabled = false
    }
}

/**
 * Pin como circulo con borde blanco: se lee sobre cualquier fondo del mapa, y
 * no necesita que el servidor mande un PNG por categoria para poder dibujarse.
 * Cuando lleguen los PNG reales, esta capa se cambia por una de simbolos.
 */
private fun capaPines(): CircleLayer =
    CircleLayer(CAPA_PINES, FUENTE_PINES).withProperties(
        PropertyFactory.circleRadius(
            Expression.interpolate(
                Expression.linear(), Expression.zoom(),
                Expression.stop(11, 4f),
                Expression.stop(15, 8f),
                Expression.stop(18, 13f),
            ),
        ),
        PropertyFactory.circleColor(Expression.get("color")),
        PropertyFactory.circleStrokeWidth(2f),
        PropertyFactory.circleStrokeColor("#FFFFFF"),
    )
