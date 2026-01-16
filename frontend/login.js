const TOKEN_KEY = "jwt_token";
const ROLE_KEY = "user_role";
const USERNAME_KEY = "username";

function saveAuth(data) {
    const token = data.token || data.accessToken || data.jwt || data.access_token;
    const role = data.role || data.userRole || data.authority;
    const username = data.username || data.user || data.email;

    if (token) localStorage.setItem(TOKEN_KEY, token);
    if (role) localStorage.setItem(ROLE_KEY, role);
    if (username) localStorage.setItem(USERNAME_KEY, username);

    // debug
    console.log("saveAuth ->", { token: !!token, role, username });
}


function logout() {
    localStorage.clear();
    window.location.href = "/login.html";
}

function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}

function getRole() {
    return localStorage.getItem(ROLE_KEY);
}

function requireAuth() {
    if (!getToken()) {
        window.location.href = "/login.html";
        return false;
    }
    return true;
}

document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("loginForm");
    if (!form) return;

    form.addEventListener("submit", async (e) => {
        e.preventDefault(); // <--- SUPER IMPORTANT, oprește refresh-ul

        const username = document.getElementById("username").value;
        const password = document.getElementById("password").value;

        try {
        const res = await fetch("/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password }),
        });

        if (!res.ok) throw new Error("Login failed");

        const data = await res.json();
        console.log("LOGIN RESPONSE:", data);

        saveAuth(data); // din login.js-ul tău
        console.log("SAVED ROLE:", getRole(), "TOKEN:", !!getToken());

        if (data.role === "ADMIN") {
            window.location.href = "/admin.html";
        } else {
            window.location.href = "/client.html";
        }
        } catch (err) {
        console.error(err);
        const status = document.getElementById("loginStatus");
        if (status) status.textContent = "Invalid credentials";
        else alert("Invalid credentials");
        }
    });
});


function authHeaders(method = "GET") {
    const token = getToken();
    const headers = {};
    if (token) headers["Authorization"] = "Bearer " + token;

    // Content-Type doar când trimiți body (POST/PUT/PATCH)
    if (method !== "GET") headers["Content-Type"] = "application/json";

    return headers;
}


async function fetchJson(url, options = {}) {
    const method = (options.method || "GET").toUpperCase();

    options.headers = {
        ...(options.headers || {}),
        ...authHeaders(method),
    };

    const res = await fetch(url, options);

    if (res.status === 401) throw new Error("Unauthorized");
    if (res.status === 403) throw new Error("Forbidden");

    const text = await res.text();
    try { return text ? JSON.parse(text) : null; } catch { return text; }
}



