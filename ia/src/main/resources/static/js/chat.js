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
        const prompt = input.value;
        if (!prompt.trim()) return;

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

            const data = await response.json();

            loadingMsg.remove();

            if (!response.ok) {
                addMessage("⚠️ " + data.message, "bot");
                return;
            }

            addMessage(data.response, "bot");

        } catch (e) {
            loadingMsg.remove();
            addMessage("⚠️ Error de conexión", "bot");
        } finally {
            input.disabled = false;
            button.disabled = !input.value.trim();
            input.focus();
        }
    }

    // ✅ ACÁ va TODO lo de eventos (UNA SOLA VEZ)
    function bindEvents() {
        button.addEventListener("click", send);
        input.addEventListener("input", () => {
            button.disabled = !input.value.trim();
        });
        input.addEventListener("keydown", (e) => {
            if (e.key === "Enter" && input.value.trim()) {
                send();
            }
        });
    }
    bindEvents();
    window.chatAppLoaded = true;
    console.log("Chat listo ✔");
}

initChat();