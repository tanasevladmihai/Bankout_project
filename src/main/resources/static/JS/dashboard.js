let currentSlide = 0;
let selectedAccountId = null;
let chartInstance = null;
let deleteMode = false;


async function checkSession() {
    try {
        const res = await fetch('/auth/check');
        if (!res.ok) {
            // Not authenticated - redirect to login
            window.location.href = 'login.html';
            return false;
        }
        return true;
    } catch (err) {
        console.error("Session check failed:", err);
        window.location.href = 'login.html';
        return false;
    }
}

async function logout() {
    try {
        await fetch('/auth/logout', { method: 'POST' });
    } catch (err) {
        console.error("Logout error:", err);
    }
    window.location.href = 'index.html';
}

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
        if (res.status === 401) {
            window.location.href = 'login.html';
            return;
        }
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
        if (res.status === 401) {
            window.location.href = 'login.html';
            return;
        }
        if (!res.ok) throw new Error("Failed to load graph data");

        const data = await res.json();
        const allCategories = new Set();
        data.forEach(d => {
            Object.keys(d).forEach(key => {
                if (key !== 'label') allCategories.add(key);
            });
        });

        const labels = data.map(d => d.label);
        const colors = ['#d16b5b', '#e0985f', '#4a90e2', '#6f935f', '#9b59b6', '#e74c3c'];

        const datasets = Array.from(allCategories).map((category, index) => ({
            label: category,
            data: data.map(d => d[category] || 0),
            backgroundColor: colors[index % colors.length]
        }));

        if(chartInstance) chartInstance.destroy();

        const ctx = document.getElementById('spendingChart').getContext('2d');
        chartInstance = new Chart(ctx, {
            type: 'bar',
            data: { labels: labels, datasets: datasets },
            options: {
                scales: {
                    x: { stacked: true },
                    y: { stacked: true, beginAtZero: true }
                },
                maintainAspectRatio: false,
                responsive: true
            }
        });document.getElementById('periodText').innerText = period.toLowerCase() + " ﹀";
        document.getElementById('periodDropdown').style.display = 'none';

        // Load budget comparison
        await loadBudgetComparison(period);
    } catch (error) {
        console.error(error);}
}


function togglePeriodMenu() {
    const el = document.getElementById('periodDropdown');
    el.style.display = el.style.display === 'block' ? 'none' : 'block';
}

// --- SLIDE 3: STORES ---
async function loadStores() {
    try {
        const storesRes = await fetch('/api/stores');
        if (storesRes.status === 401) {
            window.location.href = 'login.html';
            return;
        }
        if (!storesRes.ok) throw new Error("Failed to load stores");
        const stores = await storesRes.json();

        // REMOVED: ?userId=1 parameter
        const myRedemptionsRes = await fetch('/offers/my-redemptions');
        let myRedemptions = [];
        if (myRedemptionsRes.ok) {
            myRedemptions = await myRedemptionsRes.json();
        }

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
                        discountHTML += `
                            <div class="discount-box claimed">
                                <div style="font-weight:bold; color:#6f935f;">CODE: ${existingCode}</div>
                                <small>${offer.title}</small>
                            </div>`;
                    } else {
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

// REMOVED: ?userId=1 parameter
async function claimOffer(offerId) {
    try {
        const res = await fetch(`/offers/${offerId}/redeem`, { method: 'POST' });

        if (res.ok) {
            const redemption = await res.json();
            const code = redemption.usageCode;

            const box = document.getElementById(`offer-box-${offerId}`);
            if (box) {
                box.classList.add('claimed');
                box.onclick = null;
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
    document.getElementById('accDropdown').style.display = 'none';
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
            document.getElementById('newAccountName').value = '';
            document.getElementById('newAccountNumber').value = '';
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
    document.getElementById('accDropdown').style.display = 'none';
    loadAccounts();
}

async function deleteAccount(id) {
    if(!confirm("Are you sure? This will delete the account and all its transaction history.")) return;

    try {
        const res = await fetch(`/accounts/${id}`, { method: 'DELETE' });
        if(res.ok) {
            alert("Account deleted.");
            loadAccounts();
        } else {
            alert("Could not delete account.");
        }
    } catch(e) {
        console.error(e);
    }
}

function confirmDelete() {
    closeModal();
    if (selectedAccountId) {
        deleteAccount(selectedAccountId);
    }
}

function toggleUserMenu() {
    const dropdown = document.getElementById('userDropdown');
    dropdown.style.display = dropdown.style.display === 'block' ? 'none' : 'block';
}

function initiateDeleteAccount() {
    document.getElementById('userDropdown').style.display = 'none';
    document.getElementById('deleteLoginModal').classList.remove('hidden');
}

async function submitDeleteLogin() {
    const email = document.getElementById('deleteEmail').value;
    const password = document.getElementById('deletePassword').value;
    const errorEl = document.getElementById('deleteLoginError');

    if (!email || !password) {
        errorEl.textContent = "Please enter both email and password";
        errorEl.style.display = 'block';
        return;
    }

    try {
        const response = await fetch('/auth/verify-for-deletion', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        if (response.ok) {
            // Credentials verified, show 2FA modal
            document.getElementById('deleteLoginModal').classList.add('hidden');
            document.getElementById('delete2FAModal').classList.remove('hidden');
        } else {
            errorEl.textContent = "Invalid credentials";
            errorEl.style.display = 'block';
        }
    } catch (err) {
        console.error(err);
        errorEl.textContent = "Connection error";
        errorEl.style.display = 'block';
    }
}

async function confirmAccountDeletion() {
    const code = document.getElementById('delete2FACode').value;
    const email = document.getElementById('deleteEmail').value;
    const errorEl = document.getElementById('delete2FAError');

    if (!code || code.length !== 4) {
        errorEl.textContent = "Please enter a valid 4-digit code";
        errorEl.style.display = 'block';
        return;
    }

    try {
        const response = await fetch('/auth/delete-account', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, code })
        });

        if (response.ok) {
            alert("Your account has been permanently deleted.");
            window.location.href = 'index.html';
        } else {
            const errorText = await response.text();
            errorEl.textContent = errorText || "Invalid 2FA code";
            errorEl.style.display = 'block';
        }
    } catch (err) {
        console.error(err);
        errorEl.textContent = "An error occurred";
        errorEl.style.display = 'block';
    }
}

// --- BUDGET FUNCTIONS ---
let currentPeriod = 'MONTHLY';

async function loadBudgetComparison(period) {
    currentPeriod = period;
    try {
        const res = await fetch(`/reports/current-spending?period=${period}`);
        if (!res.ok) return;

        const data = await res.json();
        const messageEl = document.getElementById('budgetMessage');

        if (!data.hasBudget) {
            messageEl.style.display = 'none';
            return;
        }

        const spending = data.spending.toFixed(2);
        const budget = data.budget.toFixed(2);
        const periodText = period === 'DAILY' ? 'today' : period === 'WEEKLY' ? 'this week' : 'this month';

        if (data.exceeded) {
            messageEl.className = 'exceeded';
            messageEl.innerHTML = `⚠️ You've spent more than your planned €${budget} ${periodText}<br><small>Current spending: €${spending}</small>`;
        } else {
            messageEl.className = 'under-budget';
            messageEl.innerHTML = `✓ You've spent less than your planned €${budget} ${periodText}<br><small>Current spending: €${spending}</small>`;
        }

        messageEl.style.display = 'block';
    } catch (error) {
        console.error("Budget comparison error:", error);
    }
}

function openBudgetModal() {
    // Set the current period in the dropdown
    document.getElementById('budgetPeriod').value = currentPeriod;
    document.getElementById('budgetModal').classList.remove('hidden');

    // Load current budget if exists
    loadCurrentBudget(currentPeriod);
}

async function loadCurrentBudget(period) {
    try {
        const res = await fetch(`/budgets/${period}`);
        if (res.ok) {
            const data = await res.json();
            if (data.amount && data.amount > 0) {
                document.getElementById('budgetAmount').value = data.amount;
            }
        }
    } catch (error) {
        console.error("Load budget error:", error);
    }
}

async function submitBudget() {
    const period = document.getElementById('budgetPeriod').value;
    const amount = document.getElementById('budgetAmount').value;
    const errorEl = document.getElementById('budgetError');

    if (!amount || parseFloat(amount) <= 0) {
        errorEl.textContent = "Please enter a valid amount";
        errorEl.style.display = 'block';
        return;
    }

    try {
        const res = await fetch('/budgets/set', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ period, amount })
        });

        if (res.ok) {
            alert("Budget set successfully!");
            closeModal();
            document.getElementById('budgetAmount').value = '';
            errorEl.style.display = 'none';

            // Reload budget comparison if on the same period
            if (period === currentPeriod) {
                await loadBudgetComparison(currentPeriod);
            }
        } else {
            errorEl.textContent = "Failed to set budget";
            errorEl.style.display = 'block';
        }
    } catch (error) {
        console.error("Submit budget error:", error);
        errorEl.textContent = "Error connecting to server";
        errorEl.style.display = 'block';
    }
}

// INIT - Check session first
window.onload = async function() {
    const authenticated = await checkSession();
    if (!authenticated) return;

    await loadAccounts();
    await loadGraph('MONTHLY');
    await loadStores();

    // Close dropdown when clicking outside
    window.onclick = function(event) {
        if (!event.target.closest('.user-icon')) {
            const dropdown = document.getElementById('userDropdown');
            if (dropdown && dropdown.style.display === 'block') {
                dropdown.style.display = 'none';
            }
        }
    }
};