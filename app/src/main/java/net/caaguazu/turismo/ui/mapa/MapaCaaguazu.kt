package net.caaguazu.turismo.ui.mapa

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import net.caaguazu.turismo.core.Registro
import net.caaguazu.turismo.core.Resultado
import net.caaguazu.turismo.ui.tema.Tono
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

private const val ETIQUETA = "Mapa"

/** Limites del recorte que viaja en el APK: fuera de aqui no hay datos que mostrar. */
private val LIMITES = LatLngBounds.from(-25.3342, -55.8696, -25.6118, -56.1751)

/**
 * El mapa de Caaguazu, dibujado desde el archivo local.
 *
 * Sin red, sin clave de API y sin cuenta: el archivo de tiles viaja dentro del APK.
 * Si no puede cargarse, la pantalla no queda en blanco — se dibuja el aviso.
 */
@Composable
fun MapaCaaguazu(modifier: Modifier = Modifier) {
    val contexto = LocalContext.current

    // Copiar el archivo y leer el estilo es trabajo de disco: se hace una sola vez.
    val estilo = remember {
        val archivo = BaseMapa.asegurarArchivo(contexto)
        if (archivo is Resultado.Bien) {
            (BaseMapa.estilo(contexto, archivo.valor) as? Resultado.Bien)?.valor
        } else {
            null
        }
    }

    Box(modifier.fillMaxSize().background(Tono.banda)) {
        if (estilo == null) {
            MapaNoDisponible(Modifier.align(Alignment.Center))
            return@Box
        }
        LienzoMapa(estilo)
        AtribucionMapa(Modifier.align(Alignment.BottomStart).padding(8.dp))
    }
}

/**
 * El MapView es una vista clasica con ciclo de vida propio, que hay que alimentar a
 * mano. Se crea una sola vez y se conserva mientras la pantalla exista.
 */
@Composable
private fun LienzoMapa(estilo: String) {
    val contexto = LocalContext.current
    val duenoCiclo = LocalLifecycleOwner.current

    val vista = remember {
        MapLibre.getInstance(contexto)
        MapView(contexto).apply {
            onCreate(null)
            getMapAsync { mapa ->
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

                mapa.setStyle(Style.Builder().fromJson(estilo)) { cargado ->
                    Registro.info(ETIQUETA, "estilo cargado con ${cargado.layers.size} capas")
                }
            }
        }
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
