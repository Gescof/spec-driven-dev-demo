document.addEventListener('DOMContentLoaded', async () => {
    await checkSession();
    if (!requireLogin()) return;
    await renderCart();
});

async function renderCart() {
    const emptyEl   = document.getElementById('cart-empty');
    const contentEl = document.getElementById('cart-content');
    const tbody     = document.getElementById('cart-table-body');
    const totalEl   = document.getElementById('grand-total');
    const checkoutEl = document.getElementById('checkout-link');

    try {
        const cart = await apiFetch('GET', '/cart');
        updateCartBadge();

        if (!cart.items || cart.items.length === 0) {
            emptyEl.style.display   = '';
            contentEl.style.display = 'none';
            return;
        }

        emptyEl.style.display   = 'none';
        contentEl.style.display = '';

        const fmt = v => new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(v);
        const hasOos = cart.items.some(i => !i.available);

        tbody.innerHTML = cart.items.map(item => {
            const img = item.productImageUrl
                ? `<img class="cart-table__img" src="${item.productImageUrl}" alt="${escHtml(item.productName)}">`
                : `<div class="cart-table__img" style="background:#e5e7eb"></div>`;
            const oosBadge = item.available ? '' : ' <span class="badge badge--oos">Out of Stock</span>';
            return `
                <tr class="${item.available ? '' : 'cart-row--oos'}" data-product-id="${item.productId}">
                    <td>${img} ${escHtml(item.productName)}${oosBadge}</td>
                    <td>${fmt(item.unitPrice)}</td>
                    <td><input class="cart-table__qty" type="number" min="1" value="${item.quantity}" data-product-id="${item.productId}" aria-label="Quantity for ${escHtml(item.productName)}"></td>
                    <td>${fmt(item.lineTotal)}</td>
                    <td><button class="btn btn--danger" style="padding:.4rem .8rem;min-height:auto" data-remove="${item.productId}">Remove</button></td>
                </tr>`;
        }).join('');

        totalEl.textContent = fmt(cart.grandTotal);

        if (hasOos) {
            checkoutEl.setAttribute('aria-disabled', 'true');
            checkoutEl.style.opacity = '0.5';
            checkoutEl.style.pointerEvents = 'none';
            checkoutEl.title = 'Remove out-of-stock items before checking out';
        } else {
            checkoutEl.removeAttribute('aria-disabled');
            checkoutEl.style.opacity = '';
            checkoutEl.style.pointerEvents = '';
            checkoutEl.title = '';
        }

        tbody.querySelectorAll('.cart-table__qty').forEach(input => {
            input.addEventListener('change', async (e) => {
                const pid = e.target.dataset.productId;
                const qty = parseInt(e.target.value, 10);
                if (qty < 1) { e.target.value = 1; return; }
                try {
                    await apiFetch('PUT', '/cart/items/' + pid, { quantity: qty });
                    await renderCart();
                } catch (_) {}
            });
        });

        tbody.querySelectorAll('[data-remove]').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                const pid = e.target.dataset.remove;
                try {
                    await apiFetch('DELETE', '/cart/items/' + pid);
                    await renderCart();
                } catch (_) {}
            });
        });

    } catch (_) {
        document.getElementById('cart-empty').innerHTML =
            '<p class="field-error">Unable to load cart.</p>';
    }
}

function escHtml(str) {
    return String(str).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}
