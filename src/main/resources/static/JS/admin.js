const API_STORE = "/api/stores";
const API_OFFER = "/offers";

// Load stores into dropdown on init
async function loadStoresDropdown() {
    const res = await fetch(API_STORE);
    const stores = await res.json();
    const select = document.getElementById('storeSelect');
    select.innerHTML = '';

    stores.forEach(s => {
        const opt = document.createElement('option');
        opt.value = s.id;
        opt.innerText = s.name;
        select.appendChild(opt);
    });
}

// 1. Create Store
async function createStore() {
    const data = {
        name: document.getElementById('storeName').value,
        industry: document.getElementById('storeIndustry').value,
        contactEmail: document.getElementById('storeEmail').value,
        isActive: true
    };

    const res = await fetch(`${API_STORE}/create`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(data)
    });

    if(res.ok) {
        alert("Store Added!");
        loadStoresDropdown(); // Refresh dropdown
        document.getElementById('storeName').value = '';
    } else {
        alert("Error adding store");
    }
}

// 2. Create Offer
async function createOffer() {
    const data = {
        storeId: document.getElementById('storeSelect').value,
        title: document.getElementById('offerTitle').value,
        description: document.getElementById('offerDesc').value,
        discountValue: document.getElementById('offerValue').value,
        discountType: document.getElementById('offerType').value,
        expiryDate: document.getElementById('offerDate').value
    };

    const res = await fetch(`${API_OFFER}/create`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(data)
    });

    if(res.ok) {
        alert("Offer Created!");
        document.getElementById('offerTitle').value = '';
        document.getElementById('offerValue').value = '';
    } else {
        alert("Error creating offer");
    }
}

// Init
loadStoresDropdown();