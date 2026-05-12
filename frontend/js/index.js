document.addEventListener('DOMContentLoaded', async () => {
    await checkSession();
    updateCartBadge();

    await loadCategories();

    const grid = document.getElementById('popular-products-grid');
    try {
        const products = await apiFetch('GET', '/products/featured');
        if (!products || products.length === 0) {
            grid.innerHTML = '<p class="empty-state">No featured products yet.</p>';
            return;
        }
        grid.innerHTML = products.slice(0, 4).map(renderCard).join('');
    } catch (err) {
        console.error('Failed to load featured products:', err);
        grid.innerHTML = '<p class="empty-state">Unable to load products.</p>';
    }
});

async function loadCategories() {
    const grid = document.getElementById('categories-grid');
    if (!grid) return;

    const imageMap = {
        cat:    'images/cat.jpg',
        dog:    'images/dog.jpg',
        bird:   'images/bird.jpg',
        fish:   'images/small-pet.jpg',
        small:  'images/small-pet.jpg',
        rabbit: 'images/small-pet.jpg',
        rodent: 'images/small-pet.jpg',
    };

    function categoryImage(name) {
        const lower = name.toLowerCase();
        for (const [key, src] of Object.entries(imageMap)) {
            if (lower.includes(key)) return src;
        }
        return 'images/small-pet.jpg';
    }

    try {
        const categories = await apiFetch('GET', '/categories');
        if (!categories || categories.length === 0) {
            grid.closest('section').style.display = 'none';
            return;
        }

        grid.innerHTML = categories.map(cat => {
            const imgSrc = categoryImage(cat.name);
            const imgEl = imgSrc
                ? `<img class="category-card__img" src="${imgSrc}" alt="${escHtml(cat.name)} category">`
                : `<div class="category-card__img category-card--no-img">No image</div>`;
            return `<a class="category-card" href="catalog.html?categoryId=${cat.id}">${imgEl}<span class="category-card__label">${escHtml(cat.name)}</span></a>`;
        }).join('');

        const navDogs  = document.getElementById('nav-dogs');
        const navCats  = document.getElementById('nav-cats');
        const navBirds = document.getElementById('nav-birds');

        for (const cat of categories) {
            const lower = cat.name.toLowerCase();
            if (navDogs  && lower.includes('dog'))  navDogs.href  = `catalog.html?categoryId=${cat.id}`;
            if (navCats  && lower.includes('cat'))  navCats.href  = `catalog.html?categoryId=${cat.id}`;
            if (navBirds && lower.includes('bird')) navBirds.href = `catalog.html?categoryId=${cat.id}`;
        }
    } catch (err) {
        console.error('Failed to load categories:', err);
        grid.closest('section').style.display = 'none';
    }
}

function renderCard(p) {
    const price = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(p.price);
    const img = p.imageUrl
        ? `<img class="product-card__img" src="${p.imageUrl}" alt="${escHtml(p.name)}" loading="lazy">`
        : `<div class="product-card__img product-card--no-img" style="aspect-ratio:4/3;display:flex;align-items:center;justify-content:center">No image</div>`;
    return `
        <a class="product-card--warm" href="product.html?id=${p.id}">
            ${img}
            <div class="product-card__body">
                <span class="product-card__name">${escHtml(p.name)}</span>
                <span class="product-card__price">${price}</span>
            </div>
        </a>`;
}

function escHtml(str) {
    return String(str).replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;').replaceAll('"','&quot;');
}
