<?php
include 'includes/config.php'; 
include 'includes/header.php';

$error = null;
$token = getAuthToken();
$userId = getUserId();

if (!$token || !$userId) {

    $error = "Veuillez vous connecter pour voir votre panier.";
}
?>

<main class="panier-page">
    <h1>Votre Panier</h1>

    <?php if ($error): ?>
        <div class="alert error"><?= htmlspecialchars($error) ?></div>
        <p style="text-align: center;">
            <a href="/login.php?redirect=/panier.php" class="btn">Se connecter</a>
        </p>
    <?php else: ?>
        <div id="panier-loading" style="text-align: center; padding: 30px;">Chargement du panier...</div>
        <div id="panier-error" class="alert error" style="display: none;"></div>
        <div id="panier-empty" class="empty-cart" style="display: none;">
            <p>Votre panier est vide.</p>
            <a href="/index.php" class="browse-btn">Parcourir nos produits</a>
        </div>

        <div id="panier-content-container" style="display: none;">
            <div class="cart-items" id="cart-items-list">
                <div class="cart-item-template" style="display: none;">
                    <img src="/images/placeholder.jpg" alt="Produit" class="item-image">
                    <div class="item-info">
                        <h3 class="item-name">Nom du produit</h3>
                        <p class="unit-price">0.00 € / unité</p>
                    </div>
                    <div class="item-quantity">
                        <form class="update-form">
                            <button type="button" class="qty-btn minus" aria-label="Diminuer">-</button>
                            <input type="number" name="quantite" value="1" min="1" step="1" aria-label="Quantité">
                            <button type="button" class="qty-btn plus" aria-label="Augmenter">+</button>
                        </form>
                    </div>
                    <div class="item-total">
                        <p class="item-total-price">0.00 €</p>
                    </div>
                    <form class="remove-form">
                         <input type="hidden" name="productId" value="">
                        <button type="submit" class="remove-btn" aria-label="Supprimer">🗑️</button>
                    </form>
                </div>
            </div>

            <div class="cart-summary" id="cart-summary-details">
                <div class="summary-row">
                    <span>Sous-total</span>
                    <span id="summary-subtotal">0.00 €</span>
                </div>
                <div class="summary-row total">
                    <span>Total</span>
                    <span id="summary-total">0.00 €</span>
                </div>
                <button id="checkout-btn" class="checkout-btn disabled" disabled>Passer la commande</button>
                 <div id="checkout-error" style="color: red; text-align: center; margin-top: 10px; font-size: 0.9em;"></div>
            </div>
        </div>
    <?php endif; ?>
</main>

<script src="/js/api-helpers.js"></script>

<script>
document.addEventListener('DOMContentLoaded', () => {

    const panierLoading = document.getElementById('panier-loading');
    const panierError = document.getElementById('panier-error');
    const panierEmpty = document.getElementById('panier-empty');
    const panierContainer = document.getElementById('panier-content-container');
    const cartItemsList = document.getElementById('cart-items-list');
    const itemTemplate = document.querySelector('.cart-item-template');
    const summarySubtotal = document.getElementById('summary-subtotal');
    const summaryTotal = document.getElementById('summary-total');
    const checkoutBtn = document.getElementById('checkout-btn');
    const checkoutError = document.getElementById('checkout-error');

     if (typeof getAuthTokenJs !== 'function' || typeof fetchApiJs !== 'function') {
         console.error("Fonctions de api-helpers.js manquantes !");
         if (panierLoading) panierLoading.style.display = 'none';
         if (panierError) {
             panierError.textContent = 'Erreur critique: Impossible d\'initialiser le panier.';
             panierError.style.display = 'block';
         }
         return; 
     }


    let currentPanierData = null;
    let productDetailsCache = {};

    async function fetchProductDetails(productId) {
        if (productDetailsCache[productId]) {
            return productDetailsCache[productId];
        }
        if (!productId) return null;
        const token = getAuthTokenJs(); 

        try {
            const url = `${API_PRODUCTS_USERS_URL_JS}/products/${productId}`;
            const details = await fetchApiJs(url, 'GET');
            productDetailsCache[productId] = details || { nom: `Produit ${productId} (introuvable)`, prix: 0, image: 'placeholder.jpg' };
            return productDetailsCache[productId];
        } catch (error) {
            console.error(`Error fetching details for product ${productId}:`, error);
            productDetailsCache[productId] = { nom: `Produit ${productId} (erreur)`, prix: 0, image: 'placeholder.jpg' };
            return productDetailsCache[productId];
        }
    }

    async function renderCart(panier) {
        if (!cartItemsList || !itemTemplate) {
             console.error("Éléments DOM manquants pour renderCart.");
             return;
        }
         while (cartItemsList.firstChild && !cartItemsList.firstChild.classList?.contains('cart-item-template')) {
             cartItemsList.removeChild(cartItemsList.firstChild);
         }

        let subtotal = 0;

        if (!panier || !panier.panierProduits || panier.panierProduits.length === 0) {
            if(panierEmpty) panierEmpty.style.display = 'block';
            if(panierContainer) panierContainer.style.display = 'none';
            if(checkoutBtn) {
                checkoutBtn.disabled = true;
                checkoutBtn.classList.add('disabled');
            }
            if(checkoutError) checkoutError.textContent = '';
            return;
        }

        const detailPromises = panier.panierProduits.map(item => fetchProductDetails(item.productId));
        await Promise.all(detailPromises);

        panier.panierProduits.forEach(item => {
            const details = productDetailsCache[item.productId];
            if (!details) return;

            const itemElement = itemTemplate.cloneNode(true);
            itemElement.classList.remove('cart-item-template');
            itemElement.style.display = '';
            itemElement.dataset.productId = item.productId;

            itemElement.querySelector('.item-image').src = `/images/${details.image || 'placeholder.jpg'}`;
            itemElement.querySelector('.item-image').alt = details.nom || 'Produit';
            itemElement.querySelector('.item-name').textContent = details.nom || 'Produit inconnu';
            itemElement.querySelector('.unit-price').textContent = `${(details.prix || 0).toFixed(2)} € / ${item.unit}`;

            const quantityInput = itemElement.querySelector('input[name="quantite"]');
            const unit = item.unit || 'pièce';
            const step = unit === 'kg' ? 0.1 : 1;

            const min = unit === 'kg' ? 0.1 : 1;
            quantityInput.value = item.quantity;
            quantityInput.step = step;
            quantityInput.min = min;

            const itemTotalPriceElement = itemElement.querySelector('.item-total-price');
            const lineTotal = (details.prix || 0) * item.quantity;
            itemTotalPriceElement.textContent = `${lineTotal.toFixed(2)} €`;
            subtotal += lineTotal;

            attachItemEventListeners(itemElement, item.productId, unit, min, step);

            const removeFormInput = itemElement.querySelector('.remove-form input[name="productId"]');
             if (removeFormInput) removeFormInput.value = item.productId;

            cartItemsList.appendChild(itemElement);
        });

        if(summarySubtotal) summarySubtotal.textContent = `${subtotal.toFixed(2)} €`;
        if(summaryTotal) summaryTotal.textContent = `${subtotal.toFixed(2)} €`; 

        if(checkoutBtn) {
            checkoutBtn.disabled = false;
            checkoutBtn.classList.remove('disabled');
        }
        if(checkoutError) checkoutError.textContent = '';
        if(panierContainer) panierContainer.style.display = 'block'; 
        if(panierEmpty) panierEmpty.style.display = 'none'; 
    }




    // Fonction pour attacher les écouteurs aux boutons +/-/suppr d'un article
    function attachItemEventListeners(itemElement, productId, unit, min, step) {
        const form = itemElement.querySelector('.update-form');
        const input = form?.querySelector('input[name="quantite"]');
        const minus = form?.querySelector('.minus');
        const plus = form?.querySelector('.plus');
        const removeForm = itemElement.querySelector('.remove-form');

         if (!input || !minus || !plus || !removeForm) {
             console.warn("Éléments manquants pour les écouteurs sur l'article:", productId);
             return;
         }


        // Fonction générique pour mettre à jour le panier via l'API /mine
        async function updatePanierApi(updatedProduitsList) {
            if (!currentPanierData) { // Vérifier si les données du panier sont chargées
                 console.error("Données du panier non disponibles pour la mise à jour.");
                 alert("Erreur interne: Impossible de mettre à jour le panier.");
                 return; 
            }

            // Utiliser l'endpoint /mine
            const putUrl = `${API_PANIERS_URL_JS}/paniers/mine`;
            const panierPayload = {
                // l'api /mine identifie le panier via le token
                panierProduits: updatedProduitsList
            };

            console.log("Sending PUT to /mine with payload:", panierPayload);

            itemElement.style.opacity = '0.5';
            input.disabled = true;
            minus.disabled = true;
            plus.disabled = true;
            const removeButton = removeForm.querySelector('button');
             if(removeButton) removeButton.disabled = true;


            try {
                // fetchApiJs ajoute le token automatiquement
                const updatedPanier = await fetchApiJs(putUrl, 'PUT', panierPayload);
                console.log("Received updated panier from /mine:", updatedPanier);

                currentPanierData = updatedPanier;

                await renderCart(currentPanierData);

            } catch (error) {
                console.error("Erreur lors de la mise à jour du panier via /mine:", error);
                alert("Erreur de mise à jour du panier: " + error.message);

                 await renderCart(currentPanierData); 

            } finally {

                const finalElement = cartItemsList.querySelector(`[data-product-id="${productId}"]`);
                 if (finalElement) {
                     finalElement.style.opacity = '1';
                     const elInput = finalElement.querySelector('input[name="quantite"]');
                     const elMinus = finalElement.querySelector('.minus');
                     const elPlus = finalElement.querySelector('.plus');
                     const elRemoveBtn = finalElement.querySelector('.remove-form button');
                     if (elInput) elInput.disabled = false;
                     if (elMinus) elMinus.disabled = false;
                     if (elPlus) elPlus.disabled = false;
                     if (elRemoveBtn) elRemoveBtn.disabled = false;
                 }
            }
        }

        async function handleQuantityUpdate(newQuantity) {
            if (!currentPanierData || !currentPanierData.panierProduits) return;

            let updatedProduits = JSON.parse(JSON.stringify(currentPanierData.panierProduits));

            const itemIndex = updatedProduits.findIndex(p => p.productId === productId);
            if (itemIndex === -1) {
                 console.error("Article non trouvé dans les données actuelles pour la mise à jour.");
                 return; 
            }

             if (newQuantity >= min) {
                 updatedProduits[itemIndex].quantity = newQuantity;
             } else {
                  console.warn(`Quantité ${newQuantity} inférieure au minimum ${min}, ajustement ignoré ou suppression implicite.`);
                   updatedProduits.splice(itemIndex, 1);
             }


            await updatePanierApi(updatedProduits);
        }

        async function handleItemRemoval() {
             if (!currentPanierData || !currentPanierData.panierProduits) return;

            const productName = itemElement.querySelector('.item-name')?.textContent || "cet article";
            if (!confirm(`Supprimer "${productName}" du panier ?`)) return;

             // créer une nouvelle liste sans l'article supprimé
             let updatedProduits = currentPanierData.panierProduits.filter(p => p.productId !== productId);

             await updatePanierApi(updatedProduits);
        }

        // Écouteurs d'événements pour +/-
        minus.addEventListener('click', () => {
            let currentVal = parseFloat(input.value);
            let newVal = currentVal - step;
            if (step < 1) newVal = parseFloat(newVal.toFixed(1));
            newVal = Math.max(min, newVal);
            if (newVal !== currentVal) {
                 input.value = newVal; 
                 handleQuantityUpdate(newVal); 
            }
        });

        plus.addEventListener('click', () => {
            let currentVal = parseFloat(input.value);
            let newVal = currentVal + step;
             if (step < 1) newVal = parseFloat(newVal.toFixed(1));
             input.value = newVal; 
             handleQuantityUpdate(newVal);
        });

        input.addEventListener('change', () => {
            let currentValInInput = parseFloat(input.value);
            let newVal = currentValInInput;
             let needsApiUpdate = false;

             if (isNaN(newVal) || newVal < min) {
                 newVal = min;
                 input.value = newVal;
             } else if (step < 1) {
                 newVal = parseFloat(newVal.toFixed(1));
                 if (newVal !== currentValInInput) {
                      input.value = newVal; 
                 }
             } else {
                  newVal = Math.round(newVal); 
                  if (newVal !== currentValInInput) {
                       input.value = newVal; 
                  }
             }

             const itemIndex = currentPanierData?.panierProduits.findIndex(p => p.productId === productId);
             if (itemIndex !== -1 && currentPanierData.panierProduits[itemIndex].quantity !== newVal) {
                  needsApiUpdate = true;
             }

             if (needsApiUpdate) {
                 handleQuantityUpdate(newVal);
             }
        });

        removeForm.addEventListener('submit', (e) => {
            e.preventDefault();
            handleItemRemoval();
        });
    }


    async function loadCart() {
        const token = getAuthTokenJs(); 

        if(panierLoading) panierLoading.style.display = 'block';
        if(panierError) panierError.style.display = 'none';
        if(panierEmpty) panierEmpty.style.display = 'none';
        if(panierContainer) panierContainer.style.display = 'none';

        if (!token) {
            if(panierLoading) panierLoading.style.display = 'none';
            if(panierError) {
                panierError.textContent = "Veuillez vous connecter pour voir votre panier.";
                panierError.style.display = 'block';
                 const loginLink = document.createElement('a');
                 loginLink.href = '/login.php?redirect=/panier.php';
                 loginLink.textContent = 'Se connecter';
                 loginLink.classList.add('btn'); 
                 panierError.appendChild(document.createElement('br'));
                 panierError.appendChild(loginLink);
            }
            return; 
        }



        // appeler l'API pour obtenir le panier de l'utilisateur via /mine
        const apiUrl = `${API_PANIERS_URL_JS}/paniers/mine`;

        try {
            console.log("Fetching cart using /mine...");
            // fetchApiJs ajoute le token automatiquement
            const panierData = await fetchApiJs(apiUrl, 'GET', null, true); // Force refresh

            if (!panierData) {
                console.error("Réponse vide ou invalide de /paniers/mine");
                 throw new Error("Impossible de récupérer les informations du panier.");
            }

            console.log("Cart data received from /mine:", panierData);
            currentPanierData = panierData; 

            // Afficher le panier
            await renderCart(currentPanierData);
            if(panierLoading) panierLoading.style.display = 'none';

        } catch (error) {
            console.error("Erreur lors du chargement du panier via /mine:", error);
            if(panierLoading) panierLoading.style.display = 'none';
            if(panierError) {
                if (error.status === 401 || error.status === 403) {
                    panierError.textContent = "Session expirée ou invalide. Veuillez vous reconnecter.";
                } else {
                    panierError.textContent = "Erreur lors du chargement du panier: " + error.message;
                }
                panierError.style.display = 'block';
            }
        }
    }

    // bouton Passer la commande 
    if(checkoutBtn) {
        checkoutBtn.addEventListener('click', () => {
            if (!currentPanierData || !currentPanierData.panierId || !currentPanierData.panierProduits || currentPanierData.panierProduits.length === 0) {
                 if(checkoutError) checkoutError.textContent = 'Votre panier est vide ou indisponible.';
                 checkoutBtn.disabled = true;
                 checkoutBtn.classList.add('disabled');
                 return;
            }

            const panierIdForCheckout = currentPanierData.panierId;

            console.log("Redirecting to checkout with panierId:", panierIdForCheckout);
            window.location.href = `/commandes.php?action=finaliser`;
        });
    }


    <?php if (!$error): ?>
        loadCart();
    <?php else: ?>
         if(panierLoading) panierLoading.style.display = 'none';
    <?php endif; ?>

}); 
</script>

<?php include 'includes/footer.php'; ?>