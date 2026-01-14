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
    try {
        const res = await fetch('/accounts/my-accounts');
        if (!res.ok) throw new Error("Failed to load accounts");

        const accounts = await res.json();
        const list = document.getElementById('accountsList');
        list.innerHTML = '';

        if (accounts.length === 0) {
            list.innerHTML = '<div style="padding:20px; text-align:center;">No accounts found.</div>';
            return;
        }

        accounts.forEach(acc => {
            const div = document.createElement('div');
            div.className = 'account-item';
            // Note: IntelliJ might flag 'accountNumber', but it is correct if your Java class has it.
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
    } catch (error) {
        console.error(error);
    }
}

// ... (Keep your existing toggleAccountMenu, openActionModal, showWireIn, submitWireIn, closeModal functions here) ...

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
    await loadAccounts();
}

// --- ADD THESE FUNCTIONS ---

function showSendFunds() {
    document.getElementById('actionModal').classList.add('hidden'); // Hide the menu
    document.getElementById('sendFundsModal').classList.remove('hidden'); // Show the form
}

async function submitSendFunds() {
    const amount = document.getElementById('sendAmount').value;
    const recipient = document.getElementById('recipientName').value;
    const iban = document.getElementById('recipientIban').value;

    if (!amount || !recipient || !iban) {
        alert("Please fill in all fields");
        return;
    }

    try {
        const response = await fetch('/accounts/send-funds', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({
                accountId: selectedAccountId,
                amount: amount,
                recipientName: recipient,
                recipientIban: iban
            })
        });

        if (!response.ok) {
            const errorText = await response.text();
            alert("Transfer failed: " + errorText);
        } else {
            alert("Transfer successful!");
            closeModal();
            await loadAccounts(); // Refresh the list to show new balance
        }
    } catch (err) {
        console.error(err);
        alert("An error occurred during transfer.");
    }
}

function closeModal() {
    document.querySelectorAll('.modal').forEach(m => m.classList.add('hidden'));
}


// --- SLIDE 2: GRAPH ---
async function loadGraph(period) {
    try {
        const res = await fetch(`/reports/spending?period=${period}`);
        if (!res.ok) throw new Error("Failed to load graph data");

        const data = await res.json();

        // IntelliJ warns about "Rent" because it's dynamic JSON, but it exists in your Java map.
        // We use (d.Rent || 0) to be safe if the key is missing.
        const labels = data.map(d => d.label);
        const rentData = data.map(d => d.Rent || 0);
        const foodData = data.map(d => d.Food || 0);
        const clothesData = data.map(d => d.Clothes || 0);

        if(chartInstance) chartInstance.destroy();

        const ctx = document.getElementById('spendingChart').getContext('2d');
        chartInstance = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [
                    { label: 'Rent', data: rentData, backgroundColor: '#d16b5b' },
                    { label: 'Food', data: foodData, backgroundColor: '#e0985f' },
                    { label: 'Clothes', data: clothesData, backgroundColor: '#4a90e2' }
                ]
            },
            options: {
                scales: { x: { stacked: true }, y: { stacked: true } },
                maintainAspectRatio: false
            }
        });

        document.getElementById('periodText').innerText = period.toLowerCase() + " ﹀";
        document.getElementById('periodDropdown').style.display = 'none';
    } catch (error) {
        console.error(error);
    }
}

function togglePeriodMenu() {
    const el = document.getElementById('periodDropdown');
    el.style.display = el.style.display === 'block' ? 'none' : 'block';
}

// --- SLIDE 3: STORES ---
async function loadStores() {
    try {
        // Fetch REAL data from your backend
        const res = await fetch('/api/stores');
        if (!res.ok) throw new Error("Failed to load stores");

        const stores = await res.json();
        const container = document.getElementById('storesList');
        container.innerHTML = '';

        if (stores.length === 0) {
            container.innerHTML = '<p style="text-align:center; padding:20px;">No stores available</p>';
            return;
        }

        stores.forEach(store => {
            let discountHTML = '';
            // Backend returns 'offers', make sure to loop over them
            if (store.offers && store.offers.length > 0) {
                store.offers.forEach(offer => {
                    discountHTML += `
                        <div class="discount-box" onclick="this.classList.add('claimed'); this.innerText='CODE: BNK-${offer.id}'">
                            ${offer.title}<br>
                            <small>${offer.discountValue} Off</small>
                        </div>`;
                });
            } else {
                discountHTML = '<div class="discount-box" style="cursor:default; border:none; background:#eee;">No Active Offers</div>';
            }

            const row = document.createElement('div');
            row.className = 'store-row';
            row.innerHTML = `
                <img src="https://via.placeholder.com/100x50?text=${store.name.substring(0,3)}" class="store-logo" alt="logo"> 
                <div class="discount-carousel">${discountHTML}</div>
            `;
            container.appendChild(row);
        });
    } catch (error) {
        console.error("Store load error:", error);
    }
}

// INIT
window.onload = async function() {
    // We use await to ensure one finishing doesn't block the next if it fails
    await loadAccounts();
    await loadGraph('MONTHLY');
    await loadStores();
};