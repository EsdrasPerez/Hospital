# Hospital - Frontend web

Interfaz web del proyecto API Hospital (Arquitectura de Sistemas, UMES).
Stack: Vite + React (JavaScript), React Router, axios y Tailwind CSS v4.

## Requisitos

- Node.js 20.19 o superior (probado con Node 24) y npm.
- El backend (`../hospital`, Spring Boot) **debe estar corriendo en http://localhost:8080** con su base de datos PostgreSQL.

## Puesta en marcha

```bash
cd frontend
npm install
cp .env.example .env     # en Windows (PowerShell): Copy-Item .env.example .env
npm run dev
```

La aplicación queda en http://localhost:5173. El puerto está fijado (`strictPort`) porque es el único origen que permite el CORS del backend (`app.cors.allowed-origins`).

## Variables de entorno

| Variable       | Descripción                     | Valor por defecto       |
| -------------- | ------------------------------- | ----------------------- |
| `VITE_API_URL` | URL base del backend            | `http://localhost:8080` |

## Scripts

- `npm run dev`: servidor de desarrollo.
- `npm run build`: compilación de producción en `dist/`.
- `npm run lint`: análisis estático (oxlint).

## Estructura

```
src/
  api/          cliente axios, rutas centralizadas (rutas.js) y una función por endpoint
  auth/         AuthProvider, useAuth, ProtectedRoute, RoleRoute
  config/menu.js  módulos y permisos por rol (derivados de los @PreAuthorize del backend)
  layouts/      MainLayout (sidebar + header) y AuthLayout
  components/   Table, FormField, ErrorAlert, Loading, EmptyState, ConfirmDialog, Pagination
  pages/        Login, Inicio, NoAutorizado, NoEncontrado y pages/pacientes
  utils/        manejo de errores del backend y formato de fechas
```

## Notas

- La sesión usa el JWT del backend (expira en 1 hora) guardado en `localStorage`. Cualquier respuesta 401 cierra la sesión y lleva a `/login`.
- Las rutas del backend están centralizadas en `src/api/rutas.js` (Pacientes usa `/api/v1/paciente`, el resto `/api/...`).
- Módulos implementados en esta fase: autenticación e inicio, y Pacientes. El resto de módulos están declarados en `src/config/menu.js` con `implementado: false` y se activarán en próximas fases.
