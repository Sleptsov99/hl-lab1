const TEST_PHONE = '89312894756';

let token = localStorage.getItem('spamer_token');
let user = JSON.parse(localStorage.getItem('spamer_user') || 'null');

const authSection = document.getElementById('auth-section');
const appSection = document.getElementById('app-section');
const authError = document.getElementById('auth-error');
const campaignError = document.getElementById('campaign-error');
const serviceError = document.getElementById('service-error');
const serviceSuccess = document.getElementById('service-success');
const campaignsList = document.getElementById('campaigns-list');
const servicesList = document.getElementById('services-list');
const serviceSelect = document.getElementById('service-id');
const userInfo = document.getElementById('user-info');
const adminTab = document.getElementById('admin-tab');

async function api(path, options = {}) {
    const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
    if (token) headers.Authorization = `Bearer ${token}`;
    const res = await fetch(path, { ...options, headers });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) throw new Error(data.error || res.statusText);
    return data;
}

function isAdmin() {
    return user && user.role === 'ADMIN';
}

function showApp() {
    authSection.classList.add('hidden');
    appSection.classList.remove('hidden');
    userInfo.textContent = `${user.username} (${user.role}, ${user.tier})`;

    if (isAdmin()) {
        adminTab.classList.remove('hidden');
    } else {
        adminTab.classList.add('hidden');
        switchTab('campaigns');
    }

    loadServices();
    loadCampaigns();
    if (isAdmin()) {
        loadAdminServices();
    }
}

function logout() {
    token = null;
    user = null;
    localStorage.removeItem('spamer_token');
    localStorage.removeItem('spamer_user');
    appSection.classList.add('hidden');
    authSection.classList.remove('hidden');
}

async function doLogin(username, password) {
    authError.textContent = '';
    const data = await api('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username, password })
    });
    token = data.token;
    user = { username: data.username, role: data.role, tier: data.tier };
    localStorage.setItem('spamer_token', token);
    localStorage.setItem('spamer_user', JSON.stringify(user));
    showApp();
}

document.getElementById('login-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
        await doLogin(
            document.getElementById('username').value,
            document.getElementById('password').value
        );
    } catch (err) {
        authError.textContent = err.message;
    }
});

document.getElementById('login-tester-btn').addEventListener('click', async () => {
    try {
        await doLogin('tester', 'test123');
    } catch (err) {
        authError.textContent = err.message;
    }
});

document.getElementById('logout-btn').addEventListener('click', logout);
document.getElementById('fill-phone-btn').addEventListener('click', () => {
    document.getElementById('target-email').value = TEST_PHONE;
});

document.getElementById('campaign-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    campaignError.textContent = '';
    const submitBtn = e.target.querySelector('button[type="submit"]');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Запуск...';
    try {
        const serviceId = Number(document.getElementById('service-id').value);
        if (!serviceId) {
            throw new Error('Выберите сервис');
        }
        await api('/api/spam/campaigns', {
            method: 'POST',
            body: JSON.stringify({
                name: document.getElementById('campaign-name').value,
                serviceId,
                targetEmail: document.getElementById('target-email').value,
                totalRequests: Number(document.getElementById('total-requests').value),
                ratePerSecond: Number(document.getElementById('rate').value),
                concurrency: Number(document.getElementById('concurrency').value)
            })
        });
        document.getElementById('campaign-name').value = '';
        loadCampaigns();
    } catch (err) {
        campaignError.textContent = err.message;
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Запустить';
    }
});

document.getElementById('refresh-btn').addEventListener('click', loadCampaigns);

document.getElementById('service-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    serviceError.textContent = '';
    serviceSuccess.textContent = '';
    try {
        await api('/api/admin/services', {
            method: 'POST',
            body: JSON.stringify({
                name: document.getElementById('svc-name').value,
                baseUrl: document.getElementById('svc-base-url').value,
                endpointType: document.getElementById('svc-type').value,
                endpointPath: document.getElementById('svc-path').value,
                httpMethod: document.getElementById('svc-method').value,
                requestBodyTemplate: document.getElementById('svc-body').value || null
            })
        });
        document.getElementById('service-form').reset();
        serviceSuccess.textContent = 'Сервис добавлен';
        loadServices();
        loadAdminServices();
    } catch (err) {
        serviceError.textContent = err.message;
    }
});

document.getElementById('refresh-services-btn').addEventListener('click', loadAdminServices);

document.querySelectorAll('.tab').forEach(tab => {
    tab.addEventListener('click', () => switchTab(tab.dataset.tab));
});

function switchTab(name) {
    document.querySelectorAll('.tab').forEach(t => {
        t.classList.toggle('active', t.dataset.tab === name);
    });
    document.getElementById('tab-campaigns').classList.toggle('hidden', name !== 'campaigns');
    document.getElementById('tab-admin').classList.toggle('hidden', name !== 'admin');
    if (name === 'admin' && isAdmin()) {
        loadAdminServices();
    }
}

async function loadServices() {
    const services = await api('/api/services');
    serviceSelect.innerHTML = services.length === 0
        ? '<option value="">— нет сервисов —</option>'
        : services.map(s =>
            `<option value="${s.id}">${s.name} — ${s.baseUrl}${s.endpointPath}</option>`
        ).join('');
}

async function loadAdminServices() {
    if (!isAdmin()) return;
    const services = await api('/api/admin/services');
    servicesList.innerHTML = services.length === 0
        ? '<p>Нет сервисов</p>'
        : services.map(s => `
            <div class="service-item">
                <strong>${s.name}</strong>
                <span class="tag">${s.endpointType}</span>
                <div class="mono">${s.httpMethod} ${s.baseUrl}${s.endpointPath}</div>
                ${s.requestBodyTemplate ? `<pre class="body-preview">${escapeHtml(s.requestBodyTemplate)}</pre>` : ''}
            </div>
        `).join('');
}

let campaignPollTimer = null;

async function loadCampaigns() {
    const campaigns = await api('/api/spam/campaigns');
    const hasRunning = campaigns.some(c => c.status === 'RUNNING' || c.status === 'PENDING');
    if (hasRunning && !campaignPollTimer) {
        campaignPollTimer = setInterval(loadCampaigns, 2000);
    } else if (!hasRunning && campaignPollTimer) {
        clearInterval(campaignPollTimer);
        campaignPollTimer = null;
    }

    campaignsList.innerHTML = campaigns.length === 0
        ? '<p>Нет кампаний</p>'
        : campaigns.map(c => `
            <div class="campaign-item">
                <strong>${c.name}</strong>
                <span class="status ${c.status}">${c.status}</span>
                <div>Target: ${c.targetEmail}</div>
                <div>OK: ${c.successCount} / Fail: ${c.failCount} / Total: ${c.totalRequests}</div>
                ${c.status === 'RUNNING' ? `<button data-stop="${c.id}" type="button" class="btn-secondary btn-small">Стоп</button>` : ''}
            </div>
        `).join('');

    campaignsList.querySelectorAll('[data-stop]').forEach(btn => {
        btn.addEventListener('click', async () => {
            await api(`/api/spam/campaigns/${btn.dataset.stop}/stop`, { method: 'POST' });
            loadCampaigns();
        });
    });
}

function escapeHtml(str) {
    return str
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
}

if (token && user) {
    showApp();
}
