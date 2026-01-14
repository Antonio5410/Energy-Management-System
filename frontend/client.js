if (!requireAuth()) throw new Error("Not authenticated");

const statusEl = document.getElementById("status");
const deviceSelect = document.getElementById("deviceSelect");
const ctx = document.getElementById("chart").getContext("2d");

let chartInstance = null;

function setStatus(msg, type) {
    statusEl.textContent = msg || "";
    statusEl.className = "status " + (type || "");
}

async function fetchDevices() {
    return await fetchJson("/devices"); // client -> doar ale lui
}

function formatDate(d) {
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, "0");
    const dd = String(d.getDate()).padStart(2, "0");
    return `${yyyy}-${mm}-${dd}`;
}

async function fetchConsumption(deviceId) {
    const toDate = new Date();
    const fromDate = new Date();
    fromDate.setDate(toDate.getDate() - 7);

    const from = formatDate(fromDate);
    const to = formatDate(toDate);

    return await fetchJson(`/monitoring/devices/${encodeURIComponent(deviceId)}/consumption?from=${from}&to=${to}`);
}

function renderChart(labels, values) {
    if (chartInstance) chartInstance.destroy();
    chartInstance = new Chart(ctx, {
        type: "line",
        data: {
        labels,
        datasets: [{
            label: "Energy Consumption",
            data: values,
            tension: 0.2
        }]
        }
    });
}

async function loadDevicesIntoSelect() {
    setStatus("Loading devices...");
    const devices = await fetchDevices();

    deviceSelect.innerHTML = "";
    if (!devices || devices.length === 0) {
        setStatus("No devices found.", "error");
        return;
    }

    for (const d of devices) {
        const opt = document.createElement("option");
        opt.value = d.id;
        opt.textContent = `${d.name} (${d.id})`;
        deviceSelect.appendChild(opt);
    }

    setStatus("Devices loaded.", "ok");
}

async function loadMonitoring() {
    const deviceId = deviceSelect.value;
    if (!deviceId) return;

    try {
        setStatus("Loading monitoring...");
        const data = await fetchConsumption(deviceId);

        renderChart(
            data.map(x => x.hourStart),
            data.map(x => x.energyKwh)
        );

        setStatus("Monitoring loaded.", "ok");
    } catch (e) {
        console.error(e);
        setStatus("Failed to load monitoring.", "error");
    }
}

document.getElementById("btnLoad").addEventListener("click", loadMonitoring);

(async function init() {
    try {
        await loadDevicesIntoSelect();
        await loadMonitoring(); // auto-load primul
    } catch (e) {
        console.error(e);
        setStatus("Init failed.", "error");
    }
})();
