# El icono de iOS

`AppIcon-1024.png` es el mismo marcador de `ic_launcher_foreground.xml`,
rasterizado a 1024×1024 sobre el fondo `#1D1D1F` del icono de Android. Es un
solo archivo porque desde Xcode 14 el catalogo acepta un unico tamaño
universal y el sistema deriva el resto.

Tres cosas que Apple exige y que por eso son asi:

- **Sin canal alfa.** Un icono con transparencia se rechaza al subirlo
  (`ITMS-90717`). El PNG esta en RGB, sin alfa: comprobado con `file`.
- **Sin esquinas redondeadas.** La mascara la aplica el sistema. El de Android
  si las lleva, porque ahi en API 24 y 25 no hay mascara y un cuadrado duro se
  ve viejo.
- **Cuadrado y a sangre**, sin margen transparente alrededor.

Se genera del mismo vector que el de Android, para que las dos tiendas no
muestren dos iconos distintos.
