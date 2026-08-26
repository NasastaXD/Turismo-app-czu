package net.caaguazu.turismo.ui.tema

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * El movimiento del sistema.
 *
 * El sistema visual es contenido, y el movimiento tiene que serlo tambien: la
 * animacion esta para explicar de donde sale algo y adonde va, no para lucirse.
 * Nada rebota, nada gira, nada llama la atencion sobre si mismo.
 *
 * Los tiempos son deliberadamente tranquilos. El publico de esta app es en buena
 * parte gente mayor, y una transicion rapida no se lee como agil sino como un
 * parpadeo del que uno no se entera.
 */
object Movimiento {

    /** Aparecer contenido que ya estaba pedido. */
    const val ENTRADA_MS = 260

    /** Cambio entre dos cosas del mismo nivel: lista y mapa, seccion y seccion. */
    const val CRUCE_MS = 220

    /** Respuesta al dedo. Tiene que sentirse inmediata. */
    const val TOQUE_MS = 110

    /** Desaceleracion estandar: entra rapido y se acomoda. */
    val Entrada: Easing = CubicBezierEasing(0.16f, 0.84f, 0.44f, 1f)

    /** Simetrica, para lo que cambia sin entrar ni salir. */
    val Suave: Easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)

    fun <T> entrada(duracion: Int = ENTRADA_MS) = tween<T>(duracion, easing = Entrada)
    fun <T> cruce(duracion: Int = CRUCE_MS) = tween<T>(duracion, easing = Suave)
    fun <T> toque() = tween<T>(TOQUE_MS, easing = Suave)
}

/**
 * Si el telefono tiene las animaciones apagadas, se respeta.
 *
 * Se apagan por accesibilidad, por ahorro de bateria o porque el telefono es
 * viejo y el usuario prefiere que todo sea instantaneo. En cualquiera de los
 * tres casos, animar igual seria ignorar una decision que ya tomo.
 */
val AnimacionesActivas: ProvidableCompositionLocal<Boolean> = compositionLocalOf { true }

@Composable
fun recordarAnimacionesActivas(): Boolean {
    val contexto = LocalContext.current
    return remember(contexto) { animacionesDelSistema(contexto) }
}

private fun animacionesDelSistema(contexto: Context): Boolean =
    runCatching {
        Settings.Global.getFloat(
            contexto.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) > 0f
    }.getOrDefault(true)
