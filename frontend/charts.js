// charts.js

if (!requireAuth()) {
  throw new Error("Not authenticated");
}

const ctx = document.getElementById("chart").getContext("2d");
let chartInstance = null;

function formatDate(d) {
  // YYYY-MM-DD
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  return `${yyyy}-${mm}-${dd}`;
}

async function fetchMonitoredDevices() {
  // endpoint corect din MonitoringController
  return await fetchJson("/monitoring/devices");
}

async function fetchConsumption(deviceId, fromDate, toDate) {
  const from = formatDate(fromDate);
  const to = formatDate(toDate);

  // endpoint corect + query params obligatorii
  const url = `/monitoring/devices/${deviceId}/consumption?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`;
  return await fetchJson(url);
}

function renderChart(labels, values) {
  if (chartInstance) chartInstance.destroy();

  chartInstance = new Chart(ctx, {
    type: "line",
    data: {
      labels,
      datasets: [
        {
          label: "Energy Consumption (kWh)",
          data: values,
          tension: 0.2
        }
      ]
    }
  });
}

async function loadChart() {
  try {
    const devices = await fetchMonitoredDevices();

    if (!Array.isArray(devices) || devices.length === 0) {
      alert("No monitored devices available");
      return;
    }

    const first = devices[0];
    const deviceId = first.id || first.deviceId || first.idDevice;

    if (!deviceId) {
      console.error("MonitoredDevice object:", first);
      alert("Device id field mismatch (id / deviceId / idDevice).");
      return;
    }

    // ultimile 7 zile
    const toDate = new Date();
    const fromDate = new Date();
    fromDate.setDate(toDate.getDate() - 7);

    const data = await fetchConsumption(deviceId, fromDate, toDate);

    if (!Array.isArray(data)) {
      console.error("Consumption response is not an array:", data);
      alert("Monitoring returned non-array. Check console.");
      return;
    }

    // DTO: HourlyConsumptionDTO(hourStart, energyKwh)
    renderChart(
      data.map(d => d.hourStart),   // sau transformi în format mai frumos dacă vrei
      data.map(d => d.energyKwh)
    );
  } catch (e) {
    console.error(e);
    alert("Failed to load chart");
  }
}

loadChart();
