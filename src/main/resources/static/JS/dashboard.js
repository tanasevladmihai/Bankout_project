let currentSlide = 0;
let selectedAccountId = null;
let chartInstance = null;

// --- CAROUSEL ---
function goToSlide(index) {
    currentSlide = index;
    document.getElementById('mainCarousel').style.transform = `translateX(-${index * 33.33}%)`;
    document.querySelectorAll('.dot').forEach((d, i) => d.classList.toggle('active', i === index));
}

// --- SLIDE 1: ACCOUNTS ---
async function loadAccounts() {
    const res = await fetch('/accounts/my-accounts');
    const accounts = await res.json();
    const list = document.getElementById('accountsList');
    list.innerHTML = '';

    accounts.forEach(acc => {
        const div = document.createElement('div');
        div.className = 'account-item';
        div.innerHTML = `
            <div>
                <div>${acc.accountNumber}</div>
                <small>Savings</small>
            </div>
            <div class="acc-balance">${acc.balance} €</div>
            <div class="acc-dots" onclick="openActionModal(${acc.id})">•••</div>
        `;
        list.appendChild(div);
    });
}

function toggleAccountMenu() {
    const el = document.getElementById('accDropdown');
    el.style.display = el.style.display === 'block' ? 'none' : 'block';
}

function openActionModal(id) {
    selectedAccountId = id;
    document.getElementById('actionModal').classList.remove('hidden');
}

function showWireIn() {
    document.getElementById('actionModal').classList.add('hidden');
    document.getElementById('wireInModal').classList.remove('hidden');
}

async function submitWireIn() {
    const amount = document.getElementById('wireAmount').value;
    await fetch('/accounts/wire-in', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({ accountId: selectedAccountId, amount: amount })
    });
    closeModal();
    await loadAccounts(); // Refresh UI
}

function closeModal() {
    document.querySelectorAll('.modal').forEach(m => m.classList.add('hidden'));
}

// --- SLIDE 2: GRAPH ---
async function loadGraph(period) {
    const res = await fetch(`/reports/spending?period=${period}`);
    const data = await res.json();

    const labels = data.map(d => d.label);
    const rentData = data.map(d => d.Rent || 0);
    const foodData = data.map(d => d.Food || 0);

    if(chartInstance) chartInstance.destroy();


    const ctx = document.getElementById('spendingChart').getContext('2d');
    // noinspection JSUnresolvedReference
    chartInstance = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [
                { label: 'Rent', data: rentData, backgroundColor: '#d16b5b' },
                { label: 'Food', data: foodData, backgroundColor: '#e0985f' }
            ]
        },
        options: {
            scales: { x: { stacked: true }, y: { stacked: true } }
        }
    });

    // Update Text
    document.getElementById('periodText').innerText = period.toLowerCase() + " ﹀";
    document.getElementById('periodDropdown').style.display = 'none';
}

function togglePeriodMenu() {
    const el = document.getElementById('periodDropdown');
    el.style.display = el.style.display === 'block' ? 'none' : 'block';
}

// --- SLIDE 3: STORES ---
// Mocking store data for now
const stores = [
    { name: 'Smart Store Chain', discounts: ['10% Off', 'Buy 2 Get 1', 'Free Delivery'] },
    { name: 'Petite Perfumes', discounts: ['50% Off', 'Gift Card'] }
];

function loadStores() {
    const container = document.getElementById('storesList');
    container.innerHTML = '';

    stores.forEach(store => {
        let discountHTML = '';
        store.discounts.forEach(d => {
            discountHTML += `<div class="discount-box" onclick="this.classList.add('claimed'); this.innerText='CODE: 1234'">${d}</div>`;
        });

        const row = document.createElement('div');
        row.className = 'store-row';
        row.innerHTML = `
            <img src="https://via.placeholder.com/100x50?text=${store.name.substring(0,3)}" class="store-logo" alt="${store.name} logo"> 
            <div class="discount-carousel">${discountHTML}</div>
        `;
        container.appendChild(row);
    });
}

// INIT
window.onload = async function() {
    await loadAccounts();
    await loadGraph('MONTHLY');
    loadStores();
};