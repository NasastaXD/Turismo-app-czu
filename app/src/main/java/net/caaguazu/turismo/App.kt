package net.caaguazu.turismo

import android.app.Application
import net.caaguazu.turismo.core.Registro
import net.caaguazu.turismo.core.Textos

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // El registro primero: si algo falla mas abajo, queda constancia.
        Registro.iniciar(this)
        Textos.cargarEmbebido(this)
    }
}
