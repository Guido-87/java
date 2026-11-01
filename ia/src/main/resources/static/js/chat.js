function initChat() {
    console.log('Chat inicializando...');

    const input = document.getElementById("input");
    const button = document.getElementById("sendBtn");
    const newChatButton = document.getElementById("newChatBtn");
    const messages = document.getElementById("messages");
    const agentCards = document.getElementById("agentCards");
    const agentButtons = document.querySelectorAll(".agent-toggle");

    if (!input || !button || !newChatButton || !messages) {
        console.error("Faltan elementos del DOM");
        window.chatAppLoaded = false;
        throw new Error("UI no inicializada");
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
        loadingMsg.innerHTML = '<div class="spinner"></div> Pensando...';

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
                // La respuesta no contiene JSON válido
            }

            loadingMsg.remove();

            if (!response.ok) {
                let errorMessage;

                if (response.status === 400) {
                    errorMessage = "El mensaje no es válido. Revisá lo que escribiste.";
                } else if (response.status === 503) {
                    errorMessage = "El servicio no está disponible temporalmente. Intentá nuevamente en unos segundos.";
                } else if (response.status >= 500) {
                    errorMessage = "Ocurrió un error en el servidor. Intentá nuevamente más tarde.";
                } else {
                    errorMessage = data.message || "No se pudo procesar el mensaje.";
                }

                addMessage("⚠️ " + errorMessage, "bot");
                return;
            }

            addMessage(data.response, "bot");

        } catch (e) {
            console.error("Error al enviar mensaje:", e);
            loadingMsg.remove();
            addMessage("⚠️ No se pudo conectar con el servidor. Revisá tu conexión e intentá nuevamente.", "bot");
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
                throw new Error("No se pudo iniciar un nuevo chat");
            }

            messages.replaceChildren();
            input.value = "";
            button.disabled = true;
            input.focus();
        } catch (e) {
            console.error("Error al iniciar un nuevo chat:", e);
            addMessage("⚠️ No se pudo iniciar un nuevo chat. Intentá nuevamente.", "bot");
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

                const statusDot = document.createElement("span");
                statusDot.className = "status-dot";
                statusDot.setAttribute("aria-hidden", "true");

                status.append(statusDot, document.createTextNode("Disponible"));
                info.append(name, status);

                const hideButton = document.createElement("button");
                hideButton.className = "agent-card-close";
                hideButton.type = "button";
                hideButton.setAttribute("aria-label", "Ocultar " + displayName);
                hideButton.textContent = "×";
                hideButton.addEventListener("click", () => {
                    selectedAgents.delete(agentId);
                    renderAgentCards();
                });

                card.append(image, imageFallback, info, hideButton);
                return card;
            }

            function renderAgentCards() {
                const cards = Array.from(selectedAgents, createAgentCard);
                agentCards.replaceChildren(...cards);

                agentButtons.forEach((agentButton) => {
                    const agentId = agentButton.dataset.agent;
                    const displayName = agentNames[agentId];
                    const selected = selectedAgents.has(agentId);

                    agentButton.setAttribute("aria-pressed", String(selected));
                    agentButton.textContent = (selected ? "Ocultar " : "Mostrar ") + displayName;
                });
            }

            agentButtons.forEach((agentButton) => {
                agentButton.addEventListener("click", () => {
                    const selectedAgent = agentButton.dataset.agent;
                    if (!agentNames[selectedAgent]) return;

                    if (selectedAgents.has(selectedAgent)) {
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
    console.log("Chat listo ✔");
}

initChat();
