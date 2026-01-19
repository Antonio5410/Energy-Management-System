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
