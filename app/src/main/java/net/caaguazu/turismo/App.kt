package net.caaguazu.turismo

import android.app.Application
import android.content.res.Configuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.caaguazu.turismo.core.Ajustes
import net.caaguazu.turismo.core.Avisos
import net.caaguazu.turismo.core.Guardado
import net.caaguazu.turismo.core.Registro
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.core.Vigilante
import net.caaguazu.turismo.datos.Datos
import net.caaguazu.turismo.ui.tema.Tono

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // El registro primero: si algo falla mas abajo, queda constancia.
        Registro.iniciar(this)
        Ajustes.iniciar(this)
        Textos.cargarEmbebido(this)
        Datos.iniciar(this)
        Guardado.iniciar(this)

        // El modo se fija antes de la primera composicion. Si se dejara solo al
        // SideEffect de la pantalla, el primer cuadro se dibujaria en claro y
        // recien el segundo en oscuro: un parpadeo blanco al abrir de noche.
        Tono.oscuro = enModoOscuro()

        // Los canales van siempre, aunque los avisos esten apagados: son lo que
        // hace que la app aparezca en los ajustes de notificaciones del telefono
        // antes de que alguien la busque ahi.
        Avisos.crearCanales(this)
        if (Ajustes.avisosActivos) Vigilante.programar(this)

        // Los textos del servidor pisan al respaldo cuando llegan. No se espera:
        // la app arranca con el embebido y se actualiza sola.
        CoroutineScope(SupervisorJob()).launch { Datos.refrescarTextos() }
    }

    // Los parentesis son explicitos a proposito: `and` liga mas fuerte que `==`
    // en Kotlin, asi que esto ya seria correcto sin ellos, pero leerlo al reves
    // es un error clasico y no vale la pena dejarlo a la memoria de nadie.
    private fun enModoOscuro(): Boolean =
        (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
}
