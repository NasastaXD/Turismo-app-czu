package net.caaguazu.turismo.ui.perfil

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.caaguazu.turismo.BuildConfig
import net.caaguazu.turismo.core.Ajustes
import net.caaguazu.turismo.core.Registro
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.datos.Datos
import net.caaguazu.turismo.ui.piezas.Hairline
import net.caaguazu.turismo.ui.piezas.PildoraSuave
import net.caaguazu.turismo.ui.piezas.Texto
import net.caaguazu.turismo.ui.tema.Letra
import net.caaguazu.turismo.ui.tema.Medida
import net.caaguazu.turismo.ui.tema.Tono

/**
 * Pantalla de diagnostico.
 *
 * Existe para una sola cosa: que cuando algo falle en el telefono de alguien,
 * esa persona pueda mandarme el registro y yo pueda ver que paso, en vez de
 * pedirle que me lo describa.
 *
 * En release el registro sale ofuscado por R8. Para leerlo hace falta el
 * mapping.txt de esa misma compilacion, que se adjunta a cada publicacion.
 */
@Composable
fun PantallaDiagnostico(modifier: Modifier = Modifier) {
    val contexto = LocalContext.current
    var recarga by remember { mutableIntStateOf(0) }
    val lineas = remember(recarga) {
        Registro.leerTodo().lines().filter { it.isNotBlank() }.asReversed()
    }

    Column(modifier.fillMaxSize().background(Tono.fondo)) {

        Column(Modifier.padding(Medida.margen)) {
            Dato(Textos.t("diag.origen"), Datos.origen)
            Dato(Textos.t("diag.version"), "${BuildConfig.VERSION_NAME} (${BuildConfig.BUILD_TYPE})")
            Dato(Textos.t("diag.cache"), "${Datos.cache.tamano() / 1024} KB")
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = Medida.margen),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Cambiar de fuente y volver a arrancar la pantalla. El nombre del
            // boton es el origen al que se va: es un dato, no texto de producto.
            PildoraSuave(
                texto = Ajustes.nombre(Ajustes.contraria()),
                alTocar = {
                    Ajustes.origen = Ajustes.contraria()
                    Datos.iniciar(contexto.applicationContext)
                    Datos.cache.vaciar()
                    (contexto as? Activity)?.recreate()
                },
            )
            PildoraSuave(Textos.t("diag.compartir"), alTocar = { compartir(contexto) })
            PildoraSuave(
                texto = Textos.t("diag.vaciar"),
                alTocar = {
                    Datos.cache.vaciar()
                    recarga++
                },
            )
            PildoraSuave(
                texto = Textos.t("diag.borrar"),
                alTocar = {
                    Registro.borrar()
                    recarga++
                },
            )
        }

        Hairline(Modifier.fillMaxWidth().padding(Medida.margen))

        if (lineas.isEmpty()) {
            Texto(
                texto = Textos.t("diag.vacio"),
                estilo = Letra.descripcion,
                color = Tono.tintaSuave,
                modifier = Modifier.padding(Medida.margen),
            )
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = Medida.margen, vertical = 4.dp)) {
                items(lineas) { linea ->
                    Texto(
                        texto = linea,
                        estilo = Letra.descripcion.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                        color = if (linea.contains(" E/")) Tono.acento else Tono.tintaSuave,
                        modifier = Modifier.padding(vertical = 2.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Texto(etiqueta, Letra.chip, Tono.tintaSuave, maxLineas = 1)
        Texto(valor, Letra.chip, Tono.tinta, maxLineas = 1)
    }
}

private fun compartir(contexto: android.content.Context) {
    val intencion = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, Registro.leerTodo().takeLast(120_000))
    }
    contexto.startActivity(Intent.createChooser(intencion, null))
}
