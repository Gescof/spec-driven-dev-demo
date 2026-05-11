document.addEventListener('DOMContentLoaded', async () => {
    await checkSession();
    updateCartBadge();

    const grid = document.getElementById('featured-products');
    try {
        const products = await apiFetch('GET', '/products/featured');
        if (!products || products.length === 0) {
            grid.innerHTML = '<p class="empty-state">No featured products yet.</p>';
            return;
        }
        grid.innerHTML = products.map(renderCard).join('');
    } catch (_) {
        grid.innerHTML = '<p class="empty-state">Unable to load products.</p>';
    }
});

function renderCard(p) {
    const price = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(p.price);
    const img = p.imageUrl
        ? `<img class="product-card__img" src="${p.imageUrl}" alt="${escHtml(p.name)}" loading="lazy">`
        : `<div class="product-card__img" style="background:#e5e7eb;display:flex;align-items:center;justify-content:center;color:#9ca3af">No image</div>`;
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
