# Backlog

Las prioridades son relativas: **P1 es la más alta**. Cada tarea sigue pendiente de planificación e implementación, salvo el estado verificado indicado abajo.

## Estado verificado

- [x] **Calidad base (30/10/2026):** `.\mvnw.cmd verify` terminó con BUILD SUCCESS; 42 pruebas sin fallos y JaCoCo con 87,17 % de cobertura (umbral: 80 %).
- [x] **TDD:** `docs/base-standards.md` ya exige escribir primero una prueba fallida, implementar lo mínimo y refactorizar.
- [x] **Widget visual de agentes:** los seis agentes pueden permanecer seleccionados; las tarjetas se apilan en un área con scroll vertical propio y sin scroll horizontal. El estado sigue estático (“Disponible”), sin eventos de ejecución conectados.
- [x] **Spring AI:** las dependencias ya están en `pom.xml`; el flujo actual todavía usa directamente `GroqClient`.

## Tareas

### P1 — Parche de seguridad de Spring Boot y Spring AI

**Estado:** cambios aplicados localmente; falta llevarlos a la rama conectada a Render y confirmar el despliegue.

- [x] Actualizar Spring Boot 4.1.1 → 4.1.2 y Spring AI BOM 2.0.1 → 2.0.2.
- [x] Ejecutar `.\mvnw.cmd -B -ntp verify`: BUILD SUCCESS, 42 pruebas sin fallos; JaCoCo reporta 87,17% (265/304 líneas) y el control de cobertura 87,21% (266/305; umbral 80%).
- [x] Confirmar en el JAR generado Tomcat embebido 11.0.27, versión administrada por Boot 4.1.2.
- [ ] En el checkout Git correspondiente, confirmar la rama conectada, crear/enviar el commit y revisar que Render termine el despliegue automático.

### P1 — Actividad real de agentes

**Descripción:** Reemplazar el estado simulado del widget por el estado real de agentes que corren en el Codex local del usuario, visible desde la aplicación publicada en Render.

**Primera decisión:** validar la existencia de una interfaz soportada para que la aplicación en Render se conecte a Codex Desktop para Windows y observe sus agentes. La documentación pública describe App Server como un proceso Codex al que se conecta una aplicación cliente; no documenta un endpoint público de Desktop para que un sistema externo lea las tareas abiertas en esa aplicación. No confundir iniciar un App Server/worker independiente con conectarse a la instancia de Desktop. Si no hay interfaz soportada, evaluar una integración oficial alternativa antes de diseñar un puente; evitar depender de bases de datos, sockets o archivos internos no documentados.

**Criterios de aceptación:**

- [ ] Se confirma con documentación o prueba reproducible si Codex Desktop expone una interfaz oficialmente soportada para clientes externos, con acceso a threads/eventos y agentes activos.
- [ ] Si existe esa interfaz, se valida desde la aplicación en Render y se documentan autorización, renovación de credenciales, facturación y aislamiento.
- [ ] Si no existe, se documenta la limitación y se decide explícitamente si se pospone la visualización o se adopta otra arquitectura aprobada (por ejemplo, tareas iniciadas por un cliente App Server propio); esa alternativa no se presenta como observación de la app Desktop.
- [ ] La integración elegida vincula cada ejecución con su agente, normaliza estados y permite distinguir servicio sin conexión de ausencia de tareas activas.
- [ ] Se normalizan estados como en cola, trabajando, esperando aprobación, completado, fallido, interrumpido y desconectado.
- [ ] Los eventos incluyen identificadores de ejecución/agente, estado, fecha y un resumen seguro, sin exponer credenciales, prompts completos ni salida sensible.
- [ ] La interfaz recibe el estado actual y actualizaciones en vivo; al reconectar puede recuperar el estado vigente.
- [ ] El widget conserva un estado claro cuando el servicio no está conectado o no hay ejecuciones activas.
- [ ] Las pruebas usan eventos y procesos simulados; no requieren Codex real, API keys ni Internet.

### P1 — Selección múltiple y scroll de agentes

**Estado:** completado y comprobado localmente con los seis agentes seleccionados.

- [x] Los seis botones permiten seleccionar agentes simultáneamente sin ocultar los demás.
- [x] Las tarjetas aparecen debajo de los botones y se desplazan dentro de un área vertical limitada.
- [x] El panel de apoyo queda separado del área desplazable y no se genera scroll horizontal.

### P2 — Pulido gráfico y responsive

**Descripción:** revisar el acabado visual del panel de agentes en distintos tamaños de pantalla, manteniendo la selección múltiple y el scroll ya implementados.

- [ ] Revisar espaciado, jerarquía visual y estados seleccionado/no seleccionado.
- [ ] Comprobar que controles, tarjetas y apoyo se entiendan y se usen bien en escritorio y móvil.
- [ ] Mantener visible el control de selección mientras se desplaza la lista de agentes.

### P3 — Arquitectura limpia, acotada

**Descripción:** Definir límites simples entre interfaz, lógica de aplicación, proveedores de IA, ejecución de agentes y persistencia. Evitar una refactorización amplia sin necesidad.

**Criterios de aceptación:**

- [ ] Se documentan los límites y dependencias antes de refactorizar.
- [ ] La interfaz web consume contratos de aplicación; Groq/Spring AI, Codex y Redis quedan detrás de adaptadores apropiados.
- [ ] El comportamiento público existente se conserva o se documentan los cambios.
- [ ] Las pruebas existentes pasan y se agregan pruebas para cada comportamiento modificado.

### P4 — Servicio de IA

**Descripción:** Separar la orquestación del chat de los clientes concretos de proveedores, coordinado con P3.

**Criterios de aceptación:**

- [ ] El chat solicita respuestas mediante un contrato claro `AiService`.
- [ ] Selección de modelo, contexto y errores tienen comportamiento documentado y probado.
- [ ] El fallback distingue errores recuperables y define si cambia modelo o proveedor.
- [ ] No se duplica la lógica entre `ChatService` y los adaptadores.
- [ ] Hay pruebas unitarias para selección, respuesta, respuesta vacía y fallos del proveedor.

### P5 — Spring AI y proveedores

**Descripción:** Decidir si Spring AI aporta valor y, si se adopta, usarlo detrás del contrato de P4. Preparar proveedores intercambiables y soporte futuro de Ollama.

**Criterios de aceptación:**

- [ ] Se documenta la decisión de migrar o conservar el cliente HTTP actual.
- [ ] Proveedor, modelo, URL y credenciales se configuran externamente.
- [ ] Se define selección dinámica y fallback entre modelos/proveedores; no se eligen proveedores solo por ser “gratuitos” sin revisar límites, disponibilidad y condiciones vigentes.
- [ ] Ollama puede incorporarse sin cambios en controladores ni lógica central.
- [ ] Las pruebas verifican solicitudes, respuestas y errores sin conectarse a proveedores reales.
- [ ] Credenciales y prompts completos no aparecen en logs.

### P6 — Redis y persistencia de conversaciones

**Descripción:** Revisar el almacenamiento actual en Redis antes de ampliar el historial y la lista de chats.

**Criterios de aceptación:**

- [ ] Se documentan formato, aislamiento por usuario/conversación y expiración.
- [ ] Se define el resultado observable ante fallos de lectura y escritura.
- [ ] Se evita reemplazar o perder mensajes silenciosamente.
- [ ] Se establecen límites razonables para tamaño y duración.
- [ ] Hay pruebas de lectura, escritura, expiración y fallos, sin Redis real.

### P7 — Historial de la conversación actual

**Descripción:** Recuperar y mostrar, al abrir o recargar, los mensajes de la conversación asociada a la sesión.

**Criterios de aceptación:**

- [ ] Se guardan y recuperan prompt, respuesta, rol, orden y fecha/hora.
- [ ] Al recargar se muestra el historial correcto de la sesión.
- [ ] Una conversación vacía muestra un estado adecuado.
- [ ] Los errores de recuperación no borran mensajes ya visibles.
- [ ] Se prueban endpoint/servicio y renderizado del historial.

### P8 — Lista y selección de conversaciones

**Descripción:** Crear conversaciones separadas y volver a seleccionar una anterior, una vez definido el contrato de persistencia de P6/P7.

**Criterios de aceptación:**

- [ ] El usuario puede iniciar un chat y volver a uno existente.
- [ ] La lista muestra título y actividad reciente del usuario actual.
- [ ] Cambiar de chat carga el historial correcto, sin mezclar conversaciones.
- [ ] Los identificadores no permiten acceder a datos de otra sesión/usuario.
- [ ] Hay estados visibles para lista vacía, carga, error y creación.

### P9 — Selectores de modelo y personalidad

**Descripción:** Permitir elegir explícitamente modelo y personalidad, además de la selección automática disponible en el backend.

**Criterios de aceptación:**

- [ ] La interfaz solo ofrece modelos habilitados y configurados.
- [ ] La personalidad elegida se traduce a instrucciones controladas por el servidor.
- [ ] Las selecciones se validan y se mantienen según una decisión documentada (sesión o conversación).
- [ ] El selector automático actual sigue disponible.
- [ ] Hay pruebas para opciones inválidas, selección y construcción del prompt.

### P10 — Prompts e idioma

**Descripción:** Mejorar la coherencia de prompts según idioma, personalidad y contexto conversacional.

**Criterios de aceptación:**

- [ ] Se documentan idiomas soportados y valor predeterminado.
- [ ] Se cubren mensajes cortos, ambiguos, vacíos y mezclados.
- [ ] La instrucción del modelo coincide con el idioma o preferencia seleccionada.
- [ ] Mensajes de interfaz y errores siguen una estrategia coherente.
- [ ] Hay pruebas unitarias para detección y pruebas de servicio para el prompt.

### P11 — Memoria y resumen automático

**Descripción:** Revisar el resumen existente y mejorar cómo conserva el contexto cuando crece la conversación.

**Criterios de aceptación:**

- [ ] Se documentan umbral, mensajes conservados y formato del resumen.
- [ ] El resumen conserva hechos útiles y no se duplica ni aparece como mensaje visible.
- [ ] Si falla la generación, se aplica una alternativa definida sin perder contexto silenciosamente.
- [ ] Hay pruebas de umbral, conservación, resumen inválido y fallo del proveedor.

### P12 — Evaluación de RAG, herramientas, agentes y MCP

**Descripción:** Evaluar casos concretos antes de agregar infraestructura de recuperación o ejecución de herramientas.

**Criterios de aceptación:**

- [ ] Cada capacidad tiene un caso de uso y datos/fuentes definidos.
- [ ] Se comparan complejidad, seguridad, costo y mantenimiento con la necesidad real.
- [ ] Se documenta una decisión de implementar, postergar o descartar cada capacidad.
- [ ] Si se implementa alguna, se crea una propuesta separada con límites y pruebas.

### P13 — Streaming de respuestas del chat (SSE)

**Descripción:** Enviar progresivamente la respuesta del modelo a la interfaz. Esta tarea es distinta del flujo SSE de actividad de agentes de P1.

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

1. P1: validar qué ejecuciones se observarán y elegir el camino de integración; luego conectar los eventos al widget.
2. P2: pulir la presentación gráfica y el comportamiento responsive del panel de agentes.
3. P3 → P4 → P5: límites de arquitectura, servicio de IA y decisión/integración de Spring AI.
4. P6 → P7 → P8: Redis, historial de la conversación actual y lista de conversaciones.
5. P9 → P10 → P11: selectores, prompts y memoria/resúmenes.
6. P12: evaluar RAG, herramientas, agentes y MCP mediante propuestas independientes.
7. P13: streaming del chat, después de definir el contrato de streaming del proveedor.
8. P14: mantener la documentación en cada cambio y completar la revisión general de Markdown.
