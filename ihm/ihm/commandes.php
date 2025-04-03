<?php
include 'includes/config.php'; 
include 'includes/header.php';

$mode = 'historique'; 
$pageError = null;

if (isset($_GET['action']) && $_GET['action'] === 'finaliser') {
    $mode = 'finaliser';
}

$userId = getUserId();
$token = getAuthToken();

if (!$userId || !$token) {
    $pageError = "Veuillez vous connecter pour accéder à cette page.";
    $mode = 'error';
} elseif ($mode === 'finaliser' && !$userId) {
     $pageError = "Veuillez vous connecter pour finaliser votre commande.";
     $mode = 'error';
}

?>

<main class="commande-page">

    <h1><?= ($mode === 'finaliser') ? 'Finaliser votre commande' : 'Historique de vos Commandes' ?></h1>

    <?php if ($mode === 'error'): ?>
        <div class="alert error"><?= htmlspecialchars($pageError) ?></div>
        <p style="text-align:center;"><a href="/login.php?redirect=<?= urlencode($_SERVER['REQUEST_URI']) ?>" class="browse-btn">Se connecter</a></p>

    <?php elseif ($mode === 'finaliser'): ?>
        <div id="commande-loading" style="text-align: center; padding: 30px;">Chargement du récapitulatif de votre panier actuel...</div>
        <div id="commande-error" class="alert error" style="display: none;"></div>
        <div id="commande-panier-vide" class="alert info" style="display: none; text-align: center;">Votre panier est vide. <a href="/index.php" class="browse-btn">Ajouter des produits</a></div>

        <div id="commande-content" style="display: none;" class="commande-grid">
            <section class="commande-recap">
                <h2>Récapitulatif du Panier</h2>
                <img id="panier-image" src="/images/placeholder.jpg" alt="Image Panier" style="max-width:100%; height:auto; margin-bottom: 15px;">
                <p id="panier-description">Votre sélection de produits frais.</p>
                <ul id="panier-produits-recap"></ul>
                <p><strong>Total du panier : <span id="panier-total-recap">0.00 €</span></strong></p>
            </section>

            <section class="commande-form-section">
                <h2>Informations de retrait</h2>
                <form id="commande-final-form">
                    <div class="form-group">
                        <label for="relaiId">Point Relais</label>
                        <select id="relaiId" name="relaiId" required>
                            <option value="" disabled selected>Choisissez un point relais</option>
                            <option value="1">Relais Centre Ville</option>
                            <option value="2">Relais Gare</option>
                            <option value="3">Relais Marché Ouest</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="dateRetrait">Date de Retrait</label>
                        <input type="date" id="dateRetrait" name="dateRetrait" required>
                    </div>
                    <div class="commande-summary-final">
                        <p>Montant total : <strong id="form-total-price">0.00 €</strong></p>
                    </div>
                    <button type="submit" id="confirm-commande-btn" class="checkout-btn" disabled>Confirmer la commande</button>
                    <div id="confirm-error" class="alert error" style="display: none; margin-top: 15px;"></div>
                </form>
            </section>
        </div>

    <?php elseif ($mode === 'historique'): ?>
        <div id="historique-loading" style="text-align: center; padding: 30px;">Chargement de l'historique...</div>
        <div id="historique-error" class="alert error" style="display: none;"></div>
        <div id="historique-empty" class="alert info" style="display: none; text-align: center;">Vous n'avez pas encore passé de commande.</div>

        <div id="commandes-list-container" class="historique-commandes-list">
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

    <?php endif; ?>
</main>


<script src="/js/api-helpers.js"></script>


<script>
document.addEventListener('DOMContentLoaded', () => {

    if (typeof getAuthTokenJs !== 'function' || typeof getUserIdJs !== 'function' || typeof fetchApiJs !== 'function' || typeof API_PANIERS_URL_JS === 'undefined' || typeof API_PRODUCTS_USERS_URL_JS === 'undefined' || typeof API_COMMANDES_URL_JS === 'undefined') {
        console.error("Fonctions ou constantes API manquantes depuis api-helpers.js !");
        document.body.innerHTML = '<div class="alert error">Erreur critique: Impossible d\'initialiser la page.</div>';
        return;
    }

    const clientIdString = getUserIdJs(); 
    const token = getAuthTokenJs();
    const pageMode = <?= json_encode($mode) ?>;


    if (pageMode === 'finaliser') {
        const loadingDiv = document.getElementById('commande-loading');
        const errorDiv = document.getElementById('commande-error');
        const panierVideDiv = document.getElementById('commande-panier-vide'); 
        const contentDiv = document.getElementById('commande-content');
        const confirmBtn = document.getElementById('confirm-commande-btn');
        const confirmErrorDiv = document.getElementById('confirm-error');
        const recapList = document.getElementById('panier-produits-recap');
        const panierTotalRecap = document.getElementById('panier-total-recap');
        const formTotalPrice = document.getElementById('form-total-price');
        const panierImage = document.getElementById('panier-image');
        const commandeForm = document.getElementById('commande-final-form');

        let panierIdToFinalize = null; 
        let calculatedTotal = 0;
        let productDetailsCmdCache = {};

        async function fetchProductDetailsCmd(productId) {
             if (productDetailsCmdCache[productId]) return productDetailsCmdCache[productId];
             if (!productId) return null;
             try { 
                const url = `${API_PRODUCTS_USERS_URL_JS}/products/${productId}`;
                const details = await fetchApiJs(url, 'GET');
                productDetailsCmdCache[productId] = details || { nom: `Produit ${productId} (introuvable)`, prix: 0 };
                return productDetailsCmdCache[productId];
             } catch (error) {
                console.error(`Error fetching details for product ${productId}:`, error);
                productDetailsCmdCache[productId] = { nom: `Produit ${productId} (erreur)`, prix: 0 };
                return productDetailsCmdCache[productId];
             }
        }

        async function loadCommandeSummary() {
            if (!loadingDiv || !errorDiv || !panierVideDiv || !contentDiv || !confirmBtn || !recapList || !panierTotalRecap || !formTotalPrice || !panierImage) {
                 console.error("Éléments DOM manquants pour loadCommandeSummary."); return;
            }
            loadingDiv.style.display = 'block'; errorDiv.style.display = 'none'; panierVideDiv.style.display = 'none'; contentDiv.style.display = 'none'; confirmBtn.disabled = true;

            if (!token || !clientIdString) {
                 loadingDiv.style.display = 'none'; errorDiv.textContent = "Vous devez être connecté pour finaliser."; errorDiv.style.display = 'block'; return;
            }

            try {
                 const panierUrl = `${API_PANIERS_URL_JS}/paniers/mine`;
                 console.log(`Fetching current user panier details from: ${panierUrl}`);
                 const fetchedPanierData = await fetchApiJs(panierUrl, 'GET', null, true); 

                 if (!fetchedPanierData || !fetchedPanierData.panierId) {
                     throw new Error("Impossible de récupérer les informations du panier utilisateur.");
                 }

                 panierIdToFinalize = fetchedPanierData.panierId;
                 console.log("Panier ID received from /mine:", panierIdToFinalize);

                 if (!fetchedPanierData.panierProduits || fetchedPanierData.panierProduits.length === 0) {
                     loadingDiv.style.display = 'none';
                     panierVideDiv.style.display = 'block'; 
                     return; 
                 }

                 const detailPromises = fetchedPanierData.panierProduits.map(item => fetchProductDetailsCmd(item.productId));
                 await Promise.all(detailPromises);

                 recapList.innerHTML = ''; calculatedTotal = 0;
                 fetchedPanierData.panierProduits.forEach(item => {
                    const details = productDetailsCmdCache[item.productId]; if (!details) return;
                    const li = document.createElement('li'); const lineTotal = (details.prix || 0) * item.quantity;
                    li.textContent = `${details.nom} - ${item.quantity} ${item.unit} (${lineTotal.toFixed(2)} €)`;
                    recapList.appendChild(li); calculatedTotal += lineTotal;
                 });
                 panierTotalRecap.textContent = `${calculatedTotal.toFixed(2)} €`; formTotalPrice.textContent = `${calculatedTotal.toFixed(2)} €`;
                 panierImage.src = '/images/commande_recap.jpg'; panierImage.alt = 'Récapitulatif de votre commande';

                 loadingDiv.style.display = 'none'; contentDiv.style.display = 'grid'; confirmBtn.disabled = false;

            } catch (error) {
                 loadingDiv.style.display = 'none'; errorDiv.textContent = "Erreur chargement récapitulatif: " + error.message;
                 if (error.status === 401 || error.status === 403) { errorDiv.textContent += " Veuillez vous reconnecter."; }
                 else if (error.status === 404) { errorDiv.textContent = "Erreur interne lors de la récupération de votre panier."; }
                 errorDiv.style.display = 'block'; console.error("Error loading commande summary:", error);
            }
        }

        if (commandeForm) {
             commandeForm.addEventListener('submit', async function(e) {
                 e.preventDefault();
                 if (!confirmBtn || !confirmErrorDiv) return;

                 if (!panierIdToFinalize) {
                     confirmErrorDiv.textContent = 'Erreur interne: ID du panier non disponible.'; confirmErrorDiv.style.display = 'block'; return;
                 }
                 const clientIdInt = parseInt(clientIdString, 10);
                  if (isNaN(clientIdInt)) {
                      confirmErrorDiv.textContent = 'Erreur interne: ID utilisateur invalide.'; confirmErrorDiv.style.display = 'block'; return;
                  }

                 confirmBtn.disabled = true; confirmBtn.textContent = 'Confirmation...'; confirmErrorDiv.style.display = 'none'; confirmErrorDiv.textContent = '';

                 const formData = new FormData(e.target); const relaiIdValue = formData.get('relaiId'); const dateRetraitValue = formData.get('dateRetrait');
                 if (!relaiIdValue || !dateRetraitValue) { confirmBtn.disabled = false; return; }
                 const today = new Date().toISOString().split('T')[0];
                 if (dateRetraitValue < today) {  confirmBtn.disabled = false; return; }

                 const commandeData = {
                     clientId: clientIdInt,
                     panierId: panierIdToFinalize,
                     relaiId: parseInt(relaiIdValue), dateRetrait: dateRetraitValue, prixTotal: calculatedTotal, statut: 'EN_ATTENTE'
                 };

                 console.log("Submitting commande data:", commandeData);
                 const apiUrlCmd = `${API_COMMANDES_URL_JS}/commandes`;
                 let createdCommande = null;

                 try {
                     createdCommande = await fetchApiJs(apiUrlCmd, 'POST', commandeData);
                     console.log("Commande created successfully:", createdCommande);

                     if (createdCommande && createdCommande.id) {
                         console.log("Attempting to clear cart items...");
                         const clearCartUrl = `${API_PANIERS_URL_JS}/paniers/mine/items`;
                         try {
                             await fetchApiJs(clearCartUrl, 'DELETE');
                             console.log("Cart items cleared successfully.");
                             const cartCountElement = document.querySelector('.cart-count');
                             if(cartCountElement) cartCountElement.textContent = '0';
                         } catch (clearError) {
                             console.warn("Erreur lors du vidage du panier après la commande:", clearError);
                         }
                     } else { console.warn("ID commande non valide, panier non vidé."); }

                     alert('Commande confirmée avec succès ! Numéro : ' + (createdCommande?.id || 'N/A'));
                     window.location.href = '/commandes.php'; 

                 } catch (error) {
                     console.error("Erreur lors de la création de la commande:", error);
                     confirmErrorDiv.textContent = "Erreur lors de la confirmation: " + error.message;
                     if (error.status === 400) { confirmErrorDiv.textContent += " (Vérifiez les informations saisies)."; }
                     confirmErrorDiv.style.display = 'block'; confirmBtn.disabled = false; confirmBtn.textContent = 'Confirmer la commande';
                 }
             });
        } else { console.error("Formulaire #commande-final-form non trouvé en mode finalisation."); }

        loadCommandeSummary();

    } else if (pageMode === 'historique') {
        const loadingDiv = document.getElementById('historique-loading');
        const errorDiv = document.getElementById('historique-error');
        const emptyDiv = document.getElementById('historique-empty');
        const listContainer = document.getElementById('commandes-list-container');
        const itemTemplate = document.querySelector('.commande-item-template');

        async function loadCommandeHistory() {
             if (!loadingDiv || !errorDiv || !emptyDiv || !listContainer ) { console.error("Éléments DOM manquants pour loadCommandeHistory."); return; }
             loadingDiv.style.display = 'block'; errorDiv.style.display = 'none'; emptyDiv.style.display = 'none';
             listContainer.innerHTML = ''; if (itemTemplate) listContainer.appendChild(itemTemplate);

             if (!token || !clientIdString) { loadingDiv.style.display = 'none'; errorDiv.textContent = "Vous devez être connecté."; errorDiv.style.display = 'block'; return; }
             const clientIdInt = parseInt(clientIdString, 10);
             if (isNaN(clientIdInt)) { loadingDiv.style.display = 'none'; errorDiv.textContent = "Erreur ID utilisateur."; errorDiv.style.display = 'block'; return; }

             try {
                 const apiUrl = `${API_COMMANDES_URL_JS}/commandes?clientId=${clientIdInt}`;
                 console.log(`Fetching command history from: ${apiUrl}`);
                 const commandes = await fetchApiJs(apiUrl, 'GET', null, true);
                 loadingDiv.style.display = 'none';

                 if (!Array.isArray(commandes)) { throw new Error("Réponse inattendue du serveur."); }
                 if (commandes.length === 0) { emptyDiv.style.display = 'block'; }
                 else {
                     commandes.forEach(commande => {
                         const clone = itemTemplate.cloneNode(true);
                         clone.style.display = ''; clone.dataset.commandeId = commande.id;
                         clone.querySelector('.commande-id').textContent = commande.id;
                         clone.querySelector('.commande-date').textContent = commande.dateRetrait ? new Date(commande.dateRetrait).toLocaleDateString('fr-FR') : 'N/A';
                         clone.querySelector('.commande-relais').textContent = commande.relaiId || 'N/A';
                         clone.querySelector('.commande-statut').textContent = commande.statut || 'INCONNU';
                         clone.querySelector('.commande-total').textContent = commande.prixTotal?.toFixed(2) || '0.00';
                         const cancelButton = clone.querySelector('.cancel-commande-btn');
                         if (cancelButton) {
                              if (commande.statut === 'EN_ATTENTE') { cancelButton.style.display = 'inline-block'; cancelButton.onclick = () => handleCancelCommande(commande.id, clone); }
                              else { cancelButton.style.display = 'none'; }
                         }
                         listContainer.appendChild(clone);
                     });
                 }
             } catch (error) {
                loadingDiv.style.display = 'none'; errorDiv.textContent = "Erreur chargement historique: " + error.message;
                 if (error.status === 401 || error.status === 403) { errorDiv.textContent += " Veuillez vous reconnecter."; }
                 else if (error.status === 400) { errorDiv.textContent = "Erreur de requête: Impossible de récupérer les commandes."; }
                 errorDiv.style.display = 'block'; console.error("Error loading commande history:", error);
             }
        }

         async function handleCancelCommande(commandeId, itemElement) {
             if (!confirm(`Êtes-vous sûr de vouloir annuler la commande N° ${commandeId} ?`)) return;
             const cancelButton = itemElement.querySelector('.cancel-commande-btn');
             if (cancelButton) cancelButton.disabled = true; itemElement.style.opacity = '0.7';
             const apiUrl = `${API_COMMANDES_URL_JS}/commandes/${commandeId}/annulation`;
             try {
                 const updatedCommande = await fetchApiJs(apiUrl, 'POST');
                 const statutElement = itemElement.querySelector('.commande-statut');
                 if (statutElement) statutElement.textContent = updatedCommande?.statut || 'ANNULEE';
                 if (cancelButton) cancelButton.style.display = 'none';
                 alert(`Commande N° ${commandeId} annulée.`);
             } catch (error) {
                 console.error(`Erreur annulation commande ${commandeId}:`, error);
                 let errorMsg = `Impossible d'annuler commande N° ${commandeId}: ${error.message}`;
                 if (error.status === 409) { errorMsg = `Impossible d'annuler commande N° ${commandeId} (état: ${error.data?.message || 'non annulable'}).`; }
                 else if (error.status === 404) { errorMsg = `Commande N° ${commandeId} non trouvée.`; }
                 alert(errorMsg);
                 if (cancelButton) cancelButton.disabled = false;
             } finally { itemElement.style.opacity = '1'; }
         }

        loadCommandeHistory();

    } else if (pageMode === 'error') {
        console.log("Page loaded in error mode.");
    }

}); 
</script>

<?php include 'includes/footer.php'; ?>