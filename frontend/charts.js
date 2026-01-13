// charts.js

if (!requireAuth()) {
  throw new Error("Not authenticated");
}

const ctx = document.getElementById("chart").getContext("2d");
let chartInstance = null;

async function fetchDevices() {
  // ADMIN -> toate
  // CLIENT -> doar ale lui (backend deja face asta)
  return await fetchJson("/devices");
}

async function fetchConsumption(deviceId) {
  return await fetchJson(`/monitoring/device/${deviceId}/consumption`);
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
        borderColor: "rgb(75,192,192)",
        tension: 0.2
      }]
    }
  });
}

async function loadChart() {
  try {
    const devices = await fetchDevices();
    if (!devices.length) {
      alert("No devices available");
      return;
    }

    const deviceId = devices[0].id;
    const data = await fetchConsumption(deviceId);

    renderChart(
      data.map(d => d.timestamp),
      data.map(d => d.energy)
    );
  } catch (e) {
    console.error(e);
    alert("Failed to load chart");
  }
}

loadChart();
