// Handle form submission
const loginForm = document.getElementById('loginForm');
const message = document.getElementById('message');

loginForm.addEventListener('submit', function(event) {
    event.preventDefault(); // Prevent page reload
    
    const username = this.querySelector('input[type="text"]').value;
    const password = this.querySelector('input[type="password"]').value;
    
    // Simple validation (for demo purposes)
    if (username === 'admin' && password === 'password') {
        message.textContent = 'Login successful! Redirecting...';
        message.style.color = '#2ecc71';
        // Simulate redirect after 2 seconds
        setTimeout(() => {
            alert('Redirecting to dashboard...');
            // In a real app, you would redirect to another page
        }, 2000);
    } else {
        message.textContent = 'Invalid username or password. Try again.';
        message.style.color = '#e74c3c';
    }
});