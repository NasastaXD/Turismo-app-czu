package net.caaguazu.turismo.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import net.caaguazu.turismo.core.Ajustes
import net.caaguazu.turismo.core.TrabajoDeAvisos

@Composable
actual fun ManejarVolver(alVolver: () -> Boolean) {
    val actividad = LocalActivity.current
    BackHandler(enabled = true) {
        if (!alVolver()) actividad?.finish()
    }
}

/**
 * Se pide al primer arranque nomas —`avisosDecididos()` evita repetirlo
 * despues— y si se niega, el interruptor cae solo a apagado en vez de quedar
 * prendido sin avisar nunca.
 */
@Composable
actual fun PedirAvisosAlArrancar() {
    val contexto = LocalContext.current

    val pedirPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { concedido ->
        Ajustes.avisosActivos = concedido
        if (concedido) TrabajoDeAvisos.programar?.invoke() else TrabajoDeAvisos.cancelar?.invoke()
    }

    LaunchedEffect(Unit) {
        if (Ajustes.avisosDecididos()) return@LaunchedEffect
        val faltaPermiso = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            contexto.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        if (faltaPermiso) {
            pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            Ajustes.avisosActivos = true
        }
    }
}
