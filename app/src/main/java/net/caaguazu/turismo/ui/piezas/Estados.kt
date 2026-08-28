package net.caaguazu.turismo.ui.piezas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.caaguazu.turismo.core.Falla
import net.caaguazu.turismo.core.Resultado
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.ui.tema.Letra
import net.caaguazu.turismo.ui.tema.Tono

/**
 * Los cuatro estados reales de cualquier pantalla que traiga datos.
 *
 * Estan los cuatro y no dos porque "sin datos" y "fallo al traerlos" se
 * arreglan de formas distintas, y una pantalla que los confunde deja al usuario
 * sin saber si tiene que reintentar o si sencillamente no hay nada.
 */
sealed interface Estado<out T> {
    data object Cargando : Estado<Nothing>
    data class Listo<T>(val valor: T) : Estado<T>
    data class Error(val falla: Falla) : Estado<Nothing>
}

/**
 * Trae algo y lo expone como estado, volviendo a intentarlo cuando cambia la
 * clave o cuando se pide reintentar.
 */
@Composable
fun <T> cargar(vararg claves: Any?, traer: suspend () -> Resultado<T>): Pair<State<Estado<T>>, () -> Unit> {
    var intento by remember { mutableIntStateOf(0) }
    val estado = produceState<Estado<T>>(Estado.Cargando, intento, *claves) {
        value = Estado.Cargando
        value = when (val r = traer()) {
            is Resultado.Bien -> Estado.Listo(r.valor)
            is Resultado.Mal -> Estado.Error(r.falla)
        }
    }
    return estado to { intento++ }
}

/**
 * Dibuja el estado correcto. La pantalla solo escribe el caso con datos.
 *
 * `vacio` decide si lo que llego cuenta como nada: una lista de cero elementos
 * no es un error, y no debe ofrecer reintentar.
 */
@Composable
fun <T> Cargador(
    estado: Estado<T>,
    reintentar: () -> Unit,
    modifier: Modifier = Modifier,
    vacio: (T) -> Boolean = { false },
    contenido: @Composable (T) -> Unit,
) {
    // Se cruza por la CLASE de estado y no por el estado entero: si se cruzara
    // por el valor, cada dato que llegara volveria a fundir la pantalla.
    val cara = when {
        estado is Estado.Cargando -> Cara.CARGANDO
        estado is Estado.Error -> Cara.ERROR
        estado is Estado.Listo && vacio(estado.valor) -> Cara.VACIO
        else -> Cara.CONTENIDO
    }

    Cruce(cara, modifier) { actual ->
        when (actual) {
            Cara.CARGANDO -> Aviso("estado.cargando")
            Cara.ERROR -> AvisoConReintento(
                (estado as? Estado.Error)?.falla ?: Falla.DESCONOCIDA,
                reintentar,
            )
            Cara.VACIO -> Aviso("estado.vacio")
            Cara.CONTENIDO -> (estado as? Estado.Listo)?.let { contenido(it.valor) }
        }
    }
}

/** Las cuatro caras que puede mostrar una pantalla que trae datos. */
private enum class Cara { CARGANDO, ERROR, VACIO, CONTENIDO }

@Composable
private fun Aviso(clave: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Texto(
            texto = Textos.t(clave),
            estilo = Letra.descripcion,
            color = Tono.tintaSuave,
            alinear = TextAlign.Center,
        )
    }
}

@Composable
private fun AvisoConReintento(falla: Falla, reintentar: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Texto(
                texto = Textos.t(if (falla == Falla.SIN_RED) "estado.guardado" else "estado.error"),
                estilo = Letra.tituloTarjeta,
                color = Tono.tinta,
                alinear = TextAlign.Center,
            )
            PildoraPrimaria(texto = Textos.t("estado.reintentar"), alTocar = reintentar)
        }
    }
}
