document.addEventListener('DOMContentLoaded', async () => {
    const session = await checkSession();
    if (session) { location.href = 'account.html'; return; }

    document.getElementById('register-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        clearErrors();
        const email    = document.getElementById('email').value.trim();
        const name     = document.getElementById('name').value.trim();
        const password = document.getElementById('password').value;

        try {
            await apiFetch('POST', '/auth/register', { email, name, password });
            location.href = 'account.html';
        } catch (err) {
            if (err.status === 400 && err.data?.fieldErrors) {
                showFieldErrors(err.data.fieldErrors);
            } else if (err.status === 409) {
                showError('register-error', 'Email already taken. Try logging in.');
            } else {
                showError('register-error', err.message || 'Registration failed. Please try again.');
            }
        }
    });
});

function clearErrors() {
    ['register-error','name-error','email-error','password-error'].forEach(id => {
        const el = document.getElementById(id);
        if (el) { el.textContent = ''; el.style.display = 'none'; }
    });
}

function showError(id, msg) {
    const el = document.getElementById(id);
    if (el) { el.textContent = msg; el.style.display = ''; }
}

function showFieldErrors(fieldErrors) {
    Object.entries(fieldErrors).forEach(([field, msg]) => {
        const el = document.getElementById(field + '-error');
        if (el) { el.textContent = msg; el.style.display = ''; }
    });
}
