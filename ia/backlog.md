# Backlog

Las prioridades son relativas entre estas tareas: **1 es la más alta**. Cada elemento sigue pendiente de planificación detallada y no implica que se haya iniciado su implementación.

## Tareas

### P1 — Arquitectura limpia

**Descripción:** Evaluar y aplicar una separación gradual de responsabilidades entre la lógica de aplicación, el dominio del chat y los detalles de infraestructura (proveedor de IA y Redis). Mantener la solución pequeña y compatible con el proyecto actual.

**Criterios de aceptación:**

- [ ] Se documentan los límites propuestos y las dependencias entre componentes antes de refactorizar.
- [ ] Controladores, servicios de aplicación, proveedor de IA y persistencia tienen responsabilidades identificables.
- [ ] Los detalles de Groq/Spring AI y Redis no se filtran a la lógica central del chat.
- [ ] El comportamiento público existente se conserva o los cambios quedan documentados.
- [ ] Las pruebas existentes pasan y se agregan pruebas para cualquier comportamiento modificado.

### P2 — AI Service

**Descripción:** Consolidar la orquestación de prompts, selección de modelo, respuestas y errores en un servicio de IA con responsabilidades claras. Coordinar su diseño con la tarea de Arquitectura limpia.

**Criterios de aceptación:**

- [ ] La aplicación puede solicitar una respuesta de IA a través de una interfaz clara.
- [ ] La construcción de contexto y la selección de modelo tienen comportamiento documentado y probado.
- [ ] Los errores y respuestas vacías del proveedor se manejan de forma consistente sin exponer detalles sensibles al usuario.
- [ ] La lógica no queda duplicada entre `ChatService` y el cliente del proveedor.
- [ ] Hay pruebas unitarias para selección de modelo, respuesta correcta y fallos del proveedor.

### P3 — Spring AI

**Descripción:** Evaluar y, si aporta valor, integrar Spring AI para desacoplar la aplicación del protocolo específico del proveedor y facilitar el uso de modelos. El proyecto ya incluye dependencias de Spring AI, pero actualmente el cliente de Groq implementa llamadas HTTP propias.

**Criterios de aceptación:**

- [ ] Se documenta la decisión de migrar o conservar el cliente actual, con impacto en dependencias y capacidades.
- [ ] Si se migra, el proveedor, modelo, URL y credenciales se configuran externamente.
- [ ] La integración soporta las funciones requeridas por el chat, incluyendo contexto y manejo de errores.
- [ ] Las credenciales y prompts no se escriben en logs.
- [ ] Hay pruebas que verifican solicitudes, respuestas y errores sin depender de llamadas reales al proveedor.

### P4 — Redis

**Descripción:** Revisar y completar el almacenamiento de conversaciones en Redis. Actualmente se guarda una conversación por sesión con clave `chat:{userId}` y TTL de una hora.

**Criterios de aceptación:**

- [ ] El formato de almacenamiento, aislamiento por usuario/conversación y expiración están documentados.
- [ ] Se define el comportamiento observable ante fallos de lectura y escritura de Redis.
- [ ] Se evita perder o reemplazar silenciosamente mensajes ante fallos de persistencia.
- [ ] Se establecen límites razonables para el tamaño y la duración de los datos almacenados.
- [ ] Hay pruebas para lectura, escritura, expiración y fallos de Redis.

### P5 — Historial

**Descripción:** Permitir recuperar y mostrar los mensajes de la conversación actual al abrir o recargar la página. Depende de acordar el contrato de persistencia de Redis.

**Criterios de aceptación:**

- [ ] Al recargar la aplicación se muestra el historial asociado a la sesión actual.
- [ ] Los mensajes aparecen en orden y con el rol visual correcto.
- [ ] Una conversación nueva o sin historial muestra un estado vacío adecuado.
- [ ] Los errores al recuperar el historial se comunican sin borrar mensajes ya visibles.
- [ ] Se prueba el endpoint o flujo de recuperación y el renderizado del historial.

### P6 — Lista de chats

**Descripción:** Permitir crear, listar y seleccionar conversaciones separadas en la interfaz. Antes de implementar, definir cómo se identifican, nombran, ordenan y aíslan las conversaciones.

**Criterios de aceptación:**

- [ ] Un usuario puede iniciar una conversación nueva y volver a una conversación existente.
- [ ] La lista muestra conversaciones del usuario actual, con título y actividad reciente.
- [ ] Cambiar de conversación carga el historial correcto sin mezclar mensajes.
- [ ] Los identificadores de conversación no permiten acceder a datos de otra sesión/usuario.
- [ ] Los casos de lista vacía, errores de carga y creación de chat tienen estados visibles.

### P7 — SSE

**Descripción:** Entregar la respuesta del modelo progresivamente mediante Server-Sent Events, desde el proveedor hasta la interfaz, en lugar de esperar la respuesta completa.

**Criterios de aceptación:**

- [ ] El servidor transmite fragmentos de respuesta a medida que llegan del proveedor.
- [ ] El cliente muestra los fragmentos progresivamente y mantiene el orden correcto.
- [ ] Se define el comportamiento de finalización, error, cancelación y desconexión del cliente.
- [ ] El historial almacena la respuesta final completa una sola vez.
- [ ] Hay pruebas de streaming normal, error durante el flujo y desconexión.

### P8 — Resumen

**Descripción:** Mejorar el resumen automático del contexto cuando la conversación crece. Actualmente `ChatService` genera un resumen al superar el límite de mensajes; se debe verificar y mejorar su conservación y tolerancia a fallos.

**Criterios de aceptación:**

- [ ] La regla que determina cuándo resumir y qué mensajes conservar está documentada.
- [ ] El resumen conserva hechos y contexto necesarios para responder continuaciones.
- [ ] Los resúmenes no se duplican ni se envían como mensajes visibles del usuario.
- [ ] Un fallo al resumir tiene una alternativa definida y no provoca pérdida silenciosa de contexto.
- [ ] Hay pruebas para el umbral, la conservación de mensajes y el fallo del proveedor.

### P9 — Idioma

**Descripción:** Mejorar la detección del idioma del usuario y mantener respuestas coherentes con ese idioma. La detección actual es una heurística simple para inglés y español.

**Criterios de aceptación:**

- [ ] Se documentan los idiomas soportados y el idioma predeterminado.
- [ ] Se cubren mensajes cortos, ambiguos, vacíos y con mezcla de idiomas.
- [ ] La instrucción enviada al modelo coincide con el idioma detectado o con la preferencia configurada.
- [ ] Los mensajes de interfaz y error mantienen una estrategia de idioma coherente.
- [ ] Hay pruebas unitarias para detección y pruebas de servicio para la instrucción al modelo.

## Dependencias y secuencia sugerida

Seguir el orden de prioridad salvo que la planificación de una tarea revele una dependencia concreta:

1. Arquitectura limpia → AI Service → evaluación de Spring AI.
2. Redis → Historial → Lista de chats.
3. Resumen e Idioma.
4. SSE, después de definir el contrato del servicio de IA.

Las prioridades pueden revisarse al convertir cada elemento en una propuesta OpenSpec con alcance y diseño acordados.
