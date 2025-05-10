<?php
 $isLoggedIn = isset($_SESSION['user_id']) && isset($_SESSION['auth_token']);
?>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Coopérative Agricole</title>
    <link rel="stylesheet" href="/styles/main.css">
</head>
<body>
    <header class="main-header">
        <div class="logo-container">
             <a href="/index.php">
                <img src="https://i.postimg.cc/ZYxh29xH/Local-Food-1.png" alt="Local Food" class="header-logo">
            </a>
        </div>

        <nav class="main-nav">
            <a href="/index.php" class="nav-link">Accueil</a>
            <a href="/paniers.php" class="nav-link">Nos Paniers</a>
            <?php if ($isLoggedIn): ?>
                 <a href="/commandes.php" class="nav-link">Mes Commandes</a>
                 <a href="/espace-client.php" class="nav-link">Mon Compte</a>
            <?php endif; ?>
        </nav>

        <div class="user-actions">
            <?php if ($isLoggedIn): ?>
                 <a href="/logout.php" class="login-btn">Déconnexion</a>
            <?php else: ?>
                 <a href="/login.php" class="login-btn">Connexion</a>
                 <a href="/register.php" class="register-btn">Inscription</a>
            <?php endif; ?>
        </div>
        
    </header>
