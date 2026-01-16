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
        setStatus("Registering...");

        // 1) creezi user in people-service (ADMIN-only in backend-ul tau)
        // Daca tu vrei self-register, atunci people-service trebuie sa permita create fara ADMIN.
        // Momentan probabil o faci in auth-service (register) cu userId=null si apoi sincronizare.
        // Eu pornesc de la varianta ta: /auth/register exista.
        const res = await fetch("/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            username,
            password,
            role: "CLIENT",
            userId: null
        }),
        });

        const data = await res.json().catch(() => ({}));

        if (!res.ok) {
            setStatus(data.message || `Register failed (${res.status})`, "error");
            return;
        }

        setStatus("Account created. You can log in now.", "ok");
        setTimeout(() => (window.location.href = "/login.html"), 700);

    } catch (err) {
        console.error(err);
        setStatus("Register request failed.", "error");
    }
});
