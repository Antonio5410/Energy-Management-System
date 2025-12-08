// Same-origin: charts.html e servit prin Traefik de pe http://localhost
const BASE_URL = ""; // apelurile merg la /people, /devices, /monitoring pe același host

// Basic Auth: antonio/parola123
const AUTH_HEADER = "Basic " + btoa("antonio:parola123");

function authHeaders(extra = {}) {
  return {
    Authorization: AUTH_HEADER,
    ...extra,
  };
}

async function fetchJson(url, options = {}) {
  const response = await fetch(BASE_URL + url, {
    ...options,
    headers: {
      ...(options.headers || {}),
      ...authHeaders(),
    },
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(`HTTP ${response.status}: ${text || response.statusText}`);
  }

  return response.json();
}

// Elemente din DOM
const userSelect = document.getElementById("userSelect");
const deviceSelect = document.getElementById("deviceSelect");
const dayInput = document.getElementById("dayInput");
const statusMessage = document.getElementById("statusMessage");
const monitoringForm = document.getElementById("monitoringForm");
const loadDataBtn = document.getElementById("loadDataBtn");

// Chart instance (o păstrăm ca să o putem distruge înainte de a crea alta)
let consumptionChart = null;

function setStatus(message, type = "info") {
  statusMessage.textContent = message || "";
  statusMessage.className = "status"; // resetăm
  if (type === "error") {
    statusMessage.classList.add("error");
  } else if (type === "ok") {
    statusMessage.classList.add("ok");
  }
}

// 1) Load USERS în dropdown
async function loadUsers() {
  try {
    setStatus("Loading users...");
    const users = await fetchJson("/people");

    userSelect.innerHTML = `<option value="">Select a user.</option>`;
    users.forEach((u) => {
      const opt = document.createElement("option");
      opt.value = u.id;
      opt.textContent = `${u.name} (${u.username})`;
      userSelect.appendChild(opt);
    });

    setStatus(`Loaded ${users.length} users.`, "ok");
  } catch (err) {
    console.error(err);
    setStatus("Error loading users: " + err.message, "error");
  }
}

// 2) Când se schimbă user-ul, încărcăm device-urile lui
async function loadDevicesForUser(userId) {
  deviceSelect.innerHTML = `<option value="">Select a device.</option>`;
  deviceSelect.disabled = true;

  if (!userId) {
    setStatus("Please select a user.", "info");
    return;
  }

  try {
    setStatus("Loading devices for selected user...");
    // Endpoint-ul deja folosit în index: /devices/owner/{personId}
    const devices = await fetchJson(
      `/devices/owner/${encodeURIComponent(userId)}`
    );

    if (!Array.isArray(devices) || devices.length === 0) {
      setStatus("No devices found for this user.", "info");
      return;
    }

    devices.forEach((d) => {
      const opt = document.createElement("option");
      opt.value = d.id;
      opt.textContent = d.name || d.id;
      deviceSelect.appendChild(opt);
    });

    deviceSelect.disabled = false;
    setStatus(`Loaded ${devices.length} devices for selected user.`, "ok");
  } catch (err) {
    console.error(err);
    setStatus("Error loading devices: " + err.message, "error");
  }
}

// 3) Load consum pentru device + day și desenează chart-ul
async function loadConsumptionForDeviceAndDay(event) {
  if (event) {
    event.preventDefault();
  }

  const deviceId = deviceSelect.value;
  const selectedDay = dayInput.value; // format: YYYY-MM-DD

  if (!userSelect.value) {
    setStatus("Please select a user first.", "error");
    return;
  }

  if (!deviceId) {
    setStatus("Please select a device.", "error");
    return;
  }

  if (!selectedDay) {
    setStatus("Please choose a day.", "error");
    return;
  }

  // Pentru o singură zi, putem trimite from=day & to=day
  const from = selectedDay;
  const to = selectedDay;

  try {
    setStatus("Loading consumption data...");

    const data = await fetchJson(
      `/monitoring/devices/${encodeURIComponent(
        deviceId
      )}/consumption?from=${from}&to=${to}`
    );

    if (!Array.isArray(data) || data.length === 0) {
      setStatus("No consumption data for this day.", "info");
      if (consumptionChart) {
        consumptionChart.destroy();
        consumptionChart = null;
      }
      return;
    }

    // Ne asigurăm că sunt doar în ziua selectată (în caz că backend-ul întoarce interval mai larg)
    const filtered = data.filter((item) =>
      item.hourStart.startsWith(selectedDay)
    );

    if (filtered.length === 0) {
      setStatus("No consumption data for this day.", "info");
      if (consumptionChart) {
        consumptionChart.destroy();
        consumptionChart = null;
      }
      return;
    }

    // Sortăm după oră
    filtered.sort((a, b) => a.hourStart.localeCompare(b.hourStart));

    const labels = filtered.map((item) => {
      // item.hourStart e de forma "YYYY-MM-DDTHH:MM:SS"
      const parts = item.hourStart.split("T");
      if (parts.length < 2) return item.hourStart;
      return parts[1].slice(0, 5); // HH:MM
    });

    const values = filtered.map((item) => item.energyKwh);

    // Ștergem chart-ul vechi, dacă există
    if (consumptionChart) {
      consumptionChart.destroy();
    }

    const ctx = document.getElementById("consumptionChart").getContext("2d");
    consumptionChart = new Chart(ctx, {
      type: "line",
      data: {
        labels,
        datasets: [
          {
            label: "Energy (kWh)",
            data: values,
            borderWidth: 2,
            tension: 0.3,
          },
        ],
      },
      options: {
        responsive: true,
        scales: {
          x: {
            title: {
              display: true,
              text: "Hour",
            },
          },
          y: {
            title: {
              display: true,
              text: "Energy (kWh)",
            },
            beginAtZero: true,
          },
        },
      },
    });

    setStatus(
      `Loaded ${filtered.length} hourly values for ${selectedDay}.`,
      "ok"
    );
  } catch (err) {
    console.error(err);
    setStatus("Error loading consumption data: " + err.message, "error");
  }
}

// INITIALIZARE
document.addEventListener("DOMContentLoaded", () => {
  // setăm ziua de azi by default
  const today = new Date().toISOString().slice(0, 10);
  if (dayInput) {
    dayInput.value = today;
  }

  // încărcăm userii
  if (userSelect) {
    loadUsers();
    userSelect.addEventListener("change", (e) => {
      loadDevicesForUser(e.target.value);
    });
  }

  // submit pe formular → încarcă consumul
  if (monitoringForm) {
    monitoringForm.addEventListener("submit", loadConsumptionForDeviceAndDay);
  }

  // și click direct pe buton, ca backup
  if (loadDataBtn) {
    loadDataBtn.addEventListener("click", loadConsumptionForDeviceAndDay);
  }
});
