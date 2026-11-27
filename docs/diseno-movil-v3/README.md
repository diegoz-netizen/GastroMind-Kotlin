# Rediseño móvil v3 (prototipo navegable)

Referencia visual y de comportamiento para la app móvil. **No cambia nada de `app/`**: es un prototipo web (Vite + React) que se corre en el celular o en el navegador para ver cómo quedaría cada pantalla antes de pasarla a Compose.

## Cómo verlo
```bash
cd docs/diseno-movil-v3
npm install
npm run dev          # http://localhost:5173
```
En el celular por USB: `npm run build && npm run preview -- --port 4173`, luego `adb reverse tcp:4173 tcp:4173` y abrir `http://localhost:4173` en Chrome (menú ⋮ → Instalar aplicación).
PIN: cualquier 4 dígitos. "Cambiar" rota entre Mozo, Cocinero y Almacenero. Los datos viven en el navegador; para volver a los de ejemplo: Perfil → Modo demo → *Restablecer datos de demostración*.

## Qué trae por rol
| Rol | Pantallas | HU relacionadas |
|---|---|---|
| Todos | Login con PIN o correo, perfil con tema Claro / Oscuro / Automático, notificaciones por rol, estados cargando / vacío / error | HU-02 |
| Mozo | Salón con mesas por estado, pedido por categorías con notas, En cocina con seguimiento y "Confirmar entrega", cuenta de la mesa con descuento y cierre, anular un platillo con motivo | HU-09, HU-11, HU-12, HU-13, HU-18 |
| Cocinero | Comandas por mesa (Empezar → Marcar listo), anular con motivo (registra merma), Historial del turno, resumen | HU-10, HU-11, HU-18 |
| Almacenero | Stock con alertas de mínimo y valor del inventario, Kardex por insumo con periodo y tipo, Ingreso de compras, Conteo físico, Merma | HU-14, HU-15, HU-16, HU-17, HU-30 |

Lo que se hace en un rol se ve en los otros (un solo estado): el pedido del mozo llega a cocina, lo que cocina marca listo sale como "Listo para llevar", y un platillo anulado ya empezado aparece como merma en almacén. El cobro se bloquea hasta entregar todo.

## Diferencias con la app actual
- **Cocina en claro** (blanco + verde) con el mismo header y navegación que los demás roles; hoy usa el esquema oscuro del KDS. El modo oscuro pasa a ser una opción global del perfil.
- **Dos diseños:** celular (barra inferior, botones de 48 px) y tablet (riel lateral, Salón y Almacén en maestro-detalle, cocina en 3 o 4 columnas).
- **Descuento al cobrar (HU-13):** la app Kotlin aún no lo tiene; aquí se ve cómo quedaría.
- **Notificaciones** por rol y logo de GastroMind (campana con nodos) en login, riel y favicon.
- Los colores salen del sistema de diseño actual (`#1D5B47`, ámbar `#C98A1E`, error `#B42318`); si se adopta, habría que actualizar la sección "Sistema de diseño" del README principal.

## Qué NO es
Datos de ejemplo, sin servidor ni login real (el rol sale del empleado elegido). La anulación con merma no descuenta insumos porque la demo no tiene recetas. Si algo no convence, se anota en el PR y se rediseña antes de llevarlo a Compose por HU.
