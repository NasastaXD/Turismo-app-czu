package net.caaguazu.turismo.ios

import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import net.caaguazu.turismo.core.Bitacora
import platform.Foundation.NSLog
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
 * El proyecto de Xcode todavia no existe en el repo: generarlo requiere un Mac,
 * y este entorno es Linux.
 */
fun puntoDeEntrada(): UIViewController {
    engancharBitacora()
    return ComposeUIViewController { PantallaMapaIos() }
}

/**
 * El registro del lado iOS.
 *
 * `Bitacora` no escribe nada hasta que una plataforma la engancha, asi que sin
 * esto todo lo que anotan `Http` y `Cache` se perderia — justo lo que hace
 * falta para diagnosticar si los pines llegaron o no.
 *
 * Va a NSLog y no a `println` a proposito: NSLog escribe al registro unificado
 * del sistema, que es de donde el workflow "Captura de iOS" saca el log con
 * `log show`. Un `println` iria a stdout y ese paso no lo veria.
 *
 * Esto es el equivalente de lo que hace `App` en Android, donde engancha al
 * `Registro` de siempre. Aca no hay archivo rotativo ni pantalla de
 * diagnostico: cuando haga falta, es el lugar donde van.
 */
@OptIn(ExperimentalForeignApi::class)
private fun engancharBitacora() {
    // Idempotente: `puntoDeEntrada` se puede llamar mas de una vez si Swift
    // recrea la vista, y no hace falta reemplazar el destino cada vez.
    if (Bitacora.destino != null) return

    Bitacora.destino = { nivel, etiqueta, mensaje, causa ->
        val porque = causa?.message?.let { " — $it" } ?: ""

        // `%@` y un solo argumento, no `%s` con varios. Un String de Kotlin
        // llega a NSLog convertido en NSString, o sea un objeto, y `%s` espera
        // un puntero a char de C: leer un objeto como cadena de C es una falta
        // de memoria que mata el proceso entero.
        //
        // Eso es exactamente lo que pasaba. La app arrancaba, la ventana se
        // volvia key, Compose componia, el LaunchedEffect pedia los
        // marcadores, Http anotaba la primera linea aca, y el proceso moria
        // antes de la primera captura. Por eso nunca aparecio ni una linea de
        // la bitacora en el registro: se caia al escribir la primera.
        NSLog("%@", "[" + nivel.name + "] " + etiqueta + ": " + mensaje + porque)
    }
}
