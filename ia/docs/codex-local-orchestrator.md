# Orquestador local de Codex

## Alcance

La primera integración se ejecuta en la PC del usuario. La aplicación web local inicia su propio proceso `codex app-server` por `stdio`, envía la tarea y traduce eventos del protocolo a estados para el panel. Solo informa tareas que se iniciaron desde esta integración; no observa ni controla tareas abiertas independientemente en Codex Desktop.

El proceso App Server usa la autenticación ya configurada para el CLI local. No se crea una API key del modelo para esta función. El prototipo usa eventos y procesos simulados en sus pruebas; el inicio de una tarea real queda pendiente de una prueba manual controlada.

## Inicio en Windows

Desde la carpeta del proyecto Spring Boot, activa el perfil local y ejecuta la aplicación:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'local'
.\mvnw.cmd spring-boot:run
```

El perfil local habilita la función y liga el servidor a `127.0.0.1`. El CLI debe estar instalado, autenticado y disponible como `codex.exe` en `PATH`. Se puede cambiar el ejecutable con `CODEX_COMMAND` y la carpeta de trabajo con `CODEX_AGENT_WORKSPACE_ROOT`; por defecto se usa la carpeta actual desde la que arranca Spring Boot. Esa ruta se fija en el servidor y nunca se acepta desde una petición web.

Fuera del perfil local, `codex.agent.enabled` permanece desactivado. No se debe activar esta función en Render: el conector local con conexión saliente autenticada hacia Render es un trabajo futuro.

## Agentes y reglas del proyecto

Cada tarea se inicia con instrucciones para leer `AGENTS.md`, `docs/base-standards.md`, `docs/backend-standards.md`, las skills aplicables y las definiciones de `.codex/agents/`. Codex decide si delegar subtareas y a cuáles de los seis roles configurados: `enzo_seguridad`, `rucau_arquitecto`, `jona_dev1`, `jesse_dev2`, `nahitan_devops` y `juli_qa`. El panel solo asigna estados a un avatar cuando los eventos identifican inequívocamente el rol.

## Aprobaciones

El modo predeterminado es manual. Se muestran el comando, la carpeta de trabajo o los cambios concretos que solicitan aprobación. La interfaz envía solo `accept` o `decline`; no ofrece una aprobación persistente por sesión. Si los detalles no bastan para revisar la acción, solo se puede rechazar.

Las altas, actualizaciones y eliminaciones de archivos se aprueban siempre si todas las rutas están dentro del worktree configurado y la solicitud no pide acceso de red ni permisos adicionales. Los cambios fuera del worktree, las solicitudes incompletas y las concesiones amplias siguen esperando una decisión manual. El modo automático también aprueba comandos que App Server clasifica íntegramente como lectura, listado de archivos o búsqueda, con directorio y rutas dentro del workspace. Los comandos desconocidos permanecen sujetos a aprobación manual.

## API y estados

La API está descrita en [`api-spec.yml`](api-spec.yml). El navegador obtiene el estado inicial por HTTP y recibe snapshots por SSE. Las tareas y sus aprobaciones están ligadas a la sesión que las creó. Los resúmenes enviados a la interfaz no contienen el prompt completo ni la salida del proceso.

Los estados de tarea incluyen `queued`, `working`, `waiting_approval`, `completed`, `failed`, `interrupted` y `disconnected`. Una conexión desconectada con el servicio habilitado significa que se puede iniciar una tarea nueva; no impide el primer envío.

## Pruebas y límites

Las pruebas usan un proceso App Server simulado y eventos JSON de ejemplo; no llaman a Codex real, no consumen uso de la cuenta y no requieren Internet. Ejecuta la verificación completa con:

```powershell
.\mvnw.cmd verify
```

El App Server está separado de Codex Desktop y el protocolo puede cambiar entre versiones del CLI. Antes de usarlo con una tarea real, hay que hacer una prueba manual local con aprobación manual, verificar el ejecutable y confirmar el flujo de eventos y cierre del proceso.
