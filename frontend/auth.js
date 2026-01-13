const TOKEN_KEY = "jwt_token";
const ROLE_KEY = "user_role";
const USERNAME_KEY = "username";

function saveAuth(data) {
    localStorage.setItem(TOKEN_KEY, data.token);
    localStorage.setItem(ROLE_KEY, data.role);
    localStorage.setItem(USERNAME_KEY, data.username);
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
    return {
        "Authorization": "Bearer " + getToken(),
        "Content-Type": "application/json"
    };
}

async function fetchJson(url, options = {}) {
    options.headers = {
        ...(options.headers || {}),
        ...authHeaders()
    };

    const res = await fetch(url, options);

    if (res.status === 401 || res.status === 403) {
        logout();
        throw new Error("Unauthorized");
    }

    return res.json();
}
