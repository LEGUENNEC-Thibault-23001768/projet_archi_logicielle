<?php
include 'includes/config.php';

if (getUserId()) {
    header('Location: /espace-client.php');
    exit();
}

$error_message = $_GET['error'] ?? null;
$success_message = $_GET['success'] ?? null;

include 'includes/header.php';
?>

<main class="auth-page">
    <div class="auth-container">
        <h1>Créer un compte</h1>

         <div id="error-container" class="alert error" style="display: <?= $error_message ? 'block' : 'none' ?>;">
            <?= htmlspecialchars($error_message ?? '') ?>
        </div>
        <div id="success-container" class="alert success" style="display: <?= $success_message ? 'block' : 'none' ?>;">
             <?= htmlspecialchars($success_message ?? '') ?>
             <?php if ($success_message): ?>
                 <p style="margin-top: 10px;"><a href="/login.php">Vous pouvez maintenant vous connecter.</a></p>
             <?php endif; ?>
        </div>

        <form id="register-form" method="POST"> 
            <div class="form-group">
                <label for="nom">Nom Complet</label> 
                <input type="text" id="nom" name="nom" required>
            </div>


            <div class="form-group">
                <label for="email">Email</label>
                <input type="email" id="email" name="email" required>
            </div>

            <div class="form-group">
                <label for="password">Mot de passe</label>
                <input type="password" id="password" name="password" required minlength="5"> 
                <small>5 caractères minimum (pour test)</small>
            </div>

            <div class="form-group">
                <label for="confirm_password">Confirmer le mot de passe</label>
                <input type="password" id="confirm_password" name="confirm_password" required>
            </div>



            <button type="submit" class="auth-button">S'inscrire</button>
        </form>

        <div class="auth-links">
            <p>Déjà un compte ? <a href="/login.php">Se connecter</a></p>
        </div>
    </div>
</main>

<script>


document.getElementById('register-form')?.addEventListener('submit', function(e) {
    e.preventDefault();
    const form = e.target;
    const password = form.password.value;
    const confirmPassword = form.confirm_password.value;
    const errorContainer = documError ensuring panier exists: TypeError: Failed to fetch
    at fetchApiJs (api-helpers.js:45:32)
    at ensurePanierExists (api-helpers.js:76:33)
    at HTMLDocument.loadCart (paniers.php:346:27)
﻿
ent.getElementById('error-container');
    const successContainer = document.getElementById('success-container');
    const submitButton = form.querySelector('button[type="submit"]');

    errorContainer.textContent = '';
    errorContainer.style.display = 'none';
    successContainer.textContent = '';
    successContainer.style.display = 'none';

    if (password !== confirmPassword) {
        errorContainer.textContent = 'Les mots de passe ne correspondent pas.';
        errorContainer.style.display = 'block';
        return;
    }
     if (password.length < 5) {
         errorContainer.textContent = 'Le mot de passe doit faire au moins 5 caractères.';
        errorContainer.style.display = 'block';
        return;
    }

    submitButton.disabled = true;
    submitButton.textContent = 'Inscription...';

    const apiUrl = `${API_PRODUCTS_USERS_URL_JS}/users`;
    const userData = {
        nom: form.nom.value,
        email: form.email.value,
        password: password,
        role: "CLIENT"
    };

    fetch(apiUrl, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify(userData)
    })
    .then(async response => {
        if (!response.ok) {
            let errorMsg = `Erreur ${response.status}`;
            try {
                const errorData = await response.json();
                errorMsg = errorData.message || errorData.error || JSON.stringify(errorData);
            } catch (e) {
                try {
                    errorMsg = await response.text();
                } catch (e2) { /* Ignorer si même ça échoue */ }
            }
             if (response.status === 409) {
                 errorMsg = "Cet email est déjà utilisé.";
             }
            throw new Error(errorMsg);
        }

        const contentType = response.headers.get("content-type");
        if (contentType && contentType.indexOf("application/json") !== -1) {
            return response.json();
        } else {
            return response.text(); 
        }
    })
    .then(data => {
        console.log("Inscription réussie, réponse serveur:", data); 
        successContainer.innerHTML = 'Inscription réussie ! Vous allez être redirigé...';
        successContainer.style.display = 'block';
        form.reset();
        setTimeout(() => {
            window.location.href = '/login.php?success=Inscription%20r%C3%A9ussie%2C%20veuillez%20vous%20connecter.';
        }, 2500);
    })
    .catch(error => {
        console.error('Register Error:', error);
        errorContainer.textContent = "Erreur d'inscription: " + error.message;
        errorContainer.style.display = 'block';
        submitButton.disabled = false;
        submitButton.textContent = 'S\'inscrire';
    });
});
</script>

<?php include 'includes/footer.php'; ?>