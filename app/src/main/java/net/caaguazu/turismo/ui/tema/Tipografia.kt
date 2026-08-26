package net.caaguazu.turismo.ui.tema

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import net.caaguazu.turismo.R

/**
 * Una sola familia en toda la app: sans geometrica de apertura ancha.
 * La unica excepcion prevista es el titular de articulo, que lleva serif.
 */
val Sans = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
)

object Letra {
    val tituloSeccion = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Bold,
        fontSize = 22.sp, lineHeight = 26.sp, letterSpacing = (-0.2).sp,
    )
    val tituloPagina = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Bold,
        fontSize = 24.sp, lineHeight = 28.sp,
    )
    val tituloTarjeta = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Bold,
        fontSize = 17.sp, lineHeight = 22.sp,
    )
    val fecha = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Normal,
        fontSize = 15.sp, lineHeight = 20.sp,
    )
    val descripcion = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Normal,
        fontSize = 15.sp, lineHeight = 21.sp,
    )
    val chip = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 15.sp,
    )
    val etiquetaNav = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 12.sp,
    )
    val sobreFoto = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Bold,
        fontSize = 17.sp, lineHeight = 21.sp,
    )
}
