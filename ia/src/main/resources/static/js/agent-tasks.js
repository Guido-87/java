(function initLocalAgentTasks() {
    const dialog = document.getElementById("agentTaskDialog");
    const openButton = document.getElementById("openAgentTaskBtn");
    const closeButton = document.getElementById("closeAgentTaskBtn");
    const form = document.getElementById("agentTaskForm");
    const promptInput = document.getElementById("agentTaskPrompt");
    const approvalModeInput = document.getElementById("agentTaskApprovalMode");
    const submitButton = document.getElementById("submitAgentTaskBtn");
    const connectionStatus = document.getElementById("agentConnectionStatus");
    const progress = document.getElementById("agentTaskProgress");
    const taskStatus = document.getElementById("agentTaskStatus");
    const taskSummary = document.getElementById("agentTaskSummary");
    const taskResponse = document.getElementById("agentTaskResponse");
    const taskResponseText = document.getElementById("agentTaskResponseText");
    const taskMessage = document.getElementById("agentTaskMessage");
    const approvalPanel = document.getElementById("agentTaskApproval");
    const approvalSummary = document.getElementById("agentApprovalSummary");
    const approvalDetails = document.getElementById("agentApprovalDetails");
    const approveButton = document.getElementById("approveAgentActionBtn");
    const declineButton = document.getElementById("declineAgentActionBtn");
    const interruptButton = document.getElementById("interruptAgentTaskBtn");
    const agentCards = document.getElementById("agentCards");

    if (!dialog || !openButton || !form || !promptInput || !submitButton || !connectionStatus) return;

    const roleToAvatar = Object.freeze({
        enzo_seguridad: "enzo",
        rucau_arquitecto: "rucau",
        jona_dev1: "jona",
        jesse_dev2: "jesse",
        nahitan_devops: "nahitan",
        juli_qa: "juli"
    });
    const statusLabels = Object.freeze({
        queued: "En cola",
        working: "Trabajando",
        waiting_approval: "Esperando aprobación",
        completed: "Finalizada",
        failed: "Fallida",
        interrupted: "Interrumpida",
        disconnected: "Desconectada",
        available: "Disponible"
    });
    const terminalStatuses = new Set(["completed", "failed", "interrupted", "disconnected"]);
    const agentStatuses = new Map();
    let connection = "disconnected";
    let serviceEnabled = false;
    let activeTask = null;
    let responseTaskId = null;
    let eventStream = null;
    let approvalBusy = false;

    function labelForStatus(status) {
        return statusLabels[String(status || "").toLowerCase()] || "Disponible";
    }

    function showMessage(message, isError) {
        taskMessage.textContent = message || "";
        taskMessage.classList.toggle("is-error", Boolean(isError));
    }

    function updateConnectionStatus() {
        if (!serviceEnabled || connection === "disabled") {
            connectionStatus.textContent = "Codex local está deshabilitado en este entorno.";
        } else if (activeTask && !terminalStatuses.has(String(activeTask.status || "").toLowerCase())) {
            connectionStatus.textContent = "Codex local · " + labelForStatus(activeTask.status);
        } else if (connection === "connected") {
            connectionStatus.textContent = "Codex local conectado · sin tareas activas.";
        } else {
            connectionStatus.textContent = "Listo para empezar: Codex local se conectará cuando envíes una tarea.";
        }

        submitButton.disabled = !serviceEnabled
            || Boolean(activeTask && !terminalStatuses.has(String(activeTask.status || "").toLowerCase()));
        if (approvalModeInput) {
            approvalModeInput.disabled = Boolean(activeTask && !terminalStatuses.has(String(activeTask.status || "").toLowerCase()));
        }
        openButton.disabled = !serviceEnabled;
    }

    function setAgentStatuses(agents, taskId) {
        agentStatuses.clear();
        if (Array.isArray(agents)) {
            agents.forEach((agent) => {
                if (agent && typeof agent.id === "string" && roleToAvatar[agent.id]) {
                    agentStatuses.set(roleToAvatar[agent.id], String(agent.status || "available").toLowerCase());
                }
            });
        }
        if (typeof window.updateAgentTaskAgents === "function") window.updateAgentTaskAgents(agents, taskId);
        renderAgentCards();
    }

    function renderAgentCards() {
        if (!agentCards) return;
        agentCards.querySelectorAll(".agent-card").forEach((card) => {
            const image = card.querySelector(".agent-image");
            const statusElement = card.querySelector(".agent-status");
            if (!image || !statusElement) return;

            const match = image.getAttribute("src").match(/\/images\/agents\/([a-z]+)\.png(?:\?.*)?$/);
            const avatarId = match && match[1];
            const status = connection === "connected" || activeTask
                ? (agentStatuses.get(avatarId) || "available")
                : "disconnected";
            const dot = statusElement.querySelector(".status-dot");
            statusElement.dataset.status = status;
            statusElement.setAttribute("aria-label", "Estado: " + labelForStatus(status));
            if (dot) dot.dataset.status = status;

            const label = Array.from(statusElement.childNodes).find((node) => node.nodeType === Node.TEXT_NODE);
            if (!label) statusElement.appendChild(document.createTextNode(labelForStatus(status)));
            else if (label.textContent !== labelForStatus(status)) label.textContent = labelForStatus(status);
        });
    }

    function closeEventStream() {
        if (eventStream) {
            eventStream.close();
            eventStream = null;
        }
    }

    function safeApprovalDetails(details) {
        if (typeof details === "string") {
            const raw = details.trim();
            try {
                const parsed = JSON.parse(raw);
                if (Array.isArray(parsed)) {
                    return parsed.map((change) => {
                        if (!change || typeof change !== "object") return "";
                        const kind = typeof change.kind === "string" ? change.kind
                            : change.kind && typeof change.kind.type === "string" ? change.kind.type : "cambio";
                        const lines = ["Archivo: " + (change.path || "ruta desconocida") + " (" + kind + ")"];
                        const movePath = change.kind && typeof change.kind.move_path === "string" ? change.kind.move_path
                            : change.kind && typeof change.kind.movePath === "string" ? change.kind.movePath : "";
                        if (movePath) lines.push("Destino: " + movePath);
                        const diff = typeof change.diff === "string" ? change.diff : change.unified_diff;
                        if (typeof diff === "string" && diff.trim()) lines.push(diff.trim());
                        return lines.join("\n");
                    }).filter(Boolean).join("\n\n");
                }
            } catch {
                // Non-JSON details such as a command are shown as supplied.
            }
            return raw;
        }
        if (!details || typeof details !== "object") return "";
        const lines = [];
        const command = Array.isArray(details.command) ? details.command.join(" ") : details.command;
        if (typeof command === "string" && command.trim()) lines.push("Comando: " + command);
        if (typeof details.cwd === "string" && details.cwd.trim()) lines.push("Carpeta: " + details.cwd);
        const changes = Array.isArray(details.changes) ? details.changes : [];
        changes.forEach((change) => {
            if (change && typeof change.path === "string") {
                const kind = typeof change.kind === "string" ? " (" + change.kind + ")" : "";
                lines.push("Archivo: " + change.path + kind);
            }
        });
        return lines.join("\n");
    }

    function canAcceptApproval(approval) {
        if (!approval) return false;
        const hasDetails = Boolean(safeApprovalDetails(approval.details));
        return hasDetails && (typeof approval.canAccept !== "boolean" || approval.canAccept);
    }

    function renderSnapshot(snapshot) {
        if (!snapshot || typeof snapshot !== "object") return;
        if (snapshot.taskId !== responseTaskId) {
            responseTaskId = snapshot.taskId || null;
            if (taskResponseText) taskResponseText.textContent = "";
            if (taskResponse) taskResponse.hidden = true;
        }
        activeTask = snapshot;
        if (approvalModeInput && ["manual", "automatic"].includes(snapshot.approvalMode)) {
            approvalModeInput.value = snapshot.approvalMode;
        }
        progress.hidden = false;
        taskStatus.textContent = labelForStatus(snapshot.status);
        taskSummary.textContent = typeof snapshot.summary === "string" ? snapshot.summary : "";
        if (typeof snapshot.response === "string" && snapshot.response.trim() && taskResponse && taskResponseText) {
            taskResponseText.textContent = snapshot.response;
            taskResponse.hidden = false;
        }
        const approval = snapshot.approval;
        approvalPanel.hidden = !approval;
        const detailText = safeApprovalDetails(approval && approval.details);
        const canAccept = canAcceptApproval(approval) && Boolean(detailText);
        approvalSummary.textContent = approval && !canAccept
            ? "Faltan detalles para revisar esta solicitud. Rechazala y volvé a enviar una solicitud completa."
            : approval && typeof approval.summary === "string"
            ? approval.summary
            : "Revisá la solicitud y elegí si querés permitirla.";
        approvalDetails.textContent = detailText;
        approvalDetails.hidden = !detailText;
        approveButton.disabled = !canAccept || approvalBusy;
        declineButton.disabled = !approval || approvalBusy;
        interruptButton.hidden = terminalStatuses.has(String(snapshot.status || "").toLowerCase());
        setAgentStatuses(snapshot.agents, snapshot.taskId);
        updateConnectionStatus();

        if (terminalStatuses.has(String(snapshot.status || "").toLowerCase())) closeEventStream();
    }

    function subscribeToTask(taskId) {
        if (!taskId || !window.EventSource) return;
        closeEventStream();
        eventStream = new EventSource("/api/agent-tasks/" + encodeURIComponent(taskId) + "/events", {
            withCredentials: true
        });
        const applyEvent = (event) => {
            if (!activeTask || activeTask.taskId !== taskId) return;
            try {
                renderSnapshot(JSON.parse(event.data));
                showMessage("");
            } catch {
                showMessage("No se pudo leer la actualización de la tarea.", true);
            }
        };
        eventStream.addEventListener("snapshot", applyEvent);
        eventStream.onmessage = applyEvent;
        eventStream.onerror = () => {
            if (!activeTask || activeTask.taskId !== taskId) return;
            showMessage("Se interrumpió la conexión en vivo. Intentando reconectar…", false);
        };
    }

    async function readJson(response) {
        try {
            return await response.json();
        } catch {
            return {};
        }
    }

    async function refreshStatus() {
        try {
            const response = await fetch("/api/agent-tasks/status", {
                method: "GET",
                credentials: "include",
                headers: { "Accept": "application/json" }
            });
            const data = await readJson(response);
            if (!response.ok) throw new Error(data.message || "No se pudo comprobar la conexión local.");

            connection = String(data.connection || (data.enabled ? "connected" : "disabled")).toLowerCase();
            serviceEnabled = Boolean(data.enabled);
            setAgentStatuses(data.agents, data.activeTask && data.activeTask.taskId);
            if (data.activeTask) {
                renderSnapshot(data.activeTask);
                subscribeToTask(data.activeTask.taskId);
            } else {
                closeEventStream();
                activeTask = null;
                progress.hidden = true;
                updateConnectionStatus();
            }
        } catch {
            closeEventStream();
            connection = "disconnected";
            serviceEnabled = false;
            activeTask = null;
            setAgentStatuses([]);
            progress.hidden = true;
            updateConnectionStatus();
        }
    }

    async function submitTask(event) {
        event.preventDefault();
        const prompt = promptInput.value.trim();
        if (!prompt || submitButton.disabled) return;
        const approvalMode = approvalModeInput && approvalModeInput.value === "automatic" ? "automatic" : "manual";

        submitButton.disabled = true;
        showMessage("Enviando tarea…", false);
        try {
            const response = await fetch("/api/agent-tasks", {
                method: "POST",
                credentials: "include",
                headers: { "Content-Type": "application/json", "Accept": "application/json" },
                body: JSON.stringify({
                    prompt,
                    approvalMode: approvalModeInput ? approvalModeInput.value : "manual"
                })
            });
            const data = await readJson(response);
            if (!response.ok) throw new Error(data.message || "No se pudo iniciar la tarea local.");

            promptInput.value = "";
            data.approvalMode = data.approvalMode || approvalMode;
            showMessage("");
            renderSnapshot(data);
            subscribeToTask(data.taskId);
        } catch (error) {
            showMessage(error.message || "No se pudo iniciar la tarea local.", true);
        } finally {
            updateConnectionStatus();
        }
    }

    async function answerApproval(decision) {
        if (!activeTask || !activeTask.approval || approvalBusy) return;
        const approvalId = activeTask.approval.id;
        approvalBusy = true;
        approveButton.disabled = true;
        declineButton.disabled = true;
        showMessage(decision === "accept" ? "Aprobación enviada." : "Solicitud rechazada.", false);
        try {
            const response = await fetch(
                "/api/agent-tasks/" + encodeURIComponent(activeTask.taskId)
                    + "/approvals/" + encodeURIComponent(String(approvalId)),
                {
                    method: "POST",
                    credentials: "include",
                    headers: { "Content-Type": "application/json", "Accept": "application/json" },
                    body: JSON.stringify({ decision })
                }
            );
            const data = await readJson(response);
            if (!response.ok) throw new Error(data.message || "No se pudo enviar la decisión.");
            if (data.taskId) renderSnapshot(data);
        } catch (error) {
            showMessage(error.message || "No se pudo enviar la decisión.", true);
            await refreshTaskSnapshot(activeTask && activeTask.taskId);
        } finally {
            approvalBusy = false;
            if (activeTask && activeTask.approval) {
                approveButton.disabled = !canAcceptApproval(activeTask.approval);
                declineButton.disabled = false;
            }
        }
    }

    async function refreshTaskSnapshot(taskId) {
        if (!taskId) return;
        try {
            const response = await fetch("/api/agent-tasks/" + encodeURIComponent(taskId), {
                method: "GET",
                credentials: "include",
                headers: { "Accept": "application/json" }
            });
            const data = await readJson(response);
            if (response.ok && data.taskId) renderSnapshot(data);
            else await refreshStatus();
        } catch {
            await refreshStatus();
        }
    }

    async function interruptTask() {
        if (!activeTask || !activeTask.taskId) return;
        const taskId = activeTask.taskId;
        let taskNotFound = false;
        interruptButton.disabled = true;
        showMessage("Interrumpiendo tarea…", false);
        try {
            const response = await fetch(
                "/api/agent-tasks/" + encodeURIComponent(taskId) + "/interrupt",
                { method: "POST", credentials: "include", headers: { "Accept": "application/json" } }
            );
            const data = await readJson(response);
            if (!response.ok) {
                taskNotFound = response.status === 404;
                throw new Error(data.message || "No se pudo interrumpir la tarea.");
            }
            if (data.taskId) renderSnapshot(data);
        } catch (error) {
            if (taskNotFound) {
                closeEventStream();
                await refreshStatus();
                const anotherTaskIsActive = activeTask && !terminalStatuses.has(String(activeTask.status || "").toLowerCase());
                if (!serviceEnabled) {
                    showMessage("No se pudo sincronizar con el servidor local. Revisá la conexión antes de enviar otra tarea.", true);
                } else if (anotherTaskIsActive) {
                    showMessage("La tarea que intentaste interrumpir ya no existe. Se actualizó el estado de la tarea activa.", false);
                } else {
                    showMessage("La tarea anterior ya no existe. Se actualizó el estado y podés enviar otra.", false);
                }
            } else {
                showMessage(error.message || "No se pudo interrumpir la tarea.", true);
            }
        } finally {
            interruptButton.disabled = false;
        }
    }

    openButton.addEventListener("click", () => {
        if (!dialog.open) dialog.showModal();
        promptInput.focus();
    });
    closeButton.addEventListener("click", () => dialog.close());
    form.addEventListener("submit", (event) => void submitTask(event));
    approveButton.addEventListener("click", () => void answerApproval("accept"));
    declineButton.addEventListener("click", () => void answerApproval("decline"));
    interruptButton.addEventListener("click", () => void interruptTask());

    if (agentCards) new MutationObserver(renderAgentCards).observe(agentCards, { childList: true, subtree: true });
    void refreshStatus();
})();
