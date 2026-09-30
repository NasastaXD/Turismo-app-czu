package net.caaguazu.turismo.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import net.caaguazu.turismo.core.Idioma
import net.caaguazu.turismo.ui.articulos.PilaArticulos
import net.caaguazu.turismo.ui.piezas.Icono
import net.caaguazu.turismo.ui.recorridos.PilaRecorridos

/**
 * Las cuatro secciones de la barra inferior.
 *
 * La segunda es BUSCAR y no un indice de categorias. Es el cambio de fondo de
 * esta version: el inventario dejo de ser un arbol por el que se baja
 * —categorias, lista, ficha— y paso a ser una busqueda con filtros, que es como
 * la gente llega a un lugar cuando ya sabe mas o menos que quiere. Las
 * categorias siguen estando: son lo que se ve mientras no se busco nada.
 *
 * Navegacion propia y no una libreria: con cuatro secciones y pilas cortas, una
 * libreria de navegacion seria mas codigo del que ahorra.
 */
@Immutable
enum class Seccion(val clave: String, val icono: ImageVector) {
    INICIO("nav.principal", Icono.principal),
    BUSCAR("barra.buscar", Icono.buscar),
    ARTICULOS("nav.articulos", Icono.articulos),
    RECORRIDOS("nav.recorridos", Icono.recorridos),
}

/** Lo que se esta mirando dentro de la seccion de busqueda. */
@Immutable
sealed interface RutaBusqueda {
    data object Explorar : RutaBusqueda
    data class Ficha(val id: Int) : RutaBusqueda
}

/**
 * Los filtros de la busqueda.
 *
 * Viven en la pila y no en la pantalla: entrar a una ficha y volver no puede
 * deshacer lo que alguien acaba de elegir. Inmutable para que Compose no
 * recomponga la lista de resultados mientras se toca un filtro que todavia no
 * se aplico.
 */
@Immutable
data class Filtros(
    val categoria: Int? = null,
    val zona: Int? = null,
    val etiqueta: Int? = null,
    /** 0 a 4. Es el techo, no el valor exacto: "hasta este precio". */
    val precioMaximo: Int? = null,
) {
    val cantidad: Int
        get() = listOfNotNull(categoria, zona, etiqueta, precioMaximo).size

    val vacios: Boolean get() = cantidad == 0
}

/**
 * Estado de la seccion de busqueda.
 *
 * La consulta, los filtros y el modo mapa son de la seccion y no de la
 * pantalla, por la misma razon: son el contexto de lo que la persona esta
 * buscando, y ese contexto no se pierde por abrir un lugar.
 */
class PilaBusqueda {
    private val pila = mutableStateListOf<RutaBusqueda>(RutaBusqueda.Explorar)

    val actual: RutaBusqueda get() = pila.last()

    var consulta by mutableStateOf("")

    var filtros by mutableStateOf(Filtros())

    var enMapa by mutableStateOf(false)

    /** La hoja de filtros esta abierta. Es estado, no un destino de navegacion. */
    var filtrosAbiertos by mutableStateOf(false)
        private set

    /** El pin tocado en el mapa, que se muestra en la tarjeta de abajo. */
    var seleccionEnMapa by mutableStateOf<Int?>(null)

    /** Hay algo pedido: o se escribio, o se filtro. Decide que se dibuja. */
    val buscando: Boolean get() = consulta.isNotBlank() || !filtros.vacios

    fun ir(ruta: RutaBusqueda) { pila.add(ruta) }

    fun abrirFiltros() { filtrosAbiertos = true }

    fun cerrarFiltros() { filtrosAbiertos = false }

    fun limpiar() { filtros = Filtros() }

    fun volver(): Boolean {
        if (filtrosAbiertos) {
            filtrosAbiertos = false
            return true
        }
        if (seleccionEnMapa != null) {
            seleccionEnMapa = null
            return true
        }
        if (pila.size > 1) {
            pila.removeAt(pila.lastIndex)
            return true
        }
        // Salir del mapa y despues deshacer lo buscado, antes de dejar la
        // seccion. Filtrar por una categoria no empuja una pantalla nueva —es
        // la misma que cambia de cara—, asi que sin esto el gesto de volver se
        // saltaba la busqueda entera y tiraba al inicio: se perdia el contexto
        // de un toque, que es justo lo que la pantalla unica venia a evitar.
        if (enMapa) {
            enMapa = false
            return true
        }
        if (buscando) {
            consulta = ""
            filtros = Filtros()
            return true
        }
        return false
    }

    fun raiz() {
        filtrosAbiertos = false
        seleccionEnMapa = null
        while (pila.size > 1) pila.removeAt(pila.lastIndex)
    }
}

/** Estado de navegacion. Vive por encima de las pantallas y sobrevive a las recomposiciones. */
class Navegador {

    var seccion by mutableStateOf(Seccion.INICIO)
        private set

    var perfilAbierto by mutableStateOf(false)
        private set

    var diagnosticoAbierto by mutableStateOf(false)
        private set

    /** Cada seccion conserva donde estaba: cambiar de pestaña no reinicia nada. */
    val busqueda = PilaBusqueda()
    val articulos = PilaArticulos()
    val recorridos = PilaRecorridos()

    fun ir(destino: Seccion) {
        if (seccion == destino) {
            // Tocar la pestaña activa vuelve a su raiz, que es lo que espera
            // quien se metio tres niveles y quiere salir.
            when (destino) {
                Seccion.BUSCAR -> busqueda.raiz()
                Seccion.ARTICULOS -> articulos.raiz()
                Seccion.RECORRIDOS -> recorridos.raiz()
                Seccion.INICIO -> Unit
            }
            return
        }
        seccion = destino
    }

    /** Abrir un lugar desde cualquier parte lleva a la seccion donde vive la ficha. */
    fun abrirFicha(id: Int) {
        ir(Seccion.BUSCAR)
        busqueda.ir(RutaBusqueda.Ficha(id))
    }

    /** Entrar a la busqueda con una categoria ya elegida, desde el inicio. */
    fun buscarPorCategoria(id: Int) {
        ir(Seccion.BUSCAR)
        busqueda.raiz()
        busqueda.filtros = Filtros(categoria = id)
    }

    fun abrirPerfil() { perfilAbierto = true }
    fun abrirDiagnostico() { diagnosticoAbierto = true }

    /**
     * Identifica que pantalla se ve, para cruzar entre ellas.
     *
     * Se compone de la seccion y de donde este parada dentro de ella: pasar de
     * una busqueda a una ficha tambien es un cambio de pantalla, no solo cambiar
     * de pestaña.
     */
    fun caraActual(): String = Idioma.actual + ":" + cara()

    /**
     * El idioma va en la cara y no es cosmetico: es lo que hace que al
     * cambiarlo se rehagan los pedidos. Cada cara es un subarbol propio, asi
     * que uno nuevo arranca sus cargas de cero en vez de seguir mostrando lo
     * que se bajo en el idioma anterior.
     */
    private fun cara(): String = when {
        diagnosticoAbierto -> "diagnostico"
        perfilAbierto -> "perfil"
        seccion == Seccion.BUSCAR -> "bus:" + busqueda.actual::class.simpleName
        seccion == Seccion.ARTICULOS -> "art:" + (articulos.abierto?.let { "detalle" } ?: "lista")
        seccion == Seccion.RECORRIDOS -> "rec:" + (recorridos.abierto?.let { "detalle" } ?: "lista")
        else -> "inicio"
    }

    /** Devuelve true si consumio el gesto de volver. */
    fun volver(): Boolean = when {
        diagnosticoAbierto -> { diagnosticoAbierto = false; true }
        perfilAbierto -> { perfilAbierto = false; true }
        seccion == Seccion.BUSCAR && busqueda.volver() -> true
        seccion == Seccion.ARTICULOS && articulos.volver() -> true
        seccion == Seccion.RECORRIDOS && recorridos.volver() -> true
        seccion != Seccion.INICIO -> { seccion = Seccion.INICIO; true }
        else -> false
    }
}

/** La IA llega al final del desarrollo; el boton central se enciende con este interruptor. */
object Funciones {
    const val IA_ACTIVA = false
}
