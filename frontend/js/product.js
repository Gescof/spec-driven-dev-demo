document.addEventListener('DOMContentLoaded', async () => {
    await checkSession();
    updateCartBadge();

    const id = new URLSearchParams(location.search).get('id');
    const container = document.getElementById('product-detail');

    if (!id) {
        container.innerHTML = '<p class="empty-state">Product not found. <a href="catalog.html">Browse catalog</a></p>';
        return;
    }

    try {
        const p = await apiFetch('GET', '/products/' + id);
        document.title = `Pet Shop — ${p.name}`;
        const price = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(p.price);
        const badgeClass = p.available ? 'badge--in-stock' : 'badge--oos';
        const badgeText = p.available ? 'In Stock' : 'Out of Stock';
        const img = p.imageUrl
            ? `<img class="product-detail__img" src="${p.imageUrl}" alt="${escHtml(p.name)}">`
            : `<div class="product-detail__img" style="background:#e5e7eb"></div>`;

        container.innerHTML = `
            ${img}
            <div class="product-detail__info">
                <h1 class="product-detail__name">${escHtml(p.name)}</h1>
                <p class="product-detail__price">${price}</p>
                <span class="badge ${badgeClass}">${badgeText}</span>
                <p class="product-detail__desc">${escHtml(p.description)}</p>
                <div id="cart-msg"></div>
                <button id="add-to-cart" class="btn btn--primary" ${p.available ? '' : 'disabled'}>
                    ${p.available ? 'Add to Cart' : 'Out of Stock'}
                </button>
            </div>`;

        document.getElementById('add-to-cart').addEventListener('click', () => addToCart(p.id));
    } catch (err) {
        if (err.status === 404) {
            container.innerHTML = '<p class="empty-state">Product not found. <a href="catalog.html">Browse catalog</a></p>';
        } else {
            container.innerHTML = '<p class="empty-state">Unable to load product.</p>';
        }
    }
});

async function addToCart(productId) {
    if (!window.currentUser) {
        location.href = 'login.html?returnUrl=' + encodeURIComponent(location.href);
        return;
    }
    const btn = document.getElementById('add-to-cart');
    const msg = document.getElementById('cart-msg');
    btn.disabled = true;
    try {
        await apiFetch('POST', '/cart/items', { productId, quantity: 1 });
        showToast('Added to cart!');
        updateCartBadge();
        msg.innerHTML = '';
    } catch (err) {
        if (err.status === 401) {
            location.href = 'login.html?returnUrl=' + encodeURIComponent(location.href);
        } else {
            msg.innerHTML = `<p class="field-error">This product is currently out of stock.</p>`;
        }
    } finally {
        btn.disabled = false;
    }
}

function showToast(text) {
    const t = document.createElement('div');
    t.className = 'toast';
    t.textContent = text;
    document.body.appendChild(t);
    setTimeout(() => t.remove(), 2500);
}

function escHtml(str) {
    return String(str).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}
