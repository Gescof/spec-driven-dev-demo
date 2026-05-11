const BASE_URL = 'http://localhost:8080/api/v1';

async function apiFetch(method, path, body) {
    const opts = {
        method,
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
    };
    if (body !== undefined) opts.body = JSON.stringify(body);

    const res = await fetch(BASE_URL + path, opts);
    if (res.status === 204) return null;

    let data;
    try { data = await res.json(); } catch (_) { data = null; }

    if (!res.ok) {
        const err = new Error(data?.message || `HTTP ${res.status}`);
        err.status = res.status;
        err.data = data;
        throw err;
    }
    return data;
}
