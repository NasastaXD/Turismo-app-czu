package net.caaguazu.turismo.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * `Dispatchers.Default`, porque `Dispatchers.IO` no es accesible en
 * Kotlin/Native. No es una concesion: lo unico bloqueante que corre aca es leer
 * y escribir archivos chicos de cache — NSURLSession maneja la red por su
 * cuenta, sin ocupar este despachador.
 */
actual val despachadorIo: CoroutineDispatcher = Dispatchers.Default
