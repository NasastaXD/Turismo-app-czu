package net.caaguazu.turismo.ui.piezas

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Iconografia propia: linea de 2px, esquinas redondeadas, una sola familia.
 *
 * Se dibujan aca y no se toman de Material a proposito: el sistema prohibe mezclar
 * glifos de sistema con los ilustrados, y mezclarlos se nota enseguida.
 */
private fun trazo(nombre: String, contenido: String): ImageVector =
    ImageVector.Builder(
        name = nombre,
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString(contenido).toNodes(),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
    }.build()

/** Variante rellena, para el corazon marcado y la flecha del breadcrumb. */
private fun relleno(nombre: String, contenido: String): ImageVector =
    ImageVector.Builder(
        name = nombre,
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString(contenido).toNodes(),
            fill = SolidColor(Color.Black),
        )
    }.build()

object Icono {

    /** Casa: techo a dos aguas y cuerpo. */
    val principal: ImageVector by lazy {
        trazo("principal", "M3 11 L12 3 L21 11 M5.5 9.5 V20 H18.5 V9.5 M9.8 20 V14 H14.2 V20")
    }

    /** Mapa plegado con un pin encima. */
    val inventario: ImageVector by lazy {
        trazo(
            "inventario",
            "M3 6.5 L9 4 L15 6.5 L21 4 V15 L15 17.5 L9 15 L3 17.5 Z " +
                "M9 4 V15 M15 6.5 V17.5 " +
                "M17.2 9.4 m-2.2 0 a2.2 2.2 0 1 0 4.4 0 a2.2 2.2 0 1 0 -4.4 0",
        )
    }

    /** Ruta: dos paradas unidas por un camino sinuoso. */
    val recorridos: ImageVector by lazy {
        trazo(
            "recorridos",
            "M6.5 4 m-2.3 0 a2.3 2.3 0 1 0 4.6 0 a2.3 2.3 0 1 0 -4.6 0 " +
                "M17.5 20 m-2.3 0 a2.3 2.3 0 1 0 4.6 0 a2.3 2.3 0 1 0 -4.6 0 " +
                "M6.5 6.5 C6.5 12 17.5 12 17.5 17.5",
        )
    }

    /** Articulo: hoja con una imagen y renglones. */
    val articulos: ImageVector by lazy {
        trazo(
            "articulos",
            "M3.5 4.5 H20.5 V19.5 H3.5 Z " +
                "M6.5 8 H12 V13 H6.5 Z " +
                "M14.5 8 H18 M14.5 11 H18 M14.5 14 H18 M6.5 16.5 H18",
        )
    }

    /** Perfil: cabeza y hombros. */
    val perfil: ImageVector by lazy {
        trazo(
            "perfil",
            "M12 8.2 m-3.6 0 a3.6 3.6 0 1 0 7.2 0 a3.6 3.6 0 1 0 -7.2 0 " +
                "M4.8 20 C4.8 16 8 14.2 12 14.2 C16 14.2 19.2 16 19.2 20",
        )
    }

    /** Lupa. */
    val buscar: ImageVector by lazy {
        trazo("buscar", "M11 11 m-6.5 0 a6.5 6.5 0 1 0 13 0 a6.5 6.5 0 1 0 -13 0 M15.8 15.8 L20.5 20.5")
    }

    /** Campana. */
    val avisos: ImageVector by lazy {
        trazo(
            "avisos",
            "M12 3.5 C8.7 3.5 6.8 5.9 6.8 9 C6.8 13.5 5 15 5 15 H19 C19 15 17.2 13.5 17.2 9 " +
                "C17.2 5.9 15.3 3.5 12 3.5 Z M10.2 18 C10.6 19.2 11.2 19.8 12 19.8 C12.8 19.8 13.4 19.2 13.8 18",
        )
    }

    /** Flecha de volver. */
    val volver: ImageVector by lazy {
        trazo("volver", "M14.5 5 L7.5 12 L14.5 19")
    }

    /** Compartir. */
    val compartir: ImageVector by lazy {
        trazo(
            "compartir",
            "M12 3.5 L12 15 M8 7.2 L12 3.4 L16 7.2 " +
                "M5.5 13 V19.5 H18.5 V13",
        )
    }

    /** Corazon de favorito, sin relleno. */
    val corazon: ImageVector by lazy {
        trazo(
            "corazon",
            "M12 20 C12 20 3.5 14.6 3.5 9.1 C3.5 6.3 5.7 4.2 8.4 4.2 " +
                "C10.1 4.2 11.3 5.1 12 6.2 C12.7 5.1 13.9 4.2 15.6 4.2 " +
                "C18.3 4.2 20.5 6.3 20.5 9.1 C20.5 14.6 12 20 12 20 Z",
        )
    }

    /** Corazon marcado. Mismo dibujo, relleno. */
    val corazonLleno: ImageVector by lazy {
        relleno(
            "corazonLleno",
            "M12 20 C12 20 3.5 14.6 3.5 9.1 C3.5 6.3 5.7 4.2 8.4 4.2 " +
                "C10.1 4.2 11.3 5.1 12 6.2 C12.7 5.1 13.9 4.2 15.6 4.2 " +
                "C18.3 4.2 20.5 6.3 20.5 9.1 C20.5 14.6 12 20 12 20 Z",
        )
    }

    /** Flecha del breadcrumb: solida, no chevron. */
    val flechaBreadcrumb: ImageVector by lazy {
        relleno("flechaBreadcrumb", "M4 10.4 H13 V6.4 L20 12 L13 17.6 V13.6 H4 Z")
    }

    /** Casa del breadcrumb. */
    val casa: ImageVector by lazy {
        trazo("casa", "M3.5 11 L12 4 L20.5 11 M6 9.7 V19.5 H18 V9.7")
    }

    /** Lista, para el interruptor lista/mapa. */
    val lista: ImageVector by lazy {
        trazo("lista", "M4 6.5 H6 M9 6.5 H20 M4 12 H6 M9 12 H20 M4 17.5 H6 M9 17.5 H20")
    }

    /** Calendario: hoja del mes con las dos anillas y la linea de cabecera. */
    val calendario: ImageVector by lazy {
        trazo(
            "calendario",
            "M4 6 H20 V20 H4 Z M4 10 H20 M8.5 3.5 V7 M15.5 3.5 V7",
        )
    }

    /** Chevron a la derecha: "esta fila lleva a otro lado". */
    val chevron: ImageVector by lazy {
        trazo("chevron", "M9.5 5 L16.5 12 L9.5 19")
    }
}
