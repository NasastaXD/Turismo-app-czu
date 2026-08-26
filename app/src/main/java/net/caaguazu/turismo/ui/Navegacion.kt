package net.caaguazu.turismo.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import net.caaguazu.turismo.ui.articulos.PilaArticulos
import net.caaguazu.turismo.ui.inventario.PilaInventario
import net.caaguazu.turismo.ui.recorridos.PilaRecorridos
import net.caaguazu.turismo.ui.piezas.Icono

/**
 * Las cuatro secciones de la barra inferior.
 *
 * Navegacion propia y no una libreria: con cuatro secciones y pilas cortas, una
 * libreria de navegacion seria mas codigo del que ahorra.
 */
@Immutable
enum class Seccion(val clave: String, val icono: ImageVector) {
    PRINCIPAL("nav.principal", Icono.principal),
    INVENTARIO("nav.inventario", Icono.inventario),
    ARTICULOS("nav.articulos", Icono.articulos),
    RECORRIDOS("nav.recorridos", Icono.recorridos),
}

/** Estado de navegacion. Vive por encima de las pantallas y sobrevive a las recomposiciones. */
class Navegador {

    var seccion by mutableStateOf(Seccion.PRINCIPAL)
        private set

    var perfilAbierto by mutableStateOf(false)
        private set

    var diagnosticoAbierto by mutableStateOf(false)
        private set

    /** Cada seccion conserva donde estaba: cambiar de pestaña no reinicia nada. */
    val inventario = PilaInventario()
    val articulos = PilaArticulos()
    val recorridos = PilaRecorridos()

    fun ir(destino: Seccion) {
        if (seccion == destino) {
            // Tocar la pestaña activa vuelve a su raiz, que es lo que espera
            // quien se metio tres niveles y quiere salir.
            when (destino) {
                Seccion.INVENTARIO -> inventario.raiz()
                Seccion.ARTICULOS -> articulos.raiz()
                Seccion.RECORRIDOS -> recorridos.raiz()
                Seccion.PRINCIPAL -> Unit
            }
            return
        }
        seccion = destino
    }

    fun abrirPerfil() { perfilAbierto = true }
    fun abrirDiagnostico() { diagnosticoAbierto = true }

    /**
     * Identifica que pantalla se ve, para cruzar entre ellas.
     *
     * Se compone de la seccion y de donde este parada dentro de ella: pasar de
     * una lista a una ficha tambien es un cambio de pantalla, no solo cambiar
     * de pestaña.
     */
    fun caraActual(): String = when {
        diagnosticoAbierto -> "diagnostico"
        perfilAbierto -> "perfil"
        seccion == Seccion.INVENTARIO -> "inv:" + inventario.actual::class.simpleName
        seccion == Seccion.ARTICULOS -> "art:" + (articulos.abierto?.let { "detalle" } ?: "lista")
        seccion == Seccion.RECORRIDOS -> "rec:" + (recorridos.abierto?.let { "detalle" } ?: "lista")
        else -> "principal"
    }

    /** Devuelve true si consumio el gesto de volver. */
    fun volver(): Boolean = when {
        diagnosticoAbierto -> { diagnosticoAbierto = false; true }
        perfilAbierto -> { perfilAbierto = false; true }
        seccion == Seccion.INVENTARIO && inventario.volver() -> true
        seccion == Seccion.ARTICULOS && articulos.volver() -> true
        seccion == Seccion.RECORRIDOS && recorridos.volver() -> true
        seccion != Seccion.PRINCIPAL -> { seccion = Seccion.PRINCIPAL; true }
        else -> false
    }
}

/** La IA llega al final del desarrollo; el boton central se enciende con este interruptor. */
object Funciones {
    const val IA_ACTIVA = false
}
