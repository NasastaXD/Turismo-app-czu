package net.caaguazu.turismo.ui.inventario

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import net.caaguazu.turismo.datos.Categoria

/**
 * Navegacion interna del inventario: categorias -> lista -> ficha.
 *
 * El mapa no es una rama de esta pila: es una forma de ver la lista, y por eso
 * vive como interruptor dentro de ella.
 */
@Immutable
sealed interface RutaInv {
    data object Categorias : RutaInv
    data class Lista(val categoria: Categoria?) : RutaInv
    data class Ficha(val id: Int) : RutaInv
}

class PilaInventario {
    private val pila = mutableStateListOf<RutaInv>(RutaInv.Categorias)

    /** Se conserva entre idas y vueltas: volver de una ficha no reinicia el mapa. */
    var enMapa by mutableStateOf(false)

    val actual: RutaInv get() = pila.last()

    fun ir(ruta: RutaInv) { pila.add(ruta) }

    fun volver(): Boolean {
        if (pila.size <= 1) return false
        pila.removeAt(pila.lastIndex)
        return true
    }

    fun raiz() {
        while (pila.size > 1) pila.removeAt(pila.lastIndex)
    }
}

@Composable
fun Inventario(pila: PilaInventario, modifier: Modifier = Modifier) {
    when (val ruta = pila.actual) {
        is RutaInv.Categorias -> PantallaCategorias(
            alElegir = { pila.ir(RutaInv.Lista(it)) },
            alVerTodo = { pila.ir(RutaInv.Lista(null)) },
            modifier = modifier,
        )

        is RutaInv.Lista -> PantallaLista(
            categoria = ruta.categoria,
            enMapa = { pila.enMapa },
            alCambiarVista = { pila.enMapa = it },
            alAbrir = { pila.ir(RutaInv.Ficha(it)) },
            alVolver = { pila.volver() },
            modifier = modifier,
        )

        is RutaInv.Ficha -> PantallaFicha(
            id = ruta.id,
            alVolver = { pila.volver() },
            modifier = modifier,
        )
    }
}
