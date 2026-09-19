package net.caaguazu.turismo.core

/**
 * Ajustes que sobreviven al cierre de la app.
 *
 * Vive aca la memoria de los avisos, que es lo que evita notificar dos veces lo
 * mismo.
 *
 * Todo sobre `Preferencias` y nada mas: son unas pocas claves y un conjunto de
 * enteros. Una base de datos para esto seria maquinaria de sobra.
 *
 * La politica —el tope de ids recordados, que los mas recientes son los de id
 * mas alto— esta aca y por lo tanto se escribe una sola vez. Lo unico que
 * cambia entre plataformas es donde se guardan los pares, y eso lo resuelve
 * `Preferencias`.
 */
object Ajustes {

    private const val ETIQUETA = "Ajustes"
    private const val CLAVE_IDIOMA = "idioma"
    private const val CLAVE_AVISOS = "avisos"
    private const val CLAVE_ARTICULOS_VISTOS = "articulos_vistos"
    private const val CLAVE_EVENTOS_AVISADOS = "eventos_avisados"

    /**
     * Tope de ids recordados por conjunto.
     *
     * Sin tope, la lista de "ya avisado" crece para siempre. Se conservan los
     * ultimos: un articulo que salio hace dos años no va a volver a aparecer
     * como nuevo, y si apareciera, un aviso de mas es mejor que un archivo de
     * preferencias que crece sin limite.
     */
    private const val MAX_RECORDADOS = 300

    /**
     * El idioma elegido a mano. Null significa que nadie eligio todavia, que no
     * es lo mismo que haber elegido castellano: sin elegir se sigue al telefono,
     * y elegido se respeta aunque el telefono diga otra cosa.
     */
    var idioma: String?
        get() = Preferencias.texto(CLAVE_IDIOMA)
        set(valor) = Preferencias.ponerTexto(CLAVE_IDIOMA, valor)

    /** Si la app revisa y notifica. Arranca encendido; se apaga solo si se niega el permiso. */
    var avisosActivos: Boolean
        get() = Preferencias.booleano(CLAVE_AVISOS, true)
        set(valor) {
            Preferencias.ponerBooleano(CLAVE_AVISOS, valor)
            Bitacora.info(ETIQUETA, "avisos ${if (valor) "encendidos" else "apagados"}")
        }

    /**
     * Si el interruptor ya quedo fijado alguna vez, a mano o por el resultado
     * del permiso. Antes de eso, `avisosActivos` es solo el valor por defecto:
     * hace falta pedir el permiso de sistema una vez para que sea real.
     */
    fun avisosDecididos(): Boolean = Preferencias.tiene(CLAVE_AVISOS)

    /** Articulos de los que ya se aviso, o que ya existian al encender los avisos. */
    var articulosVistos: Set<Int>
        get() = leerIds(CLAVE_ARTICULOS_VISTOS)
        set(valor) = guardarIds(CLAVE_ARTICULOS_VISTOS, valor)

    /** Eventos de los que ya se aviso que se venian. */
    var eventosAvisados: Set<Int>
        get() = leerIds(CLAVE_EVENTOS_AVISADOS)
        set(valor) = guardarIds(CLAVE_EVENTOS_AVISADOS, valor)

    /** Al apagar los avisos se olvida lo anotado: volver a encenderlos empieza limpio. */
    fun olvidarAvisados() {
        Preferencias.quitar(CLAVE_ARTICULOS_VISTOS)
        Preferencias.quitar(CLAVE_EVENTOS_AVISADOS)
    }

    private fun leerIds(clave: String): Set<Int> =
        Preferencias.conjunto(clave).mapNotNull(String::toIntOrNull).toSet()

    private fun guardarIds(clave: String, ids: Set<Int>) {
        // Los mas altos son los mas recientes: los ids del panel son
        // autoincrementales, asi que ordenar por id ordena por antiguedad.
        val recortado = ids.sortedDescending().take(MAX_RECORDADOS)
        Preferencias.ponerConjunto(clave, recortado.map(Int::toString).toSet())
    }
}
