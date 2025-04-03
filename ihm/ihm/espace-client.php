<?php
include 'includes/config.php';
include 'includes/header.php'; 

$userId = getUserId(); 
$token = getAuthToken(); 

$pageError = null;
if (!$userId || !$token) {
    header('Location: /login.php?redirect=espace-client.php');
    exit();
}

$userData = null;
$userError = null;


// api pour les détails utilisateur nécessite le token
$userApiResponse = fetchFromApi('/users/' . $userId, 'GET', null, $token);
if (isset($userApiResponse['error'])) {
    $userError = "Impossible de charger les informations utilisateur: " . $userApiResponse['error'];
    error_log("Erreur API User fetch pour ID $userId: " . $userApiResponse['error'] . " Status: " . ($userApiResponse['statusCode'] ?? 'N/A'));
    if (($userApiResponse['statusCode'] ?? 0) === 401 || ($userApiResponse['statusCode'] ?? 0) === 403) {

         $userError .= " Votre session est peut-être invalide.";
    }
} elseif (is_array($userApiResponse) && isset($userApiResponse['id'])) { // verif si on a bien reçu un utilisateur valide
    $userData = $userApiResponse;
} else {
     $userError = "Réponse inattendue ou invalide de l'API utilisateur.";
     error_log("Réponse API User invalide pour ID $userId: " . json_encode($userApiResponse));
}


?>

<main class="espace-client-page" style="max-width: 900px; margin: 20px auto; padding: 20px;">
    <h1>Mon Espace Client</h1>

    <?php if (isset($_GET['commandeSuccess'])): ?>
        <div class="alert success">Votre commande (ID: <?= htmlspecialchars($_GET['id'] ?? 'N/A') ?>) a été confirmée avec succès !</div>
    <?php endif; ?>

    <section class="user-info" style="margin-bottom: 30px; background: #f9f9f9; padding: 20px; border-radius: 8px;">
        <h2>Mes Informations</h2>
        <?php if ($userError): ?>
            <div class="alert error"><?= htmlspecialchars($userError) ?></div>
        <?php elseif ($userData): ?>
            <p><strong>Nom:</strong> <?= htmlspecialchars($userData['nom'] ?? 'N/A') ?></p>
            <p><strong>Email:</strong> <?= htmlspecialchars($userData['email'] ?? 'N/A') ?></p>
            <p><strong>Role:</strong> <?= htmlspecialchars($userData['role'] ?? 'N/A') ?></p>
        <?php else: ?>
            <div class="alert info">Chargement des informations utilisateur...</div>
        <?php endif; ?>
    </section>

    <section class="commandes-history">
        <h2>Historique de Mes 10 Dernières Commandes</h2>

        <div id="client-historique-loading" style="text-align: center; padding: 30px;">Chargement de l'historique...</div>
        <div id="client-historique-error" class="alert error" style="display: none;"></div>
        <div id="client-historique-empty" class="alert info" style="display: none; text-align: center;">Vous n'avez pas encore passé de commande.</div>

        <div id="client-commandes-list" class="historique-commandes-list">
            <div class="commande-item-template" style="display: none; border: 1px solid #ccc; margin-bottom: 15px; padding: 15px; border-radius: 5px; background-color: #f9f9f9;">
                <h3>Commande N° <span class="commande-id"></span></h3>
                <p>Date de retrait: <span class="commande-date"></span></p>
                <p>Point Relais ID: <span class="commande-relais"></span></p>
                <p>Statut: <strong class="commande-statut"></strong></p>
                <p>Total: <span class="commande-total"></span> €</p>
                <div class="commande-actions" style="margin-top: 10px;">
                    <button class="cancel-commande-btn" style="display: none; background-color: #f44336; color: white; padding: 5px 10px; border: none; border-radius: 3px; cursor: pointer;">Annuler</button>

                </div>
            </div>
        </div>
    </section>

</main>


<?php include 'includes/footer.php'; ?>

<script>
document.addEventListener('DOMContentLoaded', () => {

    if (typeof getAuthTokenJs !== 'function' || typeof getUserIdJs !== 'function' || typeof fetchApiJs !== 'function' || typeof API_COMMANDES_URL_JS === 'undefined') {
        console.error("Fonctions ou constantes API manquantes depuis api-helpers.js !");
        const errorDiv = document.getElementById('client-historique-error') || document.querySelector('.espace-client-page');
        if (errorDiv) {
            errorDiv.textContent = 'Erreur critique: Impossible d\'initialiser la page Espace Client.';
            errorDiv.style.display = 'block';
            const loadingDiv = document.getElementById('client-historique-loading');
            if(loadingDiv) loadingDiv.style.display = 'none';
        }
        return;
    }

    const clientIdString = getUserIdJs();
    const token = getAuthTokenJs();

    const loadingDiv = document.getElementById('client-historique-loading');
    const errorDiv = document.getElementById('client-historique-error');
    const emptyDiv = document.getElementById('client-historique-empty');
    const listContainer = document.getElementById('client-commandes-list');
    const itemTemplate = listContainer?.querySelector('.commande-item-template');

    async function loadCommandeHistoryClient() {
        if (!loadingDiv || !errorDiv || !emptyDiv || !listContainer || !itemTemplate) {
             console.error("Éléments DOM manquants pour loadCommandeHistoryClient.");
             if(loadingDiv) loadingDiv.style.display = 'none';
             if(errorDiv) {
                errorDiv.textContent = "Erreur d'interface: Impossible d'afficher l'historique.";
                errorDiv.style.display = 'block';
             }
             return;
        }

        loadingDiv.style.display = 'block';
        errorDiv.style.display = 'none';
        emptyDiv.style.display = 'none';
        listContainer.querySelectorAll('.commande-item-template:not([style*="display: none"])').forEach(el => el.remove());


        if (!token || !clientIdString) {
             loadingDiv.style.display = 'none';
             errorDiv.textContent = "Session invalide. Veuillez vous reconnecter.";
             errorDiv.style.display = 'block';
             return;
         }

        const clientIdInt = parseInt(clientIdString, 10);
        if (isNaN(clientIdInt)) {
             loadingDiv.style.display = 'none';
             errorDiv.textContent = "Erreur interne: Votre identifiant utilisateur n'est pas valide.";
             errorDiv.style.display = 'block';
             console.error("clientId from localStorage is not a valid integer string:", clientIdString);
             return;
        }

        try {
            const apiUrl = `${API_COMMANDES_URL_JS}/commandes?clientId=${clientIdInt}`;
            console.log(`Fetching command history for client space: ${apiUrl}`);
            const commandes = await fetchApiJs(apiUrl, 'GET', null, true); // Force refresh
            loadingDiv.style.display = 'none';

            if (!Array.isArray(commandes)) {
                 console.error("API command history response is not an array:", commandes);
                 throw new Error("Réponse inattendue du serveur.");
            }

            if (commandes.length === 0) {
                emptyDiv.style.display = 'block';
            } else {
                commandes.forEach(commande => {
                    const clone = itemTemplate.cloneNode(true);
                    clone.style.display = ''; // Rendre visible
                    clone.dataset.commandeId = commande.id;

                    //  pour éviter les injections XSS
                    setTextContent(clone, '.commande-id', commande.id);
                    setTextContent(clone, '.commande-date', commande.dateRetrait ? new Date(commande.dateRetrait).toLocaleDateString('fr-FR') : 'N/A');
                    setTextContent(clone, '.commande-relais', commande.relaiId || 'N/A');
                    setTextContent(clone, '.commande-statut', commande.statut || 'INCONNU');
                    setTextContent(clone, '.commande-total', commande.prixTotal?.toFixed(2) || '0.00');

                    const cancelButton = clone.querySelector('.cancel-commande-btn');
                    if (cancelButton) {
                         if (commande.statut === 'EN_ATTENTE') {
                              cancelButton.style.display = 'inline-block';
                              cancelButton.onclick = () => handleCancelCommandeClient(commande.id, clone);
                         } else {
                              cancelButton.style.display = 'none';
                         }
                    }
                    listContainer.appendChild(clone);
                });
            }

        } catch (error) {
            loadingDiv.style.display = 'none';
            errorDiv.textContent = "Erreur lors du chargement de l'historique: " + error.message;
             if (error.status === 401 || error.status === 403) { errorDiv.textContent += " Veuillez vous reconnecter."; }
             else if (error.status === 400) { errorDiv.textContent = "Erreur de requête: Impossible de récupérer vos commandes."; }
            errorDiv.style.display = 'block';
            console.error("Error loading commande history:", error);
        }
    }

    function setTextContent(parentElement, selector, text) {
        const element = parentElement.querySelector(selector);
        if (element) {
            element.textContent = text;
        } else {
             console.warn(`Element with selector "${selector}" not found in template clone.`);
        }
    }


    async function handleCancelCommandeClient(commandeId, itemElement) {
        if (!confirm(`Êtes-vous sûr de vouloir annuler la commande N° ${commandeId} ?`)) {
            return;
        }

        const cancelButton = itemElement.querySelector('.cancel-commande-btn');
        if (cancelButton) cancelButton.disabled = true;
        itemElement.style.opacity = '0.7';

        const apiUrl = `${API_COMMANDES_URL_JS}/commandes/${commandeId}/annulation`;
        console.log(`Attempting to cancel commande ${commandeId} via POST ${apiUrl}`);

        try {
            const updatedCommande = await fetchApiJs(apiUrl, 'POST');
            console.log("Cancel response:", updatedCommande);

            setTextContent(itemElement, '.commande-statut', updatedCommande?.statut || 'ANNULEE');
            if (cancelButton) cancelButton.style.display = 'none';
            alert(`Commande N° ${commandeId} annulée avec succès.`);

        } catch (error) {
            console.error(`Erreur lors de l'annulation de la commande ${commandeId}:`, error);
            let errorMsg = `Impossible d'annuler la commande N° ${commandeId}: ${error.message}`;
            if (error.status === 409) { errorMsg = `Impossible d'annuler la commande N° ${commandeId} (état: ${error.data?.message || 'non annulable'}).`; }
            else if (error.status === 404) { errorMsg = `Commande N° ${commandeId} non trouvée.`; }
            alert(errorMsg);
            if (cancelButton) cancelButton.disabled = false;
        } finally {
            itemElement.style.opacity = '1';
        }
    }

 
    <?php if (!$pageError): ?>
        loadCommandeHistoryClient();
    <?php endif; ?>

}); 
</script>