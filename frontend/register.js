// register.js
const form = document.getElementById("registerForm");
const statusEl = document.getElementById("registerStatus");

function setStatus(msg, type = "info") {
    statusEl.textContent = msg || "";
    statusEl.className = "status";
    if (type === "error") statusEl.classList.add("error");
    if (type === "ok") statusEl.classList.add("ok");
}

form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value;
    const name = document.getElementById("name").value.trim();
    const address = document.getElementById("address").value.trim();
    const age = parseInt(document.getElementById("age").value, 10);

    if (!username || !password || !name || !address || !Number.isFinite(age)) {
        setStatus("Please fill all fields correctly.", "error");
        return;
    }

    try {
        setStatus("Creating profile...");

        // 1️⃣ CREATE PERSON in people-service
        const personRes = await fetch("/people/self-register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            username,
            password,
            name,
            address,
            age,
            role: "CLIENT"
        }),
        });

        const userId = await personRes.text();

        if (!personRes.ok) {
        setStatus(`Failed to create profile (${personRes.status})`, "error");
        return;
        }

        setStatus("Creating credentials...");

        // 2️⃣ CREATE CREDENTIALS in auth-service
        const authRes = await fetch("/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            username,
            password,
            role: "CLIENT",
            userId
        }),
        });

        const authData = await authRes.json().catch(() => ({}));

        if (!authRes.ok) {
        setStatus(authData.message || "Auth register failed", "error");
        return;
        }

        setStatus("Account created. Redirecting to login...", "ok");
        setTimeout(() => {
        window.location.href = "/login.html";
        }, 800);

    } catch (err) {
        console.error(err);
        setStatus("Register request failed.", "error");
    }
});
