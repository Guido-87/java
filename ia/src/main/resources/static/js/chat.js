function initChat() {
    console.log('Chat inicializando...');

    const input = document.getElementById("input");
    const button = document.getElementById("sendBtn");
    const messages = document.getElementById("messages");

    if (!input || !button || !messages) {
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

    function bindEvents() {
        button.addEventListener("click", () => void send);

        input.addEventListener("input", () => {
            button.disabled = !input.value.trim();
        });

        input.addEventListener("keydown", (e) => {
            if (e.key === "Enter" && !e.shiftKey && input.value.trim()) {
                e.preventDefault();
                void send();
            }
        });
    }

    bindEvents();

    window.chatAppLoaded = true;
    console.log("Chat listo ✔");
}

initChat();