package net.caaguazu.turismo.datos

import net.caaguazu.turismo.core.Resultado

/**
 * El contrato czu-app/v1, tal como lo implementa el plugin del panel.
 *
 * Esta es la unica abstraccion con dos implementaciones de toda la app, y por eso
 * se justifica: es el interruptor entre los mocks y la API real. Cuando la API
 * este publicada, enchufarla es cambiar cual se construye.
 *
 * Nada lanza. Todo devuelve un resultado que obliga a quien llama a contemplar el
 * fallo.
 */
interface Contrato {

    suspend fun categorias(): Resultado<List<Categoria>>

    suspend fun zonas(): Resultado<List<Zona>>

    suspend fun inventario(
        categoria: Int? = null,
        zona: Int? = null,
        buscar: String? = null,
        pagina: Int = 1,
        porPagina: Int = 20,
    ): Resultado<Pagina<ItemInventario>>

    suspend fun ficha(id: Int): Resultado<Ficha>

    /** Todos los pines de una sola vez: el mapa no pagina. */
    suspend fun marcadores(): Resultado<List<Marcador>>

    suspend fun eventos(desde: String? = null, hasta: String? = null): Resultado<Pagina<Evento>>

    suspend fun evento(id: Int): Resultado<Evento>

    suspend fun recorridos(): Resultado<Pagina<Recorrido>>

    suspend fun recorrido(id: Int): Resultado<Recorrido>

    suspend fun articulos(pagina: Int = 1, categoria: Int? = null): Resultado<Pagina<ResumenArticulo>>

    suspend fun articulo(id: Int): Resultado<Articulo>

    suspend fun textos(idioma: String): Resultado<Map<String, String>>

    suspend fun medios(): Resultado<Map<String, Medio>>

    suspend fun delta(desde: String?): Resultado<Delta>
}
