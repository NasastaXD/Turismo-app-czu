package net.caaguazu.turismo.ui.tema

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import net.caaguazu.turismo.interfaz.R

/**
 * Los mismos `R.font` de siempre. Los `.ttf` estan ahora en `res/font` de este
 * modulo y no de :app, asi que el `R` es el de la biblioteca — por eso el
 * import es `net.caaguazu.turismo.interfaz.R` y no el de la app.
 */
actual val Sans: FontFamily = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
)

actual val Serif: FontFamily = FontFamily(
    Font(R.font.serif_regular, FontWeight.Normal),
    Font(R.font.serif_semibold, FontWeight.SemiBold),
    Font(R.font.serif_bold, FontWeight.Bold),
)
