package net.caaguazu.turismo.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import net.caaguazu.turismo.ui.inventario.PilaInventario
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

    fun ir(destino: Seccion) {
        if (seccion == destino) {
            // Tocar la pestaña activa vuelve a su raiz, que es lo que espera
            // quien se metio tres niveles y quiere salir.
            if (destino == Seccion.INVENTARIO) inventario.raiz()
            return
        }
        seccion = destino
    }

    fun abrirPerfil() { perfilAbierto = true }
    fun abrirDiagnostico() { diagnosticoAbierto = true }

    /** Devuelve true si consumio el gesto de volver. */
    fun volver(): Boolean = when {
        diagnosticoAbierto -> { diagnosticoAbierto = false; true }
        perfilAbierto -> { perfilAbierto = false; true }
        seccion == Seccion.INVENTARIO && inventario.volver() -> true
        seccion != Seccion.PRINCIPAL -> { seccion = Seccion.PRINCIPAL; true }
        else -> false
    }
}

/** La IA llega al final del desarrollo; el boton central se enciende con este interruptor. */
object Funciones {
    const val IA_ACTIVA = false
}
