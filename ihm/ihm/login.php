<?php
include 'includes/config.php';
if (getUserId()) {
    header('Location: /espace-client.php'); exit();
}
$error_message = $_GET['error'] ?? null;
$success_message = $_GET['success'] ?? null;
$redirect_url = $_GET['redirect'] ?? '/index.php';
include 'includes/header.php';
?>

<main class="auth-page">
    <div class="auth-container">
        <h1>Connexion</h1>

        <div id="error-container" class="alert error" style="display: <?= $error_message ? 'block' : 'none' ?>;">
            <?= htmlspecialchars($error_message ?? '') ?>
        </div>
        <div id="success-container" class="alert success" style="display: <?= $success_message ? 'block' : 'none' ?>;">
             <?= htmlspecialchars($success_message ?? '') ?>
        </div>

        <form id="login-form" method="POST">
            <div class="form-group">
                <label for="email">Email</label>
                <input type="email" id="email" name="email" required>
            </div>
            <div class="form-group">
                <label for="password">Mot de passe</label>
                <input type="password" id="password" name="password" required>
            </div>
            <button type="submit" class="auth-button">Se connecter</button>
        </form>

        <div class="auth-links">
            <p>Pas encore de compte ? <a href="/register.php">S'inscrire</a></p>
        </div>
    </div>
</main>

<script src="/js/api-helpers.js"></script>

<script>
const redirectUrl = <?= json_encode($redirect_url) ?>;
if (typeof API_PRODUCTS_USERS_URL_JS === 'undefined') {
    console.error("Constante API API_PRODUCTS_USERS_URL_JS non définie !");
}


document.getElementById('login-form')?.addEventListener('submit', function(e) {
    e.preventDefault();
    const form = e.target;
    const email = form.email.value;
    const password = form.password.value;
    const errorContainer = document.getElementById('error-container');
    const successContainer = document.getElementById('success-container');
    const submitButton = form.querySelector('button[type="submit"]');

    errorContainer.textContent = ''; errorContainer.style.display = 'none';
    successContainer.textContent = ''; successContainer.style.display = 'none';
    submitButton.disabled = true; submitButton.textContent = 'Connexion...';

    const apiUrl = `${API_PRODUCTS_USERS_URL_JS}/auth/login`;

    fetch(apiUrl, { 
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify({ email: email, password: password })
    })
    .then(async response => {
        const responseData = await response.json().catch(() => null);
        if (!response.ok) {
            const message = responseData?.message || responseData?.error || (response.status === 401 ? 'Email ou mot de passe incorrect.' : `Erreur ${response.status}`);
            throw new Error(message);
        }
         if (!responseData || !responseData.token || !responseData.user || !responseData.user.id) {
              console.error("Réponse de login invalide:", responseData);
              throw new Error("Réponse invalide du serveur (token ou user.id manquant).");
         }
        return responseData;
    })
    .then(userData => {
        console.log('Login successful:', userData);
        successContainer.textContent = 'Connexion réussie ! Préparation de votre session...';
        successContainer.style.display = 'block';

        const userId = userData.user.id;
        const authToken = userData.token; // le vrai token JWT HS256

        // stocker dans localStorage
        localStorage.setItem('authToken', authToken);
        localStorage.setItem('userId', userId);

        console.log('User ID stored:', userId);
        console.log('Auth Token stored.');

        // met à jour la session PHP en arrière-plan
        fetch('/includes/set-session.php', {
             method: 'POST',
             headers: { 'Content-Type': 'application/json' },
             body: JSON.stringify({ userId: userId, token: authToken })
         })
         .then(sessionResponse => sessionResponse.json())
         .then(sessionResult => {
             if (sessionResult.status === 'success') {
                 console.log("PHP session created successfully.");
                 window.location.href = redirectUrl; 
             } else {
                 throw new Error(sessionResult.message || "Erreur lors de la création de la session PHP.");
             }
         })
         .catch(sessionError => {
             console.error('PHP Session Error:', sessionError);
             errorContainer.textContent = 'Erreur session: ' + sessionError.message;
             errorContainer.style.display = 'block';
             submitButton.disabled = false; 
             submitButton.textContent = 'Se connecter';

         });

    })
    .catch(error => {
        console.error('Login Error:', error);
        errorContainer.textContent = error.message;
        errorContainer.style.display = 'block';
        submitButton.disabled = false;
        submitButton.textContent = 'Se connecter';
    });
});
</script>

<?php include 'includes/footer.php'; ?>