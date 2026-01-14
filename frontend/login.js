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

function authHeaders() {
    const token = getToken();
    const headers = { "Content-Type": "application/json" };
    if (token) headers["Authorization"] = "Bearer " + token;
    return headers;
}

async function fetchJson(url, options = {}) {
    options.headers = {
        ...(options.headers || {}),
        ...authHeaders()
    };

    const res = await fetch(url, options);

    if (res.status === 401) {
        // logout();
        throw new Error("Unauthorized (token invalid/expirat)");
    }

    if (res.status === 403) {
        throw new Error("Forbidden (nu ai drepturi)");
    }

    const text = await res.text();
    try { return text ? JSON.parse(text) : null; } catch { return text; }
}


