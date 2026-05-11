document.addEventListener('DOMContentLoaded', async () => {
    await checkSession();
    if (!requireLogin()) return;

    const fmt = v => new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(v);

    try {
        const cart = await apiFetch('GET', '/cart');
        if (!cart.items || cart.items.length === 0) {
            location.href = 'cart.html';
            return;
        }
        const itemsEl = document.getElementById('summary-items');
        const totalEl = document.getElementById('summary-total');
        itemsEl.innerHTML = cart.items.map(i =>
            `<li><span>${escHtml(i.productName)} × ${i.quantity}</span><span>${fmt(i.lineTotal)}</span></li>`
        ).join('');
        totalEl.textContent = fmt(cart.grandTotal);
    } catch (_) {
        location.href = 'cart.html';
        return;
    }

    document.getElementById('checkout-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const errEl = document.getElementById('checkout-error');
        errEl.style.display = 'none';

        const cardholderName = document.getElementById('cardholder-name').value.trim();
        const cardNumber     = document.getElementById('card-number').value.replace(/\D/g, '');

        if (cardNumber.length !== 16) {
            errEl.textContent = 'Please enter a valid 16-digit card number.';
            errEl.style.display = '';
            return;
        }
        const cardNumberLast4 = cardNumber.slice(-4);

        try {
            const order = await apiFetch('POST', '/orders', { cardholderName, cardNumberLast4 });
            document.getElementById('checkout-main').style.display = 'none';
            const confirmEl = document.getElementById('order-confirmation');
            confirmEl.style.display = '';
            document.getElementById('confirmed-order-number').textContent = order.orderNumber;
        } catch (err) {
            if (err.status === 400) {
                errEl.textContent = (err.data?.message || 'Order failed.') + ' — ';
                const link = document.createElement('a');
                link.href = 'cart.html';
                link.textContent = 'Return to cart';
                errEl.appendChild(link);
            } else {
                errEl.textContent = 'Order failed. Please try again.';
            }
            errEl.style.display = '';
        }
    });
});

function escHtml(str) {
    return String(str).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}
