package net.caaguazu.turismo.core

/**
 * Lo que el telefono sabe y lo que el telefono hace por nosotros.
 *
 * Cuatro cosas, y las cuatro estan aca por el mismo criterio que rige el resto
 * del proyecto: **se delega en lo que el telefono ya tiene en vez de
 * construirlo de nuevo.** Abrir un mapa, agendar un evento y compartir un
 * archivo son tres cosas que la persona ya sabe hacer con sus apps de siempre,
 * y competir con eso no le aporta nada a nadie.
 *
 * Todo devuelve `Boolean` en lugar de lanzar. Un telefono sin app de mapas es
 * raro pero posible, y quien llama necesita poder no dibujar el boton en vez de
 * dibujarlo para que no haga nada.
 */
expect object Sistema {

    /**
     * El idioma del telefono, en dos letras, o null si no se pudo averiguar.
     *
     * Solo se consulta cuando la persona nunca eligio uno a mano. Si el
     * telefono esta en un idioma que la app no tiene, quien llama cae al
     * original: se abre en castellano, nunca en un idioma que no existe.
     */
    val idiomaDelTelefono: String?

    /**
     * Si el telefono tiene las animaciones encendidas.
     *
     * Se apagan por accesibilidad, por ahorro de bateria o por preferencia. En
     * los tres casos animar igual seria ignorar una decision ya tomada.
     */
    fun animacionesActivas(): Boolean

    /**
     * Abre una URL con la app que corresponda: `geo:` va al mapa del telefono,
     * `https:` al navegador.
     */
    fun abrirUrl(url: String): Boolean

    /**
     * Propone un evento al calendario de la persona, con el formulario ya
     * completado, y ella confirma.
     *
     * **No se pide ningun permiso.** La app nunca lee ni escribe en el
     * calendario: solo propone. Pedir acceso a la agenda entera de alguien para
     * esto seria pedir mucho a cambio de nada.
     */
    fun agendar(
        titulo: String,
        inicioMs: Long,
        finMs: Long,
        lugar: String?,
        descripcion: String?,
    ): Boolean

    /**
     * Entrega un archivo de texto al selector de compartir del sistema. Lo usa
     * la pantalla de diagnostico para sacar el registro del telefono.
     */
    fun compartirTexto(nombreSugerido: String, contenido: String): Boolean
}
