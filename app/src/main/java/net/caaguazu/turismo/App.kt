package net.caaguazu.turismo

import android.app.Application
import android.content.res.Configuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.caaguazu.turismo.core.Ajustes
import net.caaguazu.turismo.core.ArranqueAndroid
import net.caaguazu.turismo.core.Avisos
import net.caaguazu.turismo.core.Bitacora
import net.caaguazu.turismo.core.Guardado
import net.caaguazu.turismo.core.Idioma
import net.caaguazu.turismo.core.Registro
import net.caaguazu.turismo.core.RegistroVisible
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.core.TrabajoDeAvisos
import net.caaguazu.turismo.core.Vigilante
import net.caaguazu.turismo.datos.Datos
import net.caaguazu.turismo.ui.tema.Tono

/**
 * El arranque de la app Android.
 *
 * Casi todo lo que se arranca aca vive en :interfaz y es lo mismo en iOS; lo
 * que este archivo hace es darle a ese modulo las tres o cuatro cosas que solo
 * Android sabe —el `Context`, la URL base del `BuildConfig`, el registro a
 * Logcat, WorkManager— y despues salirse del camino.
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // El registro primero: si algo falla mas abajo, queda constancia.
        Registro.iniciar(this)

        // El codigo compartido no puede llamar a `Registro` —escribe a Logcat y
        // a un archivo, y nada de eso existe del otro lado—, asi que anota en
        // `Bitacora` y aca se la engancha al registro de siempre. Sin esta
        // linea la app funciona igual pero pierde lo que anotan `Cache`, `Http`
        // y ahora tambien todas las pantallas, que es justo lo que hace falta
        // para diagnosticar.
        Bitacora.destino = { nivel, etiqueta, mensaje, causa ->
            when (nivel) {
                Bitacora.Nivel.DETALLE -> Registro.detalle(etiqueta, mensaje)
                Bitacora.Nivel.INFO -> Registro.info(etiqueta, mensaje)
                Bitacora.Nivel.AVISO -> Registro.aviso(etiqueta, mensaje)
                Bitacora.Nivel.FALLO -> Registro.fallo(etiqueta, mensaje, causa)
            }
        }

        // Y lo mismo para la pantalla de diagnostico, que muestra el registro y
        // lo comparte: la pantalla cruzo, el archivo no.
        RegistroVisible.texto = { Registro.leerTodo() }
        RegistroVisible.borrar = { Registro.borrar() }

        // Las carpetas, la URL base y la version: lo que :interfaz no puede
        // averiguar solo.
        ArranqueAndroid.iniciar(
            contexto = this,
            urlBase = BuildConfig.URL_BASE,
            version = "${BuildConfig.VERSION_NAME} (${BuildConfig.BUILD_TYPE})",
        )

        // El idioma antes que los textos: es quien decide cual de los tres
        // juegos embebidos se carga encima del castellano.
        Idioma.iniciar()
        Textos.cargarEmbebido(Idioma.actual)
        Datos.iniciar()
        Guardado.iniciar()

        // El modo se fija antes de la primera composicion. Si se dejara solo al
        // SideEffect de la pantalla, el primer cuadro se dibujaria en claro y
        // recien el segundo en oscuro: un parpadeo blanco al abrir de noche.
        Tono.oscuro = enModoOscuro()

        // El interruptor de avisos vive en la pantalla de perfil, que es
        // compartida; WorkManager solo existe aca. Estas dos lineas son el
        // puente, y sin ellas el interruptor cambiaria la preferencia sin
        // programar nada.
        TrabajoDeAvisos.programar = { Vigilante.programar(this) }
        TrabajoDeAvisos.cancelar = { Vigilante.cancelar(this) }

        // Los canales van siempre, aunque los avisos esten apagados: son lo que
        // hace que la app aparezca en los ajustes de notificaciones del telefono
        // antes de que alguien la busque ahi.
        Avisos.crearCanales(this)
        if (Ajustes.avisosActivos) Vigilante.programar(this)

        // La lista de idiomas, que no depende de ninguna pantalla. Los textos
        // del panel los pide `Aplicacion` por idioma: si se pidieran aca
        // tambien, el primer arranque haria el mismo pedido dos veces, y un
        // cambio de idioma que llegara de `/idiomas` no se reflejaria.
        CoroutineScope(SupervisorJob()).launch {
            Datos.refrescarIdiomas()
        }
    }

    // Los parentesis son explicitos a proposito: `and` liga mas fuerte que `==`
    // en Kotlin, asi que esto ya seria correcto sin ellos, pero leerlo al reves
    // es un error clasico y no vale la pena dejarlo a la memoria de nadie.
    private fun enModoOscuro(): Boolean =
        (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
}
