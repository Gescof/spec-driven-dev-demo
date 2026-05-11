document.addEventListener('DOMContentLoaded', async () => {
    const session = await checkSession();
    if (session) { location.href = 'account.html'; return; }

    document.getElementById('login-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const errorEl = document.getElementById('login-error');
        errorEl.style.display = 'none';

        const email    = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value;

        try {
            await apiFetch('POST', '/auth/login', { email, password });
            const returnUrl = new URLSearchParams(location.search).get('returnUrl');
            location.href = returnUrl || 'account.html';
        } catch (err) {
            errorEl.textContent = err.status === 401
                ? 'Invalid email or password.'
                : 'Login failed. Please try again.';
            errorEl.style.display = '';
        }
    });
});
