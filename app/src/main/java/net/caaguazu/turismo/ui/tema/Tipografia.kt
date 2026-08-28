package net.caaguazu.turismo.ui.tema

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import net.caaguazu.turismo.R

/**
 * Una sola familia en toda la app: sans geometrica de apertura ancha.
 *
 * La unica excepcion es el articulo. Ahi la referencia es un diario, y el
 * titular serif es parte de lo que hace que un articulo se lea como un
 * articulo y no como una pantalla mas. La excepcion vale porque leer un texto
 * largo es un contexto distinto de operar una interfaz.
 */
val Sans = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
)

/** Serif de lectura. Solo para articulos: titular, bajada y cuerpo. */
val Serif = FontFamily(
    Font(R.font.serif_regular, FontWeight.Normal),
    Font(R.font.serif_semibold, FontWeight.SemiBold),
    Font(R.font.serif_bold, FontWeight.Bold),
)

object Letra {

    /**
     * Titulo de pantalla: lo primero que se lee al entrar, alineado a la
     * izquierda y sin barra que lo encierre. Reemplaza a la cabecera con el
     * nombre de la app repetido en las cinco pantallas.
     */
    val tituloPantalla = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Bold,
        fontSize = 26.sp, lineHeight = 31.sp, letterSpacing = (-0.5).sp,
    )

    /** Titulo de una ficha abierta. */
    val tituloPagina = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Bold,
        fontSize = 26.sp, lineHeight = 31.sp, letterSpacing = (-0.4).sp,
    )

    /** Encabezado de seccion dentro de una pantalla. */
    val tituloSeccion = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Bold,
        fontSize = 19.sp, lineHeight = 24.sp, letterSpacing = (-0.2).sp,
    )

    val tituloTarjeta = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 21.sp, letterSpacing = (-0.1).sp,
    )

    /** Metadato: cuando, donde, cuanto dura. */
    val fecha = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 19.sp,
    )

    /**
     * Texto que se lee de corrido. Es el unico tamano que no se toca: el
     * publico es en buena parte gente mayor leyendo en la calle, y achicar la
     * descripcion para que la pantalla se vea mas compacta se paga en quien no
     * puede leerla.
     */
    val descripcion = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Normal,
        fontSize = 15.sp, lineHeight = 22.sp,
    )

    /** Enlace de texto: "ver todo" de una seccion, "leer mas" de una ficha. */
    val enlace = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp, lineHeight = 18.sp,
    )

    /** Lo que se escribe dentro de un control: chip, pildora, campo. */
    val chip = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 14.sp,
    )

    /** Etiqueta chica: barra inferior, credito de foto, badge. */
    val etiquetaNav = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 12.sp,
    )

    /** Titulo sobre una foto, siempre con el velo debajo. */
    val sobreFoto = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Bold,
        fontSize = 16.sp, lineHeight = 20.sp,
    )

    /** El precio de la barra de accion: el dato que se busca de un vistazo. */
    val precio = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Bold,
        fontSize = 20.sp, lineHeight = 24.sp,
    )

    /* --- Articulo: el unico lugar con serif --- */

    val titularArticulo = TextStyle(
        fontFamily = Serif, fontWeight = FontWeight.Bold,
        fontSize = 32.sp, lineHeight = 37.sp, letterSpacing = (-0.4).sp,
    )
    val bajadaArticulo = TextStyle(
        fontFamily = Serif, fontWeight = FontWeight.Normal,
        fontSize = 19.sp, lineHeight = 26.sp,
    )
    val cuerpoArticulo = TextStyle(
        fontFamily = Serif, fontWeight = FontWeight.Normal,
        fontSize = 18.sp, lineHeight = 29.sp,
    )
    val titularTarjeta = TextStyle(
        fontFamily = Serif, fontWeight = FontWeight.Bold,
        fontSize = 21.sp, lineHeight = 26.sp,
    )
}
