let activeCategoryId = null;
let searchDebounce = null;

document.addEventListener('DOMContentLoaded', async () => {
    await checkSession();
    updateCartBadge();
    await Promise.all([loadCategories(), loadProducts()]);

    document.getElementById('search-input').addEventListener('input', (e) => {
        clearTimeout(searchDebounce);
        searchDebounce = setTimeout(() => loadProducts(e.target.value.trim()), 300);
    });
});

async function loadCategories() {
    const container = document.getElementById('category-filters');
    try {
        const cats = await apiFetch('GET', '/categories');
        const allBtn = makeFilterBtn('All', null);
        container.appendChild(allBtn);
        cats.forEach(c => container.appendChild(makeFilterBtn(c.name, c.id)));
        allBtn.classList.add('filter-btn--active');
    } catch (_) {}
}

function makeFilterBtn(label, categoryId) {
    const btn = document.createElement('button');
    btn.className = 'filter-btn';
    btn.textContent = label;
    btn.addEventListener('click', () => {
        document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('filter-btn--active'));
        btn.classList.add('filter-btn--active');
        activeCategoryId = categoryId;
        loadProducts(document.getElementById('search-input').value.trim());
    });
    return btn;
}

async function loadProducts(search = '') {
    const grid = document.getElementById('product-grid');
    const noResults = document.getElementById('no-results');
    grid.innerHTML = '<p class="loading">Loading…</p>';
    noResults.style.display = 'none';

    const params = new URLSearchParams();
    if (activeCategoryId) params.set('categoryId', activeCategoryId);
    if (search) params.set('search', search);
    const qs = params.toString() ? '?' + params.toString() : '';

    try {
        const data = await apiFetch('GET', '/products' + qs);
        const products = data.content || [];
        if (products.length === 0) {
            grid.innerHTML = '';
            noResults.style.display = '';
            return;
        }
        grid.innerHTML = products.map(renderCard).join('');
    } catch (_) {
        grid.innerHTML = '<p class="empty-state">Unable to load products.</p>';
    }
}

function renderCard(p) {
    const price = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(p.price);
    const img = p.imageUrl
        ? `<img class="product-card__img" src="${p.imageUrl}" alt="${escHtml(p.name)}" loading="lazy">`
        : `<div class="product-card__img" style="background:#e5e7eb"></div>`;
    return `
        <a class="product-card" href="product.html?id=${p.id}" style="text-decoration:none;color:inherit">
            ${img}
            <div class="product-card__body">
                <span class="product-card__name">${escHtml(p.name)}</span>
                <span class="product-card__price">${price}</span>
            </div>
        </a>`;
}

function escHtml(str) {
    return String(str).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}
