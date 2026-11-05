function initChat() {
    console.log('Initializing chat...');

    const input = document.getElementById("input");
    const button = document.getElementById("sendBtn");
    const newChatButton = document.getElementById("newChatBtn");
    const messages = document.getElementById("messages");
    const agentCards = document.getElementById("agentCards");
    const agentButtons = document.querySelectorAll(".agent-toggle");

    if (!input || !button || !newChatButton || !messages) {
        console.error("Required page elements are missing");
        window.chatAppLoaded = false;
        throw new Error("UI failed to initialize");
    }

    function addMessage(text, type) {
        const msg = document.createElement("div");
        msg.className = "message " + type;
        msg.innerText = text;

        messages.appendChild(msg);
        messages.scrollTop = messages.scrollHeight;
    }

    async function send() {
        const prompt = input.value.trim();
        if (!prompt) return;

        addMessage(prompt, "user");

        const loadingMsg = document.createElement("div");
        loadingMsg.className = "message bot";
        loadingMsg.innerHTML = '<div class="spinner"></div> Pensando…';

        messages.appendChild(loadingMsg);

        input.value = "";
        input.disabled = true;
        button.disabled = true;

        try {
            const response = await fetch("/api/chat", {
                method: "POST",
                credentials: "include",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ prompt })
            });

            let data = {};
            try {
                data = await response.json();
            } catch {
                // The response does not contain valid JSON.
            }

            loadingMsg.remove();

            if (!response.ok) {
                let errorMessage;

                if (response.status === 400) {
                    errorMessage = "El mensaje no es válido. Revisá lo que escribiste.";
                } else if (response.status === 503) {
                    errorMessage = "El servicio no está disponible por el momento. Probá de nuevo en unos segundos.";
                } else if (response.status >= 500) {
                    errorMessage = "Ocurrió un error en el servidor. Intentá de nuevo más tarde.";
                } else {
                    errorMessage = data.message || "No se pudo procesar el mensaje.";
                }

                addMessage("⚠️ " + errorMessage, "bot");
                return;
            }

            addMessage(data.response, "bot");

        } catch (e) {
            console.error("Error sending message:", e);
            loadingMsg.remove();
            addMessage("⚠️ No se pudo conectar con el servidor. Revisá tu conexión e intentá de nuevo.", "bot");
        } finally {
            input.disabled = false;
            button.disabled = !input.value.trim();
            input.focus();
        }
    }

    async function startNewChat() {
        newChatButton.disabled = true;
        try {
            const response = await fetch("/api/chat/new", {
                method: "POST",
                credentials: "include"
            });
            if (!response.ok) {
                throw new Error("No se pudo iniciar un chat nuevo.");
            }

            messages.replaceChildren();
            input.value = "";
            button.disabled = true;
            input.focus();
        } catch (e) {
            console.error("Error starting a new chat:", e);
            addMessage("⚠️ No se pudo iniciar un chat nuevo. Intentá de nuevo.", "bot");
        } finally {
            newChatButton.disabled = false;
        }
    }

    function bindEvents() {
        newChatButton.addEventListener("click", () => void startNewChat());
        button.addEventListener("click", () => void send());

        input.addEventListener("input", () => {
            button.disabled = !input.value.trim();
        });

        input.addEventListener("keydown", (e) => {
            if (e.key === "Enter" && !e.shiftKey && input.value.trim()) {
                e.preventDefault();
                void send();
            }
        });

        if (agentCards) {
            const agentNames = {
                enzo: "Enzo",
                rucau: "Rucau",
                jona: "Jona",
                jesse: "Jesse",
                nahitan: "Nahitan",
                juli: "Juli"
            };
            const selectedAgents = new Set();
            const taskAgentStates = new Map();
            const hiddenTaskAgents = new Set();
            let taskAgentTaskId = null;
            const agentStatusLabels = {
                available: "Disponible",
                queued: "En cola",
                working: "Trabajando",
                waiting_approval: "Esperando aprobación",
                completed: "Finalizada",
                failed: "Fallida",
                interrupted: "Interrumpida",
                disconnected: "Desconectada"
            };

            function updateTaskAgents(agents, taskId) {
                if (!taskId || taskId !== taskAgentTaskId) hiddenTaskAgents.clear();
                taskAgentTaskId = taskId || null;
                taskAgentStates.clear();
                if (Array.isArray(agents)) {
                    agents.forEach((agent) => {
                        const id = agent && String(agent.id || "").replace(/_(seguridad|arquitecto|dev1|dev2|devops|qa)$/, "");
                        if (agentNames[id]) taskAgentStates.set(id, String(agent.status || "available").toLowerCase());
                    });
                }
                renderAgentCards();
            }
            window.updateAgentTaskAgents = updateTaskAgents;

            function createAgentCard(agentId) {
                const displayName = agentNames[agentId];
                const card = document.createElement("article");
                card.className = "agent-card";
                card.setAttribute("role", "listitem");

                const image = document.createElement("img");
                image.className = "agent-image";
                image.src = "/images/agents/" + agentId + ".png";
                image.alt = "Imagen de " + displayName;
                image.hidden = true;

                const imageFallback = document.createElement("div");
                imageFallback.className = "agent-image-fallback";
                imageFallback.textContent = "🤖";
                imageFallback.setAttribute("aria-hidden", "true");

                image.addEventListener("load", () => {
                    image.hidden = false;
                    imageFallback.hidden = true;
                });
                image.addEventListener("error", () => {
                    image.hidden = true;
                    imageFallback.hidden = false;
                });

                const info = document.createElement("div");
                info.className = "agent-info";

                const name = document.createElement("strong");
                name.textContent = displayName;

                const status = document.createElement("span");
                status.className = "agent-status";
                const currentTaskStatus = taskAgentStates.get(agentId) || "available";
                status.dataset.status = currentTaskStatus;

                const statusDot = document.createElement("span");
                statusDot.className = "status-dot";
                statusDot.setAttribute("aria-hidden", "true");

                statusDot.dataset.status = currentTaskStatus;
                status.append(statusDot, document.createTextNode(agentStatusLabels[currentTaskStatus] || "Disponible"));
                info.append(name, status);

                const activity = document.createElement("details");
                activity.className = "agent-card-activity";
                activity.open = ["working", "waiting_approval", "failed", "completed", "interrupted"].includes(currentTaskStatus);
                const activitySummary = document.createElement("summary");
                activitySummary.textContent = "Estado de la tarea";
                const activityText = document.createElement("p");
                activityText.textContent = agentStatusLabels[currentTaskStatus] || "Disponible";
                activity.append(activitySummary, activityText);

                const hideButton = document.createElement("button");
                hideButton.className = "agent-card-close";
                hideButton.type = "button";
                hideButton.setAttribute("aria-label", "Ocultar a " + displayName);
                hideButton.textContent = "×";
                hideButton.addEventListener("click", () => {
                    selectedAgents.delete(agentId);
                    hiddenTaskAgents.add(agentId);
                    renderAgentCards();
                });

                card.append(image, imageFallback, info, activity, hideButton);
                return card;
            }

            function renderAgentCards() {
                const activeTaskAgents = Array.from(taskAgentStates)
                    .filter(([, status]) => !["available", "disconnected"].includes(status))
                    .map(([agentId]) => agentId);
                const visibleAgents = new Set([...selectedAgents, ...activeTaskAgents]);
                hiddenTaskAgents.forEach((agentId) => {
                    if (!selectedAgents.has(agentId)) visibleAgents.delete(agentId);
                });
                const cards = Array.from(visibleAgents, createAgentCard);
                agentCards.replaceChildren(...cards);

                agentButtons.forEach((agentButton) => {
                    const agentId = agentButton.dataset.agent;
                    const displayName = agentNames[agentId];
                    const selected = selectedAgents.has(agentId);
                    const taskStatus = taskAgentStates.get(agentId) || "available";
                    const autoVisible = !["available", "disconnected"].includes(taskStatus) && !hiddenTaskAgents.has(agentId);
                    const visible = selected || autoVisible;

                    agentButton.setAttribute("aria-pressed", String(visible));
                    agentButton.textContent = (visible ? "Ocultar a " : "Mostrar a ") + displayName;
                });
            }

            agentButtons.forEach((agentButton) => {
                agentButton.addEventListener("click", () => {
                    const selectedAgent = agentButton.dataset.agent;
                    if (!agentNames[selectedAgent]) return;

                    const taskStatus = taskAgentStates.get(selectedAgent) || "available";
                    const autoVisible = !["available", "disconnected"].includes(taskStatus) && !hiddenTaskAgents.has(selectedAgent);
                    if (autoVisible) {
                        hiddenTaskAgents.add(selectedAgent);
                        selectedAgents.delete(selectedAgent);
                    } else if (hiddenTaskAgents.has(selectedAgent) && !["available", "disconnected"].includes(taskStatus)) {
                        hiddenTaskAgents.delete(selectedAgent);
                    } else if (selectedAgents.has(selectedAgent)) {
                        selectedAgents.delete(selectedAgent);
                    } else {
                        selectedAgents.add(selectedAgent);
                    }

                    renderAgentCards();
                });
            });
        }
    }

    bindEvents();

    window.chatAppLoaded = true;
    console.log("Chat ready ✔");
}

initChat();
