package net.caaguazu.turismo

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.caaguazu.turismo.core.Guardado
import net.caaguazu.turismo.core.Registro
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.datos.Datos

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // El registro primero: si algo falla mas abajo, queda constancia.
        Registro.iniciar(this)
        Textos.cargarEmbebido(this)
        Datos.iniciar(this)
        Guardado.iniciar(this)

        // Los textos del servidor pisan al respaldo cuando llegan. No se espera:
        // la app arranca con el embebido y se actualiza sola.
        CoroutineScope(SupervisorJob()).launch { Datos.refrescarTextos() }
    }
}
