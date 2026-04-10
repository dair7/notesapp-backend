# Documentación Técnica — Autenticación y Seguridad
## Notes Pro · Backend Spring Boot + Flutter

---

## Índice

1. [Tecnologías y conceptos base](#1-tecnologías-y-conceptos-base)
2. [Registro con Email y Contraseña (JWT)](#2-registro-con-email-y-contraseña-jwt)
3. [Verificación de Correo Electrónico](#3-verificación-de-correo-electrónico)
4. [Correo de Bienvenida](#4-correo-de-bienvenida)
5. [Inicio de Sesión con Email y Contraseña (JWT)](#5-inicio-de-sesión-con-email-y-contraseña-jwt)
6. [Tokens JWT y Refresh Token](#6-tokens-jwt-y-refresh-token)
7. [Registro e Inicio de Sesión con Google (Firebase)](#7-registro-e-inicio-de-sesión-con-google-firebase)
8. [Recuperación de Contraseña](#8-recuperación-de-contraseña)
9. [Eliminación de Cuenta](#9-eliminación-de-cuenta)
10. [Panel de Administración](#10-panel-de-administración)
11. [Roles del Sistema](#11-roles-del-sistema)
12. [Seguridad General](#12-seguridad-general)

---

## 1. Tecnologías y conceptos base

| Tecnología | Uso |
|---|---|
| **Spring Security** | Protección de endpoints, manejo de roles |
| **JWT (JSON Web Token)** | Token de acceso que identifica al usuario |
| **Refresh Token** | Token de larga duración para renovar el JWT |
| **BCrypt** | Algoritmo para encriptar contraseñas en la base de datos |
| **Firebase Auth** | Verificación de identidad cuando el usuario usa Google |
| **Gmail SMTP** | Envío de correos de verificación, bienvenida y recuperación |
| **PostgreSQL** | Base de datos donde se almacenan usuarios y tokens |

---

## 2. Registro con Email y Contraseña (JWT)

**Endpoint:** `POST /api/auth/register`

### ¿Qué hace el usuario?
Ingresa nombre, correo y contraseña en la pantalla de registro.

### ¿Qué hace el sistema paso a paso?

```
1. Recibe nombre, email, contraseña
2. Verifica que el email no esté ya registrado
3. Encripta la contraseña con BCrypt
4. Crea el usuario en la base de datos con isVerified = false
5. Genera un token UUID aleatorio de verificación
6. Guarda el token en la tabla verification_tokens con expiración de 24 horas
7. Envía un correo con el enlace de verificación al email ingresado
8. Responde al Flutter: "Revisa tu correo para activar tu cuenta"
```

### ¿Por qué no entra de inmediato?
Porque `isVerified = false`. El sistema bloquea el login hasta que el usuario confirme su correo. Esto evita que se creen cuentas con correos falsos.

### Contraseña en la base de datos
La contraseña **nunca se guarda en texto plano**. BCrypt la convierte en un hash irreversible, por ejemplo:
```
mi_contraseña → $2a$10$xK8L9mN3pQ7rT2wY5vZ1...
```

---

## 3. Verificación de Correo Electrónico

**Endpoint:** `GET /api/auth/verify?token=XXXXXXXX`

### ¿Cómo funciona el correo?
El usuario recibe un email con un botón **"Verificar mi cuenta"** que contiene una URL como:
```
http://tuservidor.com/api/auth/verify?token=a3f7c2d1-8b4e-...
```

### ¿Qué es ese token?
Es un **UUID** (identificador único universal) generado aleatoriamente. Se ve así:
```
a3f7c2d1-8b4e-4c9a-b1f2-3d5e7f9a1b2c
```
No tiene información del usuario dentro — es simplemente una clave aleatoria que el sistema asocia al usuario en la base de datos.

### ¿Qué pasa al dar clic?

```
1. El backend recibe el token en la URL
2. Busca ese token en la tabla verification_tokens
3. Verifica que no haya expirado (duración: 24 horas)
4. Si es válido → marca al usuario como isVerified = true
5. Elimina el token (solo puede usarse una vez)
6. Envía el correo de bienvenida
7. Muestra página HTML de éxito (verify-success.html)
```

### ¿Qué pasa si el enlace expiró?
El sistema muestra la página `verify-error.html` con el mensaje de error. El usuario tendría que registrarse de nuevo o solicitar un nuevo enlace.

---

## 4. Correo de Bienvenida

**Se envía en dos situaciones:**

| Situación | Cuándo |
|---|---|
| **Registro con Google** | Al crear la cuenta por primera vez |
| **Registro JWT** | Inmediatamente después de verificar el correo |

### ¿Qué contiene?
- Saludo personalizado con el nombre del usuario
- Lista de funcionalidades de la app (notas, recordatorios, seguridad)
- Diseño idéntico al correo de verificación (header verde, footer, fondo gris)

### ¿Qué método lo envía?
```java
emailService.sendWelcomeEmail(usuario.getEmail(), usuario.getNombre());
```

---

## 5. Inicio de Sesión con Email y Contraseña (JWT)

**Endpoint:** `POST /api/auth/login`

### Flujo completo:

```
1. Usuario ingresa email y contraseña en Flutter
2. Flutter envía POST /api/auth/login con { email, password }
3. Backend verifica que el usuario exista
4. Verifica que isVerified = true (si no, error: "debes verificar tu correo")
5. Spring Security compara la contraseña con el hash BCrypt en la BD
6. Si coincide → genera JWT de acceso (duración: 24 horas)
7. Genera Refresh Token UUID (duración: 7 días) y lo guarda en BD
8. Responde con: { token, refreshToken, usuario }
9. Flutter guarda ambos tokens en FlutterSecureStorage (almacenamiento cifrado del dispositivo)
10. Todas las siguientes peticiones incluyen el JWT en el header: Authorization: Bearer <token>
```

### ¿Por qué dos tokens?
- **JWT (24h):** Se usa en cada petición para identificar al usuario. Es corto para minimizar el riesgo si alguien lo intercepta.
- **Refresh Token (7 días):** Solo se usa cuando el JWT expira. Pide uno nuevo sin obligar al usuario a volver a loguearse.

---

## 6. Tokens JWT y Refresh Token

### ¿Qué es un JWT?
Es una cadena codificada en Base64 dividida en 3 partes:
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGVtYWlsLmNvbSIsInJvbGUiOiJVU0VSIn0.firma
     HEADER                          PAYLOAD                              FIRMA
```

El **Payload** contiene:
- `sub` → email del usuario
- `role` → rol (USER, ADMIN, SUPER_ADMIN)
- `iat` → fecha de emisión
- `exp` → fecha de expiración

La **Firma** garantiza que nadie modificó el token. Está firmada con HMAC SHA-256 usando la clave secreta `JWT_SECRET`.

### Renovación automática (Refresh Token)

**Endpoint:** `POST /api/auth/refresh-token`

```
1. Flutter detecta respuesta 401 (token expirado)
2. Automáticamente toma el refreshToken guardado en el dispositivo
3. Envía POST /api/auth/refresh-token con { refreshToken }
4. Backend verifica que el refresh token exista y no haya expirado
5. Elimina el refresh token viejo (rotación de tokens, más seguro)
6. Genera nuevo JWT de 24h y nuevo Refresh Token de 7 días
7. Flutter actualiza los tokens guardados
8. Reintenta la petición original con el nuevo JWT
```

### Cierre de sesión

**Endpoint:** `POST /api/auth/logout`

```
1. Flutter envía el refreshToken al backend
2. Backend lo elimina de la base de datos
3. El JWT sigue siendo válido hasta su expiración (24h), pero sin refresh token
   el usuario tendrá que loguearse nuevamente después
4. Flutter borra todos los tokens del almacenamiento local
```

---

## 7. Registro e Inicio de Sesión con Google (Firebase)

**Endpoint:** `POST /api/auth/google`

> El registro y el inicio de sesión con Google usan **exactamente el mismo endpoint y flujo**. El sistema detecta automáticamente si el usuario es nuevo o ya existe.

### Tecnología usada
- **Firebase Authentication** gestiona la identidad con Google
- **Firebase Admin SDK** en el backend verifica que el token sea auténtico
- **google_sign_in** en Flutter abre el selector de cuenta de Google

### Flujo completo:

```
[FLUTTER]
1. Usuario toca "Continuar con Google"
2. Abre el selector de cuentas de Google del sistema operativo
3. Usuario selecciona su cuenta
4. Google devuelve credenciales (accessToken + idToken de Google)
5. Flutter las pasa a Firebase Authentication
6. Firebase autentica al usuario y genera un Firebase ID Token
7. Flutter envía ese Firebase ID Token al backend:
   POST /api/auth/google → { idToken: "eyJ..." }

[BACKEND]
8. Firebase Admin SDK verifica el token contra los servidores de Firebase
9. Extrae el email y nombre del usuario del token verificado
10. Busca si ya existe un usuario con ese email en la base de datos:

    ¿Existe? NO (primera vez):
    → Crea el usuario con isVerified = true (Google ya verificó el email)
    → Asigna rol USER
    → Guarda contraseña aleatoria (campo obligatorio, nunca se usa)
    → Envía correo de bienvenida

    ¿Existe? SÍ (ya tiene cuenta):
    → Solo actualiza isVerified = true si por algún motivo estaba en false
    → No envía bienvenida de nuevo

11. Genera JWT de 24 horas
12. Genera Refresh Token de 7 días y lo guarda en BD
13. Responde: { token, refreshToken, usuario }

[FLUTTER]
14. Guarda tokens en FlutterSecureStorage
15. Navega al dashboard
```

### ¿Por qué Firebase y no directamente Google?
Firebase simplifica la configuración. Sin Firebase habría que manejar manualmente los Client IDs, SHA-1, y la verificación de tokens de Google — lo cual es propenso a errores de configuración.

---

## 8. Recuperación de Contraseña

### Paso 1 — Solicitar código

**Endpoint:** `POST /api/auth/forgot-password?email=usuario@correo.com`

```
1. Usuario ingresa su email en la pantalla "¿Olvidaste tu contraseña?"
2. Backend busca si existe un usuario con ese email
3. Si no existe → responde igual que si existiera (por seguridad, no revela si el email está registrado)
4. Si existe → genera un código de 6 caracteres aleatorio en mayúsculas (ej: A3F7K2)
5. Guarda el código en la tabla password_reset_tokens con expiración de 1 hora
6. Envía el código al correo del usuario
7. Responde: "Si el correo está registrado, recibirás un código"
```

### ¿Cómo se genera el código?
```java
String codigo = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
// Ejemplo resultado: "A3F7K2"
```
Se toman los primeros 6 caracteres de un UUID aleatorio y se convierten a mayúsculas. Es suficientemente único para un uso temporal de 1 hora.

### ¿Por qué 6 caracteres y no más?
Es un balance entre seguridad y usabilidad. El usuario lo escribe manualmente en la app. Con expiración de 1 hora y máximo un intento por solicitud, es suficientemente seguro.

### Paso 2 — Restablecer contraseña

**Endpoint:** `POST /api/auth/reset-password`

```json
{ "token": "A3F7K2", "newPassword": "mi_nueva_contraseña" }
```

```
1. Usuario ingresa el código recibido y su nueva contraseña
2. Backend busca el código en password_reset_tokens
3. Verifica que no haya expirado (1 hora)
4. Encripta la nueva contraseña con BCrypt
5. Actualiza la contraseña del usuario en la base de datos
6. Elimina el código (uso único)
7. Responde: "Contraseña actualizada exitosamente"
```

---

## 9. Eliminación de Cuenta

### El usuario elimina su propia cuenta

**Endpoint:** `DELETE /api/usuarios/me`

```
1. Usuario autenticado solicita eliminar su cuenta
2. El backend extrae el email del JWT en el header
3. Busca al usuario por email
4. Elimina todos sus datos: notas, recordatorios, refresh tokens, tokens de verificación
5. Elimina el registro del usuario de la base de datos
6. Responde: confirmación de eliminación
```

### El administrador elimina una cuenta

**Endpoint:** `DELETE /api/admin/usuarios/{id}` o `DELETE /api/usuarios/{id}`

```
1. Solo accesible por usuarios con rol ADMIN o SUPER_ADMIN
2. Elimina el usuario por su ID
3. Se eliminan en cascada todos sus datos relacionados
```

---

## 10. Panel de Administración

El panel admin es una aplicación web separada hecha en **React + TypeScript** que se conecta al mismo backend.

### Acceso
Solo pueden acceder usuarios con rol `ADMIN` o `SUPER_ADMIN`. Si un `USER` intenta acceder, es redirigido al login.

### Endpoints del Admin

| Método | Ruta | Rol requerido | Descripción |
|---|---|---|---|
| `GET` | `/api/admin/usuarios` | ADMIN, SUPER_ADMIN | Listar todos los usuarios |
| `GET` | `/api/admin/usuarios/{id}` | ADMIN, SUPER_ADMIN | Ver usuario por ID |
| `POST` | `/api/admin/usuarios` | SUPER_ADMIN | Crear nuevo administrador |
| `PATCH` | `/api/admin/usuarios/{id}/rol` | SUPER_ADMIN | Cambiar rol de un usuario |
| `DELETE` | `/api/admin/usuarios/{id}` | ADMIN, SUPER_ADMIN | Eliminar usuario |

### ¿Cómo se crea el primer SUPER_ADMIN?
El backend tiene un **AdminSeeder** que corre al iniciar la aplicación. Lee las variables de entorno:
```
ADMIN_EMAIL=correo@ejemplo.com
ADMIN_PASSWORD=contraseña_segura
ADMIN_NAME=Nombre Admin
```
Si no existe ningún admin con ese correo, lo crea automáticamente con rol `SUPER_ADMIN`.

---

## 11. Roles del Sistema

| Rol | Descripción | Puede hacer |
|---|---|---|
| `USER` | Usuario regular de la app móvil | Gestionar sus propias notas y recordatorios |
| `ADMIN` | Administrador del panel web | Ver y eliminar usuarios |
| `SUPER_ADMIN` | Administrador con control total | Todo lo de ADMIN + crear admins y cambiar roles |

### ¿Cómo se verifica el rol en cada petición?
El JWT contiene el rol en su payload. El filtro `JwtAuthenticationFilter` extrae el rol y lo registra en el contexto de seguridad de Spring. Luego Spring Security evalúa si el rol tiene permiso para acceder al endpoint solicitado.

---

## 12. Seguridad General

### Endpoints públicos (no requieren token)
```
POST /api/auth/login
POST /api/auth/register
POST /api/auth/google
POST /api/auth/refresh-token
POST /api/auth/logout
POST /api/auth/forgot-password
POST /api/auth/reset-password
GET  /api/auth/verify
```

### Endpoints protegidos
Todos los demás requieren el header:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Protecciones implementadas

| Protección | Detalle |
|---|---|
| **CSRF deshabilitado** | No se necesita porque se usa JWT stateless, no sesiones con cookies |
| **Sesiones stateless** | El servidor no guarda estado de sesión. Cada petición es independiente |
| **BCrypt** | Factor de costo 10 por defecto. Hace que forzar contraseñas sea computacionalmente inviable |
| **Rate Limiting** | Filtro que limita peticiones por IP para evitar ataques de fuerza bruta |
| **CORS** | Solo orígenes permitidos pueden consumir la API |
| **Tokens de un solo uso** | Los tokens de verificación y reseteo se eliminan tras usarse |
| **Rotación de Refresh Tokens** | Cada vez que se renueva el JWT, el refresh token viejo se invalida y se crea uno nuevo |
