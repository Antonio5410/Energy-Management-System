let adminSocket = null;

function connectAdminWS() {
    adminSocket = new SockJS("http://localhost:8085/ws") // provizoriu hardcodat
    const stomp = Stomp.over(adminSocket);

    stomp.connect({}, () => {
        stomp.subscribe("/topic/chat.admin", msg => {
        logAdmin("📨 " + msg.body);
        });

        stomp.subscribe("/topic/notify.admin", msg => {
        logAdmin("🚨 " + msg.body);
        });

        logAdmin("✅ Connected to WS (ADMIN)");
        window.adminStomp = stomp;
    });
}

function sendAdminMessage() {
    const to = document.getElementById("admin-user-id").value;
    const content = document.getElementById("admin-message").value;

    window.adminStomp.send("/app/chat.send", {}, JSON.stringify({
        fromUserId: "ADMIN",
        to: to,
        content: content
    }));

    logAdmin("➡️ Sent to " + to + ": " + content);
    }

    function logAdmin(msg) {
    document.getElementById("admin-chat-log").textContent += msg + "\n";
}

connectAdminWS();

async function aiSuggestForAdmin() {
    const input = document.getElementById("admin-message");
    const content = (input?.value || "").trim();
    if (!content) return logAdmin("ℹ️ Scrie întâi mesajul clientului (sau copiază-l aici) pentru sugestie.");

    try {
        const res = await fetch("http://localhost:8085/realtime/ai/suggest", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            ...authHeaders()
        },
        body: JSON.stringify({ message: content })
        });

        const data = await res.json().catch(() => ({}));

        if (!res.ok) {
        logAdmin("❌ AI suggest failed: " + res.status + " " + JSON.stringify(data));
        return;
        }

        logAdmin("🤖 AI Suggestion: " + data.suggestion);
        // opțional: îți pune sugestia direct în input ca să dai Send imediat
        // input.value = data.suggestion;

    } catch (e) {
        logAdmin("❌ AI exception: " + e);
    }
}

window.aiSuggestForAdmin = aiSuggestForAdmin;

