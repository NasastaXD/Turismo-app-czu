package net.caaguazu.turismo.core

import kotlinx.serialization.json.Json

/**
 * Un solo analizador para toda la app.
 *
 * `ignoreUnknownKeys` es un failsafe, no una comodidad: el panel puede agregar
 * campos al contrato en cualquier momento, y una app ya publicada en la tienda no
 * debe empezar a fallar porque el servidor mando un campo de mas.
 *
 * `coerceInputValues` cubre el caso contrario: un null donde el modelo espera un
 * valor cae al de por defecto en vez de tumbar la pantalla entera.
 */
val Analizador: Json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    explicitNulls = false
    isLenient = false
}
