# La guía de publicación

`Publicar la app - Play Store y App Store.pdf` es una guía en lenguaje
sencillo para el dueño del proyecto: qué hacer, en qué orden, cuánto cuesta
y cuánto tarda cada cosa para publicar en las dos tiendas.

No reemplaza a `docs/publicar-en-tiendas.md` ni a `docs/ios.md`, que son la
auditoría técnica: qué está resuelto en el código, qué falta y qué está
verificado. La guía traduce eso a pasos.

El PDF se genera del HTML de al lado, que es la fuente y lo que se edita:

```sh
chromium --headless --no-pdf-header-footer \
  --print-to-pdf="docs/guia/Publicar la app - Play Store y App Store.pdf" \
  docs/guia/publicar-paso-a-paso.html
```

Sin tipografías externas a propósito: se usan las que trae cualquier
sistema, así que se renderiza igual sin red.

**Los precios y plazos de las consolas los cambian Google y Apple sin
aviso.** La fecha de la portada dice desde cuándo no se revisó.
