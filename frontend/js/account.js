document.addEventListener('DOMContentLoaded', async () => {
    await checkSession();
    if (!requireLogin()) return;
    updateCartBadge();

    loadProfile();
    loadOrders();

    document.getElementById('profile-update-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const name = document.getElementById('update-name').value.trim();
        const errEl = document.getElementById('profile-error');
        const okEl  = document.getElementById('profile-success');
        errEl.style.display = 'none';
        okEl.style.display  = 'none';

        if (!name) { errEl.textContent = 'Name cannot be blank.'; errEl.style.display = ''; return; }

        try {
            const updated = await apiFetch('PUT', '/users/me', { name });
            document.getElementById('profile-name').textContent = updated.name;
            okEl.style.display = '';
        } catch (_) {
            errEl.textContent = 'Update failed.';
            errEl.style.display = '';
        }
    });

    document.getElementById('logout-btn').addEventListener('click', async () => {
        try { await apiFetch('POST', '/auth/logout'); } catch (_) {}
        window.currentUser = null;
        location.href = 'index.html';
    });
});

async function loadProfile() {
    try {
        const user = await apiFetch('GET', '/users/me');
        document.getElementById('profile-name').textContent  = user.name;
        document.getElementById('profile-email').textContent = user.email;
        document.getElementById('profile-since').textContent = new Date(user.registeredAt).toLocaleDateString();
        document.getElementById('update-name').value = user.name;
    } catch (_) {}
}

async function loadOrders() {
    const container = document.getElementById('order-history');
    const emptyEl   = document.getElementById('orders-empty');
    try {
        const orders = await apiFetch('GET', '/orders');
        if (!orders || orders.length === 0) {
            emptyEl.style.display = '';
            return;
        }
        container.innerHTML = orders.map(renderOrder).join('');
        container.querySelectorAll('.order-item').forEach(el => {
            el.addEventListener('click', () => toggleOrderDetail(el));
        });
    } catch (_) {
        container.innerHTML = '<p class="field-error">Unable to load orders.</p>';
    }
}

function renderOrder(o) {
    const total = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(o.totalAmount);
    const date  = new Date(o.placedAt).toLocaleDateString();
    return `
        <div class="order-item" data-id="${o.id}" style="cursor:pointer">
            <div class="order-item__header">
                <span class="order-item__number">${escHtml(o.orderNumber)}</span>
                <span class="order-item__date">${date}</span>
                <span class="order-item__total">${total}</span>
                <span style="color:var(--color-text-muted);font-size:.8rem">${o.itemCount} item(s) — click to expand</span>
            </div>
            <div class="order-line-items" style="display:none" id="order-lines-${o.id}">
                <p class="loading">Loading…</p>
            </div>
        </div>`;
}

async function toggleOrderDetail(el) {
    const id = el.dataset.id;
    const lines = document.getElementById('order-lines-' + id);
    if (lines.style.display === 'none') {
        lines.style.display = '';
        if (lines.innerHTML.includes('Loading')) {
            try {
                const detail = await apiFetch('GET', '/orders/' + id);
                lines.innerHTML = detail.items.map(i => {
                    const lt = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' })
                        .format(i.unitPrice * i.quantity);
                    return `<div>${escHtml(i.productName)} × ${i.quantity} — ${lt}</div>`;
                }).join('');
            } catch (_) {
                lines.innerHTML = '<p class="field-error">Unable to load order details.</p>';
            }
        }
    } else {
        lines.style.display = 'none';
    }
}

function escHtml(str) {
    return String(str).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}
