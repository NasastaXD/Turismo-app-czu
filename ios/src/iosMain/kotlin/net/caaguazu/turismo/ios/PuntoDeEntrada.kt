package net.caaguazu.turismo.ios

import androidx.compose.ui.window.ComposeUIViewController
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
fun puntoDeEntrada(): UIViewController = ComposeUIViewController {
    PantallaMapaIos()
}
