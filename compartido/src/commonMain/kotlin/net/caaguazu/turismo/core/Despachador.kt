package net.caaguazu.turismo.core

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Donde corre lo que toca disco o red.
 *
 * Hace falta un expect/actual y no alcanza con `Dispatchers.IO` porque **en
 * Kotlin/Native `Dispatchers.IO` no es accesible**: el objeto `Dispatchers`
 * tiene ahi un miembro `internal val IO`, y un miembro le gana a la extension
 * publica que declara `concurrentMain`. Compila en Android y falla en iOS con
 * "it is internal in 'kotlinx.coroutines.Dispatchers'".
 *
 * En Android es el `Dispatchers.IO` de siempre, asi que no cambia nada de como
 * se comportaba. En iOS es `Dispatchers.Default`, que alcanza: NSURLSession ya
 * es asincrono por su cuenta y lo unico realmente bloqueante que queda es leer
 * y escribir la cache, que son archivos chicos.
 */
internal expect val despachadorIo: CoroutineDispatcher
