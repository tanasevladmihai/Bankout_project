let currentSlide = 0;
let selectedAccountId = null;
let chartInstance = null;
let deleteMode = false;

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

            // Logic: If deleteMode is ON, show a Red Delete Button. Otherwise, show "•••"
            let actionHtml = deleteMode
                ? `<div class="acc-dots" style="color:red; font-weight:bold;" onclick="deleteAccount(${acc.id})">✖ DELETE</div>`
                : `<div class="acc-dots" onclick="openActionModal(${acc.id})">•••</div>`;

            div.innerHTML = `
                <div>
                    <div style="font-weight:bold; color:#555;">${acc.accountName || 'General'}</div>
                    <small style="color:#888;">${acc.accountNumber}</small>
                </div>
                <div class="acc-balance">${acc.balance} €</div>
                ${actionHtml}
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
// --- SLIDE 3: STORES & OFFERS ---
async function loadStores() {
    try {
        // 1. Fetch All Stores (with their offers)
        const storesRes = await fetch('/api/stores');
        if (!storesRes.ok) throw new Error("Failed to load stores");
        const stores = await storesRes.json();

        // 2. Fetch User's Claimed Offers (The "Exclusivity" Logic)
        // Hardcoded user 1 for prototype
        const myRedemptionsRes = await fetch('/offers/my-redemptions?userId=1');
        let myRedemptions = [];
        if (myRedemptionsRes.ok) {
            myRedemptions = await myRedemptionsRes.json();
        }

        // Helper: Find if I already have a code for a specific offer ID
        const getClaimedCode = (offerId) => {
            const redemption = myRedemptions.find(r => r.offer.id === offerId);
            return redemption ? redemption.usageCode : null;
        };

        const container = document.getElementById('storesList');
        container.innerHTML = '';

        if (stores.length === 0) {
            container.innerHTML = '<p style="text-align:center; padding:20px;">No stores available</p>';
            return;
        }

        stores.forEach(store => {
            let discountHTML = '';

            if (store.offers && store.offers.length > 0) {
                store.offers.forEach(offer => {
                    const existingCode = getClaimedCode(offer.id);

                    if (existingCode) {
                        // RENDER CLAIMED STATE (Greyed out, Code visible)
                        discountHTML += `
                            <div class="discount-box claimed">
                                <div style="font-weight:bold; color:#6f935f;">CODE: ${existingCode}</div>
                                <small>${offer.title}</small>
                            </div>`;
                    } else {
                        // RENDER UNCLAIMED STATE (Clickable)
                        discountHTML += `
                            <div class="discount-box" id="offer-box-${offer.id}" onclick="claimOffer(${offer.id})">
                                <div class="offer-title">${offer.title}</div>
                                <small>${offer.discountValue} ${offer.discountType || 'OFF'}</small>
                                <div class="click-reveal">Click to Reveal</div>
                            </div>`;
                    }
                });
            } else {
                discountHTML = '<div class="discount-box" style="cursor:default; border:none; background:#eee;">No Active Offers</div>';
            }

            const row = document.createElement('div');
            row.className = 'store-row';
            row.innerHTML = `
                <div style="text-align:center; width: 100px;">
                     <img src="https://via.placeholder.com/100x50?text=${store.name.substring(0,3)}" class="store-logo" alt="logo">
                     <div style="font-size:0.8rem; font-weight:bold;">${store.name}</div>
                </div>
                <div class="discount-carousel">${discountHTML}</div>
            `;
            container.appendChild(row);
        });
    } catch (error) {
        console.error("Store load error:", error);
    }
}

// Logic to call backend and save to DB
async function claimOffer(offerId) {
    try {
        const res = await fetch(`/offers/${offerId}/redeem?userId=1`, { method: 'POST' });

        if (res.ok) {
            const redemption = await res.json();
            const code = redemption.usageCode;

            // Update UI instantly without reloading
            const box = document.getElementById(`offer-box-${offerId}`);
            if (box) {
                box.classList.add('claimed');
                box.onclick = null; // Remove click listener
                box.innerHTML = `
                    <div style="font-weight:bold; color:#6f935f;">CODE: ${code}</div>
                    <small>Discount Unlocked!</small>
                `;
            }
        } else {
            alert("Error claiming offer.");
        }
    } catch (e) {
        console.error(e);
    }
}

function createNewAccount() {
    // Hide the dropdown menu if it's open
    document.getElementById('accDropdown').style.display = 'none';
    // Show the modal
    document.getElementById('createAccountModal').classList.remove('hidden');
}

async function submitCreateAccount() {
    const name = document.getElementById('newAccountName').value;
    const number = document.getElementById('newAccountNumber').value;

    if (!name || !number) {
        alert("Please enter both a Title and Account Number.");
        return;
    }

    try {
        const response = await fetch('/accounts/create', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({
                accountName: name,
                accountNumber: number
            })
        });

        if (response.ok) {
            alert("Account created successfully!");
            closeModal();
            // Clear inputs
            document.getElementById('newAccountName').value = '';
            document.getElementById('newAccountNumber').value = '';
            // Refresh list
            await loadAccounts();
        } else {
            alert("Failed to create account.");
        }
    } catch (err) {
        console.error(err);
        alert("Error connecting to server.");
    }
}

function toggleDeleteMode() {
    deleteMode = !deleteMode;
    // Hide the dropdown
    document.getElementById('accDropdown').style.display = 'none';
    // Refresh the list to show/hide delete buttons
    loadAccounts();
}

async function deleteAccount(id) {
    if(!confirm("Are you sure? This will delete the account and all its transaction history.")) return;

    try {
        const res = await fetch(`/accounts/${id}`, { method: 'DELETE' });
        if(res.ok) {
            alert("Account deleted.");
            loadAccounts(); // Refresh list
        } else {
            alert("Could not delete account.");
        }
    } catch(e) {
        console.error(e);
    }
}
function confirmDelete() {
    // Hide the popup first
    closeModal();

    // Use the global variable 'selectedAccountId' set by openActionModal()
    if (selectedAccountId) {
        deleteAccount(selectedAccountId);
    }
}
// INIT
window.onload = async function() {
    // We use await to ensure one finishing doesn't block the next if it fails
    await loadAccounts();
    await loadGraph('MONTHLY');
    await loadStores();
};