# Filosofía de trabajo

Criterio con el que se construye en este ecosistema. No es específico de una
tecnología ni de un proyecto: aplica igual a una app nativa, a un sitio web o a
un plugin.

Está escrito para que lo lea un agente antes de tocar nada. Si algo estorba, se
discute y se cambia el documento; lo que no se hace es ignorarlo en silencio.

---

## 1. La redacción es humana

**Esta es la regla dura del ecosistema.**

Un agente escribe, como mucho, un título o tres palabras. No escribe artículos,
descripciones, textos de ayuda, copy de relleno, ni textos de ejemplo que puedan
quedar en producción. Todo texto que un usuario lee lo escribe una persona.

No es una preferencia estética. El texto de producto es donde una organización
suena como sí misma, y es lo primero que delata cuando algo lo escribió una
máquina que no conoce el lugar del que habla.

**Si hace falta un marcador para poder maquetar, va marcado de forma
inequívoca.** `‹clave›`, `[FALTA: titular]`, lo que sea — pero imposible de
confundir con texto terminado. Un párrafo plausible de relleno es peor que un
hueco visible, porque el hueco se arregla y el párrafo plausible se publica.

### Corolario: ningún texto vive en el código

Todo texto de interfaz se pide por clave a una fuente editable: un JSON, el CMS,
una tabla. Nunca escrito dentro de una plantilla, un componente o una clase.

Esto tiene una consecuencia práctica que justifica todo el trabajo: **cambiar
cualquier texto no requiere volver a publicar**. Quien escribe edita donde
escribe, y el cambio aparece.

Dos detalles que suelen olvidarse y rompen esto en silencio:

- **Los textos del servidor se fusionan sobre el respaldo local, nunca lo
  reemplazan.** Una fuente a medio cargar no puede dejar sin texto al resto del
  sitio.
- **Un valor vacío no pisa.** Una clave en blanco es un descuido de quien
  redacta, no la intención de borrar el texto que ya había.

### Lo mismo con las imágenes

Las imágenes de interfaz salen de un manifiesto editable, no incrustadas en el
código. Y **nunca imágenes de archivo**: o son del lugar del que se habla, o son
marcadores evidentemente sintéticos. Una foto de stock de una montaña genérica
en un sitio sobre un lugar real es una mentira pequeña que el visitante detecta
aunque no sepa nombrarla.

---

## 2. El color es un sistema cerrado, no una elección por caso

Una paleta no es una lista de colores bonitos: es **un conjunto cerrado de
tokens con reglas sobre dónde puede aparecer cada uno.**

### Los tokens se nombran por rol, no por color

`--acento`, `--tinta`, `--papel`, `--banda`, `--linea`. No `--rojo`, no
`--gris-3`, no `--azul-primario`. El nombre tiene que sobrevivir a que alguien
decida que el acento ahora es verde.

Y el corolario que importa: **ningún color se escribe suelto en una pantalla.**
Un `#E9503F` dentro de un componente es un token fuera del sistema que después
nadie encuentra para cambiar.

### El acento es el recurso más escaso

Casi todo el trabajo de una paleta está en decidir **dónde no** va el acento.

Una regla estrecha, escrita, del tipo: *el acento va en fechas y metadatos, en
badges, en bordes de iconos circulares y en el breadcrumb; nunca como fondo de
una superficie grande, nunca en un botón principal.* Y con eso, el acento
aparece poco y cuando aparece significa algo.

Cuando el acento está en todos lados deja de señalar. Un sitio donde el rojo de
marca está en el botón, en el título, en el borde y en el fondo de la sección
destacada no tiene identidad: tiene ruido con el color correcto.

### Los neutros se eligen

Un gris medio puro se lee como no elegido. Un gris con un sesgo mínimo hacia el
tono del acento se lee como decidido. Es una diferencia que casi nadie sabe
nombrar y que todo el mundo percibe.

### Restricciones que dan carácter

Las que valen la pena son las que se pueden decir en una frase y verificar:

- **Cuántas sombras hay en todo el sitio.** La respuesta correcta suele ser cero
  o una. Un sistema donde cada tarjeta flota un poco no tiene jerarquía.
- **Cuántos radios existen.** Dos o tres, nombrados. No un número escrito a mano
  en cada componente.
- **Cuántas familias tipográficas.** Una, y las excepciones se justifican por
  contexto de lectura, no por variedad. Un artículo largo puede pedir otra cosa
  que una interfaz que se opera.
- **Gradientes.** Normalmente ninguno.

Una restricción que no se puede verificar es una intención, no una regla.

---

## 3. Las reglas se verifican, no se documentan

**Este es probablemente el punto más importante del documento.**

Una convención escrita en un documento dura hasta el primer día de apuro. Una
convención escrita como prueba dura hasta que alguien la borre a propósito.

Todo lo anterior —el acento, los radios, las sombras, los colores sueltos, los
textos en el código— se puede comprobar automáticamente. En un sitio web eso es
una regla de lint, un test de CI o un script que recorre las fuentes. Ejemplos
de comprobaciones que valen:

- Ningún texto visible escrito dentro de un componente o plantilla.
- Ninguna clave de texto que el código pida y no exista, ni declarada que nadie
  muestre.
- Ningún color literal fuera del archivo de tokens.
- Ningún radio ni sombra fuera del conjunto declarado.

En este ecosistema, **cada una de esas comprobaciones encontró algo real el
mismo día que se escribió.** No son ceremonia: son la única forma de que "al
milímetro" siga siendo cierto dentro de tres meses.

Escribir la comprobación en las dos direcciones: que detecte el caso malo **y**
que no marque el caso bueno. Una comprobación que siempre pasa es peor que
ninguna, porque da confianza sin darla.

---

## 4. Calidad del código por encima de su comprensión inmediata

Se optimiza de verdad. Si la versión rápida es menos evidente de leer, se
escribe la rápida y se explica en un comentario **por qué**.

Esto es una decisión explícita del dueño del proyecto, y conviene decirla
completa: prefiere código que tenga que preguntar antes que código lento y obvio.
Ante la duda, se hace lo correcto técnicamente y se documenta el razonamiento.

Dos matices que la hacen sostenible:

**Los comentarios explican el porqué, nunca el qué.** El código ya dice qué
hace. Lo que no dice es qué alternativa se descartó y por qué — y eso es lo
único que el siguiente que pase necesita para no deshacer la decisión.

**La optimización real se mide, no se supone.** Antes de optimizar, saber qué es
lo caro. En una interfaz casi nunca es la CPU: es el peso que se descarga, el
tiempo hasta el primer dibujado, la memoria y el trabajo repetido al redibujar.
Optimizar un bucle que corre diez veces mientras se descarga un megabyte de
tipografías sin usar es teatro.

### Sin ceremonia

- Si hay una sola implementación de algo, no hay interfaz, ni fábrica, ni
  contenedor de dependencias.
- Un único modelo por entidad. Nada de cadenas que copian campos de un objeto a
  otro sin agregar nada.
- Cada capa tiene que ganarse su existencia.

### Cada dependencia se justifica

Una dependencia se paga en peso, en superficie de mantenimiento y en el día en
que deja de mantenerse. Antes de agregar una: ¿qué hace que no se pueda hacer
con lo que ya hay, y cuánto código real ahorra?

Y el mismo criterio, más fuerte, para los **servicios externos**: cada servicio
del que algo depende es algo que puede cortarse, cambiar de precio o desaparecer.
Preferir lo que funciona sin cuenta, sin clave y sin conexión, aunque cueste más
trabajo la primera vez.

---

## 5. El público no lee párrafos

Buena parte de la gente que usa esto es mayor y no lee bloques de texto.

En la práctica: una idea por pantalla, objetivos táctiles grandes, tipografía
grande, y donde haría falta explicar algo, **una animación breve o un icono en
lugar de un párrafo**. Sin texto de ayuda largo en ninguna parte.

Es un requisito, no una preferencia estética.

### El movimiento explica o no está

La animación sirve para mostrar de dónde sale algo y adónde va. Nada rebota,
nada gira, nada llama la atención sobre sí mismo.

Los tiempos, tranquilos: para este público una transición rápida no se lee como
ágil sino como un parpadeo del que uno no se entera.

Y **si el sistema tiene las animaciones reducidas, se respeta.** Se apagan por
accesibilidad, por batería o porque el equipo es viejo; en los tres casos animar
igual es ignorar una decisión que el usuario ya tomó.

---

## 6. Nada falla en silencio

**Los estados son cuatro, no dos.** Cargando, con datos, vacío, y error con
reintentar. "No hay nada" y "falló al traerlo" se arreglan de formas distintas, y
confundirlos deja al usuario sin saber si esperar, reintentar o irse.

**Los errores no cruzan capas como excepciones sueltas.** Lo que puede fallar
devuelve un resultado explícito, y quien llama está obligado a contemplar el
fallo.

**Se es tolerante con lo que llega de afuera.** Un campo nuevo que el servidor
agregue mañana no puede tumbar algo ya publicado. Un dato que falta tiene valor
por defecto y una línea en el registro, no una pantalla en blanco.

**Sin conexión se muestra lo último que se tenía**, avisando que puede estar
desactualizado. Es mejor que una pantalla de error, sobre todo donde la señal es
mala por defecto.

### El registro existe para cuando falle en la máquina de otro

Propio, sin telemetría saliendo del dispositivo del usuario, exportable por
quien lo sufre. Dos detalles que lo hacen servir de verdad:

- **Las etiquetas se escriben a mano.** Derivarlas del nombre de la clase se
  rompe en cuanto el código se ofusca o se minifica, que es justo cuando hace
  falta.
- **Cada publicación guarda lo necesario para desofuscar sus propios
  registros.** Sin eso, un error reportado por un usuario es ilegible incluso
  para quien escribió el código.

---

## 7. No se construye de nuevo lo que ya existe

Antes de escribir algo, saber qué parte de eso ya está resuelta en el
ecosistema, y consumirlo en vez de duplicarlo.

Duplicar identidad, permisos o flujo editorial no es solo trabajo de más: es
crear un segundo lugar donde la verdad puede diferir del primero. El día que
difieran, nadie va a saber cuál es el bueno.

Caché local sí, siempre, y descartable por definición. Fuente de verdad, una
sola.

**Y si el contrato con lo que ya existe no alcanza, se pide el cambio en lugar de
compensarlo con parches.** Un parche del lado del cliente para cubrir un campo
que el servidor debería mandar es deuda que se paga con intereses.

---

## 8. Se dice lo que no está verificado

Un agente puede compilar, pasar pruebas y aun así no haber visto nunca el
resultado dibujado. Decir "listo" sobre eso es mentir.

Se separa siempre: **esto lo comprobé, esto no lo pude comprobar y por qué.** Y
cuando algo se descubre mal —incluido el propio trabajo—, se dice primero y se
arregla después, no al revés.

---

## Lo que no se hace

- Escribir contenido de producto.
- Usar imágenes de archivo.
- Meter un texto o un color directamente en un componente.
- Usar el acento fuera de su regla.
- Agregar una dependencia o un servicio externo que se pueda evitar.
- Inventar un radio, una sombra o un tamaño que no esté en el sistema.
- Construir de nuevo algo que el ecosistema ya resuelve.
- Compensar con un parche lo que debería arreglarse en el origen.
- Dar por bueno lo que no se comprobó.
