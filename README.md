# GastroMind — App móvil (Kotlin / Android)

Módulo **Operativo** de GastroMind para el personal del restaurante: mozo, cocinero y almacenero.
Curso: *Programación en Móviles (Kotlin)*.

> Repo principal (Spring Boot + React + documentación): [diegoz-netizen/GastroMind](https://github.com/diegoz-netizen/GastroMind)

## Arquitectura

```
App Android (este repo) ──▶ Spring Boot (repo GastroMind) ──▶ PostgreSQL
```

La app **no** se conecta directo a la base de datos: todo pasa por la API de Spring Boot.

## Qué cubre (según el backlog)

| Rol | Pantallas | Épica |
|---|---|---|
| Mozo | Mesas, catálogo, tomar pedido, cerrar mesa | EP-03 |
| Cocinero | KDS (comandas), registrar merma | EP-03, EP-05 |
| Almacenero | Ingresos de compras, conteo cíclico | EP-04 |

## Reglas

1. **Login único:** `POST /api/auth/login` de Spring Boot devuelve el JWT. Se envía en cada petición como `Authorization: Bearer <token>`. Un empleado desactivado recibe **401** y la app debe volver al login.
2. El token se guarda de forma segura (DataStore / EncryptedSharedPreferences), nunca en texto plano.
3. La URL de la API va en configuración (por ejemplo `local.properties` o *build config*), no fija en el código.

## Estado

Repo creado el 12/10/2026 (Sprint 2). El proyecto Android todavía no está inicializado. La app se trabaja desde el Sprint 3 (EP-03).

## Flujo de trabajo

Igual que en el repo principal: una rama por tarea (`feature/HU-xx-...`), Pull Request a `main` con 1 aprobación de otro integrante y merge con *Create a merge commit*. `main` está protegida.

## Equipo

Diego Panez · Victor Santamaría · Piero Guevara
