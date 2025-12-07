let chartInstance = null;

document.addEventListener("DOMContentLoaded", () => {
    const deviceSelect = document.getElementById("deviceSelect");
    const dateInput = document.getElementById("dateInput");
    const loadButton = document.getElementById("loadDataBtn");
    const statusMessage = document.getElementById("statusMessage");

    // 1. Populate device select on load
    loadDevices();

    // 2. Optionally, set today's date as default
    const today = new Date().toISOString().slice(0, 10);
    dateInput.value = today;

    // 3. Load data when user clicks button
    loadButton.addEventListener("click", () => {
        const deviceId = deviceSelect.value;
        const day = dateInput.value;

        if (!deviceId) {
            setStatus("Please select a device.", true);
            return;
        }
        if (!day) {
            setStatus("Please select a day.", true);
            return;
        }

        loadConsumptionForDay(deviceId, day);
    });

    function setStatus(message, isError = false) {
        statusMessage.textContent = message || "";
        statusMessage.classList.toggle("error", !!isError);
        statusMessage.classList.toggle("ok", !isError && !!message);
    }

    function loadDevices() {
        setStatus("Loading devices...");

        fetch("/monitoring/devices")
            .then(response => {
                if (!response.ok) {
                    throw new Error("Failed to load devices");
                }
                return response.json();
            })
            .then(devices => {
                // Clear existing options, keep the placeholder
                deviceSelect.innerHTML = '<option value="">Select a device...</option>';

                devices.forEach(d => {
                    const opt = document.createElement("option");
                    opt.value = d.id;
                    opt.textContent = `${d.id} (max ${d.maxHourlyConsumption} kWh)`;
                    deviceSelect.appendChild(opt);
                });

                if (devices.length === 0) {
                    setStatus("No monitored devices found.", true);
                } else {
                    setStatus(`Loaded ${devices.length} devices.`);
                }
            })
            .catch(err => {
                console.error(err);
                setStatus("Error loading devices.", true);
            });
    }

    function loadConsumptionForDay(deviceId, day) {
        setStatus("Loading consumption data...");

        // Cerem interval [day, day] – un singur day
        const url = `/monitoring/devices/${encodeURIComponent(deviceId)}` +
                    `/consumption?from=${day}&to=${day}`;

        fetch(url)
            .then(response => {
                if (!response.ok) {
                    throw new Error("Failed to load consumption data");
                }
                return response.json();
            })
            .then(data => {
                if (!Array.isArray(data) || data.length === 0) {
                    setStatus("No consumption data for this device and day.", true);
                    updateChart([], []);
                    return;
                }

                // Transformăm în „hour” + „energy”
                const labels = [];
                const values = [];

                data.forEach(entry => {
                    // hourStart: "2025-12-02T09:00:00"
                    const hourStr = entry.hourStart;
                    let hourLabel = hourStr;

                    try {
                        const date = new Date(hourStr);
                        const hour = date.getHours().toString().padStart(2, "0");
                        hourLabel = `${hour}:00`;
                    } catch (e) {
                        // dacă parse nu merge, folosim direct stringul
                    }

                    labels.push(hourLabel);
                    values.push(entry.energyKwh);
                });

                setStatus(`Loaded ${data.length} hourly values.`);
                updateChart(labels, values);
            })
            .catch(err => {
                console.error(err);
                setStatus("Error loading consumption data.", true);
                updateChart([], []);
            });
    }

    function updateChart(labels, values) {
        const ctx = document.getElementById("consumptionChart").getContext("2d");

        if (chartInstance) {
            chartInstance.data.labels = labels;
            chartInstance.data.datasets[0].data = values;
            chartInstance.update();
            return;
        }

        chartInstance = new Chart(ctx, {
            type: "bar",  // poți schimba în "line" dacă vrei
            data: {
                labels: labels,
                datasets: [{
                    label: "Energy (kWh)",
                    data: values,
                    backgroundColor: "rgba(25, 118, 210, 0.4)",
                    borderColor: "rgba(25, 118, 210, 1)",
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                scales: {
                    x: {
                        title: {
                            display: true,
                            text: "Hour of day"
                        }
                    },
                    y: {
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: "Energy [kWh]"
                        }
                    }
                }
            }
        });
    }
});
