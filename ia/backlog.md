# Backlog

Las prioridades son relativas: **P1 es la más alta**. Las tareas marcadas se completaron con la evidencia indicada; las demás siguen pendientes.

## Estado verificado

- [x] **Calidad base (30/10/2026):** `.\mvnw.cmd verify` terminó con BUILD SUCCESS; 42 pruebas sin fallos y JaCoCo con 87,17 % de cobertura (umbral: 80 %).
- [x] **TDD:** `docs/base-standards.md` ya exige escribir primero una prueba fallida, implementar lo mínimo y refactorizar.
- [x] **Widget visual de agentes:** los seis agentes pueden permanecer seleccionados; las tarjetas se apilan en un área con scroll vertical propio y sin scroll horizontal. El estado sigue estático (“Disponible”), sin eventos de ejecución conectados.
- [x] **Spring AI:** las dependencias ya están en `pom.xml`; el flujo actual todavía usa directamente `GroqClient`.
- [x] **Actualización de seguridad y despliegue (01/11/2026):** PR #6 integrado en `main`; commit `d6203e4` desplegado con estado **Live** en Render `chat-ia`. La versión también pasó por staging en `chat-ia-dev`.
- [x] **Funcionalidades base** (marcadas completas en la captura del chat fijado): idioma, enlace de donación, mejoras de código y manejo de errores. El código actual incluye detección de idioma, enlace a MercadoPago y manejo global de errores.
- [x] **Tests de chat:** existen `ChatControllerTest` y `ChatServiceTest`, además de `ChatIntegrationTest`; seguir manteniéndolos independientes de proveedores reales.

## Tareas

### Completado — Parche de seguridad de Spring Boot y Spring AI

**Estado:** integrado en `main` y desplegado en producción mediante el PR #6.

- [x] Actualizar Spring Boot 4.1.1 → 4.1.2 y Spring AI BOM 2.0.1 → 2.0.2.
- [x] Ejecutar `.\mvnw.cmd -B -ntp verify`: BUILD SUCCESS, 42 pruebas sin fallos; JaCoCo reporta 87,17% (265/304 líneas) y el control de cobertura 87,21% (266/305; umbral 80%).
- [x] Confirmar en el JAR generado Tomcat embebido 11.0.27, versión administrada por Boot 4.1.2.
- [x] Confirmar el checkout Git y la rama conectada a staging: `feature/agentic-programming`.
- [x] Crear/enviar el commit de los cambios: `1a86a3c` (`Improve multi-agent widget and update project backlog`).
- [x] Confirmar el despliegue automático de Render en `chat-ia-dev`: estado **Live** para el commit `1a86a3c`.
- [x] Integrar el PR #6 en `main` y confirmar producción **Live** para el merge `d6203e4`.

### P1 — Actividad real de agentes

**Descripción:** Reemplazar el estado simulado del widget por actividad real de Codex. La distinción clave es si las tareas se inician a través del proceso App Server que controla la integración o si se pretende observar tareas iniciadas independientemente en la interfaz Codex Desktop.

**Investigación (02/11/2026):** App Server ofrece transporte `stdio` (documentado) y streaming de eventos de threads/turns/items para el cliente que inicia o reanuda esas tareas. El transporte WebSocket TCP está marcado por la documentación oficial como experimental y no soportado. No se encontró un contrato público documentado para que un cliente externo se conecte al proceso interno de Codex Desktop y observe cualquier tarea iniciada desde la interfaz. Por lo tanto, iniciar un App Server separado permite monitorear tareas que pasen por ese proceso; no equivale a observar todo lo que ocurre en Desktop. El CLI `codex` está disponible en este equipo (`0.166.0-alpha.5.1`), pero el daemon local no está corriendo.

**Decisión inicial (02/11/2026):** implementar primero la integración local. El backend de localhost inicia App Server por `stdio`, traduce sus notificaciones y las publica a la interfaz por SSE. El widget muestra solo ejecuciones iniciadas por esta integración; no se presenta como lectura de tareas preexistentes de Codex Desktop. La conexión de un conector local con Render queda fuera de esta etapa y requiere una propuesta de seguridad y despliegue separada.

**Criterios de aceptación:**

- [x] Se revisa la documentación oficial: App Server expone eventos al cliente conectado, pero no documenta acceso externo al proceso interno de Desktop; WebSocket TCP está marcado experimental/no soportado.
- [x] El prototipo local inicia App Server por `stdio`, mapea eventos de threads/turns/items y los publica al widget por SSE. La cobertura automatizada usa proceso/eventos simulados; la prueba manual con Codex real sigue pendiente.
- [ ] Si se elige Render, se implementa un conector local con conexión saliente TLS, autenticación por dispositivo, reconexión/heartbeat y aislamiento; no se expone el App Server local directamente a Internet.
- [x] Se decide que el widget muestre solo ejecuciones iniciadas desde esta integración local; no observa tareas independientes de Desktop.
- [x] La integración vincula ejecuciones con roles conocidos, normaliza estados y distingue que el servicio esté habilitado pero aún desconectado.
- [x] Se normalizan estados como en cola, trabajando, esperando aprobación, completado, fallido, interrumpido y desconectado.
- [x] Los eventos incluyen identificadores de ejecución/agente, estado, fecha y un resumen seguro, sin exponer credenciales, prompts completos ni salida sensible.
- [x] La interfaz recibe el estado actual y actualizaciones en vivo; al reconectar puede recuperar el snapshot vigente.
- [x] El widget permite iniciar una tarea cuando el servicio está habilitado aunque todavía no exista un proceso App Server conectado.
- [x] Las pruebas usan eventos y procesos simulados; no requieren Codex real, API keys ni Internet.

### Completado — Selección múltiple y scroll de agentes

**Estado:** completado y comprobado localmente con los seis agentes seleccionados.

- [x] Los seis botones permiten seleccionar agentes simultáneamente sin ocultar los demás.
- [x] Las tarjetas aparecen debajo de los botones y se desplazan dentro de un área vertical limitada.
- [x] El panel de apoyo queda separado del área desplazable y no se genera scroll horizontal.

### P2 — Pulido gráfico y responsive

**Descripción:** revisar el acabado visual del panel de agentes en distintos tamaños de pantalla, manteniendo la selección múltiple y el scroll ya implementados.

- [ ] Revisar espaciado, jerarquía visual y estados seleccionado/no seleccionado.
- [ ] Comprobar que controles, tarjetas y apoyo se entiendan y se usen bien en escritorio y móvil.
- [ ] Mantener visible el control de selección mientras se desplaza la lista de agentes.

### P3 — Historial de la conversación actual

**Descripción:** Recuperar y mostrar, al abrir o recargar, los mensajes de la conversación asociada a la sesión. Primera entrega acotada: guardar y mostrar los últimos mensajes con Redis existente, sin introducir H2.

**Criterios de aceptación:**

- [ ] Se guardan y recuperan prompt, respuesta, rol, orden y fecha/hora.
- [ ] Al recargar se muestra el historial correcto de la sesión; la primera versión muestra los últimos 10 mensajes.
- [ ] Una conversación vacía muestra un estado adecuado y la lista permite desplazamiento si hace falta.
- [ ] Los errores de recuperación no borran mensajes ya visibles.
- [ ] Se prueban endpoint/servicio y renderizado del historial, sin Redis real ni proveedor de IA real.

### P4 — Lista y selección de conversaciones

**Descripción:** Crear conversaciones separadas y volver a seleccionar una anterior, una vez resuelto el historial de P3.

**Criterios de aceptación:**

- [ ] El usuario puede iniciar un chat y volver a uno existente.
- [ ] La lista muestra título y actividad reciente del usuario actual.
- [ ] Cambiar de chat carga el historial correcto, sin mezclar conversaciones.
- [ ] Los identificadores no permiten acceder a datos de otra sesión/usuario.
- [ ] Hay estados visibles para lista vacía, carga, error y creación.

### P5 — Redis y persistencia de conversaciones

**Descripción:** Mantener Redis como almacenamiento ligero para el alcance actual y consolidar su contrato después del MVP de historial/lista, antes de ampliarlo. No agregar H2 por ahora; reconsiderar una base relacional solo si aparecen necesidades de persistencia permanente o consultas relacionales.

**Criterios de aceptación:**

- [ ] Se documentan formato, aislamiento por usuario/conversación y expiración.
- [ ] Se define el resultado observable ante fallos de lectura y escritura.
- [ ] Se evita reemplazar o perder mensajes silenciosamente.
- [ ] Se establecen límites razonables para tamaño y duración.
- [ ] Hay pruebas de lectura, escritura, expiración y fallos, sin Redis real.

### P6 — Memoria y resumen automático

**Descripción:** Revisar el resumen existente y mejorar cómo conserva el contexto cuando crece la conversación; depende del historial disponible.

**Criterios de aceptación:**

- [ ] Se documentan umbral, mensajes conservados y formato del resumen.
- [ ] El resumen conserva hechos útiles y no se duplica ni aparece como mensaje visible.
- [ ] Si falla la generación, se aplica una alternativa definida sin perder contexto silenciosamente.
- [ ] Hay pruebas de umbral, conservación, resumen inválido y fallo del proveedor.

### P7 — Servicio de IA

**Descripción:** Separar la orquestación del chat de los clientes concretos de proveedores; dejar el contrato preparado para selección y fallback.

**Criterios de aceptación:**

- [ ] El chat solicita respuestas mediante un contrato claro `AiService`.
- [ ] Selección de modelo, contexto y errores tienen comportamiento documentado y probado.
- [ ] El fallback distingue errores recuperables y define si cambia modelo o proveedor.
- [ ] No se duplica la lógica entre `ChatService` y los adaptadores.
- [ ] Hay pruebas unitarias para selección, respuesta, respuesta vacía y fallos del proveedor.

### P8 — Spring AI y proveedores

**Descripción:** Decidir si Spring AI aporta valor y, si se adopta, usarlo detrás del contrato de P7. Preparar proveedores intercambiables y soporte futuro de Ollama.

**Criterios de aceptación:**

- [ ] Se compara documentadamente mantener `WebClient` con adoptar Spring AI; no migrar solo por tener la dependencia.
- [ ] Se define un contrato de proveedor, por ejemplo `AIClient.completeChat(messages, model)`, y adaptadores para Groq y futuros proveedores.
- [ ] Strategy/registro selecciona el cliente según proveedor/modelo; Factory solo se agrega si simplifica esa creación/selección.
- [ ] Proveedor, modelo, URL y credenciales se configuran externamente.
- [ ] Se define selección dinámica y fallback entre modelos/proveedores; priorizar Groq y otros proveedores sin costo inicial verificable, sin incorporar OpenAI/ChatGPT en este alcance. Revisar límites, disponibilidad y condiciones vigentes; no asumir que un plan gratuito es permanente.
- [ ] Ollama puede incorporarse sin cambios en controladores ni lógica central.
- [ ] Las pruebas verifican solicitudes, respuestas y errores sin conectarse a proveedores reales; credenciales y prompts completos no aparecen en logs.

### P9 — Arquitectura limpia, acotada

**Descripción:** Definir límites simples entre interfaz, lógica de aplicación, proveedores de IA, ejecución de agentes y persistencia. Evitar una refactorización amplia sin necesidad; hacerla después de validar los flujos de usuario prioritarios.

**Criterios de aceptación:**

- [ ] Se documentan los límites y dependencias antes de refactorizar.
- [ ] La interfaz web consume contratos de aplicación; Groq/Spring AI, Codex y Redis quedan detrás de adaptadores apropiados.
- [ ] El comportamiento público existente se conserva o se documentan los cambios.
- [ ] Las pruebas existentes pasan y se agregan pruebas para cada comportamiento modificado.

### P10 — Selectores de modelo y personalidad

**Descripción:** Permitir elegir explícitamente modelo y personalidad, además de la selección automática disponible en el backend.

**Criterios de aceptación:**

- [ ] La interfaz solo ofrece modelos habilitados y configurados.
- [ ] La personalidad elegida se traduce a instrucciones controladas por el servidor.
- [ ] Las selecciones se validan y se mantienen según una decisión documentada (sesión o conversación).
- [ ] El selector automático actual sigue disponible.
- [ ] Hay pruebas para opciones inválidas, selección y construcción del prompt.

### P11 — Prompts e idioma

**Descripción:** Mejorar la coherencia de prompts según idioma, personalidad y contexto conversacional.

**Criterios de aceptación:**

- [ ] Se documentan idiomas soportados y valor predeterminado.
- [ ] Se cubren mensajes cortos, ambiguos, vacíos y mezclados.
- [ ] La instrucción del modelo coincide con el idioma o preferencia seleccionada.
- [ ] Mensajes de interfaz y errores siguen una estrategia coherente.
- [ ] Hay pruebas unitarias para detección y pruebas de servicio para el prompt.

### P12 — Evaluación de RAG, herramientas, agentes y MCP

**Descripción:** Evaluar casos concretos antes de agregar infraestructura de recuperación o ejecución de herramientas.

**Criterios de aceptación:**

- [ ] Cada capacidad tiene un caso de uso y datos/fuentes definidos.
- [ ] Se comparan complejidad, seguridad, costo y mantenimiento con la necesidad real.
- [ ] Se documenta una decisión de implementar, postergar o descartar cada capacidad.
- [ ] Si se implementa alguna, se crea una propuesta separada con límites y pruebas.

### P13 — Streaming de respuestas del chat (SSE)

**Descripción:** Enviar progresivamente la respuesta del modelo a la interfaz. Esta tarea es distinta del flujo SSE de actividad de agentes de P2.

**Criterios de aceptación:**

- [ ] El servidor transmite fragmentos en cuanto los recibe del proveedor.
- [ ] El cliente muestra fragmentos en orden y conserva el historial final una sola vez.
- [ ] Se definen finalización, error, cancelación y desconexión.
- [ ] Hay pruebas de flujo normal, error y desconexión con proveedor simulado.

### P14 — Documentación y metodología

**Descripción:** Mantener los Markdown de configuración e instrucciones alineados con la arquitectura y decisiones reales.

**Criterios de aceptación:**

- [ ] Se documentan arquitectura y decisiones (incluida la integración de agentes y proveedores).
- [ ] `AGENTS.md`, estándares y `openspec/config.yaml` describen el estado actual.
- [ ] TDD continúa explícito en las instrucciones; no se duplica innecesariamente.
- [ ] README o guía de desarrollo apunta al backlog y explica cómo ejecutar `verify`.

## Dependencias y orden sugerido

1. P1: validar la integración de actividad real de agentes con Codex Desktop y definir una alternativa soportada si no existe una interfaz accesible.
2. P2: pulir la presentación gráfica y el comportamiento responsive del panel de agentes.
3. P3 → P4: historial mínimo y navegación entre conversaciones.
4. P5: endurecer el contrato de Redis usado por P3 y P4; mantener Redis y postergar H2.
5. P6: resumen automático sobre un historial confiable.
6. P7 → P8: abstraer la orquestación y decidir el uso de Spring AI/proveedores.
7. P9: refactorización arquitectónica acotada después de validar los flujos prioritarios.
8. P10 → P11: selectores, prompts e idioma.
9. P12: evaluar RAG, herramientas, agentes y MCP mediante propuestas independientes.
10. P13: streaming del chat, después de definir el contrato de streaming del proveedor.
11. P14: mantener la documentación en cada cambio y completar la revisión general de Markdown.
