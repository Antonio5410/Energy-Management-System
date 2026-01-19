let stompClient = null;

function logClient(msg) {
    const logEl = document.getElementById("client-chat-log");
    if (!logEl) {
        console.log("[WS]", msg);
        return;
    }
    logEl.textContent += msg + "\n";
    logEl.scrollTop = logEl.scrollHeight;
}

function connectClientWS() {
    const userId = getUserIdFromToken(); // DIN JWT
    if (!userId) {
        logClient("❌ No userId in token. WS not connected.");
        return;
    }

    // FOARTE IMPORTANT: prin Traefik / nginx
    const socket = new SockJS("http://localhost:8085/ws");
    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect({}, () => {
        logClient("✅ Connected to WS as user " + userId);

        stompClient.subscribe(
            "/topic/notify.user." + userId,
            msg => logClient("🚨 " + msg.body)
        );

        stompClient.subscribe(
            "/topic/chat.user." + userId,
            msg => logClient("📨 " + msg.body)
        );
    }, err => {
        logClient("❌ WS error: " + err);
    });
}

function sendClientMessage() {
    const input = document.getElementById("client-message");
    if (!input || !stompClient) return;

    const content = input.value.trim();
    if (!content) return;

    stompClient.send("/app/chat.send", {}, JSON.stringify({
        fromUserId: getUserIdFromToken(),
        to: "ADMIN",
        content
    }));

    logClient("➡️ Sent: " + content);
    input.value = "";
}

/* 🔥 AICI e răspunsul la întrebarea ta 🔥 */
window.addEventListener("load", () => {
    console.log("Client page loaded → connecting WS");
    connectClientWS();
});
