const API_URL = "http://localhost:8080/auth";

// --- REGISTER ---
const registerForm = document.getElementById('registerForm');
if (registerForm) {
    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const fullName = document.getElementById('fullName').value.split(" ");
        const data = {
            firstName: fullName[0],
            lastName: fullName.length > 1 ? fullName[1] : "",
            email: document.getElementById('email').value,
            phoneNumber: document.getElementById('phone_number').value,
            password: document.getElementById('password').value
        };

        try {
            const response = await fetch(`${API_URL}/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });
            if (response.ok) {
                // Store email for 2FA step
                localStorage.setItem('pendingEmail', data.email);
                window.location.href = 'verify.html';
            } else {
                document.getElementById('errorMsg').innerText = "Registration failed.";
            }
        } catch (err) { console.error(err); }
    });
}

// --- LOGIN ---
const loginForm = document.getElementById('loginForm');
if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;

        try {
            const response = await fetch(`${API_URL}/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            });
            if (response.ok) {
                localStorage.setItem('pendingEmail', email);
                window.location.href = 'verify.html';
            } else {
                document.getElementById('errorMsg').innerText = "Invalid credentials.";
            }
        } catch (err) { console.error(err); }
    });
}

// --- 2FA VERIFY ---
async function verify2FA() {
    const code = document.getElementById('2faCode').value;
    const email = localStorage.getItem('pendingEmail');

    const response = await fetch(`${API_URL}/verify-2fa`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: email, code: code })
    });

    if (response.ok) {
        window.location.href = 'dashboard.html'; // Create a dummy dashboard.html
    } else {
        document.getElementById('errorMsg').innerText = "Invalid Code";
    }
}