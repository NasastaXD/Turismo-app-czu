package net.caaguazu.turismo.ui.perfil

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import net.caaguazu.turismo.core.Ajustes
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.core.TrabajoDeAvisos
import net.caaguazu.turismo.ui.piezas.Interruptor
import net.caaguazu.turismo.ui.piezas.Texto
import net.caaguazu.turismo.ui.tema.Letra
import net.caaguazu.turismo.ui.tema.Medida
import net.caaguazu.turismo.ui.tema.Tono

actual val avisosDisponibles: Boolean = true

/**
 * El interruptor de avisos.
 *
 * Encenderlo pide el permiso del sistema en Android 13 o mas nuevo. Si la
 * persona lo niega, el interruptor vuelve solo a apagado en vez de quedar
 * encendido sin avisar nunca — que es la forma mas segura de que alguien crea
 * que la app esta rota.
 *
 * Programar y cancelar el trabajo periodico no se hace aca directamente: pasa
 * por `TrabajoDeAvisos`, un enganche que :app rellena con `Vigilante`. Es lo
 * que permite que el modulo de interfaz no dependa de WorkManager ni del
 * modulo de la app.
 */
@Composable
actual fun FilaDeAvisos() {
    val contexto = LocalContext.current
    var encendido by remember { mutableStateOf(Ajustes.avisosActivos) }

    fun encender() {
        Ajustes.avisosActivos = true
        encendido = true
        TrabajoDeAvisos.programar?.invoke()
    }

    fun apagar() {
        Ajustes.avisosActivos = false
        encendido = false
        // Se olvida lo anotado: volver a encender empieza limpio y no dispara
        // un aviso por cada cosa publicada mientras estuvo apagado.
        Ajustes.olvidarAvisados()
        TrabajoDeAvisos.cancelar?.invoke()
    }

    val pedirPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { concedido -> if (concedido) encender() else apagar() }

    Row(
        modifier = Modifier.fillMaxWidth().padding(Medida.dentroTarjeta),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f)) {
            Texto(Textos.t("perfil.avisos"), Letra.tituloTarjeta, Tono.tinta, maxLineas = 1)
            Texto(Textos.t("perfil.avisosDetalle"), Letra.chip, Tono.tintaSuave, maxLineas = 2)
        }
        Interruptor(
            encendido = encendido,
            descripcion = Textos.t("perfil.avisos"),
            alCambiar = { quiere ->
                if (!quiere) {
                    apagar()
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    contexto.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED
                ) {
                    pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    encender()
                }
            },
        )
    }
}
