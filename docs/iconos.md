# Los iconos de las dos tiendas

Salen del mismo vector, `app/src/main/res/drawable/ic_launcher_foreground.xml`,
sobre el fondo `#1D1D1F` de `colors.xml`. Un icono distinto por tienda seria
dos identidades.

## Android

| Carpeta | Para que |
|---|---|
| `mipmap-anydpi-v26/ic_launcher.xml` | El adaptativo, de API 26 en adelante. El sistema aplica la mascara. |
| `mipmap-{m,h,x,xx,xxx}hdpi/ic_launcher.png` | API 24 y 25, que no tienen iconos adaptativos. |

Los PNG **no son decoracion redundante**: el calificador `-v26` excluye por
completo a Android 7.0 y 7.1, y `minSdk` es 24. Sin ellos no habia ninguna
configuracion que coincidiera en esos telefonos y el sistema mostraba el icono
gris por omision.

Cada PNG es el **recorte visible** del adaptativo: la zona central de 72 de las
108 unidades del lienzo, que es lo que la mascara deja ver desde API 26. Asi el
icono se ve igual en un Android 7 y en un Android 15. Lleva esquinas
redondeadas porque en API 24 y 25 no hay mascara del sistema y un cuadrado duro
se ve viejo.

## iOS

`ios/xcode/Recursos/Assets.xcassets/AppIcon.appiconset/AppIcon-1024.png`, un
solo archivo: desde Xcode 14 el catalogo acepta un tamaño universal y el
sistema deriva el resto. Sin canal alfa, sin esquinas redondeadas y a sangre,
que es lo que exige App Store Connect — ver el README de al lado.

## Como se regeneran

Sin dependencias nuevas: se arma un HTML con el mismo SVG y se fotografia con
el Chromium que ya hay en el entorno. `headless_shell` y no `chrome --headless`,
porque el segundo descuenta la altura de la barra del navegador del viewport y
la captura sale recortada — se perdio una vuelta por eso.

```sh
# Android, los cinco tamaños (48, 72, 96, 144, 192 px), con alfa y redondeado.
# iOS, 1024 px, opaco y cuadrado.
# El SVG es el mismo de ic_launcher_foreground.xml con viewBox="18 18 72 72".
headless_shell --no-sandbox --disable-gpu --hide-scrollbars \
  --default-background-color=00000000 --force-device-scale-factor=1 \
  --window-size=192,192 --screenshot=ic_launcher.png file://$PWD/icono.html
```

Si el vector cambia, se regeneran los seis archivos de una vez: son la misma
imagen a seis tamaños y que uno quede viejo se nota.
