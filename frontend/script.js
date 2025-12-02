// Same-origin: index.html este servit prin Traefik de pe http://localhost
    const BASE_URL = ''; // requests merg la /people, /devices pe același host

    // Basic Auth: antonio/parola123
    const AUTH_HEADER = 'Basic ' + btoa('antonio:parola123');

    function show(text) {
      document.getElementById('result').innerText = text;
    }

    async function callApi(path, options = {}) {
      try {
        const response = await fetch(BASE_URL + path, {
          method: options.method || 'GET',
          headers: {
            'Authorization': AUTH_HEADER,
            ...(options.headers || {})
          },
          body: options.body || null
        });

        const contentType = response.headers.get('Content-Type') || '';
        let bodyText;

        if (contentType.includes('application/json')) {
          const json = await response.json();
          bodyText = JSON.stringify(json, null, 2);
        } else {
          bodyText = await response.text();
        }

        show(
          'URL: ' + (BASE_URL + path) +
          '\nMethod: ' + (options.method || 'GET') +
          '\nStatus: ' + response.status +
          '\n\n' + bodyText
        );
      } catch (err) {
        show('Request failed:\n' + err);
      }
    }

    // ---- HEALTH / LISTĂRI ----

    async function checkBackend() {
      show('Checking people-service and device-service...');
      try {
        const [peopleRes, deviceRes] = await Promise.all([
          fetch(BASE_URL + '/people', { headers: { 'Authorization': AUTH_HEADER }}),
          fetch(BASE_URL + '/devices', { headers: { 'Authorization': AUTH_HEADER }})
        ]);

        const text =
          'Health check:\n' +
          'people-service: ' + peopleRes.status + '\n' +
          'device-service: ' + deviceRes.status + '\n';

        show(text);
      } catch (err) {
        show('Health check failed:\n' + err);
      }
    }

    function getAllPersons() {
      callApi('/people');
    }

    function getAllDevices() {
      callApi('/devices');
    }

    // ---- INSERT PERSON ----

    async function insertPersonFromForm() {
      const person = {
        username: document.getElementById('username').value || null,
        password: document.getElementById('password').value || null,
        role: document.getElementById('role').value || null,
        name: document.getElementById('name').value || null,
        address: document.getElementById('address').value || null,
        age: parseInt(document.getElementById('age').value, 10) || null
      };

      await callApi('/people', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(person)
      });
    }

    // ---- UPDATE / DELETE PERSON ----

    async function updatePerson() {
      const id = document.getElementById('upd-person-id').value.trim();
      if (!id) {
        alert('Te rog introdu Person ID (UUID) pentru update.');
        return;
      }

      const username = document.getElementById('upd-username').value.trim();
      const password = document.getElementById('upd-password').value.trim();

      if (!username || !password) {
        alert('Username și password sunt obligatorii la update (backend validation).');
        return;
      }

      const name = document.getElementById('upd-name').value.trim();
      const role = document.getElementById('upd-role').value;
      const address = document.getElementById('upd-address').value.trim();
      const ageStr = document.getElementById('upd-age').value;

      const body = {
        username: username,
        password: password,
        name: name || null,
        address: address || null,
        role: role || null,
        age: ageStr ? parseInt(ageStr, 10) : null
      };

      await callApi('/people/' + encodeURIComponent(id), {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(body)
      });
    }


    async function deletePerson() {
      const id = document.getElementById('del-person-id').value.trim();
      if (!id) {
        alert('Te rog introdu Person ID (UUID) pentru delete.');
        return;
      }

      await callApi('/people/' + encodeURIComponent(id), {
        method: 'DELETE'
      });
    }


    // ---- INSERT DEVICE ----

    async function insertDeviceFromForm() {
      const name = document.getElementById('dev-name').value || null;
      const maxConsStr = document.getElementById('dev-max-consumption').value;
      const ownerId = document.getElementById('dev-owner-id').value.trim();

      const device = {
        name: name,
        consumMaxim: maxConsStr ? parseFloat(maxConsStr) : null,
        ownerId: ownerId || null
      };

      await callApi('/devices', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(device)
      });
    }


    // ---- DEVICES BY PERSON ----

    async function getDevicesForPersonFromInput() {
      const personId = document.getElementById('personId').value.trim();
      if (!personId) {
        alert('Te rog introdu un UUID pentru persoană.');
        return;
      }
      await callApi('/devices/owner/' + encodeURIComponent(personId));
    }

    // ---- UPDATE / DELETE DEVICE ----

    async function updateDevice() {
      const id = document.getElementById('upd-device-id').value.trim();
      if (!id) {
        alert('Te rog introdu Device ID (UUID) pentru update.');
        return;
      }

      const name = document.getElementById('upd-dev-name').value.trim();
      const maxStr = document.getElementById('upd-dev-max-consumption').value;
      const ownerId = document.getElementById('upd-dev-owner-id').value.trim();

      const body = {};

      if (name) body.name = name;
      if (maxStr) body.consumMaxim = parseFloat(maxStr);
      if (ownerId) body.ownerId = ownerId;

      if (Object.keys(body).length === 0) {
        alert('Completează măcar un câmp de actualizat.');
        return;
      }

      await callApi('/devices/' + encodeURIComponent(id), {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(body)
      });
    }

    async function deleteDevice() {
      const id = document.getElementById('del-device-id').value.trim();
      if (!id) {
        alert('Te rog introdu Device ID (UUID) pentru delete.');
        return;
      }

      await callApi('/devices/' + encodeURIComponent(id), {
        method: 'DELETE'
      });
    }