package net.caaguazu.turismo.ios

import androidx.compose.ui.window.ComposeUIViewController
import net.caaguazu.turismo.core.Bitacora
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
 * cambio no se pierde la app. Para diagnosticar hay dos vias mejores que ya
 * estan en el workflow: los informes de caida y la consola atada.
 *
 * Esto es el equivalente de lo que hace `App` en Android, donde engancha al
 * `Registro` de siempre. Aca no hay archivo rotativo ni pantalla de
 * diagnostico: cuando haga falta, es el lugar donde van.
 */
private fun engancharBitacora() {
    // Idempotente: `puntoDeEntrada` se puede llamar mas de una vez si Swift
    // recrea la vista, y no hace falta reemplazar el destino cada vez.
    if (Bitacora.destino != null) return

    Bitacora.destino = { nivel, etiqueta, mensaje, causa ->
        val porque = causa?.message?.let { " — $it" } ?: ""
        println("[" + nivel.name + "] " + etiqueta + ": " + mensaje + porque)
    }
}
