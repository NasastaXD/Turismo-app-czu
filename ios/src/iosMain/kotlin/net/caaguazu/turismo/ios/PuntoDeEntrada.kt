package net.caaguazu.turismo.ios

import androidx.compose.ui.window.ComposeUIViewController
import net.caaguazu.turismo.core.ArranqueIos
import net.caaguazu.turismo.core.Bitacora
import net.caaguazu.turismo.core.Guardado
import net.caaguazu.turismo.core.Idioma
import net.caaguazu.turismo.core.RegistroVisible
import net.caaguazu.turismo.core.Textos
import net.caaguazu.turismo.core.configurarImagenes
import net.caaguazu.turismo.datos.Datos
import net.caaguazu.turismo.ui.Aplicacion
import platform.UIKit.UIViewController

/**
 * Lo unico que Xcode necesita llamar.
 *
 * Del lado de Swift son dos lineas dentro de un `UIViewControllerRepresentable`
 * —esta escrito en `ios/xcode/VistaCompose.swift`—:
 *
 *     import TurismoKit
 *     PuntoDeEntradaKt.puntoDeEntrada()
 *
 * En Objective-C la clase se llama `TurismoKitPuntoDeEntradaKt`: Kotlin/Native
 * le pone delante el `baseName` del framework. Swift se come ese prefijo porque
 * coincide con el nombre del modulo que se importa, y por eso los dos nombres
 * no son iguales. El paso "Que exporta el framework" del workflow imprime los
 * dos leidos del encabezado generado, que es la unica fuente que manda.
 *
 * El framework se llama TurismoKit y no Turismo porque el modulo de la app en
 * Xcode ya se llama Turismo, y Swift no puede importar un modulo homonimo del
 * que se esta compilando.
 *
 * **Este archivo es toda la cascara de iOS.** Lo que dibuja es `Aplicacion()`,
 * que vive en :interfaz y es exactamente la misma funcion que dibuja la app de
 * Android. Si este archivo empieza a crecer, es que algo que deberia
 * compartirse se esta escribiendo dos veces.
 */
fun puntoDeEntrada(): UIViewController {
    engancharBitacora()

    // El orden importa y es el mismo que en `App.onCreate` de Android:
    // primero las carpetas y la URL base, despues el idioma —que decide cual de
    // los tres juegos embebidos se carga—, y recien despues los textos.
    ArranqueIos.iniciar()
    configurarImagenes()

    Idioma.iniciar()
    Textos.cargarEmbebido(Idioma.actual)
    Datos.iniciar()
    Guardado.iniciar()

    val controlador = ComposeUIViewController { Aplicacion() }
    // Se guarda para poder presentar la hoja de compartir, que en iOS exige un
    // controlador desde el cual presentarla. Es lo que usan "compartir el
    // registro" y "agendar".
    ArranqueIos.raiz = controlador
    return controlador
}

/**
 * El registro del lado iOS.
 *
 * `Bitacora` no escribe nada hasta que una plataforma la engancha, asi que sin
 * esto se perderia todo lo que anotan `Http`, `Cache` y las pantallas — justo
 * lo que hace falta para diagnosticar.
 *
 * Va a `println` y NO a NSLog, y esto costo dos caidas: **un String de Kotlin
 * pasado como vararg de Objective-C no se convierte a NSString**. NSLog recibe
 * un objeto de Kotlin, le pregunta `respondsToSelector` y el proceso muere con
 * EXC_BAD_ACCESS. La pila del informe de caida lo dice entero:
 *
 *   Bitacora.anotar -> engancharBitacora$1.invoke -> NSLog ->
 *   __CFSTRING_IS_CALLING_OUT_TO_AN_OBJECT_FORMAT_ARGUMENT ->
 *   objc_opt_respondsToSelector -> SIGSEGV
 *
 * No era el especificador de formato. Cambiar `%s` por `%@` no arreglo nada
 * porque el problema es el paso del argumento, no como se lo imprime. Y el
 * sintoma era de los peores: la app arrancaba, dibujaba el mapa, pedia por red
 * con exito, y moria recien al anotar la primera linea.
 *
 * `println` es de Kotlin y no cruza a Objective-C, asi que no puede pasar. Se
 * pierde el registro unificado del sistema —`log show` no ve stdout— y a
 * cambio no se pierde la app.
 *
 * Ademas se guardan las ultimas lineas en memoria para la pantalla de
 * diagnostico, que ahora tambien cruzo. No es un archivo rotativo como el de
 * Android —eso es trabajo para mas adelante— pero alcanza para ver que paso en
 * esta corrida y compartirlo.
 */
private fun engancharBitacora() {
    // Idempotente: `puntoDeEntrada` se puede llamar mas de una vez si Swift
    // recrea la vista, y no hace falta reemplazar el destino cada vez.
    if (Bitacora.destino != null) return

    Bitacora.destino = { nivel, etiqueta, mensaje, causa ->
        val porque = causa?.message?.let { " — $it" } ?: ""
        val linea = "[" + nivel.name + "] " + etiqueta + ": " + mensaje + porque
        println(linea)
        AnilloDeRegistro.anotar(linea)
    }

    RegistroVisible.texto = { AnilloDeRegistro.todo() }
    RegistroVisible.borrar = { AnilloDeRegistro.vaciar() }
}

/**
 * Las ultimas lineas del registro, en memoria.
 *
 * Con tope, y por la misma razon que la memoria de avisos lo tiene: sin el,
 * una app abierta toda la tarde termina guardando megabytes de texto que nadie
 * va a leer. Se conservan las ultimas, que son las que explican lo que acaba de
 * pasar.
 */
private object AnilloDeRegistro {

    private const val MAX_LINEAS = 500

    private val lineas = ArrayDeque<String>(MAX_LINEAS)

    fun anotar(linea: String) {
        if (lineas.size >= MAX_LINEAS) lineas.removeFirst()
        lineas.addLast(linea)
    }

    fun todo(): String = lineas.joinToString("\n")

    fun vaciar() = lineas.clear()
}
