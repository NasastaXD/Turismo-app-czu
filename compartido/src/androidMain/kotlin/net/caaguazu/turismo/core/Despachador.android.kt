package net.caaguazu.turismo.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** El de siempre: en la JVM `Dispatchers.IO` es publico y esta para esto. */
actual val despachadorIo: CoroutineDispatcher = Dispatchers.IO
