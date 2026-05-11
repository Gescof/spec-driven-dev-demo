window.currentUser = null;

async function checkSession() {
    try {
        window.currentUser = await apiFetch('GET', '/auth/me');
    } catch (_) {
        window.currentUser = null;
    }
    updateNavForAuthState();
    return window.currentUser;
}

function requireLogin(returnUrl) {
    if (!window.currentUser) {
        const url = returnUrl || location.href;
        location.href = 'login.html?returnUrl=' + encodeURIComponent(url);
        return false;
    }
    return true;
}

function updateNavForAuthState() {
    const guestEls = document.querySelectorAll('.nav__auth-guest');
    const userEls  = document.querySelectorAll('.nav__auth-user');
    if (window.currentUser) {
        guestEls.forEach(el => el.style.display = 'none');
        userEls.forEach(el  => el.style.display = '');
    } else {
        guestEls.forEach(el => el.style.display = '');
        userEls.forEach(el  => el.style.display = 'none');
    }

    const logoutBtn = document.getElementById('nav-logout');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', async (e) => {
            e.preventDefault();
            try { await apiFetch('POST', '/auth/logout'); } catch (_) {}
            window.currentUser = null;
            location.href = 'index.html';
        });
    }
}

async function updateCartBadge() {
    const badge = document.getElementById('nav-cart-count');
    if (!badge || !window.currentUser) return;
    try {
        const cart = await apiFetch('GET', '/cart');
        if (cart && cart.itemCount > 0) {
            badge.textContent = cart.itemCount;
            badge.style.display = '';
        } else {
            badge.style.display = 'none';
        }
    } catch (_) {
        badge.style.display = 'none';
    }
}
