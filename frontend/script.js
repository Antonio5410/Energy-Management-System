// script.js (ADMIN PAGE) - JWT ONLY
const BASE_URL = ""; // same-origin prin Traefik

// guard
if (!requireAuth()) {
  // requireAuth face redirect
}
if (getRole() !== "ADMIN") {
  window.location.href = "/client.html";
}

function show(text) {
  document.getElementById("result").innerText = text;
}

async function callApi(path, options = {}) {
  const res = await fetch(BASE_URL + path, {
    method: options.method || "GET",
    headers: {
      ...(options.headers || {}),
      ...authHeaders(), // Bearer din login.js
    },
    body: options.body || null,
  });

  const contentType = res.headers.get("Content-Type") || "";
  let bodyText = "";

  if (contentType.includes("application/json")) {
    const json = await res.json().catch(() => null);
    bodyText = JSON.stringify(json, null, 2);
  } else {
    bodyText = await res.text().catch(() => "");
  }

  show(
    "URL: " + (BASE_URL + path) +
    "\nMethod: " + (options.method || "GET") +
    "\nStatus: " + res.status +
    "\n\n" + bodyText
  );

  return res;
}

// ---- HEALTH / LISTĂRI ----

async function checkBackend() {
  show("Checking people-service and device-service...");
  const peopleRes = await fetch(BASE_URL + "/people", { headers: authHeaders() });
  const deviceRes = await fetch(BASE_URL + "/devices", { headers: authHeaders() });

  show(
    "Health check:\n" +
    "people-service: " + peopleRes.status + "\n" +
    "device-service: " + deviceRes.status
  );
}

function getAllPersons() {
  callApi("/people");
}

function getAllDevices() {
  callApi("/devices");
}

// ---- INSERT PERSON ----

async function insertPersonFromForm() {
  const person = {
    username: document.getElementById("username").value || null,
    password: document.getElementById("password").value || null,
    role: document.getElementById("role").value || null,
    name: document.getElementById("name").value || null,
    address: document.getElementById("address").value || null,
    age: parseInt(document.getElementById("age").value, 10) || null,
  };

  await callApi("/people", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(person),
  });
}

// ---- UPDATE / DELETE PERSON ----

async function updatePerson() {
  const id = document.getElementById("upd-person-id").value.trim();
  if (!id) {
    alert("Te rog introdu Person ID (UUID) pentru update.");
    return;
  }

  const username = document.getElementById("upd-username").value.trim();
  const password = document.getElementById("upd-password").value.trim();
  const name = document.getElementById("upd-name").value.trim();
  const roleSelected = document.getElementById("upd-role").value; // "" dacă no change
  const address = document.getElementById("upd-address").value.trim();
  const ageStr = document.getElementById("upd-age").value;

  // Dacă backend-ul tău cere username + password obligatoriu la update, păstrează asta:
  if (!username || !password) {
    alert("Username și password sunt obligatorii la update (backend validation).");
    return;
  }

  try {
    // 1) Luăm persoana curentă ca să aflăm role-ul existent (și orice altceva vrei)
    const currentRes = await fetch(BASE_URL + "/people/" + encodeURIComponent(id), {
      method: "GET",
      headers: {
        ...authHeaders(), // Bearer token
      },
    });

    if (!currentRes.ok) {
      const t = await currentRes.text();
      show(
        "GET /people/" + id +
          "\nStatus: " + currentRes.status +
          "\n\n" + t
      );
      return;
    }

    const current = await currentRes.json();
    const currentRole = current.role; // ex: "CLIENT" / "ADMIN"

    // 2) Construim body pentru PUT
    // IMPORTANT: dacă nu selectezi nimic, trimitem currentRole
    const body = {
      username: username,
      password: password,
      role: roleSelected && roleSelected.length > 0 ? roleSelected : currentRole,
      name: name || current.name || null,
      address: address || current.address || null,
      age: ageStr ? parseInt(ageStr, 10) : (current.age ?? null),
    };

    // 3) Facem PUT
    await callApi("/people/" + encodeURIComponent(id), {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(body),
    });

  } catch (err) {
    console.error(err);
    show("Update failed:\n" + err);
  }
}


async function deletePerson() {
  const id = document.getElementById("del-person-id").value.trim();
  if (!id) return alert("Te rog introdu Person ID (UUID) pentru delete.");

  await callApi("/people/" + encodeURIComponent(id), { method: "DELETE" });
}

// ---- INSERT DEVICE ----

async function insertDeviceFromForm() {
  const name = document.getElementById("dev-name").value || null;
  const maxConsStr = document.getElementById("dev-max-consumption").value;
  const ownerId = document.getElementById("dev-owner-id").value.trim();

  const device = {
    name,
    consumMaxim: maxConsStr ? parseFloat(maxConsStr) : null,
    ownerId: ownerId || null,
  };

  await callApi("/devices", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(device),
  });
}

// ---- DEVICES BY PERSON ----

async function getDevicesForPersonFromInput() {
  const personId = document.getElementById("personId").value.trim();
  if (!personId) return alert("Te rog introdu un UUID pentru persoană.");
  await callApi("/devices/owner/" + encodeURIComponent(personId));
}

// ---- UPDATE / DELETE DEVICE ----

async function updateDevice() {
  const id = document.getElementById("upd-device-id").value.trim();
  if (!id) return alert("Te rog introdu Device ID (UUID) pentru update.");

  const name = document.getElementById("upd-dev-name").value.trim();
  const maxStr = document.getElementById("upd-dev-max-consumption").value;
  const ownerId = document.getElementById("upd-dev-owner-id").value.trim();

  const body = {};
  if (name) body.name = name;
  if (maxStr) body.consumMaxim = parseFloat(maxStr);
  if (ownerId) body.ownerId = ownerId;

  if (Object.keys(body).length === 0) return alert("Completează măcar un câmp de actualizat.");

  await callApi("/devices/" + encodeURIComponent(id), {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
}

async function deleteDevice() {
  const id = document.getElementById("del-device-id").value.trim();
  if (!id) return alert("Te rog introdu Device ID (UUID) pentru delete.");

  await callApi("/devices/" + encodeURIComponent(id), { method: "DELETE" });
}
