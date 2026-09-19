package net.caaguazu.turismo.ui.tema

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Tokens del sistema visual.
 *
 * La forma del sistema es la tarjeta blanda: rectangulo muy redondeado que
 * flota sobre el fondo con una sombra ancha y clara. Todo lo que se toca
 * —botones, chips, filtros, campos— va a radio completo sobre un relleno
 * suave, sin contornos: el sistema separa por color de superficie, no por
 * lineas.
 *
 * La paleta es cerrada y tiene tres colores de marca con un rol cada uno:
 * verde es accion, coral es afecto y fecha, mango es lo que esta pasando ahora.
 * Ninguno se usa fuera de su rol, porque tres colores sin regla son ruido.
 */

/* --------------------------------------------------------------------------
 * Marca. No cambian entre modo claro y oscuro: son la identidad, y una
 * identidad que cambia de color segun la hora del dia deja de serlo.
 * ------------------------------------------------------------------------ */

private val EtonBlue = Color(0xFF96C8A2)
private val Mango = Color(0xFFFFC300)
private val Bittersweet = Color(0xFFFF6F61)

/**
 * Tinta sobre el verde.
 *
 * La referencia pone texto blanco sobre el verde claro, que da un contraste de
 * 1,9:1 — ilegible al sol y muy por debajo del minimo accesible. Con este
 * verde oscuro sobre el mismo fondo la pieza se ve igual y el contraste sube a
 * 7:1. Para un publico que en buena parte es gente mayor leyendo en la calle,
 * eso no es un detalle.
 */
private val VerdeOscuro = Color(0xFF1E3A28)

/** Tinta sobre foto. Es blanco en los dos modos: debajo siempre hay una foto. */
private val Blanco = Color(0xFFFFFFFF)

object Tono {

    /**
     * Modo oscuro. Lo fija el armazon de la app leyendo el ajuste del sistema.
     *
     * Es estado de Compose: leer cualquier color de aca dentro de una
     * composicion la suscribe, asi que cambiar el modo redibuja la app entera
     * sin que ninguna pantalla tenga que enterarse.
     */
    var oscuro by mutableStateOf(false)

    /* --- Marca --- */

    /** Accion principal: el boton que hace la cosa que la pantalla propone. */
    val primario = EtonBlue

    /** Lo que se escribe encima del verde. */
    val sobrePrimario = VerdeOscuro

    /** Favoritos, fechas y metadatos de cuando/donde. */
    val acento = Bittersweet

    /** Lo que esta ocurriendo ahora: un evento en curso, un badge. */
    val destacado = Mango

    /* --- Superficies --- */

    /** Fondo de pagina. */
    val fondo get() = if (oscuro) Color(0xFF0F0F10) else Color(0xFFF7F7F5)

    /** Tarjeta: la superficie que flota sobre el fondo. */
    val papel get() = if (oscuro) Color(0xFF1B1B1D) else Color(0xFFFFFFFF)

    /** Banda de seccion alterna. */
    val banda get() = if (oscuro) Color(0xFF151517) else Color(0xFFF2F1EF)

    /**
     * Relleno de control: buscador, chip en reposo, pildora de metadato,
     * contenedor de atajo.
     *
     * Es un token propio y no la banda porque hacen dos trabajos distintos: la
     * banda separa secciones de una pagina, esto dice "esto se toca". Sobre
     * papel tiene que verse; sobre el fondo tambien.
     */
    val campo get() = if (oscuro) Color(0xFF232326) else Color(0xFFF0EFED)

    /* --- Texto --- */

    val tinta get() = if (oscuro) Color(0xFFF2F2F2) else Color(0xFF333333)
    val tintaSuave get() = if (oscuro) Color(0xFF9A9AA0) else Color(0xFF6E6E73)

    /** Lo que se escribe sobre una foto, siempre con el velo debajo. */
    val sobreFoto = Blanco

    /* --- Estructura --- */

    val linea get() = if (oscuro) Color(0xFF2C2C2E) else Color(0xFFE8E6E3)

    /**
     * Control de maximo contraste: el item activo de la barra inferior, el chip
     * elegido, el segmento activo de un interruptor. Se invierte con el modo
     * —en claro es casi negro, en oscuro es casi blanco— porque su trabajo es
     * destacar sobre el fondo, y el fondo cambia.
     */
    val contraste get() = if (oscuro) Color(0xFFF2F2F2) else Color(0xFF1F1F21)

    /** Lo que se escribe sobre el contraste. */
    val sobreContraste get() = if (oscuro) Color(0xFF1F1F21) else Color(0xFFFFFFFF)

    /** Scrim sobre foto. Igual en los dos modos: la foto ya es oscura debajo. */
    val velo = Color(0x59000000)

    /**
     * Velo mas cargado, para la parte baja de una foto que lleva titulo grande
     * encima. Un titular de 26 sobre una foto clara necesita mas que el velo
     * parejo, y cargar el velo entero apagaria la foto.
     */
    val veloProfundo = Color(0x8C000000)

    /**
     * Color de la sombra. En claro es un gris que no ensucia; en oscuro las
     * sombras casi no se ven, asi que la separacion la hace el propio salto de
     * luminancia entre fondo y tarjeta.
     */
    val sombra get() = if (oscuro) Color(0xFF000000) else Color(0x142B2B2B)
}

/**
 * Ritmo de la pagina.
 *
 * El sistema es aireado: el margen es ancho y las tarjetas respiran. Apretarlo
 * fue lo primero que se noto mal en la version anterior.
 */
object Medida {
    val margen = 20.dp
    val entreTarjetas = 14.dp
    val dentroTarjeta = 16.dp
    val tituloACarrusel = 14.dp
    val entreSecciones = 30.dp

    /** Aire que deja el contenido al final para no morir contra la barra. */
    val colaDeLista = 28.dp

    /** El ancho de tarjeta del carrusel: dos enteras y el borde de la tercera. */
    const val FRACCION_TARJETA = 0.45f
}

/**
 * Radios del sistema.
 *
 * Contenido muy redondeado, control a radio completo. No hay una sexta opcion:
 * un radio escrito a mano es un token fuera del sistema que despues nadie
 * encuentra para cambiar.
 */
object Radio {
    /**
     * Sin redondeo. Es para el medio que va a sangre dentro de una tarjeta: el
     * contenedor ya recorta, y redondear de nuevo dejaria una esquina doble.
     */
    val ninguno = 0.dp

    /** Tarjeta de contenido, tile de menu, foto suelta sobre la pagina. */
    val tarjeta = 20.dp

    /** Media dentro de una tarjeta, y miniatura de fila. */
    val media = 16.dp
    val lista = 16.dp

    /** Hoja que sube desde abajo, y la cabecera de una ficha. */
    val hoja = 28.dp

    /** Chips, buscador, filtros, toggles, botones. */
    val completo = 999.dp
}

/**
 * Elevacion.
 *
 * Dos alturas y nada mas. La tarjeta apenas se despega del fondo; lo flotante
 * —el corazon sobre una foto, el boton de volver sobre un mapa— se despega de
 * verdad.
 */
object Elevacion {
    val tarjeta = 8.dp
    val flotante = 14.dp
}
